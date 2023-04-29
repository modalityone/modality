package one.modality.hotel.backoffice.activities.roomcalendar;

import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import one.modality.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;

final class RoomCalendarActivity extends EventDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    private final RoomCalendarGanttCanvas roomCalendarGanttCanvas = new RoomCalendarGanttCanvas();

    @Override
    public Node buildUi() {
        BorderPane borderPane = new BorderPane(roomCalendarGanttCanvas.buildCanvasContainer());
        CheckBox groupBlocksCheckBox = new CheckBox("Group blocks");
        roomCalendarGanttCanvas.blocksGroupingProperty.bind(groupBlocksCheckBox.selectedProperty());
        borderPane.setBottom(groupBlocksCheckBox);
        return borderPane;
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
        roomCalendarGanttCanvas.startLogic(this);
    }

}
