package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import java.time.LocalDate;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetails;

/**
 * @author Bruno Salmon
 */
public interface Person extends Entity, EntityHasPersonalDetails, EntityHasEvent {

  default Object getBirthDateField() {
    return "birthdate";
  }

  default void setBirthDate(LocalDate birthDate) {
    setFieldValue(getBirthDateField(), birthDate);
  }

  default LocalDate getBirthDate() {
    return getLocalDateFieldValue(getBirthDateField());
  }
}
