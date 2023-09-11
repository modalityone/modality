package one.modality.base.backoffice.activities.mainframe;

import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import one.modality.base.backoffice.ganttcanvas.MainFrameGanttCanvas;
import one.modality.base.client.application.ModalityClientMainFrameContainerActivity;
import one.modality.base.client.gantt.fx.interstice.FXGanttInterstice;

import java.util.Comparator;

/**
 * @author Bruno Salmon
 */
public class ModalityBackOfficeMainFrameContainerActivity extends ModalityClientMainFrameContainerActivity {

    protected Pane frameContainer;
    private Region containerHeader;
    private Region containerFooter;
    private final Region ganttCanvasContainer = MainFrameGanttCanvas.getCanvasContainer();
    private Insets breathingPadding; // actual value will be computed depending on compact mode

    @Override
    public Node buildUi() {
        frameContainer = new Pane() {
            @Override
            protected void layoutChildren() {
                // TODO determine value to use for canvasPane based on which activity we are in
                double width = getWidth(), height = getHeight();
                double headerHeight = containerHeader.prefHeight(width);
                double footerHeight = containerFooter.prefHeight(width);
                layoutInArea(containerHeader, 0, 0, width, headerHeight, 0, HPos.CENTER, VPos.TOP);
                layoutInArea(containerFooter, 0, height - footerHeight, width, footerHeight, 0, HPos.CENTER, VPos.BOTTOM);
                double nodeY = FXLayoutMode.isCompactMode() ? 0 : headerHeight;
                double nodeHeight = 0;
                if (ganttCanvasContainer.isVisible()) {
                    nodeHeight = ganttCanvasContainer.prefHeight(width) + breathingPadding.getTop() + (FXGanttInterstice.isGanttIntersticeRequired() ? breathingPadding.getBottom() : 0);
                    layoutInArea(ganttCanvasContainer, 0, nodeY, width, nodeHeight, 0, breathingPadding, HPos.CENTER, VPos.TOP);
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
        }, FXLayoutMode.layoutModeProperty(), FXGanttInterstice.ganttIntersticeRequiredProperty(), frameContainer.widthProperty(), frameContainer.heightProperty());
        // When not in compact mode, the nodes don't cover the whole surface of this container, because there are some
        // breathing areas (see breathingPadding) which appear as empty areas but with this container background, so we
        // need to give these areas the same color as the nodes background (seen as primary facets by the LuminanceTheme).
        LuminanceTheme.createPrimaryPanelFacet(frameContainer).style(); // => will have the same background as the nodes
        return frameContainer;
    }

    @Override
    protected void startLogic() {
        MainFrameGanttCanvas.setupFXBindingsAndStartLogic(this);
    }

    private void updateMountNode() {
        // Note: the order of the children is important in compact mode, where the container header overlaps the mount
        // node (as a transparent button bar on top of it) -> so the container header must be after the mount node,
        // otherwise it will be hidden.
        frameContainer.getChildren().setAll(Collections.listOfRemoveNulls(ganttCanvasContainer, getMountNode(), containerHeader, containerFooter));
    }

    @Override
    protected HBox createContainerHeaderCenterItem() {
        String[] expectedOrder = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.base.backoffice.application")
                .getString("headerNodesOrder").split(",");
        return new HBox(5, MainFrameHeaderNodeProvider.getProviders().stream()
                .sorted(Comparator.comparingInt(o -> Arrays.indexOf(expectedOrder, o.getName())))
                .map(p -> p.getHeaderNode(this, frameContainer, getDataSourceModel()))
                .toArray(Node[]::new));
    }

    @Override
    protected Region createContainerFooter() {
        Text text = new Text(" ");
        TextTheme.createDefaultTextFacet(text).style();
        HBox containerFooter = new HBox(text);
        containerFooter.setAlignment(Pos.CENTER);
        containerFooter.setPadding(new Insets(5));
        LuminanceTheme.createApplicationFrameFacet(containerFooter).style();
        return containerFooter;
    }
}
