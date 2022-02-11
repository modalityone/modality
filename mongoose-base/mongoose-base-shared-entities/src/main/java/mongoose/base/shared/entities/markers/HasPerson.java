package mongoose.base.shared.entities.markers;

import mongoose.base.shared.entities.Person;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasPerson {

    void setPerson(Object person);

    EntityId getPersonId();

    Person getPerson();

}
