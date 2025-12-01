package one.modality.hotel.backoffice.accommodation;

import dev.webfx.extras.time.window.TimeWindow;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.LocalDate;

public class AccommodationPresentationModel extends OrganizationDependentGenericTablePresentationModel
        implements TimeWindow<LocalDate> {

    private final ObjectProperty<LocalDate> timeWindowStartProperty = new SimpleObjectProperty<>();
    public ObjectProperty<LocalDate> timeWindowStartProperty() { return timeWindowStartProperty; }

    private final ObjectProperty<LocalDate> timeWindowEndProperty = new SimpleObjectProperty<>();
    public ObjectProperty<LocalDate> timeWindowEndProperty() { return timeWindowEndProperty; }

    // Note: this part of the TimeWindow is not used in the pm logic
    private final DoubleProperty timeWindowTranslateXProperty = new SimpleDoubleProperty();
    @Override
    public DoubleProperty timeWindowTranslateXProperty() {
        return timeWindowTranslateXProperty;
    }

    public void doFXBindings() {
        organizationIdProperty().bind(FXOrganizationId.organizationIdProperty());
        bindTimeWindowBidirectional(FXGanttTimeWindow.ganttTimeWindow());
    }
}
