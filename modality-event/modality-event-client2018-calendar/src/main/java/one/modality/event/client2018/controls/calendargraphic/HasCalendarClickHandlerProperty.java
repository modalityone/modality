package one.modality.event.client2018.controls.calendargraphic;

import javafx.beans.property.Property;

import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
interface HasCalendarClickHandlerProperty {

    Property<Consumer<CalendarClickEvent>> calendarClickHandlerProperty();
    default void setCalendarClickHandler(Consumer<CalendarClickEvent> calendarClickEventHandler) {
        calendarClickHandlerProperty().setValue(calendarClickEventHandler);
    }
    default Consumer<CalendarClickEvent> getCalendarClickHandler() {
        return calendarClickHandlerProperty().getValue();
    }

}
