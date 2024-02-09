package one.modality.event.frontoffice.activities.booking.fx;

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
            //FXEventId.setEventId(getEventId());
        }
    };

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
