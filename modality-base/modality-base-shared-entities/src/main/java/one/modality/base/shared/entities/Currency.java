package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasCode;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Currency extends
    EntityHasCode,
    EntityHasName,
    EntityHasIcon {
    String symbol = "symbol";

    default void setSymbol(String value) {
        setFieldValue(symbol, value);
    }

    default String getSymbol() {
        return getStringFieldValue(symbol);
    }
}