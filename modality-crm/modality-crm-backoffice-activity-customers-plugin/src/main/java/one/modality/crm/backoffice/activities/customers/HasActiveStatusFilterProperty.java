package one.modality.crm.backoffice.activities.customers;

import javafx.beans.property.StringProperty;

/**
 * Interface for presentation models that support active status filtering.
 *
 * @author Claude Code
 */
public interface HasActiveStatusFilterProperty {

    StringProperty activeStatusFilterProperty();

    default String getActiveStatusFilter() {
        return activeStatusFilterProperty().get();
    }

    default void setActiveStatusFilter(String value) {
        activeStatusFilterProperty().set(value);
    }
}
