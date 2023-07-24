package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasOnline extends Entity, HasOnline {

  @Override
  default void setOnline(Boolean online) {
    setFieldValue("online", online);
  }

  @Override
  default Boolean isOnline() {
    return getBooleanFieldValue("online");
  }
}
