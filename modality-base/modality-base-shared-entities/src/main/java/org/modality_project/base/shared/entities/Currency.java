package org.modality_project.base.shared.entities;

import org.modality_project.base.shared.entities.markers.EntityHasCode;
import org.modality_project.base.shared.entities.markers.EntityHasIcon;
import org.modality_project.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Currency extends
        EntityHasCode,
        EntityHasName,
        EntityHasIcon {

    default void setSymbol(String symbol) {
        setFieldValue("symbol", symbol);
    }

    default String getSymbol() {
        return getStringFieldValue("symbol");
    }

}
