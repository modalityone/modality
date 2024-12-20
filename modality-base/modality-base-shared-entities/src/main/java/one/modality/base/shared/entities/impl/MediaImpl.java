package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Media;

/**
 * @author Bruno Salmon
 */
public final class MediaImpl extends DynamicEntity implements Media {

    public MediaImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Media> {
        public ProvidedFactory() {
            super(Media.class, MediaImpl::new);
        }
    }
}
