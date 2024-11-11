package one.modality.base.frontoffice.activities.mainframe;

import dev.webfx.extras.panes.*;
import dev.webfx.extras.panes.transitions.CircleTransition;
import dev.webfx.extras.panes.transitions.FadeTransition;
import dev.webfx.extras.player.Players;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.os.OperatingSystem;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter;
import dev.webfx.stack.routing.uirouter.UiRouter;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.action.ActionGroup;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.application.ModalityClientMainFrameActivity;
import one.modality.base.client.application.RoutingActions;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.mainframe.fx.FXMainFrameOverlayArea;
import one.modality.base.client.mainframe.fx.FXMainFrameTransiting;
import one.modality.base.frontoffice.mainframe.fx.FXBackgroundNode;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseFooter;
import one.modality.base.frontoffice.utility.tyler.StyleUtility;

public final class ModalityFrontOfficeMainFrameActivity extends ModalityClientMainFrameActivity {

    private static final double MAX_PAGE_WIDTH = 1200; // Similar value to website
    private static final double WEB_MENU_HEIGHT = 96;

    private final static String[] sortedPossibleRoutingOperations =
        SourcesConfig.getSourcesRootConfig().childConfigAt("modality.base.frontoffice.application")
            .getString("buttonRoutingOperations").split(",");

    private final BooleanProperty mobileLayoutProperty =
        FXProperties.newBooleanProperty(OperatingSystem.isMobile(), this::onMobileLayoutChange);

    private Pane mainFrameContainer;
    private Node backgroundNode; // can be used to hold a WebView, and prevent iFrame reload in the web version
    private final TransitionPane mountTransitionPane = new TransitionPane();
    private CollapsePane overlayWebMenuBar;
    private CollapsePane mobileMenuBar;
    private CollapsePane insideButtonBar;
    private ScalePane[] scaledMobileButtons;
    private Pane dialogArea;
    private int firstOverlayChildIndex;

