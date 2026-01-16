package one.modality.ecommerce.policy.service.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import dev.webfx.stack.db.query.QueryResult;
import one.modality.ecommerce.policy.service.LoadPolicyArgument;
import one.modality.ecommerce.policy.service.PolicyService;

/**
 * @author Bruno Salmon
 */
public class LoadAvailabilitiesMethodEndpoint extends AsyncFunctionBusCallEndpoint<LoadPolicyArgument, QueryResult> {

    public LoadAvailabilitiesMethodEndpoint() {
        super(PolicyServiceBusAddress.LOAD_AVAILABILITIES_METHOD_ADDRESS, PolicyService::loadAvailabilities);
    }

}
