//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package one.modality.event.frontoffice.eventheader;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Region;
import one.modality.base.shared.entities.Event;

public interface EventHeader {

    Region getView();

    default Event getEvent() {
        return (Event)this.eventProperty().get();
    }

    default void setEvent(Event event) {
        this.eventProperty().set(event);
    }

    ObjectProperty<Event> eventProperty();

    default Object getLanguage() {
        return this.languageProperty().get();
    }

    default void setLanguage(Object language) {
        this.languageProperty().set(language);
    }

    ObjectProperty<Object> languageProperty();

}
