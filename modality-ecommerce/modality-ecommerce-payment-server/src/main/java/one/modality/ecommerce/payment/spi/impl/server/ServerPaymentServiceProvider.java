package one.modality.ecommerce.payment.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.service.MultipleServiceProviders;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.SystemUserId;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.db.DatabasePayment;
import one.modality.base.shared.entities.triggers.Triggers;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.AbstractExistingMoneyTransferEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.events.gateway.UpdateMoneyTransferEvent;
import one.modality.ecommerce.document.service.events.registration.documentline.RemoveDocumentLineEvent;
import one.modality.ecommerce.history.server.HistoryRecorder;
import one.modality.ecommerce.payment.*;
import one.modality.ecommerce.payment.server.gateway.*;
import one.modality.ecommerce.payment.server.gateway.PaymentGateway; // for CLI
import one.modality.ecommerce.payment.spi.PaymentServiceProvider;

import java.util.*;

/**
 * @author Bruno Salmon
 */
public final class ServerPaymentServiceProvider implements PaymentServiceProvider {

    private static List<PaymentGateway> getProvidedPaymentGateways() {
        return MultipleServiceProviders.getProviders(PaymentGateway.class, () -> ServiceLoader.load(PaymentGateway.class));
    }

    private static PaymentGateway findMatchingPaymentGatewayProvider(String gatewayCompanyName) {
        return getProvidedPaymentGateways().stream()
            .filter(pg -> pg.getName().trim().equalsIgnoreCase(gatewayCompanyName.trim()))
            .findFirst()
            .orElse(null);
    }

    private static <T> Future<T> gatewayNotFoundFailedFuture(String gatewayName) {
        return Future.failedFuture("'" + gatewayName + "' payment gateway not found! (none of the registered payment gateways matches this name)");
    }

    @Override
    public Future<InitiatePaymentResult> initiatePayment(InitiatePaymentArgument argument) {
        // Step 1: Inserting the payment in the database with its payment allocations
        return insertPayment(argument.amount(), argument.paymentAllocations()) // insertPayment
            .compose(databasePayment -> {
                MoneyTransfer totalTransfer = databasePayment.totalTransfer();
                // Step 2: Loading the primary document for the database payment
                return loadDatabasePaymentDocuments(databasePayment)
                    .compose(v -> {
                        // Step 3: Finding a Gateway provider registered in the software that matches the money account of the payment
                        String gatewayName = totalTransfer.getToMoneyAccount().getGatewayCompany().getName();
                        PaymentGateway paymentGateway = findMatchingPaymentGatewayProvider(gatewayName);
                        if (paymentGateway == null)
                            return gatewayNotFoundFailedFuture(gatewayName);
                        // Step 4: Loading the relevant payment gateway parameters (depending on the event state)
                        Event event = getDatabasePaymentPrimaryDocument(databasePayment).getEvent();
                        EventState state = event.getState();
                        boolean live = state != null && state.compareTo(EventState.OPEN) >= 0 /* KBS3 way */
                                       || state == null && event.isLive(); /* KBS2 way */
                        String moneyTransferId = totalTransfer.getPrimaryKey().toString();
                        String returnUrl = Strings.replaceAll(argument.returnUrl(), ":moneyTransferId", moneyTransferId);
                        String cancelUrl = Strings.replaceAll(argument.cancelUrl(), ":moneyTransferId", moneyTransferId);
                        return loadPaymentGatewayParameters(totalTransfer, live)
                            .compose(parameters -> {
                                // Step 5: Calling the payment gateway with all the data collected
                                String currencyCode = totalTransfer.getToMoneyAccount().getCurrency().getCode();
                                return paymentGateway.initiatePayment(new GatewayInitiatePaymentArgument(
                                    moneyTransferId,
                                    createGatewayItem(databasePayment),
                                    currencyCode,
                                    live,
                                    argument.preferredFormType(),
                                    argument.favorSeamless(),
                                    argument.isOriginOnHttps(),
                                    returnUrl,
                                    cancelUrl,
                                    parameters
                                )).map(gatewayResult -> new InitiatePaymentResult( // Step 5: Returning an InitiatePaymentResult
                                    paymentGateway.getName(),
                                    totalTransfer.getPrimaryKey(),
                                    argument.amount(),
                                    gatewayResult.isLive(),
                                    gatewayResult.url(),
                                    gatewayResult.formType(),
                                    gatewayResult.htmlContent(),
                                    gatewayResult.isSeamless(),
                                    gatewayResult.hasHtmlPayButton(),
                                    gatewayResult.sandboxCards()
                                ));
                            });
                    });
            });
    }

