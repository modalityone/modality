package org.modality_project.base.shared.entities;

import org.modality_project.base.shared.entities.markers.EntityHasCode;
import org.modality_project.base.shared.entities.markers.EntityHasIcon;
import org.modality_project.base.shared.entities.markers.EntityHasLabel;
import org.modality_project.base.shared.entities.markers.EntityHasName;

public interface MoneyAccountType extends
        EntityHasName,
        EntityHasLabel,
        EntityHasIcon,
        EntityHasCode {

    default void setCustomer(Boolean customer) {
        setFieldValue("customer", customer);
    }

    default Boolean isCustomer() {
        return getBooleanFieldValue("customer");
    }

    default void setSupplier(Boolean supplier) {
        setFieldValue("supplier", supplier);
    }

    default Boolean isSupplier() {
        return getBooleanFieldValue("supplier");
    }

    default void setInternal(Boolean internal) {
        setFieldValue("internal", internal);
    }

    default Boolean isInternal() {
        return getBooleanFieldValue("internal");
    }

}
