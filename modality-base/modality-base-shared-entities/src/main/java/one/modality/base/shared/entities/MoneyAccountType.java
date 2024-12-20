package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasCode;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

public interface MoneyAccountType extends
    EntityHasName,
    EntityHasLabel,
    EntityHasIcon,
    EntityHasCode {

    String customer = "customer";
    String supplier = "supplier";
    String internal = "internal";

    default void setCustomer(Boolean value) {
        setFieldValue(customer, value);
    }

    default Boolean isCustomer() {
        return getBooleanFieldValue(customer);
    }

    default void setSupplier(Boolean value) {
        setFieldValue(supplier, value);
    }

    default Boolean isSupplier() {
        return getBooleanFieldValue(supplier);
    }

    default void setInternal(Boolean value) {
        setFieldValue(internal, value);
    }

    default Boolean isInternal() {
        return getBooleanFieldValue(internal);
    }
}