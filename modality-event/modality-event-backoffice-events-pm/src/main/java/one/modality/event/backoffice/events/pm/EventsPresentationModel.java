package one.modality.event.backoffice.events.pm;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class EventsPresentationModel extends OrganizationDependentGenericTablePresentationModel {

    // Display input

    private final Property<Boolean> withBookingsProperty = new SimpleObjectProperty<>(true); // Limit initially set to true
    public Property<Boolean> withBookingsProperty() { return withBookingsProperty; }

    private final Property<LocalDate> timeWindowStartProperty = new SimpleObjectProperty<>(LocalDate.now().minusWeeks(1));
    public Property<LocalDate> timeWindowStartProperty() { return timeWindowStartProperty; }

    private final Property<LocalDate> timeWindowEndProperty = new SimpleObjectProperty<>(LocalDate.now().plusWeeks(3));
    public Property<LocalDate> timeWindowEndProperty() { return timeWindowEndProperty; }

}
