package one.modality.base.shared.entities.markers;

import one.modality.hotel.shared2018.businessdata.time.DateTimeRange;

/**
 * @author Bruno Salmon
 */
public interface HasMinDateTimeRange {

    void setMinDateTimeRange(String minDateTimeRange);

    String getMinDateTimeRange();

    default DateTimeRange getParsedMinDateTimeRange() { // Should be overridden by implementing class to have a cached value
        return DateTimeRange.parse(getMinDateTimeRange());
    }


}
