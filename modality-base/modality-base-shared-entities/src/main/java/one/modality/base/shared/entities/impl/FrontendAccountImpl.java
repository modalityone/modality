package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.FrontendAccount;

/**
 * @author Bruno Salmon
 */
public final class FrontendAccountImpl extends DynamicEntity implements FrontendAccount {

    public FrontendAccountImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<FrontendAccount> {
        public ProvidedFactory() {
            super(FrontendAccount.class, FrontendAccountImpl::new);
        }
    }

}
