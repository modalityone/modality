package one.modality.event.client.businessdata.calendar;

import one.modality.hotel.shared.businessdata.time.TimeInterval;

import java.util.Collection;

/**
 * @author Bruno Salmon
 */
public interface Calendar {

    TimeInterval getPeriod();

    Collection<CalendarTimeline> getTimelines();

}
