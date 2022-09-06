package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Country extends
        EntityHasName,
        EntityHasLabel,
        EntityHasIcon {

    default void setIsoAlpha2(String isoAlpha2) {
        setFieldValue("iso_alpha2", isoAlpha2);
    }

    default String getIsoAlpha2() {
        return getStringFieldValue("iso_alpha2");
    }

}
