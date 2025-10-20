package one.modality.base.shared.entities.impl;

import dev.webfx.platform.substitution.Substitutor;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Event;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Bruno Salmon
 */
public final class EventImpl extends DynamicEntity implements Event {

    private static final LocalDateTime APP_START_AT_EVENT_LOCAL_DATETIME = Times.toLocalDateTime(Substitutor.substitute("${{APP_START_AT_EVENT_LOCAL_DATETIME}}")); // Ex: 2025-06-11T23:58:50

    public EventImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    private ZoneId eventZoneId;
    private Clock eventClock;

    public ZoneId getEventZoneId() {
        if (eventZoneId == null) {
            String timezone = getTimezone();
            if (timezone != null)
                eventZoneId = ZoneId.of(timezone);
        }
        return eventZoneId;
    }

    public Clock getEventClock() {
        if (eventClock == null) {
            if (getEventZoneId() != null) {
                eventClock = Clock.system(eventZoneId);
            } else
                eventClock = Clock.systemDefaultZone();
            if (APP_START_AT_EVENT_LOCAL_DATETIME != null)
                eventClock = Clock.offset(eventClock, Duration.between(LocalDateTime.now(eventClock), APP_START_AT_EVENT_LOCAL_DATETIME));
        }
        return eventClock;
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Event> {
        public ProvidedFactory() {
            super(Event.class, EventImpl::new);
            // To make ReactiveDqlStatementAPI.ifInstanceOf() work with Event.class (see BookingsActivity)
            Objects.registerInstanceOf(Event.class, o -> o instanceof Event);
        }
    }
}
