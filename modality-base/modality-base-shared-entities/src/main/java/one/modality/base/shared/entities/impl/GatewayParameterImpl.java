package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.GatewayParameter;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class GatewayParameterImpl extends DynamicEntity implements GatewayParameter {

    public GatewayParameterImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<GatewayParameter> {
        public ProvidedFactory() {
            super(GatewayParameter.class, GatewayParameterImpl::new);
        }
    }
}
