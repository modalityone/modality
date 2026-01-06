package one.modality.ecommerce.policy.service.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.policy.service.*;

/**
 * @author Bruno Salmon
 */
public interface PolicyServiceProvider {

    Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument);

}
