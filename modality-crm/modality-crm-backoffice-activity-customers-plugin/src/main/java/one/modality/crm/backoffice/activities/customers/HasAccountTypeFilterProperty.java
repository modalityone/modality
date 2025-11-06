package one.modality.crm.backoffice.activities.customers;

import javafx.beans.property.StringProperty;

/**
 * Interface for presentation models that have an account type filter property.
 *
 * @author Claude Code
 */
public interface HasAccountTypeFilterProperty {

    StringProperty accountTypeFilterProperty();

    default void setAccountTypeFilter(String value) {
        accountTypeFilterProperty().set(value);
    }
}
