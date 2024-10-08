package one.modality.ecommerce.payment.server.gateway.impl.authorizedotnet;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.scheduler.Scheduler;
import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;
import one.modality.ecommerce.payment.server.gateway.*;
import one.modality.ecommerce.payment.server.gateway.GatewayInitiatePaymentArgument;
import one.modality.ecommerce.payment.server.gateway.GatewayInitiatePaymentResult;
import one.modality.ecommerce.payment.server.gateway.GatewayMakeApiPaymentArgument;
import one.modality.ecommerce.payment.server.gateway.PaymentGateway;

import java.math.BigDecimal;

/**
 * @author Bruno Salmon
 */
public class AuthorizeDotNetApiPaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "Authorize.net";
    private final static String API_KEY = "6VEbu4X922";
    private final static String TRANSACTION_KEY = "7pZts3g48E3L58Da"; // @TODO hide this or pull from configuration

    private final static net.authorize.Environment ENV = Environment.SANDBOX;

    public AuthorizeDotNetApiPaymentGateway() {
    }

    @Override
    public String getName() {
        return GATEWAY_NAME;
    }

    @Override
    public Future<GatewayInitiatePaymentResult> initiatePayment(GatewayInitiatePaymentArgument argument) {
        return Future.failedFuture("initiatePayment() not yet implemented for Authorize.net");
    }

    @Override
    public Future<GatewayCompletePaymentResult> completePayment(GatewayCompletePaymentArgument argument) {
        return Future.failedFuture("completePayment() not yet implemented for Authorize.net");
    }

    public Future<GatewayMakeApiPaymentResult> makeApiPayment(GatewayMakeApiPaymentArgument argument) {
        Promise<GatewayMakeApiPaymentResult> promise = Promise.promise();
        Scheduler.runInBackground(() -> {
            try {
                promise.complete(makeDirectPaymentBlocking(argument));
            } catch (Throwable throwable) {
                promise.fail(throwable);
            }
        });
        return promise.future();
    }

    private GatewayMakeApiPaymentResult makeDirectPaymentBlocking(GatewayMakeApiPaymentArgument argument) {
        ApiOperationBase.setEnvironment(ENV);
        String ccNumber = argument.getCcNumber();
        String ccExpiry = argument.getCcExpiry();
        System.out.println("AuthorizeDotNetDirectPaymentGatewayProvider called...");
        System.out.println("The cc number is: " + ccNumber);
        System.out.println("The expiry date is: " + ccExpiry);

        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(API_KEY);
        merchantAuthenticationType.setTransactionKey(TRANSACTION_KEY);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        System.out.println("Preparing request to Authorize.net 2");

        // Populate the payment data
        PaymentType paymentType = new PaymentType();
        CreditCardType creditCard = new CreditCardType();
        creditCard.setCardNumber(ccNumber);
        creditCard.setExpirationDate(ccExpiry);
        paymentType.setCreditCard(creditCard);

        // Create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        txnRequest.setPayment(paymentType);
        txnRequest.setAmount(new BigDecimal(argument.getAmount()));

        // Make the API Request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(txnRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        System.out.println("Sent request to Authorize.net 3");

        CreateTransactionResponse trxResponse = controller.getApiResponse();
        boolean success = false;
        if (trxResponse == null)
            System.out.println("Response is null!");
        else {

            TransactionResponse result = trxResponse.getTransactionResponse();
            System.out.println("Response code: " + result.getResponseCode());
            System.out.println("Auth code: " + result.getAuthCode());
            System.out.println("Trans ID: " + result.getTransId());
            if (result.getResponseCode().equals("1")) {
                System.out.println("Successful Credit Card Transaction");
                success = true;
            } else {
                System.out.println("Failed Credit Card Transaction");
                System.out.println("Failed Transaction: " + trxResponse.getMessages().getResultCode());
            }

        }

        return new GatewayMakeApiPaymentResult(success);
    }
}