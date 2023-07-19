package one.modality.event.client2018.controls.calendargraphic;

import one.modality.event.client2018.businessdata.calendar.Calendar;
import one.modality.event.client2018.controls.calendargraphic.impl.CalendarGraphicImpl;
import javafx.scene.Node;

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
