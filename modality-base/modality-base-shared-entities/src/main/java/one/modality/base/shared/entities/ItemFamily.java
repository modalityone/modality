package one.modality.base.shared.entities;

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

    String TEACHING_FAMILY_CODE = "teach";
    String MEALS_FAMILY_CODE = "meals";
    String ACCOMMODATION_FAMILY_CODE = "acco";

    @Override
    default ItemFamilyType getItemFamilyType() {
        return ItemFamilyType.fromCode(getCode());
    }
}
