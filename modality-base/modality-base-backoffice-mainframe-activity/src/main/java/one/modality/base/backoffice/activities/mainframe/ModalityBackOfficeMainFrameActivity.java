package one.modality.base.backoffice.activities.mainframe;

import dev.webfx.extras.canvas.pane.CanvasPane;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.panes.LayoutPane;
import dev.webfx.extras.panes.MonoClipPane;
import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.com.bus.Bus;
import dev.webfx.stack.com.bus.BusService;
import dev.webfx.stack.com.bus.call.PendingBusCall;
import dev.webfx.stack.com.bus.spi.impl.client.NetworkBus;
import dev.webfx.stack.session.state.client.fx.FXConnected;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;
import one.modality.base.backoffice.ganttcanvas.MainFrameGanttCanvas;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.backoffice.mainframe.headernode.MainFrameHeaderNodeProvider;
import one.modality.base.client.application.ModalityClientMainFrameActivity;
import one.modality.base.client.brand.Brand;
import one.modality.base.client.gantt.fx.interstice.FXGanttInterstice;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.profile.fx.FXProfile;
import one.modality.base.client.tile.Tab;

import java.util.Comparator;
import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
public final class ModalityBackOfficeMainFrameActivity extends ModalityClientMainFrameActivity {

    private Pane mainFrame;
    private Region mainFrameHeader;
    private Region mainFrameFooter;
    private final Region ganttCanvasContainer = MainFrameGanttCanvas.getCanvasContainer();
    private Pane dialogArea;
    private Node profilePanel;
    private Insets breathingPadding; // actual value will be computed depending on compact mode
    private boolean wasGanttCanvasShowingBeforeTabsChange;

