package one.modality.ecommerce.payment.api.spi.impl.remote;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.com.bus.call.BusCallService;
import one.modality.ecommerce.payment.api.GetApiPaymentGatewayInfosArgument;
import one.modality.ecommerce.payment.api.GetApiPaymentGatewayInfosResult;
import one.modality.ecommerce.payment.api.MakeApiPaymentArgument;
import one.modality.ecommerce.payment.api.MakeApiPaymentResult;
import one.modality.ecommerce.payment.api.buscall.ApiPaymentServiceBusAddress;
import one.modality.ecommerce.payment.api.spi.ApiPaymentProvider;

/**
 * @author Bruno Salmon
 */
public class RemoteApiPaymentProvider implements ApiPaymentProvider {

    @Override
    public Future<MakeApiPaymentResult> makeApiPayment(MakeApiPaymentArgument argument) {
        return BusCallService.call(ApiPaymentServiceBusAddress.MAKE_API_PAYMENT_METHOD_ADDRESS, argument);
    }

    @Override
    public Future<GetApiPaymentGatewayInfosResult> getApiPaymentGatewayInfos(GetApiPaymentGatewayInfosArgument argument) {
        return BusCallService.call(ApiPaymentServiceBusAddress.GET_API_PAYMENT_GATEWAY_INFOS_METHOD_ADDRESS, argument);
    }
}
