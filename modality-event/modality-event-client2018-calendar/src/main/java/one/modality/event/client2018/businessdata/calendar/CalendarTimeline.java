package one.modality.event.client2018.businessdata.calendar;

import javafx.beans.property.Property;
import one.modality.hotel.shared2018.businessdata.time.DateTimeRange;
import one.modality.hotel.shared2018.businessdata.time.DayTimeRange;
import javafx.scene.paint.Paint;

/**
 * @author Bruno Salmon
 */
public interface CalendarTimeline {

    DateTimeRange getDateTimeRange();

    DayTimeRange getDayTimeRange();

    Property<String> displayNameProperty();

    Paint getBackgroundFill();

    Object getSource();

}
