package one.modality.event.frontoffice.eventheader;

import dev.webfx.platform.async.Future;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public interface EventHeader {

    Region getView();

    default Event getEvent() {
        return eventProperty().get();
    }

    default void setEvent(Event event) {
        this.eventProperty().set(event);
    }

    ObjectProperty<Event> eventProperty();

    default Object getLanguage() {
        return languageProperty().get();
    }

    default void setLanguage(Object language) {
        languageProperty().set(language);
    }

    ObjectProperty<Object> languageProperty();

    // Methods from LocalEventHeader when moving down from StepBBookEventSlide (they maybe lack genericity)

    String getLoadEventFields();

    Future<Event> loadAndSetEvent(Event event);

    default boolean isEventLoaded() {
        return eventLoadedProperty().get();
    }

    ObservableBooleanValue eventLoadedProperty();

    ObjectProperty<Font> eventFontProperty();

    ObjectProperty<Font> descriptionFontProperty();

    default void setMaxPageWidth(double maxPageWidth) {
        getView().setMaxWidth(maxPageWidth);
    }

}
