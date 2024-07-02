package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Person;

/**
 * @author Bruno Salmon
 */
public interface HasUserPerson {

    void setUserPerson(Object userPerson);

    EntityId getUserPersonId();

    Person getUserPerson();

}
