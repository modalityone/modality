package one.modality.ecommerce.policy.service;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.SingleServiceProvider;
import one.modality.ecommerce.policy.service.spi.PolicyServiceProvider;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class PolicyService {

    private static PolicyServiceProvider getProvider() {
        return SingleServiceProvider.getProvider(PolicyServiceProvider.class, () -> ServiceLoader.load(PolicyServiceProvider.class));
    }

    public static Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument) {
        return getProvider().loadPolicy(argument);
    }

}