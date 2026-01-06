package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.PolicyScope;

/**
 * @author Bruno Salmon
 */
public final class PolicyScopeImpl extends DynamicEntity implements PolicyScope {

    public PolicyScopeImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<PolicyScope> {
        public ProvidedFactory() {
            super(PolicyScope.class, PolicyScopeImpl::new);
        }
    }
}
