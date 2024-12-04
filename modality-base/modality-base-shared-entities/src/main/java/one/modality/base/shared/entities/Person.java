package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetails;
import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface Person extends Entity, EntityHasPersonalDetails, EntityHasEvent {
    String birthDate = "birthdate";

    default void setBirthDate(LocalDate value) {
        setFieldValue(birthDate, value);
    }

    default LocalDate getBirthDate() {
        return getLocalDateFieldValue(birthDate);
    }
}