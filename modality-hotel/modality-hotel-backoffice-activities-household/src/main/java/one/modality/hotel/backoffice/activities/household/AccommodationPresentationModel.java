package one.modality.hotel.backoffice.activities.household;

import dev.webfx.extras.time.window.TimeWindow;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.time.LocalDate;

public class AccommodationPresentationModel extends OrganizationDependentGenericTablePresentationModel
        implements TimeWindow<LocalDate> {

    // Display input

    private final ObjectProperty<LocalDate> timeWindowStartProperty = new SimpleObjectProperty<>(LocalDate.now().minusWeeks(1));
    public ObjectProperty<LocalDate> timeWindowStartProperty() { return timeWindowStartProperty; }

    private final ObjectProperty<LocalDate> timeWindowEndProperty = new SimpleObjectProperty<>(LocalDate.now().plusWeeks(3));
    public ObjectProperty<LocalDate> timeWindowEndProperty() { return timeWindowEndProperty; }

    public void bindFXs() {
        organizationIdProperty().bind(FXOrganization.organizationProperty());
        bindTimeWindow(FXGanttTimeWindow.ganttTimeWindow()); // barsLayout will itself be bound to FXGanttTimeWindow (see below)
    }
}
