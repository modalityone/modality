package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface EntityHasLocalDateTime extends Entity, HasLocalDateTime {

    @Override
    default void setDate(LocalDateTime date) {
        setFieldValue("date", date);
    }

    @Override
    default LocalDateTime getDate() {
        return getLocalDateTimeFieldValue("date");
    }

}
