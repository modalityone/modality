package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

import one.modality.base.shared.entities.Rate;

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
