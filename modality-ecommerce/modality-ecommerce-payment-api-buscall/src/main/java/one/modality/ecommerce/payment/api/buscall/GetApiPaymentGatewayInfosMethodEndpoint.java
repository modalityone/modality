package one.modality.ecommerce.payment.api.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.api.ApiPaymentService;
import one.modality.ecommerce.payment.api.GetApiPaymentGatewayInfosArgument;
import one.modality.ecommerce.payment.api.GetApiPaymentGatewayInfosResult;

/**
 * @author Bruno Salmon
 */
public final class GetApiPaymentGatewayInfosMethodEndpoint extends AsyncFunctionBusCallEndpoint<GetApiPaymentGatewayInfosArgument, GetApiPaymentGatewayInfosResult> {

    public GetApiPaymentGatewayInfosMethodEndpoint() {
        super(ApiPaymentServiceBusAddress.GET_API_PAYMENT_GATEWAY_INFOS_METHOD_ADDRESS, ApiPaymentService::getApiPaymentGatewayInfos);
    }

}
