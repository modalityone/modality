package one.modality.event.client.businessdata.calendar.impl;

import javafx.beans.property.Property;
import one.modality.hotel.shared.businessdata.time.DateTimeRange;
import one.modality.hotel.shared.businessdata.time.DayTimeRange;
import one.modality.event.client.businessdata.calendar.CalendarTimeline;
import javafx.scene.paint.Paint;

/**
 * @author Bruno Salmon
 */
public final class CalendarTimelineImpl implements CalendarTimeline {

    private final DateTimeRange dateTimeRange;
    private final DayTimeRange dayTimeRange;
    private final Property<String> displayNameProperty;
    private final Paint backgroundFill;
    private final Object source;


    public CalendarTimelineImpl(DateTimeRange dateTimeRange, DayTimeRange dayTimeRange, Property<String> displayNameProperty, Paint backgroundFill, Object source) {
        this.dateTimeRange = dateTimeRange;
        this.dayTimeRange = dayTimeRange;
        this.displayNameProperty = displayNameProperty;
        this.backgroundFill = backgroundFill;
        this.source = source;
    }

    @Override
    public DateTimeRange getDateTimeRange() {
        return dateTimeRange;
    }

    @Override
    public DayTimeRange getDayTimeRange() {
        return dayTimeRange;
    }

    @Override
    public Property<String> displayNameProperty() {
        return displayNameProperty;
    }

    @Override
    public Paint getBackgroundFill() {
        return backgroundFill;
    }

    @Override
    public Object getSource() {
        return source;
    }
}
