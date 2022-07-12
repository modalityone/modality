package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.ItemFamily;
import org.modality_project.base.shared.entities.ItemFamilyType;
import dev.webfx.stack.orm.entity.EntityId;

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
