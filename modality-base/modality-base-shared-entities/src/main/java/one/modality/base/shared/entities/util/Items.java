package one.modality.base.shared.entities.util;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.knownitems.KnownItemFamily;

/**
 * @author Bruno Salmon
 */
public final class Items {

    public static boolean isOfFamily(Item item, KnownItemFamily family) {
        return item != null && Entities.samePrimaryKey(item.getFamilyId(), family.getPrimaryKey());
    }

}
