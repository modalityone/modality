package one.modality.event.frontoffice.eventheader;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractEventHeader implements EventHeader {

    protected final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>(); // The event loaded from the event id
    protected final ObjectProperty<Object> languageProperty = new SimpleObjectProperty<>();

    public ObjectProperty<Event> eventProperty() {
        return eventProperty;
    }

    public ObjectProperty<Object> languageProperty() {
        return languageProperty;
    }

}
