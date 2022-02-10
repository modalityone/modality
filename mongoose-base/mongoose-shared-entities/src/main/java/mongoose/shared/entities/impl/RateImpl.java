package mongoose.shared.entities.impl;

import mongoose.shared.entities.Rate;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class RateImpl extends DynamicEntity implements Rate {

    public RateImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Rate> {
        public ProvidedFactory() {
            super(Rate.class, RateImpl::new);
        }
    }
}
