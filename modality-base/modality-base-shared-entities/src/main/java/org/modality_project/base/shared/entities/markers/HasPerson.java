package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Person;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasPerson {

    void setPerson(Object person);

    EntityId getPersonId();

    Person getPerson();

}
