package one.modality.ecommerce.payment.api.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.api.GetApiPaymentGatewayInfosArgument;
import one.modality.ecommerce.payment.api.GetApiPaymentGatewayInfosResult;
import one.modality.ecommerce.payment.api.MakeApiPaymentArgument;
import one.modality.ecommerce.payment.api.MakeApiPaymentResult;

/**
 * @author Bruno Salmon
 */
public interface ApiPaymentProvider {


    Future<MakeApiPaymentResult> makeApiPayment(MakeApiPaymentArgument argument);

    Future<GetApiPaymentGatewayInfosResult> getApiPaymentGatewayInfos(GetApiPaymentGatewayInfosArgument argument);

}
