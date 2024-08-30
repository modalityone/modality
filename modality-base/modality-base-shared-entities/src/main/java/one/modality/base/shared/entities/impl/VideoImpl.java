package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Video;

/**
 * @author Bruno Salmon
 */
public final class VideoImpl extends DynamicEntity implements Video {

    public VideoImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Video> {
        public ProvidedFactory() {
            super(Video.class, VideoImpl::new);
        }
    }
}