    @Override
    public Node buildUi() {
        // Starting with a circle transition animation for the first activity displayed
        mountTransitionPane.setAnimateFirstContent(true);
        mountTransitionPane.setTransition(new CircleTransition());
        // And then a fade transition for subsequent activities
        FXProperties.runOrUnregisterOnPropertyChange((thisListener, oldValue, transiting) -> {
            if (!transiting) { // Ending a transition
                mountTransitionPane.setTransition(new FadeTransition());
                thisListener.unregister();
            }
        }, mountTransitionPane.transitingProperty());
        //mountTransitionPane.setKeepsLeavingNodes(true); // Note: activities with video players should call TransitionPane.setKeepsLeavingNode(node, false)
        FXMainFrameTransiting.transitingProperty().bind(mountTransitionPane.transitingProperty());
        mainFrameContainer = new LayoutPane() { // Children are set later in updateMountNode()
            @Override
            protected void layoutChildren(double width, double height) {
                double headerHeight = 0, footerHeight = 0;
                boolean isMobileLayout = mobileLayoutProperty.get();
                if (isMobileLayout) {
                    footerHeight = mobileMenuBar.prefHeight(width);
                    layoutInArea(mobileMenuBar, 0, height - footerHeight, width, footerHeight);
                } else if (insideButtonBar != null) {
                    Point2D p = insideButtonBar.localToScene(0, 0);
                    layoutInArea(overlayWebMenuBar, p.getX(), 0, insideButtonBar.getWidth(), insideButtonBar.getHeight());
                    headerHeight = 0;
                }
                double mountNodeY = headerHeight;
                double mountNodeHeight = height - headerHeight - footerHeight;
                mountTransitionPane.setMinHeight(mountNodeHeight);
                layoutInArea(mountTransitionPane, 0, mountNodeY, width, mountNodeHeight);
                if (backgroundNode != null) { // Same position & size as the mount node (if present)
                    layoutInArea(backgroundNode, 0, mountNodeY, width, mountNodeHeight);
                }
                if (dialogArea != null) { // Same position & size as the mount node (if present)
                    layoutInArea(dialogArea, 0, mountNodeY, width, mountNodeHeight);
                }
            }
        };
        mobileMenuBar = createRouteButtonBar(true);
        overlayWebMenuBar = createRouteButtonBar(false);
        overlayWebMenuBar.setVisible(false);
        double[] lastMouseY = { 0 };
        mainFrameContainer.setOnMouseMoved(e -> {
            double mouseY = e.getY();
            if (Math.abs(mouseY - lastMouseY[0]) > 5 && !FXCollapseFooter.isCollapseFooter()) {
                boolean up = mouseY < lastMouseY[0];
                if (up && mouseY < mainFrameContainer.getHeight() / 3)
                    overlayWebMenuBar.setCollapsed(false);
                else if (!up && mouseY > WEB_MENU_HEIGHT)
                    overlayWebMenuBar.setCollapsed(true);
            }
            lastMouseY[0] = mouseY;
        });

        // To be aware: if backgroundNode is set to a WebView (which is actually its main purpose), then modifying the
        // mainFrame children again will cause the iFrame to reload in the web version, which is what we want to prevent
        // here: when the user is navigating back to the WebView, we want him to retrieve the WebView in the exact same
        // state as when he left it. So we try to not modify these children anymore once the backgroundNode is set.
        // That's why we encapsulated the mount node inside a container that won't change in that list.
        FXProperties.runNowAndOnPropertiesChange(() -> {
            backgroundNode = FXBackgroundNode.getBackgroundNode();
            boolean isMobileLayout = mobileLayoutProperty.get();
            mainFrameContainer.getChildren().setAll(Collections.listOfRemoveNulls(
                backgroundNode,      // may be a WebView
                mountTransitionPane, // contains a standard mount node, or null if we want to display the backgroundNode
                isMobileLayout ? mobileMenuBar : overlayWebMenuBar));   // the footer (front-office navigation buttons bar)
            firstOverlayChildIndex = mainFrameContainer.getChildren().size();
            updateOverlayChildren();
            if (getMountNode() == null)
                overlayWebMenuBar.setCollapsed(true);
        }, FXBackgroundNode.backgroundNodeProperty(), mobileLayoutProperty, mountNodeProperty());

        // Reacting to the mount node changes:
        FXProperties.runNowAndOnPropertyChange(mountNode -> {
            // Updating the mount node container with the new mount node
            if (mountNode instanceof Region) {
                ((Region) mountNode).minHeightProperty().bind(mountTransitionPane.heightProperty());
            }
            UiRouter uiRouter = getUiRouter();
            mountTransitionPane.setReverse(uiRouter.getHistory().isGoingBackward());
            ScrollPane scrollPane = mountNode == null ? null : (ScrollPane) mountNode.getProperties().get("embedding-scrollpane");
            if (scrollPane == null && mountNode != null) {
                CollapsePane topButtonBar = createRouteButtonBar(false);
                VBox vBox = new VBox(topButtonBar, mountNode);
                vBox.setMaxWidth(MAX_PAGE_WIDTH);
                BorderPane borderPane = new BorderPane(vBox);
                ScrollPane finalScrollPane = scrollPane = ControlUtil.createVerticalScrollPane(borderPane);
                mountNode.getProperties().put("embedding-scrollpane", scrollPane);
                double[] lastScrollPaneContentHeight = { 0 };
                FXProperties.runOnPropertyChange((o, oldValue, newValue) -> {
                    insideButtonBar = topButtonBar;
                    if (mountTransitionPane.isTransiting())
                        return;
                    // Visibility management:
                    if (newValue.doubleValue() == 0)// Making the overlay web menu bar invisible when reaching the top
                        overlayWebMenuBar.setVisible(false); // because there is already a web menu on top of that page
                    else if (ControlUtil.computeScrollPaneVTopOffset(finalScrollPane) > finalScrollPane.getHeight())
                        overlayWebMenuBar.setVisible(true); // Making it visible when the top one is no more in the view
                    // port (however it will not be showing while it's collapsed)
                    // Collapse management:
                    // Collapsing the overlay web menu if an activity explicitly asked to do so
                    if (FXCollapseFooter.isCollapseFooter())
                        overlayWebMenuBar.setCollapsed(true);
                    // or if the content change its height (ex: lazy loading) because the change of the scroll position
                    // (ex: new content added => scroll position goes up) is not coming from the user in that case.
                    else if (lastScrollPaneContentHeight[0] != borderPane.getHeight())
                        overlayWebMenuBar.setCollapsed(true);
                    // otherwise (we assume the scroll position comes from the user) if the user is scrolling up
                    else
                        overlayWebMenuBar.setCollapsed(newValue.doubleValue() > oldValue.doubleValue());
                    lastScrollPaneContentHeight[0] = borderPane.getHeight();
                }, scrollPane.vvalueProperty());
            }
            mountTransitionPane.transitToContent(scrollPane);
            // When the mount node is null, this is to indicate that we want to display the background node instead
            boolean displayBackgroundNode = mountNode == null;
            // We make the background node visible only when we want to display it
            if (backgroundNode != null)
                backgroundNode.setVisible(displayBackgroundNode);
            // Also when we display the background node, we need make the mount node container transparent to the mouse
            // (as the background node is behind) to allow the user to interact with it (ex: WebView).
            mountTransitionPane.setMouseTransparent(displayBackgroundNode);
            updateDialogArea();
        }, mountNodeProperty());

        FXMainFrameOverlayArea.setOverlayArea(mainFrameContainer);
        ObservableLists.runOnListChange(this::updateOverlayChildren, FXMainFrameOverlayArea.getOverlayChildren());

        // Requesting a layout for containerPane on layout mode changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            double footerHeight = Math.max(0.08 * (Math.min(mainFrameContainer.getHeight(), mainFrameContainer.getWidth())), 40);
            Arrays.forEach(scaledMobileButtons, scaledButton -> scaledButton.setPrefHeight(footerHeight));
        }, mainFrameContainer.widthProperty(), mainFrameContainer.heightProperty());

        setUpContextMenu(mainFrameContainer, this::contextMenuActionGroup);

        setupPlayersGlobalConfiguration();

        mainFrameContainer.setOnMouseClicked(e -> {
            if (e.isShiftDown())
                FXProperties.toggleProperty(mobileLayoutProperty);
        });

        mainFrameContainer.setBackground(Background.fill(Color.WHITE));
        return mainFrameContainer;
    }

    private static void setupPlayersGlobalConfiguration() {
        // Fullscreen button
        ModalityFullscreenButton.setupModalityFullscreenButton();
        // Players color (actually only Wistia supports it)
        Players.setGlobalPlayerColor(StyleUtility.MAIN_BRAND_COLOR);
    }

    private void updateDialogArea() {
        if (dialogArea != null)
            mainFrameContainer.getChildren().remove(dialogArea);
        dialogArea = null;
        Node relatedDialogNode = getMountNode();
        if (relatedDialogNode != null) {
            var properties = relatedDialogNode.getProperties();
            String arbitraryKey = "modality-dialogArea";
            dialogArea = (Pane) properties.get(arbitraryKey);
            if (dialogArea == null) {
                properties.put(arbitraryKey, dialogArea = new Pane());
                // We request focus on mouse clicked. This is to allow dropdown dialog in ButtonSelector to automatically
                // close when the user clicks outside (this auto-close mechanism is triggered by fucus change).
                dialogArea.setOnMouseClicked(e -> dialogArea.requestFocus());
                // We automatically show or hide the dialog area, depending on the presence or not of children:
                dialogArea.getChildren().addListener((InvalidationListener) observable -> showHideDialogArea());
            } else
                showHideDialogArea();
        }
        FXMainFrameDialogArea.setDialogArea(dialogArea);
    }

    private void showHideDialogArea() {
        ObservableList<Node> mainFrameChildren = mainFrameContainer.getChildren();
        if (dialogArea.getChildren().isEmpty())
            mainFrameChildren.remove(dialogArea);
        else if (!mainFrameChildren.contains(dialogArea)) {
            mainFrameChildren.add(firstOverlayChildIndex - 1, dialogArea);
        }
    }

    private void onMobileLayoutChange(boolean mobileLayout) {
        Console.log("mobileLayout = " + mobileLayout);
    }

    private void updateOverlayChildren() {
        ObservableList<Node> mainFrameChildren = mainFrameContainer.getChildren();
        ObservableList<Node> overlayChildren = FXMainFrameOverlayArea.getOverlayChildren();
        while (firstOverlayChildIndex < mainFrameChildren.size())
            mainFrameChildren.remove(firstOverlayChildIndex);
        mainFrameChildren.addAll(firstOverlayChildIndex, overlayChildren);
    }

    private ActionGroup contextMenuActionGroup() {
        return newActionGroup(
            ChangeLanguageRequestEmitter.getProvidedEmitters().stream()
                .map(emitter -> newOperationAction(emitter::emitLanguageRequest))
                .toArray(Action[]::new)
        );
    }

    private CollapsePane createRouteButtonBar(boolean mobileLayout) {
        Button[] buttons = RoutingActions.filterRoutingActions(this, this, sortedPossibleRoutingOperations)
            .stream().map(action -> createRouteButton(action, mobileLayout))
            .toArray(Button[]::new);
        Node buttonBar;
        if (mobileLayout) {
            scaledMobileButtons = Arrays.map(buttons, ModalityFrontOfficeMainFrameActivity::scaleButton, ScalePane[]::new);
            buttonBar = new ColumnsPane(scaledMobileButtons);
        } else {
            HBox hBox = new HBox(10, buttons);
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setMaxWidth(MAX_PAGE_WIDTH);
            hBox.setPrefHeight(WEB_MENU_HEIGHT);
            hBox.setBackground(Background.fill(Color.WHITE));
            buttonBar = hBox;
        }
        buttonBar.getStyleClass().setAll("button-bar"); // Style class used in Modality.css to make buttons square (remove round corners)
        CollapsePane collapsePane = new CollapsePane(buttonBar);
        if (mobileLayout) {
            collapsePane.setEffect(new DropShadow());
            collapsePane.setClipEnabled(false);
            collapsePane.collapsedProperty().bind(FXCollapseFooter.collapseFooterProperty());
            // Considering the bottom of the safe area, in particular for OS like iPadOS with a bar at the bottom
            FXProperties.runNowAndOnPropertyChange(sai -> {
                double safeAreaBottom = sai.getBottom();
                // we already have a 5px padding for the buttons
                collapsePane.setPadding(new Insets(0, 0, Math.max(0, safeAreaBottom - 5), 0));
            }, WebFxKitLauncher.safeAreaInsetsProperty());
        }
        return collapsePane;
    }

    private Button createRouteButton(Action routeAction, boolean mobileLayout) {
        Button button = new Button();
        ActionBinder.bindButtonToAction(button, routeAction);
        // Route buttons should never be disabled, because even if the route is not authorized, users should be able to
        // press the route button, they will either get the "Unauthorized message" from the UI router if they are logged
        // in, or - most importantly - the login window if they are not logged in (ex: Account button).
        button.disableProperty().unbind();
        button.setDisable(false);
        button.setCursor(Cursor.HAND);
        button.setContentDisplay(ContentDisplay.TOP);
        button.setGraphicTextGap(0);
        // Temporarily hardcoded style. TODO: move to CSS
        if (!mobileLayout)
            button.setTextFill(Color.BLACK);
        button.setBackground(Background.fill(Color.WHITE));
        FXProperties.runNowAndOnPropertyChange(graphic -> {
            if (graphic instanceof SVGPath) {
                SVGPath svgPath = (SVGPath) graphic;
                boolean hasStroke = svgPath.getStroke() != null;
                ObjectProperty<Paint> svgColorProperty = hasStroke ? svgPath.strokeProperty() : svgPath.fillProperty();
                if (mobileLayout) {
                    button.textFillProperty().bind(svgColorProperty);
                } else
                    svgColorProperty.bind(button.textFillProperty());
            }
        }, button.graphicProperty());
        int fontSize = mobileLayout ? 6 : 13;
        button.setFont(Font.font("Montserrat", FontWeight.BOLD, fontSize));
        button.setStyle("-fx-font-family: Montserrat; -fx-font-weight: bold; -fx-font-size: " + (fontSize) + "px; -fx-background-color: white; -fx-background-radius: 0");
        button.setPadding(new Insets(5));
        return button;
    }

    private static ScalePane scaleButton(Button button) {
        ScalePane scalePane = new ScalePane(ScaleMode.FIT_HEIGHT, button);
        scalePane.setStretchWidth(true);
        scalePane.setStretchHeight(true);
        scalePane.managedProperty().bind(button.managedProperty()); // Should it be in MonoPane?
        return scalePane;
    }
}
