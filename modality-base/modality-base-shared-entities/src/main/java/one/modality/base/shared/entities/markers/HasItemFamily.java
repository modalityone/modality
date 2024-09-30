package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.ItemFamily;
import one.modality.base.shared.entities.KnownItemFamily;
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
    default KnownItemFamily getItemFamilyType() {
        ItemFamily itemFamily = getItemFamily();
        return itemFamily == null ? KnownItemFamily.UNKNOWN : itemFamily.getItemFamilyType();
    }
}
