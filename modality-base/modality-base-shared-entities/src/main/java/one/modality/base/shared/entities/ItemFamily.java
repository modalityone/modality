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
        EntityHasOrd,
        HasItemFamilyType {

    @Override
    default KnownItemFamily getItemFamilyType() {
        return KnownItemFamily.fromCode(getCode());
    }
}
