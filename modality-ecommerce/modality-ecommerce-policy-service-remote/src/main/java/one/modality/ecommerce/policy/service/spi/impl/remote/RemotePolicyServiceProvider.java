package one.modality.ecommerce.policy.service.spi.impl.remote;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.com.bus.call.BusCallService;
import dev.webfx.stack.db.query.QueryResult;
import one.modality.ecommerce.policy.service.LoadPolicyArgument;
import one.modality.ecommerce.policy.service.PolicyAggregate;
import one.modality.ecommerce.policy.service.buscall.PolicyServiceBusAddress;
import one.modality.ecommerce.policy.service.spi.PolicyServiceProvider;
/**
 * @author Bruno Salmon
 */
public class RemotePolicyServiceProvider implements PolicyServiceProvider {

    @Override
    public Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument) {
        return BusCallService.call(PolicyServiceBusAddress.LOAD_POLICY_METHOD_ADDRESS, argument);
    }

    @Override
    public Future<QueryResult> loadAvailabilities(LoadPolicyArgument argument) {
        return BusCallService.call(PolicyServiceBusAddress.LOAD_AVAILABILITIES_METHOD_ADDRESS, argument);
    }

}
