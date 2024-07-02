package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Person;

/**
 * @author Bruno Salmon
 */
public interface EntityHasUserPerson extends Entity, HasUserPerson {

    @Override
    default void setUserPerson(Object person) {
        setForeignField("userPerson", person);
    }

    @Override
    default EntityId getUserPersonId() {
        return getForeignEntityId("userPerson");
    }

    @Override
    default Person getUserPerson() {
        return getForeignEntity("userPerson");
    }

}
