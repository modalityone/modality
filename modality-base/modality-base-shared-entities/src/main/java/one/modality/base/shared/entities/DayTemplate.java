package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasName;

public interface DayTemplate extends
    EntityHasEvent,
    EntityHasName {

    default void setDates(String dates) {
        setFieldValue("dates", dates);
    }

    default String getDates() {
        return getStringFieldValue("dates");
    }

}
