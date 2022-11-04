package one.modality.ecommerce.payment.direct.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.direct.DirectPaymentService;
import one.modality.ecommerce.payment.direct.GetDirectPaymentGatewayInfosArgument;
import one.modality.ecommerce.payment.direct.GetDirectPaymentGatewayInfosResult;

/**
 * @author Bruno Salmon
 */
public final class GetDirectPaymentGatewayInfosMethodEndpoint extends AsyncFunctionBusCallEndpoint<GetDirectPaymentGatewayInfosArgument, GetDirectPaymentGatewayInfosResult> {

    public GetDirectPaymentGatewayInfosMethodEndpoint() {
        super(DirectPaymentServiceBusAddress.GET_DIRECT_PAYMENT_GATEWAY_INFOS_METHOD_ADDRESS, DirectPaymentService::getDirectPaymentGatewayInfos);
    }

}
