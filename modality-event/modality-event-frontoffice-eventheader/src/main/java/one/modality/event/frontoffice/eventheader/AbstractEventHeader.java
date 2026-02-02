package one.modality.event.frontoffice.eventheader;

import dev.webfx.platform.async.Future;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.text.Font;
import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractEventHeader implements EventHeader {

    protected final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>(); // The event loaded from the event id
    protected final ObjectProperty<Object> languageProperty = new SimpleObjectProperty<>();
    protected final BooleanProperty eventLoadedProperty = new SimpleBooleanProperty();
    protected final ObjectProperty<Font> eventFontProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<Font> descriptionFontProperty = new SimpleObjectProperty<>();

    @Override
    public ObjectProperty<Event> eventProperty() {
        return eventProperty;
    }

    @Override
    public ObjectProperty<Object> languageProperty() {
        return languageProperty;
    }

    @Override
    public Future<Event> loadAndSetEvent(Event event) {
        eventLoadedProperty.set(false);
        return event.<Event>onExpressionLoadedWithCache("modality/event/event-header", getLoadEventFields())
            .inUiThread()
            .onSuccess(ignored -> {
                setEvent(event);
                eventLoadedProperty.set(true);
            });
    }

    @Override
    public boolean isEventLoaded() {
        return eventLoadedProperty.get();
    }

    @Override
    public ObservableBooleanValue eventLoadedProperty() {
        return eventLoadedProperty;
    }

    @Override
    public ObjectProperty<Font> eventFontProperty() {
        return eventFontProperty;
    }

    @Override
    public ObjectProperty<Font> descriptionFontProperty() {
        return descriptionFontProperty;
    }

}
