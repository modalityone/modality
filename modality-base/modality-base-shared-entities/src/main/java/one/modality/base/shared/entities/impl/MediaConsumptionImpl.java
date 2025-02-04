package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.MediaConsumption;

/**
 * @author Bruno Salmon
 */
public final class MediaConsumptionImpl extends DynamicEntity implements MediaConsumption {

    public MediaConsumptionImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<MediaConsumption> {
        public ProvidedFactory() {
            super(MediaConsumption.class, MediaConsumptionImpl::new);
        }
    }
}
