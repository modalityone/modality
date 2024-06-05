package one.modality.ecommerce.payment.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.InitiatePaymentArgument;
import one.modality.ecommerce.payment.InitiatePaymentResult;
import one.modality.ecommerce.payment.MakeApiPaymentArgument;
import one.modality.ecommerce.payment.MakeApiPaymentResult;

public interface PaymentServiceProvider {

    Future<InitiatePaymentResult> initiatePayment(InitiatePaymentArgument argument);

    Future<MakeApiPaymentResult> makeApiPayment(MakeApiPaymentArgument argument);

}
