package one.modality.event.client2018.businessdata.calendar;

import one.modality.hotel.shared2018.businessdata.time.TimeInterval;

import java.util.Collection;

/**
 * @author Bruno Salmon
 */
public interface Calendar {

    TimeInterval getPeriod();

    Collection<CalendarTimeline> getTimelines();

}
