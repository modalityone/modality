package one.modality.event.backoffice.event.fx;

import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Event;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXEvent {

    private final static ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            FXEventId.setEventId(getEventId());
        }
    };

    static {
        FXEventId.init();
    }

    static EntityId getEventId() {
        return Entities.getId(getEvent());
    }

    static EntityStore getEventStore() {
        Event event = getEvent();
        return event != null ? event.getStore() : EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());
    }

    public static ObjectProperty<Event> eventProperty() {
        return eventProperty;
    }

    public static Event getEvent() {
        return eventProperty.get();
    }

    public static void setEvent(Event event) {
        if (!Objects.equals(event, getEvent()))
            eventProperty.set(event);
    }

}
