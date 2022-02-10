package mongoose.shared.entities.impl;

import mongoose.shared.entities.GatewayParameter;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

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
