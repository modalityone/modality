package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface EntityHasLocalDate extends Entity, HasLocalDate {

    String date = "date";

    @Override
    default void setDate(LocalDate value) {
        setFieldValue(date, value);
    }

    @Override
    default LocalDate getDate() {
        return getLocalDateFieldValue(date);
    }
}