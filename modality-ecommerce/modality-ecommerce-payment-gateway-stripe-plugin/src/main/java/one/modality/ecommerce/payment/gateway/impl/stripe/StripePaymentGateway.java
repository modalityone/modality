package one.modality.ecommerce.payment.gateway.impl.stripe;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.resource.Resource;
import one.modality.ecommerce.payment.InitiatePaymentResult;
import one.modality.ecommerce.payment.gateway.*;

/**
 * @author Bruno Salmon
 */
public class StripePaymentGateway implements PaymentGateway {

    private final static String API_SECRET_KEY = "sk_test_26PHem9AhJZvU623DfE1x4sd";
    private final static String API_PUBLIC_KEY = "pk_test_qblFNYngBkEdjEZ16jxxoWSM";

    @Override
    public String getName() {
        return "Stripe";
    }

    @Override
    public Future<GatewayInitiatePaymentResult> initiatePayment(GatewayInitiatePaymentArgument argument) {
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
            return Future.succeededFuture(new GatewayInitiatePaymentResult(html));
        } catch (Exception e) {
            return Future.failedFuture(e);
        }
    }

    public Future<InitiatePaymentResult> initiatePaymentRedirect(GatewayInitiatePaymentArgument argument) {
        Stripe.apiKey = API_SECRET_KEY;
        // Extract the following to a 'StripeClient'
        // Assemble the purchase objects
        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(argument.getProductName())
                        .build();

        // Create the price data and add the product data to this
        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(argument.getCurrency())
                        .setUnitAmount(argument.getAmount())
                        .setProductData(productData)
                        .build();

        // Create the line item and add the price data
        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(priceData)
                        .build();

        // Create the session params and add the line item
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(null/*argument.getSuccessUrl()*/)
                        .setCancelUrl(null/*argument.getFailUrl()*/)
                        .addLineItem(lineItem)
                        .build();

        try {
            Session session = Session.create(params);
            return Future.succeededFuture(new InitiatePaymentResult(null, session.getUrl(), true));
        } catch (Exception e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<GatewayMakeApiPaymentResult> makeApiPayment(GatewayMakeApiPaymentArgument argument) {
        return Future.failedFuture("makeApiPayment() not yet implemented for Stripe");
    }
}
