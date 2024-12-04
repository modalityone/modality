package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Person;

/**
 * @author Bruno Salmon
 */
public interface EntityHasUserPerson extends Entity, HasUserPerson {

    String userPerson = "userPerson";

    @Override
    default void setUserPerson(Object value) {
        setForeignField(userPerson, value);
    }

    @Override
    default EntityId getUserPersonId() {
        return getForeignEntityId(userPerson);
    }

    @Override
    default Person getUserPerson() {
        return getForeignEntity(userPerson);
    }
}