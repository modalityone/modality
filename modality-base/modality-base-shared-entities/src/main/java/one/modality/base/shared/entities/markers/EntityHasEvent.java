package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public interface EntityHasEvent extends Entity, HasEvent {

  @Override
  default void setEvent(Object event) {
    setForeignField("event", event);
  }

  @Override
  default EntityId getEventId() {
    return getForeignEntityId("event");
  }

  @Override
  default Event getEvent() {
    return getForeignEntity("event");
  }
}
