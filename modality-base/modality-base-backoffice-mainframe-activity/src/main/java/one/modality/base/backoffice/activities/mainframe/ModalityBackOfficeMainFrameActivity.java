package one.modality.base.backoffice.activities.mainframe;

import dev.webfx.extras.canvas.pane.CanvasPane;
import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.pane.MonoClipPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import one.modality.base.backoffice.activities.mainframe.fx.FXMainFrame;
import one.modality.base.backoffice.activities.mainframe.headernode.MainFrameHeaderNodeProvider;
import one.modality.base.backoffice.ganttcanvas.MainFrameGanttCanvas;
import one.modality.base.backoffice.tile.Tab;
import one.modality.base.client.application.ModalityClientMainFrameActivity;
import one.modality.base.client.gantt.fx.interstice.FXGanttInterstice;
import one.modality.base.client.profile.fx.FXProfile;

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
    private Pane dialogArea;
    private Node profilePanel;
    private Insets breathingPadding; // actual value will be computed depending on compact mode
    private boolean wasGanttCanvasShowingBeforeTabsChange;

    @Override
    public Node buildUi() {
        mainFrame = new Pane() { // Children are set later in updateMountNode()
            @Override
            protected void layoutChildren() {
                double width = getWidth(), height = getHeight();
                double headerHeight = mainFrameHeader.prefHeight(width);
                double footerHeight = mainFrameFooter.prefHeight(width);
                layoutInArea(mainFrameHeader, 0, 0, width, headerHeight, 0, HPos.CENTER, VPos.TOP);
                layoutInArea(mainFrameFooter, 0, height - footerHeight, width, footerHeight, 0, HPos.CENTER, VPos.BOTTOM);
                double nodeY = FXLayoutMode.isCompactMode() ? 0 : headerHeight; // Note: breathingPadding is passed as margin in layoutInArea() calls
                double nodeHeight = 0;
                if (dialogArea != null) {
                    layoutInArea(dialogArea, 0, nodeY, width, height - nodeY - footerHeight, 0, breathingPadding, HPos.CENTER, VPos.TOP);
                }
                if (profilePanel != null) {
                    layoutInArea(profilePanel, width - profilePanel.prefWidth(-1) - 10, nodeY + 10, profilePanel.prefWidth(-1), profilePanel.prefHeight(-1), 0, HPos.RIGHT, VPos.TOP);
                    if (startProfilePanelEnteringAnimationOnLayout) {
                        if (profilePanelAnimation != null)
                            profilePanelAnimation.stop();
                        profilePanel.setTranslateX(profilePanel.prefWidth(-1) + 10);
                        profilePanelAnimation = Animations.animateProperty(profilePanel.translateXProperty(), 0);
                        profilePanelAnimation.setOnFinished(e -> {
                            profilePanelAnimation = null;
                        });
                    }
                    startProfilePanelEnteringAnimationOnLayout = false;
                }
                if (ganttCanvasContainer.isVisible()) {
                    nodeHeight = ganttCanvasContainer.prefHeight(width) + (FXGanttInterstice.isGanttIntersticeRequired() ? breathingPadding.getBottom() : 0);
                    layoutInArea(ganttCanvasContainer, 0, nodeY, width, nodeHeight, 0, breathingPadding, HPos.CENTER, VPos.TOP);
                }
                Node mountNode = getMountNode();
                if (mountNode != null) {
                    nodeY += nodeHeight;
                    layoutInArea(mountNode, 0, nodeY, width, height - nodeY - footerHeight, 0, breathingPadding, HPos.CENTER, VPos.CENTER);
                }
                wasGanttCanvasShowingBeforeTabsChange = isGanttCanvasShowing();
            }
        };
        mainFrameHeader = createMainFrameHeader();
        mainFrameFooter = createMainFrameFooter();
        FXProperties.runNowAndOnPropertiesChange(this::updateMountNode, mountNodeProperty(), FXProfile.profilePanelProperty());

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

    private Timeline profilePanelAnimation;
    private boolean startProfilePanelEnteringAnimationOnLayout;

    private void updateMountNode() {
        Node oldProfilePanel = profilePanel;
        Node newProfilePanel = FXProfile.getProfilePanel();
        // When the profile panel is set to null, we animate its exit
        if (newProfilePanel == null && mainFrame.getChildren().contains(oldProfilePanel)) {
            if (profilePanelAnimation != null)
                profilePanelAnimation.stop();
            profilePanelAnimation = Animations.animateProperty(oldProfilePanel.translateXProperty(), oldProfilePanel.prefWidth(-1) + 10);
            profilePanelAnimation.setOnFinished(e -> {
                mainFrame.getChildren().remove(oldProfilePanel);
                if (profilePanel == oldProfilePanel)
                    profilePanel = null;
                profilePanelAnimation = null;
            });
        } else {
            if ((oldProfilePanel == null || profilePanelAnimation != null) && newProfilePanel != null) {
                startProfilePanelEnteringAnimationOnLayout = true;
            }
            profilePanel = newProfilePanel;
        }
        // Note: the order of the children is important in compact mode, where the container header overlaps the mount
        // node (as a transparent button bar on top of it) -> so the container header must be after the mount node,
        // otherwise it will be hidden.
        mainFrame.getChildren().setAll(Collections.listOfRemoveNulls(
                ganttCanvasContainer,
                getMountNode(),
                mainFrameHeader,
                mainFrameFooter,
                profilePanel));
        updateDialogArea();
    }

    private void updateDialogArea() {
        if (dialogArea != null)
            mainFrame.getChildren().remove(dialogArea);
        Node relatedDialogNode = null;
        for (Tab headerTab : FXMainFrame.getHeaderTabsObservableList()) {
            var properties = headerTab.getProperties();
            String arbitraryKey = "modality-mainframe-listener-installed";
            if (properties.get(arbitraryKey) == null) {
                headerTab.selectedProperty().addListener(observable -> updateDialogArea());
                properties.put(arbitraryKey, true);
            }
            if (headerTab.isSelected()) {
                relatedDialogNode = headerTab;
                break;
            }
        }
        if (relatedDialogNode == null)
            relatedDialogNode = getMountNode();
        if (relatedDialogNode != null) {
            var properties = relatedDialogNode.getProperties();
            String arbitraryKey = "modality-dialogArea";
            dialogArea = (Pane) properties.get(arbitraryKey);
            if (dialogArea == null) {
                properties.put(arbitraryKey, dialogArea = new Pane());
                dialogArea.getChildren().addListener((InvalidationListener) observable -> showHideDialogArea());
            } else
                showHideDialogArea();
        }
        FXMainFrame.setDialogArea(dialogArea);
    }

    private void showHideDialogArea() {
        ObservableList<Node> mainFrameChildren = mainFrame.getChildren();
        if (dialogArea.getChildren().isEmpty())
            mainFrameChildren.remove(dialogArea);
        else if (!mainFrameChildren.contains(dialogArea)) {
            int profilePanelIndex = mainFrameChildren.indexOf(profilePanel);
            if (profilePanelIndex > 0)
                mainFrameChildren.add(profilePanelIndex, dialogArea);
            else
                mainFrameChildren.add(dialogArea);
        }
    }

    @Override
    protected Node createBrandNode() {
        ImageView logo = new ImageView(Resource.toUrl("modality-logo.png", getClass()));
        Text modality = new Text("modality");
        Text one = new Text("one");
        modality.setFont(Font.font("Montserrat", FontWeight.NORMAL, 18));
        one.setFont(Font.font("Montserrat", FontWeight.BOLD, 18));
        modality.setFill(Color.web("4D4D4D"));
        one.setFill(Color.web("1589BF"));
        HBox brand = new HBox(logo, modality, one);
        HBox.setMargin(logo, new Insets(0, 3, 0, 0)); // 3px gap between logo and text
        brand.setAlignment(Pos.CENTER);
        return brand;
    }

    @Override
    protected HBox createMainFrameHeaderCenterItem() {
        String[] expectedNodes = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.base.backoffice.mainframe.headernode")
                .getString("headerNodes").split(",");
        HBox hBox = new HBox(5, MainFrameHeaderNodeProvider.getProviders().stream()
                .filter(o -> Arrays.contains(expectedNodes, o.getName()))
                .sorted(Comparator.comparingInt(o -> Arrays.indexOf(expectedNodes, o.getName())))
                .map(p -> p.getHeaderNode(this, mainFrame, getDataSourceModel()))
                .toArray(Node[]::new));
        hBox.setAlignment(Pos.CENTER);
        return hBox;
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
        ObservableList<Tab> tabs = FXMainFrame.getHeaderTabsObservableList();
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
