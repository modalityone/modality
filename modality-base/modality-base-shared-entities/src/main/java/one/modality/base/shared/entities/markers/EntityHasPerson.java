package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Person;

/**
 * @author Bruno Salmon
 */
public interface EntityHasPerson extends Entity, HasPerson {

    String person = "person";

    @Override
    default void setPerson(Object value) {
        setForeignField(person, value);
    }

    @Override
    default EntityId getPersonId() {
        return getForeignEntityId(person);
    }

    @Override
    default Person getPerson() {
        return getForeignEntity(person);
    }
}