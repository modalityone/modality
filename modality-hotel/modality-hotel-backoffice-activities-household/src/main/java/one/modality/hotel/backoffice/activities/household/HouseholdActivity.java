package one.modality.hotel.backoffice.activities.household;

import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.hotel.backoffice.accommodation.AccommodationStatusBarUpdater;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AccommodationStatusBar;
import one.modality.hotel.backoffice.accommodation.AttendeeLegend;

import java.util.List;

final class HouseholdActivity extends OrganizationDependentViewDomainActivity implements
        AccommodationStatusBarUpdater,
        OperationActionFactoryMixin {

    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();
    private final HouseholdGanttCanvas householdGanttCanvas = new HouseholdGanttCanvas(pm, this);
    private final AccommodationStatusBar accommodationStatusBar = new AccommodationStatusBar();
    private final AttendeeLegend attendeeLegend = new AttendeeLegend();

    public HouseholdActivity() {
        pm.doFXBindings();
    }

    @Override
    public Node buildUi() {
        BorderPane borderPane = new BorderPane(householdGanttCanvas.buildCanvasContainer());

        CheckBox allRoomsCheckBox = new CheckBox("All rooms");
        allRoomsCheckBox.setSelected(false);
        householdGanttCanvas.parentsProvidedProperty().bind(allRoomsCheckBox.selectedProperty());

        CheckBox showKeyCheckBox = new CheckBox("Show Legend");
        showKeyCheckBox.setOnAction(e -> {
            borderPane.setLeft(showKeyCheckBox.isSelected() ? attendeeLegend : null);
        });
        HBox bottomPane = new HBox(10, allRoomsCheckBox, showKeyCheckBox, accommodationStatusBar);
        bottomPane.setBackground(new Background(new BackgroundFill(Color.web("#e0dcdc"), null, null)));
        bottomPane.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(accommodationStatusBar, Priority.ALWAYS);
        borderPane.setBottom(bottomPane);

        return borderPane;
    }

    @Override
    public void setEntities(List<Attendance> attendances) {
        accommodationStatusBar.setEntities(attendances);
    }

    @Override
    public void setAllScheduledResource(List<ScheduledResource> allScheduledResource) {
        accommodationStatusBar.setAllScheduledResource(allScheduledResource);
    }

    @Override
    public void onResume() {
        super.onResume();
        FXGanttVisibility.setGanttVisibility(GanttVisibility.EVENTS);
    }

    @Override
    public void onPause() {
        FXGanttVisibility.setGanttVisibility(GanttVisibility.HIDDEN);
        super.onPause();
    }

    /*==================================================================================================================
    =================================================== Logical layer ==================================================
    ==================================================================================================================*/

    @Override
    protected void startLogic() {
        householdGanttCanvas.startLogic(this);
    }

    @Override
    public AccommodationPresentationModel getPresentationModel() {
        return pm;
    }

}
