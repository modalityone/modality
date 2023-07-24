package one.modality.event.client.controls.calendargraphic;

import javafx.scene.Node;
import one.modality.event.client.businessdata.calendar.Calendar;
import one.modality.event.client.controls.calendargraphic.impl.CalendarGraphicImpl;

/**
 * @author Bruno Salmon
 */
public interface CalendarGraphic extends HasCalendarClickHandlerProperty {

  Calendar getCalendar();

  Node getNode();

  void setCalendar(Calendar calendar);

  static CalendarGraphic create(Calendar calendar) {
    return new CalendarGraphicImpl(calendar);
  }
}
