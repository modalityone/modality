package one.modality.base.backoffice.activities.mainframe;

import dev.webfx.extras.canvas.pane.CanvasPane;
import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.pane.MonoClipPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import one.modality.base.backoffice.activities.mainframe.fx.FXMainFrameHeaderTabsBar;
import one.modality.base.backoffice.activities.mainframe.headernode.MainFrameHeaderNodeProvider;
import one.modality.base.backoffice.ganttcanvas.MainFrameGanttCanvas;
import one.modality.base.client.application.ModalityClientMainFrameActivity;
import one.modality.base.client.gantt.fx.interstice.FXGanttInterstice;

import java.util.Comparator;
import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
public class ModalityBackOfficeMainFrameActivity extends ModalityClientMainFrameActivity {

    protected Pane mainFrame;
    private Region mainFrameHeader;
    private Region mainFrameFooter;
    private final Region ganttCanvasContainer = MainFrameGanttCanvas.getCanvasContainer();
    private Insets breathingPadding; // actual value will be computed depending on compact mode
    private boolean wasGanttCanvasShowingBeforeTabsChange;

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
                wasGanttCanvasShowingBeforeTabsChange = isGanttCanvasShowing();
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

    private boolean isGanttCanvasShowing() {
        // The ganttCanvas is not showing if invisible
        if (!ganttCanvasContainer.isVisible())
            return false;
        // The previous condition was enough if ganttCanvasContainer is not a CanvasPane
        if (!(ganttCanvasContainer instanceof CanvasPane))
            return true;
        // A CanvasPane can hide the gantt canvas through animation. What matters here is to know if the gantt canvas
        // is showing at the end of the animation. The height at the end of the animation is requestedCanvasHeight.
        return ((CanvasPane) ganttCanvasContainer).getRequestedCanvasHeight() > 0;
    }

    @Override
    protected void startLogic() {
        MainFrameGanttCanvas.setupFXBindingsAndStartLogic(this);
    }

    private void updateMountNode() {
        // Note: the order of the children is important in compact mode, where the container header overlaps the mount
        // node (as a transparent button bar on top of it) -> so the container header must be after the mount node,
        // otherwise it will be hidden.
        mainFrame.getChildren().setAll(Collections.listOfRemoveNulls(
                ganttCanvasContainer,
                getMountNode(),
                mainFrameHeader,
                mainFrameFooter));
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
    protected Region createHeaderTabsBar() {
        // We use a FlowPane to display the tabs bar, but we embed it in a clip pane for the animation. We animate the
        // tabs bar for a smooth transition between screens with no tabs (bar height = 0) and those with tabs (bar
        // height > 0), to avoid a brutal shift of the gantt canvas located just under the tabs bar.
        FlowPane headerTabsBar = new FlowPane();
        LuminanceTheme.createApplicationFrameFacet(headerTabsBar)
                .style();
        MonoClipPane clipPane = new MonoClipPane(headerTabsBar);
        // The clipPane is initially contracted. Will be expanded (eventually through animation) once not empty.
        clipPane.setPrefHeight(0);
        // The tabs are communicated from the activities to this main frame through FXMainFrameHeaderTabsBar
        ObservableList<Node> tabs = FXMainFrameHeaderTabsBar.getTabsBarButtonsObservableList();
        // We don't bind them directly to the flow pane children, because we want to animate them. So the following code
        // is the animation management:
        tabs.addListener((ListChangeListener<Node>) c -> { // We listen to the tabs changes
            // We capture the state (i.e. showing or not) of the gantt canvas in the exiting screen
            boolean capturedWasGanttCanvasShowingBeforeTabsChange = wasGanttCanvasShowingBeforeTabsChange;
            // To know the state of the gantt canvas in the new incoming screen, we need to postpone the call to
            // isGanttCanvasShowing() to give it the time to set up the CanvasPane animation it and set the requestedHeight.
            UiScheduler.scheduleInAnimationFrame(() -> {
                // Now we can get the state of the gantt canvas in the new incoming screen.
                boolean isGanttCanvasShowing = isGanttCanvasShowing();
                // If the new screen has no tabs and doesn't show the gantt canvas (ex: home screen), we don't animate
                Consumer<Double> animationConsumer = newHeight -> {
                    // We animate the tabs bar if the gantt canvas is showing in both screens (one screen may have tabs
                    // but not the other).
                    boolean animate = isGanttCanvasShowing == capturedWasGanttCanvasShowingBeforeTabsChange;
                    // We animate prefHeightProperty to show the tabs (when new height > 0) or hide them (when new height = 0)
                    Timeline timeline = Animations.animateProperty(clipPane.prefHeightProperty(), newHeight, animate);
                    if (timeline == null)
                        headerTabsBar.getChildren().setAll(tabs);
                    else
                        // At the end of the animation, we clear the bar if the new screen has no bars
                        timeline.setOnFinished(e -> headerTabsBar.getChildren().setAll(tabs));
                };
                // For other cases, we will animate the height transition and need for that to give the new height
                if (tabs.isEmpty()) { // Case where we transition from a screen with tabs to a screen with no tabs (but with gantt canvas)
                    // We will animate the height up to 0, but we don't remove the tabs yes, they are still showing
                    // until the animation ends (we will remove them at this point)
                    animationConsumer.accept(0d);
                } else { // Case where we transition to a screen with tabs from a screen with no tabs
                    // We apply the new tabs right now as they are showing during the animation
                    headerTabsBar.getChildren().setAll(tabs);
                    // We compute the new height we will need to transit to (final height in animation)
                    double currentPrefHeight = clipPane.getPrefHeight();
                    clipPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
                    double newPrefHeight = clipPane.prefHeight(mainFrame.getWidth()); // We compute the new height
                    clipPane.setPrefHeight(currentPrefHeight);
                    // We animate the transition
                    animationConsumer.accept(newPrefHeight);
                }
            }, 1); // 1 animation frame is enough
        });
        return clipPane;
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
