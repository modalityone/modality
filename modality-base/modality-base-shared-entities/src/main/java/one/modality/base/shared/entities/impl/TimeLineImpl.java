package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Timeline;
import one.modality.base.shared.entities.Topic;

/**
 * @author Bruno Salmon
 */
public final class TimeLineImpl extends DynamicEntity implements Timeline {

    public TimeLineImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Timeline> {
        public ProvidedFactory() {
            super(Timeline.class, TimeLineImpl::new);
        }
    }
}
