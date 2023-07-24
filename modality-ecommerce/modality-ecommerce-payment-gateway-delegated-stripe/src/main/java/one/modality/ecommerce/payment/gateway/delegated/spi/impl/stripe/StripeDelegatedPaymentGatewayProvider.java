package one.modality.ecommerce.payment.gateway.delegated.spi.impl.stripe;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentArgument;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentResult;
import one.modality.ecommerce.payment.gateway.delegated.spi.DelegatedPaymentGatewayProvider;
import one.modality.ecommerce.payment.gateway.delegated.spi.InitiateDelegatedPaymentGatewayArgument;

/**
 * @author Bruno Salmon
 */
public class StripeDelegatedPaymentGatewayProvider implements DelegatedPaymentGatewayProvider {

  @Override
  public Future<InitiateDelegatedPaymentResult> initiateDelegatedPayment(
      InitiateDelegatedPaymentGatewayArgument argument) {
    InitiateDelegatedPaymentArgument userArgument = argument.getUserArgument();
    Stripe.apiKey = argument.getAccountId();
    // Extract the following to a 'StripeClient'
    // Assemble the purchase objects
    SessionCreateParams.LineItem.PriceData.ProductData productData =
        SessionCreateParams.LineItem.PriceData.ProductData.builder()
            .setName(userArgument.getDescription())
            .build();

    // Create the price data and add the product data to this
    SessionCreateParams.LineItem.PriceData priceData =
        SessionCreateParams.LineItem.PriceData.builder()
            .setCurrency(userArgument.getCurrency())
            .setUnitAmount((long) userArgument.getAmount())
            .setProductData(productData)
            .build();

    // Create the line item and add the price data
    SessionCreateParams.LineItem lineItem =
        SessionCreateParams.LineItem.builder().setQuantity(1L).setPriceData(priceData).build();

    // Create the session params and add the line item
    SessionCreateParams params =
        SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(argument.getSuccessUrl())
            .setCancelUrl(argument.getCancelUrl())
            .addLineItem(lineItem)
            .build();

    try {
      Session session = Session.create(params);
      return Future.succeededFuture(new InitiateDelegatedPaymentResult(session.getUrl()));
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }
}
