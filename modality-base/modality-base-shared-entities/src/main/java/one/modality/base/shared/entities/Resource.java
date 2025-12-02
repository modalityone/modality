package one.modality.base.shared.entities;

import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasSite;

import java.time.LocalDateTime;

public interface Resource extends Entity,
        EntityHasSite,
        EntityHasName {

    String cleaningState = "cleaningState";
    String lastCleaningDate = "lastCleaningDate";
    String lastInspectionDate = "lastInspectionDate";
    String kbs2ToKbs3GlobalResource = "kbs2ToKbs3GlobalResource";

    default void setCleaningState(Object value) {
        setFieldValue(cleaningState, Strings.stringValue(value));
    }

    default CleaningState getCleaningState() {
        return CleaningState.of(getStringFieldValue(cleaningState));
    }

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

    default void setKbs2ToKbs3GlobalResource(Object value) {
        setForeignField(kbs2ToKbs3GlobalResource, value);
    }

    default EntityId getKbs2ToKbs3GlobalResourceId() {
        return getForeignEntityId(kbs2ToKbs3GlobalResource);
    }

    default Resource getKbs2ToKbs3GlobalResource() {
        return getForeignEntity(kbs2ToKbs3GlobalResource);
    }

}
