package org.modality_project.base.shared.entities.markers;

import org.modality_project.hotel.shared.businessdata.time.DateTimeRange;

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
