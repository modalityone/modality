package one.modality.event.frontoffice.activities.booking.fx;

import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Event;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.frontoffice.activities.booking.process.EventAggregate;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXEventAggregate {

    private final static ObjectProperty<EventAggregate> eventAggregateProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            //FXEventId.setEventId(getEventId());
        }
    };

    public static ObjectProperty<EventAggregate> eventAggregateProperty() {
        return eventAggregateProperty;
    }

    public static EventAggregate getEventAggregate() {
        return eventAggregateProperty.get();
    }

    public static void setEventAggregate(EventAggregate eventAggregate) {
        if (!Objects.equals(eventAggregate, getEventAggregate()))
            eventAggregateProperty.set(eventAggregate);
    }

    static {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Event event = FXEvent.getEvent();
            FXEventAggregate.setEventAggregate(event == null ? null : new EventAggregate(event));
        }, FXEvent.eventProperty());
    }

}
