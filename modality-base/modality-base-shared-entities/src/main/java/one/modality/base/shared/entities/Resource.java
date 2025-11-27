package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasSite;

import java.time.LocalDateTime;

public interface Resource extends Entity,
        EntityHasSite,
        EntityHasName {

    String lastCleaningDate = "lastCleaningDate";
    String lastInspectionDate = "lastInspectionDate";

    default void setLastCleaningDate(LocalDateTime value) {
        setFieldValue(lastCleaningDate, value);
    }

    default LocalDateTime getLastCleaningDate() {
        return getLocalDateTimeFieldValue(lastCleaningDate);
    }

    default void setLastInspectionDate(LocalDateTime value) {
        setFieldValue(lastInspectionDate, value);
    }

    default LocalDateTime getLastInspectionDate() {
        return getLocalDateTimeFieldValue(lastInspectionDate);
    }

}
