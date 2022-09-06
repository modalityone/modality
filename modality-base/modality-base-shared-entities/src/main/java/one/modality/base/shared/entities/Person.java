package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetails;
import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface Person extends Entity, EntityHasPersonalDetails, EntityHasEvent {

    default Object getBirthDateField() { return "birthdate";}

    default void setBirthDate(LocalDate birthDate) {
        setFieldValue(getBirthDateField(), birthDate);
    }

    default LocalDate getBirthDate() {
        return getLocalDateFieldValue(getBirthDateField());
    }


}
