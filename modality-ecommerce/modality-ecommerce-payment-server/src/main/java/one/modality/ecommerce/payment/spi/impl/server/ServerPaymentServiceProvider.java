package one.modality.ecommerce.payment.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.MultipleServiceProviders;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.GatewayParameter;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.ecommerce.payment.InitiatePaymentArgument;
import one.modality.ecommerce.payment.InitiatePaymentResult;
import one.modality.ecommerce.payment.MakeApiPaymentArgument;
import one.modality.ecommerce.payment.MakeApiPaymentResult;
import one.modality.ecommerce.payment.gateway.GatewayInitiatePaymentArgument;
import one.modality.ecommerce.payment.gateway.GatewayMakeApiPaymentArgument;
import one.modality.ecommerce.payment.gateway.PaymentGateway;
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
                    return EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                            .<GatewayParameter>executeQuery("select name,value from GatewayParameter where (account=? or account==null and lower(company.name)=lower(?)) and (? ? live : test) order by account nulls first", moneyTransfer.getToMoneyAccount(), moneyTransfer.getToMoneyAccount().getGatewayCompany().getName(), live)
                            .compose(gpList -> {
                                Map<String, String> parameters = new HashMap<>();
                                gpList.forEach(gp -> parameters.put(gp.getName(), gp.getValue()));
                                // Step 4: Calling the payment gateway with all the data collected
                                String currencyCode = moneyTransfer.getToMoneyAccount().getCurrency().getCode();
                                return paymentGateway.initiatePayment(new GatewayInitiatePaymentArgument(
                                        argument.getAmount(),
                                        currencyCode,
                                        null,
                                        parameters
                                )).map(res -> new InitiatePaymentResult( // Step 5: Returning a InitiatePaymentResult
                                        moneyTransfer.getPrimaryKey(),
                                        res.getHtmlContent(),
                                        res.getUrl(),
                                        res.isRedirect()
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
        // TODO: See if we should use the DocumentService instead to create the money transfer (with event sourcing)
        UpdateStore updateStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());
        MoneyTransfer moneyTransfer = updateStore.insertEntity(MoneyTransfer.class);
        moneyTransfer.setDocument(documentPrimaryKey);
        moneyTransfer.setAmount(amount);
        moneyTransfer.setPending(true);
        moneyTransfer.setSuccessful(false);
        moneyTransfer.setMethod(5); // Online (temporarily hardcoded for now)
        return updateStore.submitChanges()
                // On success, we load the necessary data associated with this moneyTransfer for the payment gateway
                .compose(batch ->
                        moneyTransfer.onExpressionLoaded("toMoneyAccount.(currency.code, gatewayCompany.name), document.event.live")
                        .map(ignored -> moneyTransfer));
    }
}
