package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Person;

/**
 * @author Bruno Salmon
 */
public interface HasPerson {

  void setPerson(Object person);

  EntityId getPersonId();

  Person getPerson();
}
