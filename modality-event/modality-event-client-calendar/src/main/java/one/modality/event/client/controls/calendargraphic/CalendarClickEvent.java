package one.modality.event.client.controls.calendargraphic;

import javafx.scene.input.MouseEvent;

import one.modality.event.client.businessdata.calendar.CalendarTimeline;

/**
 * @author Bruno Salmon
 */
public final class CalendarClickEvent {

    private final MouseEvent mouseEvent;
    private final CalendarCell calendarCell;
    private final CalendarTimeline calendarTimeline;

    public CalendarClickEvent(
            MouseEvent mouseEvent, CalendarCell calendarCell, CalendarTimeline calendarTimeline) {
        this.mouseEvent = mouseEvent;
        this.calendarCell = calendarCell;
        this.calendarTimeline = calendarTimeline;
    }

    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }

    public CalendarCell getCalendarCell() {
        return calendarCell;
    }

    public CalendarTimeline getCalendarTimeline() {
        return calendarTimeline;
    }
}
