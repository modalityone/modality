package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface EntityHasDate extends Entity, HasDate {

    @Override
    default void setDate(LocalDate date) {
        setFieldValue("date", date);
    }

    @Override
    default LocalDate getDate() {
        return getLocalDateFieldValue("date");
    }

}
