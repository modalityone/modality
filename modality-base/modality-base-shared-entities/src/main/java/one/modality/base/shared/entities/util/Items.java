package one.modality.base.shared.entities.util;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.markers.HasItem;
import one.modality.base.shared.knownitems.KnownItemFamily;

/**
 * @author Bruno Salmon
 */
public final class Items {

    public static boolean isOfFamily(Item item, KnownItemFamily family) {
        return item != null && Entities.samePrimaryKey(item.getFamilyId(), family.getPrimaryKey());
    }

    public static boolean isOfFamily(HasItem hasItem, KnownItemFamily family) {
        return hasItem != null && isOfFamily(hasItem.getItem(), family);
    }

}
