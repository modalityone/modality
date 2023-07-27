package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Podcast;

/**
 * @author Bruno Salmon
 */
public final class PodcastImpl extends DynamicEntity implements Podcast {

    public PodcastImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Podcast> {
        public ProvidedFactory() {
            super(Podcast.class, PodcastImpl::new);
        }
    }
}
