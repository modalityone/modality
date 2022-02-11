package mongoose.base.shared.entities.markers;

import mongoose.base.shared.entities.ItemFamily;
import mongoose.base.shared.entities.ItemFamilyType;
import dev.webfx.framework.shared.orm.entity.EntityId;

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
