package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasName;

public interface DayTemplate extends
    EntityHasEvent,
    EntityHasName {
    String dates = "dates";

    default void setDates(String value) {
        setFieldValue(dates, value);
    }

    default String getDates() {
        return getStringFieldValue(dates);
    }
}