package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.ItemFamily;
import one.modality.base.shared.entities.ItemFamilyType;

/**
 * @author Bruno Salmon
 */
public interface HasItemFamily extends HasItemFamilyType {

  void setItemFamily(Object itemFamily);

  EntityId getItemFamilyId();

  ItemFamily getItemFamily();

  default boolean hasItemFamily() {
    return getItemFamily() != null;
  }

  @Override
  default ItemFamilyType getItemFamilyType() {
    ItemFamily itemFamily = getItemFamily();
    return itemFamily == null ? ItemFamilyType.UNKNOWN : itemFamily.getItemFamilyType();
  }
}
