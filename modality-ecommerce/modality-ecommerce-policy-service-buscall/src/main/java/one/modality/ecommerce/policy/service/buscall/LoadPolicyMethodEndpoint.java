package one.modality.ecommerce.policy.service.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.policy.service.PolicyService;
import one.modality.ecommerce.policy.service.LoadPolicyArgument;
import one.modality.ecommerce.policy.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
public class LoadPolicyMethodEndpoint extends AsyncFunctionBusCallEndpoint<LoadPolicyArgument, PolicyAggregate> {

    public LoadPolicyMethodEndpoint() {
        super(PolicyServiceBusAddress.LOAD_POLICY_METHOD_ADDRESS, PolicyService::loadPolicy);
    }

}
