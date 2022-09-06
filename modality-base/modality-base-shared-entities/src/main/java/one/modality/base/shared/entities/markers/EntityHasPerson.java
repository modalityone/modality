package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Person;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasPerson extends Entity, HasPerson {

    @Override
    default void setPerson(Object person) {
        setForeignField("person", person);
    }

    @Override
    default EntityId getPersonId() {
        return getForeignEntityId("person");
    }

    @Override
    default Person getPerson() {
        return getForeignEntity("person");
    }

}