    private Future<Void> loadDatabasePaymentDocuments(DatabasePayment databasePayment) {
        return databasePayment.totalTransfer().getStore()
            .executeQuery("select toMoneyAccount.(currency.code, gatewayCompany.name), document.(ref,person_name,event.(state,live)) from MoneyTransfer where id=$1 or parent=$1", databasePayment.totalTransfer())
            .mapEmpty();
    }

    private static Document getDatabasePaymentPrimaryDocument(DatabasePayment databasePayment) {
        Document primaryDocument = databasePayment.totalTransfer().getDocument();
        if (primaryDocument == null) {
            primaryDocument = databasePayment.allocatedTransfers()[0].getDocument();
        }
        return primaryDocument;
    }

    @Override
    public Future<CompletePaymentResult> completePayment(CompletePaymentArgument argument) {
        String gatewayName = argument.gatewayName();
        Object paymentPrimaryKey = argument.paymentPrimaryKey();
        boolean live = argument.isLive();
        PaymentGateway paymentGateway = findMatchingPaymentGatewayProvider(gatewayName);
        if (paymentGateway == null)
            return gatewayNotFoundFailedFuture(gatewayName);
        SystemUserId gatewayUserId = new SystemUserId(gatewayName);

        // The following code is executed just after the call to the Payment Gateway (which will take a bit of time to
        // finalize the payment and return the status). However, we add a record in the history to indicate that the
        // booker submitted valid cc details.
        UpdateStore updateStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());
        MoneyTransfer moneyTransfer = updateStore.updateEntity(MoneyTransfer.class, paymentPrimaryKey);
        DatabasePayment databasePayment = new DatabasePayment(moneyTransfer, null);
        HistoryRecorder.preparePaymentHistoriesBeforeSubmit("Submitted card details to " + gatewayName + " for payment [amount]", databasePayment)
            .onFailure(Console::log)
            .onSuccess(x -> updateStore.submitChanges());

