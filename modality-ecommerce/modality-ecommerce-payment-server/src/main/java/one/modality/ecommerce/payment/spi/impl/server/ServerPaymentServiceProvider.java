package one.modality.ecommerce.payment.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.service.MultipleServiceProviders;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.SystemUserId;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.*;
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
public class ServerPaymentServiceProvider implements PaymentServiceProvider {

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
        // Step 1: Adding a payment to the document in the database
        return addDocumentPayment(argument.getDocumentPrimaryKey(), argument.getAmount())
            .compose(moneyTransfer -> {
                // Step 2: Finding a Gateway provider registered in the software that matches the money account of the payment
                String gatewayName = moneyTransfer.getToMoneyAccount().getGatewayCompany().getName();
                PaymentGateway paymentGateway = findMatchingPaymentGatewayProvider(gatewayName);
                if (paymentGateway == null)
                    return gatewayNotFoundFailedFuture(gatewayName);
                // Step 3: Loading the relevant payment gateway parameters
                Event event = moneyTransfer.getDocument().getEvent();
                EventState state = event.getState();
                boolean live = state != null && state.compareTo(EventState.OPEN) >= 0 /* KBS3 way */ || event.isLive() /* KBS2 way */;
                return loadPaymentGatewayParameters(moneyTransfer, live)
                    .compose(parameters -> {
                        // Step 4: Calling the payment gateway with all the data collected
                        String currencyCode = moneyTransfer.getToMoneyAccount().getCurrency().getCode();
                        return paymentGateway.initiatePayment(new GatewayInitiatePaymentArgument(
                            argument.getAmount(),
                            currencyCode,
                            live,
                            argument.isSeamlessIfSupported(),
                            argument.isParentPageHttps(),
                            null,
                            parameters
                        )).map(gatewayResult -> new InitiatePaymentResult( // Step 5: Returning a InitiatePaymentResult
                            moneyTransfer.getPrimaryKey(),
                            argument.getAmount(),
                            gatewayResult.isLive(),
                            gatewayResult.isSeamless(),
                            gatewayResult.getHtmlContent(),
                            gatewayResult.getUrl(),
                            gatewayResult.isRedirect(),
                            gatewayResult.hasHtmlPayButton(),
                            paymentGateway.getName(),
                            gatewayResult.getSandboxCards()
                        ));
                    });
            });
    }

    @Override
    public Future<CompletePaymentResult> completePayment(CompletePaymentArgument argument) {
        String gatewayName = argument.getGatewayName();
        Object paymentPrimaryKey = argument.getPaymentPrimaryKey();
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
        HistoryRecorder.preparePaymentHistoryBeforeSubmit("Submitted card details to " + gatewayName + " for [payment]", moneyTransfer)
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
            Event event = document.getEvent();
            GatewayItem item = new GatewayItem(
                "E#" + event.getPrimaryKey() + " - D#" + document.getRef() + " - P#" + paymentPrimaryKey,
                "Deposit",
                event.getName() + " - Booking Ref " + document.getRef(),
                1,
                amount);
            // TODO check accessToken is set, otherwise return an error
            return paymentGateway.completePayment(new GatewayCompletePaymentArgument(live, accessToken, argument.getGatewayCompletePaymentPayload(), parameters, amount, customer, item))
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
                    String gatewayResponse = result.getGatewayResponse();
                    String gatewayTransactionRef = result.getGatewayTransactionRef();
                    String gatewayStatus = result.getGatewayStatus();
                    PaymentStatus paymentStatus = result.getPaymentStatus();
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

    @Override
    public Future<CancelPaymentResult> cancelPayment(CancelPaymentArgument argument) {
        return updatePaymentStatusImpl(UpdatePaymentStatusArgument.createCancelStatusArgument(argument.getPaymentPrimaryKey(), argument.isExplicitUserCancellation()))
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
                            if (!(e instanceof AbstractExistingMoneyTransferEvent))
                                return true;
                            AbstractExistingMoneyTransferEvent aemte = (AbstractExistingMoneyTransferEvent) e;
                            if (aemte.getMoneyTransfer() != lastSuccessfulPayment)
                                return true;
                            if (aemte.isPending() || !aemte.isSuccessful())
                                return true;
                            return false;
                        }).forEach(e -> {
                            if (e instanceof AddAttendancesEvent) {
                                AddAttendancesEvent aae = (AddAttendancesEvent) e;
                                removeEvents.add(new RemoveAttendancesEvent(aae.getAttendances()));
                            } else if (e instanceof AddDocumentLineEvent) {
                                AddDocumentLineEvent aee = (AddDocumentLineEvent) e;
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

    private Future<MoneyTransfer> addDocumentPayment(Object documentPrimaryKey, int amount) {
        UpdateStore updateStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());
        MoneyTransfer moneyTransfer = updateStore.insertEntity(MoneyTransfer.class);
        moneyTransfer.setDocument(documentPrimaryKey);
        moneyTransfer.setAmount(amount);
        moneyTransfer.setPending(true);
        moneyTransfer.setSuccessful(false);
        moneyTransfer.setMethod(Method.ONLINE_METHOD_ID);

        return HistoryRecorder.preparePaymentHistoryBeforeSubmit("Initiated [payment]", moneyTransfer)
            .compose(history ->
                updateStore.submitChanges()
                    // On success, we load the necessary data associated with this moneyTransfer for the payment gateway
                    .compose(batch ->
                        moneyTransfer.<MoneyTransfer>onExpressionLoaded("toMoneyAccount.(currency.code, gatewayCompany.name), document.event.(live,state)")
                            .compose(result -> // Completing the history recording (changes column with resolved primary keys)
                                HistoryRecorder.completeDocumentHistoryAfterSubmit(result, history, new AddMoneyTransferEvent(moneyTransfer))
                            )
                    ));
    }

    // Internal server-side method only (no serialization support)

    public Future<Map<String, String>> loadPaymentGatewayParameters(Object paymentId, boolean live) {
        if (paymentId instanceof String)
            paymentId = Integer.parseInt((String) paymentId);
        return EntityStore.create()
            .<GatewayParameter>executeQuery("select name,value from GatewayParameter where (account=(select toMoneyAccount from MoneyTransfer where id=?) or account==null and lower(company.name)=lower((select lower(toMoneyAccount.gatewayCompany.name) from MoneyTransfer where id=?))) and (? ? live : test) order by account nulls first", paymentId, paymentId, live)
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
        MoneyTransfer moneyTransfer = updateStore.updateEntity(MoneyTransfer.class, argument.getPaymentPrimaryKey());
        String gatewayResponse = argument.getGatewayResponse();
        String gatewayTransactionRef = argument.getGatewayTransactionRef();
        String gatewayStatus = argument.getGatewayStatus();
        boolean pending = argument.isPendingStatus();
        boolean successful = argument.isSuccessfulStatus();
        boolean isExplicitUserCancellation = argument.isExplicitUserCancellation();
        String errorMessage = argument.getErrorMessage();
        Object userId = ThreadLocalStateHolder.getUserId(); // Capturing userId because we may have an async call
        boolean isGatewayUser = userId instanceof SystemUserId;
        String fieldsToLoad = "amount"; // As it will be required for history anyway
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
                errorMessage != null ?      "Raised an error while processing [payment]" :
                !pending && successful  ?   (wasPending ? "Processed [payment] successfully" : "Reported [payment] is successful") :
                !pending && !successful ?   (isGatewayUser ? "Reported [payment] is failed" : isExplicitUserCancellation ? "Cancelled [payment]" : "Abandoned [payment]" /* typically closed window */) :
                pending && successful   ?   "Reported [payment] is authorised (not yet completed)" :
                /*pending && !successful?*/ "Reported [payment] is pending";

            return HistoryRecorder.preparePaymentHistoryBeforeSubmit(historyComment, moneyTransfer, userId)
                .compose(history ->
                    updateStore.submitChanges(Triggers.frontOfficeTransaction(updateStore))
                        .compose(result -> { // Checking that something happened in the database
                            int rowCount = result.getRowCount();
                            if (rowCount == 0)
                                return Future.failedFuture("Unknown payment");
                            // Completing the history recording (changes column with resolved primary keys)
                            return HistoryRecorder.completeDocumentHistoryAfterSubmit(moneyTransfer, history, new UpdateMoneyTransferEvent(moneyTransfer));
                        })
                );
        });
    }

}
