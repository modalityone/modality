package one.modality.event.client.businessdata.calendar;

import java.util.Collection;
import one.modality.hotel.shared.businessdata.time.TimeInterval;

/**
 * @author Bruno Salmon
 */
public interface Calendar {

  TimeInterval getPeriod();

  Collection<CalendarTimeline> getTimelines();
}
