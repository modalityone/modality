package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.ItemFamily;

/**
 * @author Bruno Salmon
 */
public interface EntityHasItemFamily extends Entity, HasItemFamily {

    String itemFamily = "itemFamily";

    @Override
    default void setItemFamily(Object value) {
        setForeignField(itemFamily, value);
    }

    @Override
    default EntityId getItemFamilyId() {
        return getForeignEntityId(itemFamily);
    }

    @Override
    default ItemFamily getItemFamily() {
        return getForeignEntity(itemFamily);
    }
}