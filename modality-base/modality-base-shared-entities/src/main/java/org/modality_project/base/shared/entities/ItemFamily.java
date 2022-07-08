package org.modality_project.base.shared.entities;

import org.modality_project.base.shared.entities.markers.*;
import org.modality_project.base.shared.entities.markers.*;

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
