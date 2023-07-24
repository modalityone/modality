package one.modality.event.client.businessdata.calendar;

import javafx.beans.property.Property;
import javafx.scene.paint.Paint;
import one.modality.hotel.shared.businessdata.time.DateTimeRange;
import one.modality.hotel.shared.businessdata.time.DayTimeRange;

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
