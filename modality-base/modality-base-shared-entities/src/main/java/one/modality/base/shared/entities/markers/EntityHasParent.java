package one.modality.base.shared.entities.markers;

import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasParent<P extends Entity> extends Entity, HasParent<P> {

  @Override
  default void setParent(Object parent) {
    setForeignField("parent", parent);
  }

  @Override
  default EntityId getParentId() {
    return getForeignEntityId("parent");
  }

  @Override
  default P getParent() {
    return getForeignEntity("parent");
  }

  default Object getFieldValueOrParent(Object domainFieldId) {
    Object fieldValue = getFieldValue(domainFieldId);
    if (fieldValue != null) return fieldValue;
    P parent = getParent();
    if (parent instanceof EntityHasParent)
      return ((EntityHasParent) parent).getFieldValueOrParent(domainFieldId);
    return parent == null ? null : parent.getFieldValue(domainFieldId);
  }

  default String getStringFieldValueOrParent(Object domainFieldId) {
    return Strings.toString(getFieldValueOrParent(domainFieldId));
  }

  default boolean hasParent() {
    return getParent() != null;
  }

  default boolean hasNoParent() {
    return !hasParent();
  }
}