    @Override
    public Node buildUi() {
        mainFrame = new LayoutPane() { // Children are set later in updateMountNode()
            @Override
            protected void layoutChildren(double width, double height) {
                double headerHeight = mainFrameHeader.prefHeight(width);
                double footerHeight = mainFrameFooter.prefHeight(width);
                layoutInArea(mainFrameHeader, 0, 0, width, headerHeight, Pos.TOP_CENTER);
                layoutInArea(mainFrameFooter, 0, height - footerHeight, width, footerHeight, Pos.BOTTOM_CENTER);
                double nodeY = FXLayoutMode.isCompactMode() ? 0 : headerHeight; // Note: breathingPadding is passed as margin in layoutInArea() calls
                double nodeHeight = 0;
                if (dialogArea != null) {
                    layoutInArea(dialogArea, 0, nodeY, width, height - nodeY - footerHeight, 0, breathingPadding, HPos.CENTER, VPos.TOP);
                }
                if (profilePanel != null) {
                    layoutInArea(profilePanel, width - profilePanel.prefWidth(-1) - 10, headerHeight + 10, profilePanel.prefWidth(-1), profilePanel.prefHeight(-1), Pos.TOP_RIGHT);
                    onProfileLayout(); // for profile panel animation management
                }
                if (ganttCanvasContainer.isVisible()) {
                    nodeHeight = ganttCanvasContainer.prefHeight(width) + (FXGanttInterstice.isGanttIntersticeVisible() ? breathingPadding.getBottom() : 0);
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
        FXProperties.runNowAndOnPropertiesChange(this::updateMountNode,
                mountNodeProperty(), FXProfile.profilePanelProperty(), FXProfile.showProfilePanelProperty());

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
        Node newProfilePanel = FXProfile.isProfilePanelShown() ? FXProfile.getProfilePanel() : null;
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
                getMountNode(),
                ganttCanvasContainer, // Also after the mount node because of the circle & chevron collapse decoration
                mainFrameHeader,
                mainFrameFooter,
                profilePanel));
        updateDialogArea();
    }

    private void onProfileLayout() {
        if (startProfilePanelEnteringAnimationOnLayout) {
            if (profilePanelAnimation != null)
                profilePanelAnimation.stop();
            profilePanel.setTranslateX(profilePanel.prefWidth(-1) + 10);
            profilePanelAnimation = Animations.animateProperty(profilePanel.translateXProperty(), 0);
            profilePanelAnimation.setOnFinished(e -> {
                profilePanelAnimation = null;
            });
            startProfilePanelEnteringAnimationOnLayout = false;
        }
    }

    private void updateDialogArea() {
        if (dialogArea != null)
            mainFrame.getChildren().remove(dialogArea);
        Node relatedDialogNode = null;
        for (Tab headerTab : FXMainFrameHeaderTabs.getHeaderTabsObservableList()) {
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
                dialogArea.getStyleClass().add("modality-dialog-area");
                ObservableLists.runOnListChange(this::showHideDialogArea, dialogArea.getChildren());
            } else
                showHideDialogArea();
        }
        FXMainFrameDialogArea.setDialogArea(dialogArea);
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
        return Brand.createModalityBackOfficeBrandNode();
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
        clipPane.setMinHeight(Region.USE_PREF_SIZE);
        clipPane.setMaxHeight(Region.USE_PREF_SIZE);
        clipPane.setAlignment(Pos.TOP_CENTER);
        // The clipPane is initially contracted. Will be expanded (eventually through animation) once not empty.
        clipPane.setPrefHeight(0);
        // The tabs are communicated from the activities to this main frame through FXMainFrameHeaderTabsBar
        ObservableList<Tab> tabs = FXMainFrameHeaderTabs.getHeaderTabsObservableList();
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
                    // At the end of the animation, we clear the bar if the new screen has no bars
                    Animations.setOrCallOnTimelineFinished(timeline, e -> headerTabsBar.getChildren().setAll(tabs));
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

    private static final Color CONNECTED_COLOR = Color.web("#21BF73");
    private static final Color DISCONNECTED_COLOR = Color.web("#FF1E00");
    private static final Color TRAFFIC_COLOR = Color.web("#F7EA00");
    private static final Color NO_TRAFFIC_COLOR = Color.gray(0.75);
    private static final long TRAFFIC_MILLIS = 100;


    @Override
    protected Region createMainFrameFooter() {
        Text connectionText = createStatusText(ModalityBackOfficeMainFrameI18nKeys.Connection);
        Shape connectionLed = new Circle(8);
        FXProperties.runNowAndOnPropertyChange(connected ->
            connectionLed.setFill(connected ? CONNECTED_COLOR : DISCONNECTED_COLOR), FXConnected.connectedProperty()
        );
        HBox statusBar = new HBox(10, connectionText, connectionLed);
        Bus bus = BusService.bus();
        if (bus instanceof NetworkBus) { // Actually always true
            NetworkBus networkBus = (NetworkBus) bus;
            Text trafficText = createStatusText(ModalityBackOfficeMainFrameI18nKeys.Traffic);
            // Outgoing traffic (from client to server)
            Shape outgoingTrafficLed = new Circle(8, NO_TRAFFIC_COLOR);
            Scheduled[] noOutgoingTrafficScheduled = { UiScheduler.scheduleDeferred(() -> {}) };
            networkBus.setOutgoingTrafficListener(() -> {
                outgoingTrafficLed.setFill(TRAFFIC_COLOR);
                noOutgoingTrafficScheduled[0].cancel();
                noOutgoingTrafficScheduled[0] = UiScheduler.scheduleDelay(TRAFFIC_MILLIS, () -> outgoingTrafficLed.setFill(NO_TRAFFIC_COLOR));
            });
            // Outgoing traffic (from server to client)
            Shape incomingTrafficLed = new Circle(8, NO_TRAFFIC_COLOR);
            Scheduled[] noIncomingTrafficScheduled = { UiScheduler.scheduleDeferred(() -> {}) };
            networkBus.setIncomingTrafficListener(() -> {
                incomingTrafficLed.setFill(TRAFFIC_COLOR);
                noIncomingTrafficScheduled[0].cancel();
                noIncomingTrafficScheduled[0] = UiScheduler.scheduleDelay(TRAFFIC_MILLIS, () -> incomingTrafficLed.setFill(NO_TRAFFIC_COLOR));
            });
            statusBar.getChildren().addAll(Layouts.createHSpace(10), trafficText, outgoingTrafficLed, incomingTrafficLed);
        }
        // Pending operations
        Text pendingText = createStatusText(ModalityBackOfficeMainFrameI18nKeys.PendingCalls);
        Text pendingCountText = createStatusText(null);
        Region pendingIndicator = Controls.createProgressIndicator(18);
        Timeline[] pendingFadeTimeline = { new Timeline() };
        PendingBusCall.addPendingCallsCountHandler(pendingCallsCount -> UiScheduler.runInUiThread(() -> {
            pendingCountText.setText("" + pendingCallsCount);
            if (pendingCallsCount > 0) {
                pendingFadeTimeline[0].stop();
                pendingIndicator.setOpacity(1);
            } else {
                pendingFadeTimeline[0] = Animations.animateProperty(pendingIndicator.opacityProperty(), 0, Duration.seconds(2));
            }
        }));
        StackPane pendingPane = new StackPane(pendingIndicator, pendingCountText);
        statusBar.getChildren().addAll(Layouts.createHSpace(10), pendingText, pendingPane);

        statusBar.setAlignment(Pos.CENTER);
        // Considering the bottom of the safe area, in particular for OS like iPadOS with a bar at the bottom
        FXProperties.runNowAndOnPropertyChange(sai ->
            statusBar.setPadding(new Insets(Math.max(5, sai.getTop()), Math.max(5, sai.getRight()), Math.max(5, sai.getBottom()), Math.max(5, sai.getLeft()))), WebFxKitLauncher.safeAreaInsetsProperty()
        );
        LuminanceTheme.createApplicationFrameFacet(statusBar).style();
        return statusBar;
    }

    private static Text createStatusText(Object i18nKey) {
        Text statusText = new Text();
        if (i18nKey != null)
            I18n.bindI18nProperties(statusText, i18nKey);
        TextTheme.createDefaultTextFacet(statusText).style();
        return statusText;
    }
}
