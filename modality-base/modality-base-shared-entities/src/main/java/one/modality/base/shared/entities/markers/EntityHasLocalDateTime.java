package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface EntityHasLocalDateTime extends Entity, HasLocalDateTime {

    String date = "date";

    @Override
    default void setDate(LocalDateTime value) {
        setFieldValue(date, value);
    }

    @Override
    default LocalDateTime getDate() {
        return getLocalDateTimeFieldValue(date);
    }

}