        return Future.all(
            loadPaymentGatewayParameters(paymentPrimaryKey, live),
            // We also load the amount and customer info to pass it to gateways like Authorize.net that set the amount on completion
            // TODO: Should we skip this unnecessary loading for other gateways like Square?
            moneyTransfer.onExpressionLoaded("amount,document.(ref,person,person_firstName,person_lastName,person_email,person_phone,person_street,person_postCode,person_cityName,person_admin1Name,person_country.name,person_countryName,event.name)")
        ).compose(cf -> {
            Map<String, String> parameters = (Map<String, String>) cf.list().get(0); // result of loadPaymentGatewayParameters(paymentPrimaryKey, live)
            String accessToken = parameters.get("access_token");
            int amount = moneyTransfer.getAmount();
            Document document = moneyTransfer.getDocument();
            GatewayCustomer customer = new GatewayCustomer(
                Strings.toString(Entities.getPrimaryKey(document.getPersonId())),
                document.getFirstName(),
                document.getLastName(),
                document.getEmail(),
                document.getPhone(),
                document.getStreet(),
                document.getCityName(),
                document.getPostCode(),
                document.getAdmin1Name(),
                document.evaluate("coalesce(person_country.name,person_countryName)")
            );
            GatewayItem item = createGatewayItem(databasePayment);
            // TODO check accessToken is set, otherwise return an error
            return paymentGateway.completePayment(new GatewayCompletePaymentArgument(live, accessToken, argument.gatewayCompletePaymentPayload(), parameters, amount, customer, item))
                .onFailure(e -> {
                    Console.log("An error occurred while completing payment: " + e.getMessage());
                    // We finally update the payment status through the payment service (this will also create a history entry)
                    gatewayUserId.callAndReturn(() ->
                        updatePaymentStatus(UpdatePaymentStatusArgument.createExceptionStatusArgument(
                            paymentPrimaryKey, null, e.getMessage()))
                            .onFailure(ex -> Console.log("An error occurred while completing payment: " + ex.getMessage()))
                    );
                })
                .compose(result -> {
                    String gatewayResponse = result.gatewayResponse();
                    String gatewayTransactionRef = result.gatewayTransactionRef();
                    String gatewayStatus = result.gatewayStatus();
                    PaymentStatus paymentStatus = result.paymentStatus();
                    boolean pending = paymentStatus.isPending();
                    boolean successful = paymentStatus.isSuccessful();
                    // We finally update the payment status through the payment service (this will also create a history entry)
                    return gatewayUserId.callAndReturn(() ->
                        updatePaymentStatus(UpdatePaymentStatusArgument.createCapturedStatusArgument(
                            paymentPrimaryKey,
                            gatewayResponse,
                            gatewayTransactionRef,
                            gatewayStatus,
                            pending,
                            successful))
                            .map(ignoredVoid -> new CompletePaymentResult(paymentStatus))
                            .onFailure(Console::log)
                    );
                });
        });
    }

    private static GatewayItem createGatewayItem(DatabasePayment databasePayment) {
        MoneyTransfer moneyTransfer = databasePayment.totalTransfer();
        Document document = getDatabasePaymentPrimaryDocument(databasePayment);
        Event event = document.getEvent();
        return new GatewayItem(
            // Node: Some payment gateways like Square don't allow spaces and # in identifiers
            "E" + event.getPrimaryKey() + "-REF" + document.getRef() + "-MT" + moneyTransfer.getPrimaryKey(),
            event.getName() + " #" + document.getRef(),
            event.getName() + " - " + document.getFullName(),
            1,
            moneyTransfer.getAmount());
    }

    @Override
    public Future<CancelPaymentResult> cancelPayment(CancelPaymentArgument argument) {
        return updatePaymentStatusImpl(UpdatePaymentStatusArgument.createCancelStatusArgument(argument.paymentPrimaryKey(), argument.isExplicitUserCancellation()))
            // When payments are canceled on recurring events, we automatically un-book unpaid options
            .compose(this::unbookUnpaidOptionsIfRecurringEvent)
            .onFailure(Console::log);
    }

    private Future<CancelPaymentResult> unbookUnpaidOptionsIfRecurringEvent(MoneyTransfer moneyTransfer) {
        return moneyTransfer.onExpressionLoaded("document.(event.type.recurringItem,price_deposit)")
            .compose(x -> {
                EntityId recurringItemId = moneyTransfer.evaluate("document.event.type.recurringItem");
                // We check it's a recurring event, otherwise we skip that feature
                if (recurringItemId == null)
                    return Future.succeededFuture(new CancelPaymentResult(false));
                Document document = moneyTransfer.getDocument();
                // If there was no deposit on the booking, we cancel that booking
                if (document.getPriceDeposit() == 0) {
                    return SystemUserId.SYSTEM.callAndReturn(() -> DocumentService.submitDocumentChanges(new SubmitDocumentChangesArgument(
                        "Cancelled booking",
                        new CancelDocumentEvent(document, true))
                    ).map(ignored -> new CancelPaymentResult(true)));
                }
                // If there is a deposit, we remove all options added after the last successful payment (that is
                // meant to pay all previous options).
                return DocumentService.loadDocumentWithPolicyAndWholeHistory(document)
                    .compose(policyAndDocumentAggregates -> {
                        DocumentAggregate documentAggregate = policyAndDocumentAggregates.getDocumentAggregate();
                        // Searching for the last successful payment (shouldn't be null as there is a price deposit)
                        MoneyTransfer lastSuccessfulPayment = documentAggregate.getSuccessfulMoneyTransfersStream().reduce((first, second) -> second).orElse(null);
                        // Searching for the event marking this payment as successful
                        List<AbstractDocumentEvent> documentEvents = documentAggregate.getNewDocumentEvents();
                        List<AbstractDocumentEvent> removeEvents = new ArrayList<>();
                        documentEvents.stream().dropWhile(e -> {
                            if (!(e instanceof AbstractExistingMoneyTransferEvent aemte))
                                return true;
                            if (aemte.getMoneyTransfer() != lastSuccessfulPayment)
                                return true;
                            return aemte.isPending() || !aemte.isSuccessful();
                        }).forEach(e -> {
                            if (e instanceof AddAttendancesEvent aae) {
                                removeEvents.add(new RemoveAttendancesEvent(aae.getAttendances()));
                            } else if (e instanceof AddDocumentLineEvent aee) {
                                removeEvents.add(new RemoveDocumentLineEvent(aee.getDocumentLine()));
                            }
                        });
                        if (removeEvents.isEmpty())
                            return Future.succeededFuture(new CancelPaymentResult(false));
                        return SystemUserId.SYSTEM.callAndReturn(() -> DocumentService.submitDocumentChanges(
                                new SubmitDocumentChangesArgument("Unbooked unpaid options",
                                    removeEvents.toArray(new AbstractDocumentEvent[0])))
                            .map(ignoredResult -> new CancelPaymentResult(false)));
                    });
            });
    }

    private Future<DatabasePayment> insertPayment(int amount, PaymentAllocation[] paymentAllocations) {
        UpdateStore updateStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());
        MoneyTransfer moneyTransfer = updateStore.insertEntity(MoneyTransfer.class);
        moneyTransfer.setAmount(amount);
        moneyTransfer.setPending(true);
        moneyTransfer.setSuccessful(false);
        moneyTransfer.setMethod(Method.ONLINE_METHOD_ID);

        DatabasePayment databasePayment;
        if (paymentAllocations.length == 1) {
            moneyTransfer.setDocument(paymentAllocations[0].documentPrimaryKey());
            databasePayment = new DatabasePayment(moneyTransfer, null);
        } else {
            moneyTransfer.setSpread(true);
            MoneyTransfer[] paymentChildren = Arrays.map(paymentAllocations, paymentAllocation -> {
                MoneyTransfer allocatedTransfer = updateStore.insertEntity(MoneyTransfer.class);
                allocatedTransfer.setParent(moneyTransfer);
                allocatedTransfer.setDocument(paymentAllocation.documentPrimaryKey());
                allocatedTransfer.setAmount(paymentAllocation.amount());
                allocatedTransfer.setPending(true);
                allocatedTransfer.setSuccessful(false);
                allocatedTransfer.setMethod(Method.ONLINE_METHOD_ID);
                return allocatedTransfer;
            }, MoneyTransfer[]::new);
            databasePayment = new DatabasePayment(moneyTransfer, paymentChildren);
        }

        return HistoryRecorder.preparePaymentHistoriesBeforeSubmit("Initiated payment [amount]", databasePayment)
            .compose(histories ->
                updateStore.submitChanges()
                    // On success, we load the necessary data associated with this moneyTransfer for the payment gateway
                    .compose(batch ->
                        HistoryRecorder.completeDocumentHistoriesAfterSubmit(histories, new AddMoneyTransferEvent(moneyTransfer))
                            .map(ignoredVoid -> databasePayment)
                    )
            );
    }

    // Internal server-side method only (no serialization support)

    public Future<Map<String, String>> loadPaymentGatewayParameters(Object paymentId, boolean live) {
        paymentId = Numbers.toShortestNumber(Entities.getPrimaryKey(paymentId));
        return EntityStore.create()
            .<GatewayParameter>executeQuery("select name,value from GatewayParameter where (account=(select toMoneyAccount from MoneyTransfer where id=$1) or account==null and lower(company.name)=lower((select lower(toMoneyAccount.gatewayCompany.name) from MoneyTransfer where id=$1))) and ($2 ? live : test) order by account nulls first", paymentId, live)
            .onFailure(e -> Console.log("An error occurred while loading paymentGatewayParameters", e))
            .map(gpList -> {
                Map<String, String> parameters = new HashMap<>();
                gpList.forEach(gp -> parameters.put(gp.getName(), gp.getValue()));
                return parameters;
            });
    }

    @Override
    public Future<Void> updatePaymentStatus(UpdatePaymentStatusArgument argument) {
        return updatePaymentStatusImpl(argument).mapEmpty();
    }

    private Future<MoneyTransfer> updatePaymentStatusImpl(UpdatePaymentStatusArgument argument) {
        UpdateStore updateStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());
        MoneyTransfer moneyTransfer = updateStore.updateEntity(MoneyTransfer.class, argument.paymentPrimaryKey());
        String gatewayResponse = argument.gatewayResponse();
        String gatewayTransactionRef = argument.gatewayTransactionRef();
        String gatewayStatus = argument.gatewayStatus();
        boolean pending = argument.isPendingStatus();
        boolean successful = argument.isSuccessfulStatus();
        boolean isExplicitUserCancellation = argument.isExplicitUserCancellation();
        String errorMessage = argument.errorMessage();
        Object userId = ThreadLocalStateHolder.getUserId(); // Capturing userId because we may have an async call
        boolean isGatewayUser = userId instanceof SystemUserId;
        String fieldsToLoad = "amount,spread"; // As it will be required for history anyway
        if (!pending && successful) { // If the payment is successful, we check if it was pending before (to adjust the history comment)
            fieldsToLoad += ",pending";
        }
        return moneyTransfer.<MoneyTransfer>onExpressionLoaded(fieldsToLoad).compose(x -> {
            Boolean wasPending = x.isPending();
            moneyTransfer.setPending(pending);
            moneyTransfer.setSuccessful(successful);
            if (gatewayTransactionRef != null)
                moneyTransfer.setTransactionRef(gatewayTransactionRef);
            if (gatewayStatus != null)
                moneyTransfer.setStatus(gatewayStatus);
            if (gatewayResponse != null)
                moneyTransfer.setGatewayResponse(gatewayResponse);
            if (errorMessage != null)
                moneyTransfer.setComment(errorMessage);

            String historyComment =
                errorMessage != null ? "Raised an error while processing payment [amount]" :
                    !pending && successful ? (wasPending ? "Processed payment [amount] successfully" : "Reported payment [amount] is successful") :
                        !pending && !successful ? (isGatewayUser ? "Reported payment [amount] is failed" : isExplicitUserCancellation ? "Cancelled payment [amount]" : "Abandoned payment [amount]" /* typically closed window */) :
                            pending && successful ? "Reported payment [amount] is authorised (not yet completed)" :
                                /*pending && !successful?*/ "Reported payment [amount] is pending";

            return loadDatabasePayment(moneyTransfer)
                .compose(databasePayment ->
                    HistoryRecorder.preparePaymentHistoriesBeforeSubmit(historyComment, databasePayment, userId)
                        .compose(histories ->
                            updateStore.submitChanges(Triggers.frontOfficeTransaction(updateStore))
                                .compose(result -> { // Checking that something happened in the database
                                    int rowCount = result.getRowCount();
                                    if (rowCount == 0)
                                        return Future.failedFuture("Unknown payment");
                                    // Completing the histories recording (changes column with resolved primary keys)
                                    return HistoryRecorder.completeDocumentHistoriesAfterSubmit(histories, new UpdateMoneyTransferEvent(moneyTransfer))
                                        .map(ignoredVoid -> moneyTransfer);
                                })
                        )
                );
        });
    }

    private Future<DatabasePayment> loadDatabasePayment(MoneyTransfer moneyTransfer) {
        if (!moneyTransfer.isSpread())
            return Future.succeededFuture(new DatabasePayment(moneyTransfer, null));
        return moneyTransfer.getStore()
            .executeQuery("select amount,document.id from MoneyTransfer where parent=$1", moneyTransfer)
            .map(allocatedTransfers -> new DatabasePayment(moneyTransfer, allocatedTransfers.toArray(new MoneyTransfer[0])));
    }

}
