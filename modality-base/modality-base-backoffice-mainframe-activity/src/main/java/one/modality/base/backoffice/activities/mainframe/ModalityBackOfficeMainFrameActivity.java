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
import one.modality.base.backoffice.activities.mainframe.headernode.MainFrameHeaderNodeProvider;
import one.modality.base.backoffice.ganttcanvas.MainFrameGanttCanvas;
import one.modality.base.client.application.ModalityClientMainFrameActivity;
import one.modality.base.client.gantt.fx.interstice.FXGanttInterstice;

import java.util.Comparator;

/**
 * @author Bruno Salmon
 */
public class ModalityBackOfficeMainFrameActivity extends ModalityClientMainFrameActivity {

    protected Pane mainFrame;
    private Region mainFrameHeader;
    private Region mainFrameFooter;
    private final Region ganttCanvasContainer = MainFrameGanttCanvas.getCanvasContainer();
    private Insets breathingPadding; // actual value will be computed depending on compact mode

    @Override
    public Node buildUi() {
        mainFrame = new Pane() {
            @Override
            protected void layoutChildren() {
                // TODO determine value to use for canvasPane based on which activity we are in
                double width = getWidth(), height = getHeight();
                double headerHeight = mainFrameHeader.prefHeight(width);
                double footerHeight = mainFrameFooter.prefHeight(width);
                layoutInArea(mainFrameHeader, 0, 0, width, headerHeight, 0, HPos.CENTER, VPos.TOP);
                layoutInArea(mainFrameFooter, 0, height - footerHeight, width, footerHeight, 0, HPos.CENTER, VPos.BOTTOM);
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
        mainFrameHeader = createMainFrameHeader();
        mainFrameFooter = createMainFrameFooter();
        FXProperties.runNowAndOnPropertiesChange(this::updateMountNode, mountNodeProperty());
        // Requesting a layout for containerPane on layout mode changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean compactMode = FXLayoutMode.isCompactMode();
            double hBreathing = compactMode ? 0 : 0.03 * mainFrame.getWidth();
            double vBreathing = compactMode ? 0 : 0.03 * mainFrame.getHeight();
            breathingPadding = new Insets(vBreathing, hBreathing, vBreathing, hBreathing);
            mainFrame.requestLayout();
        }, FXLayoutMode.layoutModeProperty(), FXGanttInterstice.ganttIntersticeRequiredProperty(), mainFrame.widthProperty(), mainFrame.heightProperty());
        // When not in compact mode, the nodes don't cover the whole surface of this container, because there are some
        // breathing areas (see breathingPadding) which appear as empty areas but with this container background, so we
        // need to give these areas the same color as the nodes background (seen as primary facets by the LuminanceTheme).
        LuminanceTheme.createPrimaryPanelFacet(mainFrame).style(); // => will have the same background as the nodes
        return mainFrame;
    }

    @Override
    protected void startLogic() {
        MainFrameGanttCanvas.setupFXBindingsAndStartLogic(this);
    }

    private void updateMountNode() {
        // Note: the order of the children is important in compact mode, where the container header overlaps the mount
        // node (as a transparent button bar on top of it) -> so the container header must be after the mount node,
        // otherwise it will be hidden.
        mainFrame.getChildren().setAll(Collections.listOfRemoveNulls(ganttCanvasContainer, getMountNode(), mainFrameHeader, mainFrameFooter));
    }

    @Override
    protected HBox createMainFrameHeaderCenterItem() {
        String[] expectedOrder = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.base.backoffice.mainframe.headernode")
                .getString("headerNodesOrder").split(",");
        return new HBox(5, MainFrameHeaderNodeProvider.getProviders().stream()
                .sorted(Comparator.comparingInt(o -> Arrays.indexOf(expectedOrder, o.getName())))
                .map(p -> p.getHeaderNode(this, mainFrame, getDataSourceModel()))
                .toArray(Node[]::new));
    }

    @Override
    protected Region createMainFrameFooter() {
        Text text = new Text(" ");
        TextTheme.createDefaultTextFacet(text).style();
        HBox containerFooter = new HBox(text);
        containerFooter.setAlignment(Pos.CENTER);
        containerFooter.setPadding(new Insets(5));
        LuminanceTheme.createApplicationFrameFacet(containerFooter).style();
        return containerFooter;
    }
}
