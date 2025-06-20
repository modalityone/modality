package one.modality.ecommerce.payment.server.gateway.impl.anet;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.scheduler.Scheduler;
import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.GetHostedPaymentPageController;
import net.authorize.api.controller.base.ApiOperationBase;
import one.modality.ecommerce.payment.server.gateway.*;

import java.math.BigDecimal;

/**
 * @author Bruno Salmon
 */
public class AuthorizePaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "Authorize.net";

    private static final String AUTHORIZE_LIVE_PAYMENT_FORM_URL = "https://accept.authorize.net/payment/paymentform?token=${token}";
    private static final String AUTHORIZE_SANDBOX_PAYMENT_FORM_URL = "https://test.accept.authorize.net/payment/paymentform?token=${token}";

    public AuthorizePaymentGateway() {
    }

    @Override
    public String getName() {
        return GATEWAY_NAME;
    }

    @Override
    public Future<GatewayInitiatePaymentResult> initiatePayment(GatewayInitiatePaymentArgument argument) {
        boolean live = argument.isLive();
        long amount = argument.getAmount();
        String apiLoginId = argument.getAccountParameter("api_login_id"); // KBS3
        if (apiLoginId == null)
            apiLoginId = argument.getAccountParameter("x_login"); // KBS2 (was redirected payment)
        String apiTransactionKey = argument.getAccountParameter("api_transaction_key");

        Environment environment = live ? Environment.PRODUCTION : Environment.SANDBOX;

        //ApiOperationBase.setEnvironment(environment);

        MerchantAuthenticationType merchantAuthentication = new MerchantAuthenticationType();
        merchantAuthentication.setName(apiLoginId);
        merchantAuthentication.setTransactionKey(apiTransactionKey);
        //ApiOperationBase.setMerchantAuthentication(merchantAuthentication);

        SettingType hostedPaymentButtonOptions = new SettingType();
        hostedPaymentButtonOptions.setSettingName("hostedPaymentButtonOptions");
        hostedPaymentButtonOptions.setSettingValue("{\"text\": \"Pay\"}");

        SettingType hostedPaymentOrderOptions = new SettingType();
        hostedPaymentOrderOptions.setSettingName("hostedPaymentOrderOptions");
        hostedPaymentOrderOptions.setSettingValue("{\"show\": false}");

        ArrayOfSetting paymentSettings = new ArrayOfSetting();
        paymentSettings.getSetting().add(hostedPaymentButtonOptions);
        paymentSettings.getSetting().add(hostedPaymentOrderOptions);

        TransactionRequestType transactionRequest = new TransactionRequestType();
        transactionRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        transactionRequest.setAmount(BigDecimal.valueOf(0.01 * amount)); // amount

        GetHostedPaymentPageRequest apiRequest = new GetHostedPaymentPageRequest();
        apiRequest.setMerchantAuthentication(merchantAuthentication);
        apiRequest.setTransactionRequest(transactionRequest);
        apiRequest.setHostedPaymentSettings(paymentSettings);

        GetHostedPaymentPageController controller = new GetHostedPaymentPageController(apiRequest);
        controller.execute(environment);

        GetHostedPaymentPageResponse response = controller.getApiResponse();
        MessageTypeEnum resultCode = response == null ? null : response.getMessages().getResultCode();
        if (resultCode == MessageTypeEnum.OK) {
            String token = response.getToken();
            String urlTemplate = live ? AUTHORIZE_LIVE_PAYMENT_FORM_URL : AUTHORIZE_SANDBOX_PAYMENT_FORM_URL;
            String url = urlTemplate.replace("${token}", token);
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedUrlInitiatePaymentResult(live, false, url, null));
        } else {
            ANetApiResponse errorResponse = controller.getErrorResponse();
            if (errorResponse != null) {
                return Future.failedFuture(errorResponse.getMessages().getMessage().toString());
            } else
                return Future.failedFuture("Authorize call failed with resultCode = " + resultCode);
        }
    }

    @Override
    public Future<GatewayCompletePaymentResult> completePayment(GatewayCompletePaymentArgument argument) {
        return Future.failedFuture("completePayment() not yet implemented for Authorize.net");
    }


    // Ben draft implementation of makeApiPayment()

    private final static String API_KEY = "6VEbu4X922";
    private final static String TRANSACTION_KEY = "7pZts3g48E3L58Da"; // @TODO hide this or pull from configuration
    private final static net.authorize.Environment ENV = Environment.SANDBOX;

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
