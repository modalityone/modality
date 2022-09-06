package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.*;
import one.modality.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface ItemFamily extends
        EntityHasCode,
        EntityHasName,
        EntityHasLabel,
        EntityHasIcon,
        HasItemFamilyType {

    @Override
    default ItemFamilyType getItemFamilyType() {
        return ItemFamilyType.fromCode(getCode());
    }
}
