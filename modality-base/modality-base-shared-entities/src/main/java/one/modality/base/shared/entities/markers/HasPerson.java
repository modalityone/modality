package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Person;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasPerson {

    void setPerson(Object person);

    EntityId getPersonId();

    Person getPerson();

}
