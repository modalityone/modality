package one.modality.ecommerce.payment.server.gateway.impl.anet;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.uuid.Uuid;
import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import one.modality.ecommerce.payment.PaymentStatus;
import one.modality.ecommerce.payment.SandboxCard;
import one.modality.ecommerce.payment.server.gateway.*;
import one.modality.ecommerce.payment.server.gateway.impl.util.RestApiOneTimeHtmlResponsesCache;

import java.math.BigDecimal;

import static one.modality.ecommerce.payment.server.gateway.impl.anet.AuthorizeRestApiJob.AUTHORIZE_PAYMENT_FORM_LOAD_ENDPOINT;

/**
 * @author Bruno Salmon
 */
public class AuthorizePaymentGateway implements PaymentGateway {

    static final String GATEWAY_NAME = "Authorize.net";

    private static final String HTML_TEMPLATE = Resource.getText(Resource.toUrl("modality-anet-payment-form-iframe.html", AuthorizePaymentGateway.class));

    private static final SandboxCard[] SANDBOX_CARDS = {
        new SandboxCard("Visa - Success", "4111 1111 1111 1111", null, "123", "46282"),
        new SandboxCard("Mastercard - Success", "5424 0000 0000 0015", null, "123", "46282"),
        new SandboxCard("Discover - Success", "6011 0000 0000 0012", null, "123", "46282"),
        new SandboxCard("American Express - Success", "3700 0000 0000 0002", null, "1234", "46282"),
        new SandboxCard("JCB - Success", "3088 0000 0000 0017", null, "123", "46282"),
        new SandboxCard("Dinners Club - Success", "3800 0000 0000 06", null, "123", "46282"),
        new SandboxCard("Wrong CVV", "4111 1111 1111 1111", null, "901", "46282"),
    };

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
        String apiLoginID = argument.getAccountParameter("apiLoginID");
        String clientKey = argument.getAccountParameter("clientKey");
        boolean seamless = false; //argument.isSeamlessIfSupported();

        String paymentFormContent = HTML_TEMPLATE
            .replace("${apiLoginID}", apiLoginID)
            .replace("${clientKey}", clientKey)
            ;

        SandboxCard[] sandboxCards = live ? null : SANDBOX_CARDS;
        if (seamless) {
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedContentInitiatePaymentResult(live, true, paymentFormContent, sandboxCards));
        } else { // In other cases, we embed the page in a WebView/iFrame that can be loaded through https (assuming this server is on https)
            String htmlCacheKey = Uuid.randomUuid();
            RestApiOneTimeHtmlResponsesCache.registerOneTimeHtmlResponse(htmlCacheKey, paymentFormContent);
            String url = AUTHORIZE_PAYMENT_FORM_LOAD_ENDPOINT.replace(":htmlCacheKey", htmlCacheKey);
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedUrlInitiatePaymentResult(live, false, url, sandboxCards));
        }
    }

    @Override
    public Future<GatewayCompletePaymentResult> completePayment(GatewayCompletePaymentArgument argument) {
        boolean live = argument.isLive();
        long amount = argument.getAmount();
        ReadOnlyAstObject payload = AST.parseObject(argument.getPayload(), "json");
        String dataDescriptor = payload.getString("dataDescriptor");
        String dataValue = payload.getString("dataValue");
        String apiLoginID = argument.getAccountParameter("apiLoginID");
        String apiTransactionKey = argument.getAccountParameter("apiTransactionKey");

        Environment environment = live ? Environment.PRODUCTION : Environment.SANDBOX;

        MerchantAuthenticationType merchantAuthentication = new MerchantAuthenticationType();
        merchantAuthentication.setName(apiLoginID);
        merchantAuthentication.setTransactionKey(apiTransactionKey);

        OpaqueDataType opaqueData = new OpaqueDataType();
        opaqueData.setDataDescriptor(dataDescriptor);
        opaqueData.setDataValue(dataValue);

        PaymentType paymentType = new PaymentType();
        paymentType.setOpaqueData(opaqueData);

        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        txnRequest.setAmount(BigDecimal.valueOf(0.01 * amount)); // the modality amount is in cents
        txnRequest.setPayment(paymentType);

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setMerchantAuthentication(merchantAuthentication);
        request.setTransactionRequest(txnRequest);

        CreateTransactionController controller = new CreateTransactionController(request);
        controller.execute(environment);
        CreateTransactionResponse response = controller.getApiResponse();

        MessageTypeEnum resultCode = response == null ? null : response.getMessages().getResultCode();
        if (resultCode == MessageTypeEnum.OK) { // API request succeeded (doesn't mean the payment is successful)
            TransactionResponse transactionResponse = response.getTransactionResponse();
            String transId = transactionResponse.getTransId();
            String responseCode = transactionResponse.getResponseCode();
            PaymentStatus paymentStatus = "1".equals(responseCode) ? PaymentStatus.COMPLETED :  PaymentStatus.FAILED;
            return Future.succeededFuture(new GatewayCompletePaymentResult(
                null,
                transId,
                responseCode,
                paymentStatus
            ));
        } else { // API request failed (technical error)
            ANetApiResponse errorResponse = controller.getErrorResponse();
            MessagesType messages;
            if (errorResponse != null) {
                messages = errorResponse.getMessages();
            } else if (response != null)
                messages = response.getMessages();
            else
                messages = null;
            MessagesType.Message message = messages == null ? null : Collections.first(messages.getMessage());
            return Future.failedFuture("Authorize payment completion failed: " + (message == null ? "no response or no message" : message.getText() + " - code = " + message.getCode()));
        }
    }


    public Future<GatewayMakeApiPaymentResult> makeApiPayment(GatewayMakeApiPaymentArgument argument) {
        return Future.failedFuture("makeApiPayment() not yet implemented for Authorize.net");
    }

}
