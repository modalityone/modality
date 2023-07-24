package one.modality.ecommerce.payment.custom.spi;

import dev.webfx.platform.async.Future;

import one.modality.ecommerce.payment.custom.InitiateCustomPaymentArgument;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentResult;

public interface CustomPaymentProvider {

    Future<InitiateCustomPaymentResult> initiateCustomPayment(
            InitiateCustomPaymentArgument argument);
}
