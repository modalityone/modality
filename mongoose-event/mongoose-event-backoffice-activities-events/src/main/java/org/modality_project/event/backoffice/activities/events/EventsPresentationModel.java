package org.modality_project.event.backoffice.activities.events;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.modality_project.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;

/**
 * @author Bruno Salmon
 */
final class EventsPresentationModel extends OrganizationDependentGenericTablePresentationModel {

    // Display input

    private final Property<Boolean> withBookingsProperty = new SimpleObjectProperty<>(true); // Limit initially set to true
    Property<Boolean> withBookingsProperty() { return withBookingsProperty; }

}
