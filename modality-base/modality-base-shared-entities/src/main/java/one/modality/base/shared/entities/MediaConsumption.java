package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasLocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface MediaConsumption extends Entity,
    EntityHasLocalDateTime {
    String media = "media";
    String attendance = "attendance";
    String played = "played";
    String downloaded = "downloaded";
    String durationMillis = "durationMillis";

    default void setMedia(Object value) {
        setForeignField(media, value);
    }

    default EntityId getMediaId() {
        return getForeignEntityId(media);
    }

    default Media getMedia() {
        return getForeignEntity(media);
    }

    default void setAttendance(Object value) {
        setForeignField(attendance, value);
    }

    default EntityId getAttendanceId() {
        return getForeignEntityId(attendance);
    }

    default Attendance getAttendance() {
        return getForeignEntity(attendance);
    }

    default void setPlayed(Boolean value) {
        setFieldValue(played, value);
    }

    default Boolean isPlayed() {
        return getBooleanFieldValue(played);
    }

    default void setDownloaded(Boolean value) {
        setFieldValue(downloaded, value);
    }

    default Boolean isDownloaded() {
        return getBooleanFieldValue(downloaded);
    }

    default void setDurationMillis(Long value) {
        setFieldValue(durationMillis, value);
    }

    default Long getDurationMillis() {
        return getLongFieldValue(durationMillis);
    }
}