package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.GatewayCompany;

/**
 * @author Bruno Salmon
 */
public final class GatewayCompanyImpl extends DynamicEntity implements GatewayCompany {

    public GatewayCompanyImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<GatewayCompany> {
        public ProvidedFactory() {
            super(GatewayCompany.class, GatewayCompanyImpl::new);
        }
    }
}
