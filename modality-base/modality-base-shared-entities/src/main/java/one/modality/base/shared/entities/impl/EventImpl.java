package one.modality.base.shared.entities.impl;

import dev.webfx.platform.util.Objects;
import one.modality.base.shared.entities.Event;
import one.modality.hotel.shared2018.businessdata.time.DateTimeRange;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class EventImpl extends DynamicEntity implements Event {

    public EventImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    private DateTimeRange parsedDateTimeRange;
    @Override
    public DateTimeRange getParsedDateTimeRange() {
        if (parsedDateTimeRange == null)
            parsedDateTimeRange = DateTimeRange.parse(getDateTimeRange());
        return parsedDateTimeRange;
    }

    private DateTimeRange parsedMinDateTimeRange;
    @Override
    public DateTimeRange getParsedMinDateTimeRange() {
        if (parsedMinDateTimeRange == null)
            parsedMinDateTimeRange = DateTimeRange.parse(getMinDateTimeRange());
        return parsedMinDateTimeRange;
    }

    private DateTimeRange parsedMaxDateTimeRange;
    @Override
    public DateTimeRange getParsedMaxDateTimeRange() {
        if (parsedMaxDateTimeRange == null)
            parsedMaxDateTimeRange = DateTimeRange.parse(getMaxDateTimeRange());
        return parsedMaxDateTimeRange;
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Event> {
        public ProvidedFactory() {
            super(Event.class, EventImpl::new);
            // To make ReactiveDqlStatementAPI.ifInstanceOf() work with Event.class (see BookingsActivity)
            Objects.registerInstanceOf(Event.class, o -> o instanceof Event);
        }
    }
}
