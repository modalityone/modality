package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Item;

/**
 * @author Bruno Salmon
 */
public interface EntityHasItem extends Entity, HasItem {

  @Override
  default void setItem(Object item) {
    setForeignField("item", item);
  }

  @Override
  default EntityId getItemId() {
    return getForeignEntityId("item");
  }

  @Override
  default Item getItem() {
    return getForeignEntity("item");
  }
}
