package one.modality.base.shared.entities.impl;

import dev.webfx.platform.util.Objects;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.EventType;

/**
 * @author Bruno Salmon
 */
public final class EventTypeImpl extends DynamicEntity implements EventType {

    public EventTypeImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<EventType> {
        public ProvidedFactory() {
            super(EventType.class, EventTypeImpl::new);
        }
    }
}
