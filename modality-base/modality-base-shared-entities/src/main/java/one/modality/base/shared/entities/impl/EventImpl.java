package one.modality.base.shared.entities.impl;

import dev.webfx.platform.util.Objects;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public final class EventImpl extends DynamicEntity implements Event {

    public EventImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Event> {
        public ProvidedFactory() {
            super(Event.class, EventImpl::new);
            // To make ReactiveDqlStatementAPI.ifInstanceOf() work with Event.class (see BookingsActivity)
            Objects.registerInstanceOf(Event.class, o -> o instanceof Event);
        }
    }
}
