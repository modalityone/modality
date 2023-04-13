package one.modality.all.backoffice.application;

import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import one.modality.base.client.application.ModalityClientFrameContainerActivity;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.backoffice.event.fx.FXEvent;
import one.modality.base.client.gantt.visibility.fx.FXGanttVisibility;
import one.modality.base.client.gantt.visibility.GanttVisibility;
import one.modality.event.backoffice.events.ganttcanvas.EventsGanttCanvas;
import one.modality.event.backoffice.events.pm.EventsPresentationModel;

/**
 * @author Bruno Salmon
 */
public class ModalityBackOfficeFrameContainerActivity extends ModalityClientFrameContainerActivity {

    protected Pane containerPane;
    private Region containerHeader;
    private final EventsGanttCanvas eventsGanttCanvas = new EventsGanttCanvas(new EventsPresentationModel());
    private final Pane canvasPane = eventsGanttCanvas.getCanvasPane();

    @Override
    public Node buildUi() {
        containerPane = new Pane() {
            @Override
            protected void layoutChildren() {
                double width = getWidth();
                double nodeY = 0, nodeHeight = containerHeader.prefHeight(width);
                layoutInArea(containerHeader, 0, nodeY, width, nodeHeight, 0, HPos.CENTER, VPos.TOP);
                nodeY = FXLayoutMode.isCompactMode() ? 0 : nodeHeight;
                nodeHeight = 0;
                if (canvasPane.isManaged()) {
                    nodeHeight = canvasPane.prefHeight(width);
                    layoutInArea(canvasPane, 0, nodeY, width, nodeHeight, 0, HPos.CENTER, VPos.TOP);
                }
                Node mountNode = getMountNode();
                if (mountNode != null) {
                    nodeY += nodeHeight;
                    layoutInArea(mountNode, 0, nodeY, width, getHeight() - nodeY, 0, HPos.CENTER, VPos.TOP);
                }
            }
        };
        containerHeader = createContainerHeader();
        updateMountNode();
        mountNodeProperty().addListener(observable -> updateMountNode());
        // Requesting a layout for containerPane on layout mode changes
        FXLayoutMode.layoutModeProperty().addListener(observable -> containerPane.requestLayout());
        return containerPane;
    }

    @Override
    protected void startLogic() {
        eventsGanttCanvas.bindFXEventToSelection();
        eventsGanttCanvas.bindFXOrganization();
        eventsGanttCanvas.startLogic(this);
    }

    private void updateMountNode() {
        Node mountNode = getMountNode();
        if (mountNode == null)
            containerPane.getChildren().setAll(canvasPane, containerHeader);
        else
            containerPane.getChildren().setAll(canvasPane, mountNode, containerHeader);
    }

    @Override
    protected HBox createContainerHeaderCenterItem() {
        // Creating the organization selector
        EntityButtonSelector<Organization> organizationSelector = new EntityButtonSelector<>(
                "{class: 'Organization', alias: 'o', where: 'exists(select Event where organization=o)'}",
                this, containerPane, getDataSourceModel()
        );
        // Doing a bidirectional binding with FXOrganization
        FXOrganization.organizationProperty().bindBidirectional(organizationSelector.selectedItemProperty());
        Button organizationButton = organizationSelector.getButton();
        // Creating the event selector
        EntityButtonSelector<Event> eventSelector = new EntityButtonSelector<>(
                "{class: 'Event'}",
                this, containerPane, getDataSourceModel()
        );
        // Doing a bidirectional binding with FXOrganization
        FXEvent.eventProperty().bindBidirectional(eventSelector.selectedItemProperty());
        Button eventButton = eventSelector.getButton();
        // Updating the event condition when organisation changes
        FXProperties.runNowAndOnPropertiesChange(() ->
                eventSelector.setJsonOrClass("{class: 'Event', alias: 'e', columns: 'icon,name,dateIntervalFormat(startDate,endDate)', where: 'organization=" + Entities.getPrimaryKey(organizationSelector.getSelectedItem()) + "', orderBy: 'startDate desc'}")
        , organizationSelector.selectedItemProperty());
        //
        eventButton.visibleProperty().bind(FXProperties.compute(FXGanttVisibility.ganttVisibilityProperty(), value -> value == GanttVisibility.EVENTS));
        eventButton.managedProperty().bind(eventButton.visibleProperty());
        // Returning the button of that organization selector
        return new HBox(5, organizationButton, eventButton);
    }
}
