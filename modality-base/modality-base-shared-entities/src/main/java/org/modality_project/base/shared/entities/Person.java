package org.modality_project.base.shared.entities;

import org.modality_project.base.shared.entities.markers.EntityHasEvent;
import org.modality_project.base.shared.entities.markers.EntityHasPersonalDetails;
import dev.webfx.stack.framework.shared.orm.entity.Entity;

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
