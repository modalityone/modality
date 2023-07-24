package one.modality.event.client.controls.calendargraphic;

import java.util.function.Consumer;
import javafx.beans.property.Property;

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
