package mongoose.base.shared.entities;

import mongoose.base.shared.entities.markers.EntityHasCode;
import mongoose.base.shared.entities.markers.EntityHasIcon;
import mongoose.base.shared.entities.markers.EntityHasName;

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
