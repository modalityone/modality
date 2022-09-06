package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.ItemFamily;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasItemFamily extends Entity, HasItemFamily {

    @Override
    default void setItemFamily(Object itemFamily) {
        setForeignField("itemFamily", itemFamily);
    }

    @Override
    default EntityId getItemFamilyId() {
        return getForeignEntityId("itemFamily");
    }

    @Override
    default ItemFamily getItemFamily() {
        return getForeignEntity("itemFamily");
    }
}
