package one.modality.hotel.backoffice.accommodation;

import dev.webfx.extras.time.window.TimeWindow;
import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

public class AccommodationPresentationModel
    extends OrganizationDependentGenericTablePresentationModel implements TimeWindow<LocalDate> {

  private final ObjectProperty<LocalDate> timeWindowStartProperty = new SimpleObjectProperty<>();

  public ObjectProperty<LocalDate> timeWindowStartProperty() {
    return timeWindowStartProperty;
  }

  private final ObjectProperty<LocalDate> timeWindowEndProperty = new SimpleObjectProperty<>();

  public ObjectProperty<LocalDate> timeWindowEndProperty() {
    return timeWindowEndProperty;
  }

  public void doFXBindings() {
    organizationIdProperty().bind(FXOrganization.organizationProperty());
    bindTimeWindowBidirectional(FXGanttTimeWindow.ganttTimeWindow());
  }
}
