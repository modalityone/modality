package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetails;
import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface Person extends Entity, EntityHasPersonalDetails, EntityHasEvent {
    String birthDate = "birthdate";
    String frontendAccount = "frontendAccount";

    default void setFrontendAccount(Object value) {
        setForeignField(frontendAccount, value);
    }

    default EntityId getFrontendAccountId() {
        return getForeignEntityId(frontendAccount);
    }

    default FrontendAccount getFrontendAccount() {
        return getForeignEntity(frontendAccount);
    }

    default void setBirthDate(LocalDate value) {
        setFieldValue(birthDate, value);
    }

    default LocalDate getBirthDate() {
        return getLocalDateFieldValue(birthDate);
    }
}