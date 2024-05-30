package one.modality.ecommerce.payment.embedded.gateway.spi.impl.stripe;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.resource.Resource;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentArgument;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentResult;
import one.modality.ecommerce.payment.embedded.gateway.spi.EmbeddedPaymentGatewayProvider;

/**
 * @author Bruno Salmon
 */
public class StripeEmbeddedPaymentGatewayProvider implements EmbeddedPaymentGatewayProvider {

    private final static String API_SECRET_KEY = "sk_test_26PHem9AhJZvU623DfE1x4sd";
    private final static String API_PUBLIC_KEY = "pk_test_qblFNYngBkEdjEZ16jxxoWSM";

    @Override
    public Future<InitiateEmbeddedPaymentResult> initiateEmbeddedPayment(InitiateEmbeddedPaymentArgument argument) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(argument.getAmount())
                    .setCurrency(argument.getCurrency())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                    )
                    .build();
            String clientSecret = PaymentIntent.create(params).getClientSecret();
            String html = Resource.getText(Resource.toUrl("stripe-checkout.html", getClass()))
                    .replace("{{API_KEY}}", API_PUBLIC_KEY)
                    .replace("{{CLIENT_SECRET}}", clientSecret)
                    .replace("{{RETURN_URL}}", "http://127.0.0.1:8080/checkout/success/");
            return Future.succeededFuture(new InitiateEmbeddedPaymentResult(html));
        } catch (Exception e) {
            return Future.failedFuture(e);
        }
    }
}
