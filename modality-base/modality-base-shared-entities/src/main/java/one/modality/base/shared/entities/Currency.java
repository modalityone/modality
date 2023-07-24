package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasCode;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Currency extends EntityHasCode, EntityHasName, EntityHasIcon {

  default void setSymbol(String symbol) {
    setFieldValue("symbol", symbol);
  }

  default String getSymbol() {
    return getStringFieldValue("symbol");
  }
}
