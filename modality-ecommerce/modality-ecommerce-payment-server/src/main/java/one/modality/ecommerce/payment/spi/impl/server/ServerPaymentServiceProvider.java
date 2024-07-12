package one.modality.ecommerce.payment.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.MultipleServiceProviders;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.GatewayParameter;
import one.modality.base.shared.entities.Method;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.ecommerce.document.service.events.book.AddMoneyTransferEvent;
import one.modality.ecommerce.document.service.events.gateway.UpdateMoneyTransferEvent;
import one.modality.ecommerce.history.server.HistoryRecorder;
import one.modality.ecommerce.payment.*;
import one.modality.ecommerce.payment.server.gateway.GatewayInitiatePaymentArgument;
import one.modality.ecommerce.payment.server.gateway.GatewayMakeApiPaymentArgument;
import one.modality.ecommerce.payment.server.gateway.PaymentGateway;
import one.modality.ecommerce.payment.spi.PaymentServiceProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public class ServerPaymentServiceProvider implements PaymentServiceProvider {

    private static List<PaymentGateway> getProvidedPaymentGateways() {
        return MultipleServiceProviders.getProviders(PaymentGateway.class, () -> ServiceLoader.load(PaymentGateway.class));
    }

    private static PaymentGateway findMatchingPaymentGatewayProvider(MoneyTransfer moneyTransfer) {
        String gatewayCompanyName = moneyTransfer.getToMoneyAccount().getGatewayCompany().getName();
        return getProvidedPaymentGateways().stream()
                .filter(pg -> pg.getName().equalsIgnoreCase(gatewayCompanyName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Future<InitiatePaymentResult> initiatePayment(InitiatePaymentArgument argument) {
        // Step 1: Adding a payment to the document in the database
        return addDocumentPayment(argument.getDocumentPrimaryKey(), argument.getAmount())
                .compose(moneyTransfer -> {
                    // Step 2: Finding a Gateway provider registered in the software that matches the money account of the payment
                    PaymentGateway paymentGateway = findMatchingPaymentGatewayProvider(moneyTransfer);
                    if (paymentGateway == null)
                        return Future.failedFuture(new IllegalStateException("No payment gateway found!"));
                    // Step 3: Loading the relevant payment gateway parameters
                    boolean live = false; //moneyTransfer.getDocument().getEvent().isLive();
                    return loadPaymentGatewayParameters(moneyTransfer, live)
                            .compose(parameters -> {
                                // Step 4: Calling the payment gateway with all the data collected
                                String currencyCode = moneyTransfer.getToMoneyAccount().getCurrency().getCode();
                                return paymentGateway.initiatePayment(new GatewayInitiatePaymentArgument(
                                        "" + moneyTransfer.getPrimaryKey(),
                                        argument.getAmount(),
                                        currencyCode,
                                        live,
                                        argument.isSeamlessIfSupported(),
                                        argument.isParentPageHttps(),
                                        null,
                                        parameters
                                )).map(gatewayResult -> new InitiatePaymentResult( // Step 5: Returning a InitiatePaymentResult
                                        moneyTransfer.getPrimaryKey(),
                                        gatewayResult.isLive(),
                                        gatewayResult.isSeamless(),
                                        gatewayResult.getHtmlContent(),
                                        gatewayResult.getUrl(),
                                        gatewayResult.isRedirect(),
                                        paymentGateway.getName(),
                                        gatewayResult.getSandboxCards()
                                ));
                            });
                });
    }

    @Override
    public Future<MakeApiPaymentResult> makeApiPayment(MakeApiPaymentArgument argument) {
        return addDocumentPayment(argument.getDocumentPrimaryKey(), argument.getAmount())
                .compose(moneyTransfer -> {
                    PaymentGateway paymentGateway = findMatchingPaymentGatewayProvider(moneyTransfer);
                    if (paymentGateway == null)
                        return Future.failedFuture(new IllegalStateException("No payment gateway found!"));
                    String currencyCode = moneyTransfer.getToMoneyAccount().getCurrency().getCode();
                    return paymentGateway.makeApiPayment(new GatewayMakeApiPaymentArgument(
                            argument.getAmount(),
                            currencyCode,
                            argument.getCcNumber(),
                            argument.getCcExpiry()
                    )).map(result -> new MakeApiPaymentResult(
                            result.isSuccess()
                    ));
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

        return HistoryRecorder.preparePaymentHistoryBeforeSubmit("Initiated payment " + (amount / 100) + "." + Numbers.twoDigits(amount % 100), moneyTransfer)
                .compose(history ->
                    updateStore.submitChanges()
                    // On success, we load the necessary data associated with this moneyTransfer for the payment gateway
                    .compose(batch ->
                        moneyTransfer.<MoneyTransfer>onExpressionLoaded("toMoneyAccount.(currency.code, gatewayCompany.name), document.event.live")
                        .onSuccess(ignored -> // Completing the history recording (changes column with resolved primary keys)
                            HistoryRecorder.completeDocumentHistoryAfterSubmit(history, new AddMoneyTransferEvent(moneyTransfer))
                        )
                    ));
    }

    // Internal server-side method only (no serialisation support)

    public Future<Map<String, String>> loadPaymentGatewayParameters(Object paymentId, boolean live) {
        if (paymentId instanceof String)
            paymentId = Integer.parseInt((String) paymentId);
        return EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                .<GatewayParameter>executeQuery("select name,value from GatewayParameter where (account=(select toMoneyAccount from MoneyTransfer where id=?) or account==null and lower(company.name)=lower((select lower(toMoneyAccount.gatewayCompany.name) from MoneyTransfer where id=?))) and (? ? live : test) order by account nulls first", paymentId, paymentId, live)
                .map(gpList -> {
                    Map<String, String> parameters = new HashMap<>();
                    gpList.forEach(gp -> parameters.put(gp.getName(), gp.getValue()));
                    return parameters;
                });
    }

    @Override
    public Future<Void> updatePaymentStatus(UpdatePaymentStatusArgument argument) {
        UpdateStore updateStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());
        MoneyTransfer moneyTransfer = updateStore.updateEntity(MoneyTransfer.class, Integer.parseInt(argument.getPaymentId()));
        String gatewayResponse = argument.getGatewayResponse();
        String gatewayTransactionRef = argument.getGatewayTransactionRef();
        String gatewayStatus = argument.getGatewayStatus();
        boolean pending = argument.isPendingStatus();
        boolean successful = argument.isSuccessfulStatus();
        String errorMessage = argument.getErrorMessage();
        Future<MoneyTransfer> loadingFuture;
        Object gatewayUserId = ThreadLocalStateHolder.getUserId(); // Capturing gateway userId because we may have an async call
        if (!pending && successful) { // If the payment is successful, we check if it was pending before (to adjust the history comment)
            loadingFuture = moneyTransfer.onExpressionLoaded("pending");
        } else {
            loadingFuture = Future.succeededFuture(moneyTransfer);
        }
        return loadingFuture.compose(x -> {
            boolean wasPending = x.isPending();
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
                !pending && successful  ?   (wasPending ? "Processed payment successfully" : "Reported payment is successful") :
                !pending && !successful ?   "Reported payment is failed" :
                pending && successful   ?   "Reported payment is authorised (not yet completed)" :
                /*pending && !successful?*/ "Reported payment is pending";

            return HistoryRecorder.preparePaymentHistoryBeforeSubmit(historyComment, moneyTransfer, gatewayUserId)
                .compose(history ->
                    updateStore.submitChanges()
                        .compose(submitResultBatch -> { // Checking that something happened in the database
                            int rowCount = submitResultBatch.get(0).getRowCount();
                            if (rowCount == 0)
                                return Future.failedFuture("Unknown payment");
                            return Future.succeededFuture((Void) null);
                        })
                        .onSuccess(ignored -> // Completing the history recording (changes column with resolved primary keys)
                                HistoryRecorder.completeDocumentHistoryAfterSubmit(history, new UpdateMoneyTransferEvent(moneyTransfer))
                        )
                );
        });
    }

}
