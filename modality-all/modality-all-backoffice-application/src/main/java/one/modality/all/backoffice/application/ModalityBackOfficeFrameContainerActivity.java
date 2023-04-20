package one.modality.all.backoffice.application;

import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import one.modality.base.client.application.ModalityClientFrameContainerActivity;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.backoffice.event.fx.FXEvent;
import one.modality.event.backoffice.events.ganttcanvas.EventsGanttCanvas;
import one.modality.event.backoffice.events.pm.EventsPresentationModel;

/**
 * @author Bruno Salmon
 */
public class ModalityBackOfficeFrameContainerActivity extends ModalityClientFrameContainerActivity {

    protected Pane frameContainer;
    private Region containerHeader;
    private Region containerFooter;
    private final EventsGanttCanvas eventsGanttCanvas = new EventsGanttCanvas(new EventsPresentationModel());
    private final Pane canvasPane = eventsGanttCanvas.getCanvasPane();
    private Insets breathingPadding; // actual value will be computed depending on compact mode

    @Override
    public Node buildUi() {
        frameContainer = new Pane() {
            @Override
            protected void layoutChildren() {
                double width = getWidth(), height = getHeight();
                double headerHeight = containerHeader.prefHeight(width);
                double footerHeight = containerFooter.prefHeight(width);
                layoutInArea(containerHeader, 0, 0, width, headerHeight, 0, HPos.CENTER, VPos.TOP);
                layoutInArea(containerFooter, 0, height - footerHeight, width, footerHeight, 0, HPos.CENTER, VPos.BOTTOM);
                double nodeY = FXLayoutMode.isCompactMode() ? 0 : headerHeight;
                double nodeHeight = 0;
                if (canvasPane.isVisible()) {
                    nodeHeight = canvasPane.prefHeight(width) + breathingPadding.getTop() + breathingPadding.getBottom();
                    layoutInArea(canvasPane, 0, nodeY, width, nodeHeight, 0, breathingPadding, HPos.CENTER, VPos.TOP);
                }
                Node mountNode = getMountNode();
                if (mountNode != null) {
                    nodeY += nodeHeight - breathingPadding.getTop();
                    layoutInArea(mountNode, 0, nodeY, width, height - nodeY - footerHeight, 0, breathingPadding, HPos.CENTER, VPos.TOP);
                }
            }
        };
        containerHeader = createContainerHeader();
        containerFooter = createContainerFooter();
        FXProperties.runNowAndOnPropertiesChange(this::updateMountNode, mountNodeProperty());
        // Requesting a layout for containerPane on layout mode changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean compactMode = FXLayoutMode.isCompactMode();
            double hBreathing = compactMode ? 0 : 0.03 * frameContainer.getWidth();
            double vBreathing = compactMode ? 0 : 0.03 * frameContainer.getHeight();
            breathingPadding = new Insets(vBreathing, hBreathing, vBreathing, hBreathing);
            frameContainer.requestLayout();
        }, FXLayoutMode.layoutModeProperty(), frameContainer.widthProperty(), frameContainer.heightProperty());
        // When not in compact mode, the nodes don't cover the whole surface of this container, because there are some
        // breathing areas (see breathingPadding) which appear as empty areas but with this container background, so we
        // need to give these areas the same color as the nodes background (seen as primary facets by the LuminanceTheme).
        LuminanceTheme.createPrimaryPanelFacet(frameContainer).style(); // => will have the same background as the nodes
        return frameContainer;
    }

    @Override
    protected void startLogic() {
        eventsGanttCanvas.bindFXEventToSelection();
        eventsGanttCanvas.bindFXOrganization();
        eventsGanttCanvas.bindFXGanttSelection();
        eventsGanttCanvas.startLogic(this);
    }

    private void updateMountNode() {
        // Note: the order of the children is important in compact mode, where the container header overlaps the mount
        // node (as a transparent button bar on top of it) -> so the container header must be after the mount node,
        // otherwise it will be hidden.
        frameContainer.getChildren().setAll(Collections.listOfRemoveNulls(canvasPane, getMountNode(), containerHeader, containerFooter));
    }

    @Override
    protected HBox createContainerHeaderCenterItem() {
        // Creating the organization selector
        EntityButtonSelector<Organization> organizationSelector = new EntityButtonSelector<>(
                "{class: 'Organization', alias: 'o', where: 'exists(select Event where organization=o)'}",
                this, frameContainer, getDataSourceModel()
        );
        // Doing a bidirectional binding with FXOrganization
        FXOrganization.organizationProperty().bindBidirectional(organizationSelector.selectedItemProperty());
        Button organizationButton = organizationSelector.getButton();
        // Creating the event selector
        EntityButtonSelector<Event> eventSelector = new EntityButtonSelector<>(
                "{class: 'Event'}",
                this, frameContainer, getDataSourceModel()
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

    @Override
    protected Region createContainerFooter() {
        Text text = new Text(" Status ");
        TextTheme.createDefaultTextFacet(text).style();
        HBox containerFooter = new HBox(text);
        containerFooter.setAlignment(Pos.CENTER);
        containerFooter.setPadding(new Insets(5));
        LuminanceTheme.createApplicationFrameFacet(containerFooter).style();
        return containerFooter;
    }
}
