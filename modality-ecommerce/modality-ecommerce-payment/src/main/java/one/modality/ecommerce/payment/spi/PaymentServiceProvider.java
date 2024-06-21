package one.modality.ecommerce.payment.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.*;

import java.util.Map;

public interface PaymentServiceProvider {

    Future<InitiatePaymentResult> initiatePayment(InitiatePaymentArgument argument);

    Future<MakeApiPaymentResult> makeApiPayment(MakeApiPaymentArgument argument);

    // Internal server-side method only (no serialisation support)

    Future<Map<String, String>> loadPaymentGatewayParameters(Object paymentId, boolean live);

    Future<Void> updatePaymentStatus(UpdatePaymentStatusArgument argument);

}
