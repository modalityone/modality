package one.modality.ecommerce.payment.server.gateway.impl.anet;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.uuid.Uuid;
import one.modality.ecommerce.payment.SandboxCard;
import one.modality.ecommerce.payment.server.gateway.*;
import one.modality.ecommerce.payment.server.gateway.impl.util.RestApiOneTimeHtmlResponsesCache;

import static one.modality.ecommerce.payment.server.gateway.impl.anet.AuthorizeRestApiJob.AUTHORIZE_PAYMENT_FORM_ENDPOINT;

/**
 * @author Bruno Salmon
 */
public class AuthorizePaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "Authorize.net";

/*
    private static final String AUTHORIZE_LIVE_PAYMENT_FORM_URL = "https://accept.authorize.net/payment/paymentform?token=${token}";
    private static final String AUTHORIZE_SANDBOX_PAYMENT_FORM_URL = "https://test.authorize.net/payment/paymentform?token=${token}";
*/

    private static final String HTML_TEMPLATE = Resource.getText(Resource.toUrl("modality-anet-payment-form-iframe.html", AuthorizePaymentGateway.class));


    public AuthorizePaymentGateway() {
    }

    @Override
    public String getName() {
        return GATEWAY_NAME;
    }

    @Override
    public Future<GatewayInitiatePaymentResult> initiatePayment(GatewayInitiatePaymentArgument argument) {
        // Integrating the Authorize.net Hosted Payment Form inside the Modality page
        boolean live = argument.isLive();
        long amount = argument.getAmount();
        String apiLoginID = argument.getAccountParameter("apiLoginID");
        String clientKey = argument.getAccountParameter("clientKey");
        boolean seamless = false; //argument.isSeamlessIfSupported();

        String paymentFormContent = HTML_TEMPLATE
            .replace("${apiLoginID}", apiLoginID)
            .replace("${clientKey}", clientKey);

        SandboxCard[] sandboxCards = null;
        if (seamless) {
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedContentInitiatePaymentResult(live, true, paymentFormContent, sandboxCards));
        } else { // In other cases, we embed the page in a WebView/iFrame that can be loaded through https (assuming this server is on https)
            String htmlCacheKey = Uuid.randomUuid();
            RestApiOneTimeHtmlResponsesCache.registerOneTimeHtmlResponse(htmlCacheKey, paymentFormContent);
            String url = AUTHORIZE_PAYMENT_FORM_ENDPOINT.replace(":htmlCacheKey", htmlCacheKey);
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedUrlInitiatePaymentResult(live, false, url, sandboxCards));
        }
/*
        String apiTransactionKey = argument.getAccountParameter("api_transaction_key");

        Environment environment = live ? Environment.PRODUCTION : Environment.SANDBOX;

        //ApiOperationBase.setEnvironment(environment);

        MerchantAuthenticationType merchantAuthentication = new MerchantAuthenticationType();
        merchantAuthentication.setName(apiLoginID);
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
            MessagesType messages;
            if (errorResponse != null) {
                messages = errorResponse.getMessages();
            } else if (response != null)
                messages = response.getMessages();
            else
                messages = null;
            MessagesType.Message message = messages == null ? null : Collections.first(messages.getMessage());
            return Future.failedFuture("Authorize payment initialization failed: " + (message == null ? "no response or no message" : message.getText() + " - code = " + message.getCode()));
        }
*/
    }

    @Override
    public Future<GatewayCompletePaymentResult> completePayment(GatewayCompletePaymentArgument argument) {
        return Future.failedFuture("completePayment() not yet implemented for Authorize.net");
    }


    public Future<GatewayMakeApiPaymentResult> makeApiPayment(GatewayMakeApiPaymentArgument argument) {
        return Future.failedFuture("makeApiPayment() not yet implemented for Authorize.net");
    }

}
