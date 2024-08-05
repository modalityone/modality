package one.modality.base.frontoffice.activities.mainframe;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.action.ActionGroup;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.application.ModalityClientMainFrameActivity;
import one.modality.base.client.application.RoutingActions;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.mainframe.backgroundnode.fx.FXBackgroundNode;

public class ModalityFrontOfficeMainFrameActivity extends ModalityClientMainFrameActivity {

    private final static String[] sortedPossibleRoutingOperations =
            SourcesConfig.getSourcesRootConfig().childConfigAt("modality.base.frontoffice.application")
                    .getString("buttonRoutingOperations").split(",");

    protected Pane mainFrame;
    private Node backgroundNode; // can be used to hold a WebView, and prevent iFrame reload in the web version
    private final BorderPane mountNodeContainer = new BorderPane();
    private Region mainFrameFooter;
    private Pane dialogArea;

    @Override
    public Node buildUi() {
        mainFrame = new Pane() { // Children are set later in updateMountNode()
            @Override
            protected void layoutChildren() {
                double width = getWidth(), height = getHeight();
                double headerHeight = 0;
                double footerHeight = 0;
                if (mainFrameFooter != null) {
                    footerHeight = mainFrameFooter.prefHeight(width);
                    layoutInArea(mainFrameFooter, 0, height - footerHeight, width, footerHeight, 0, HPos.CENTER, VPos.BOTTOM);
                }
                double nodeY = headerHeight;
                layoutInArea(mountNodeContainer, 0, nodeY, width, height - nodeY - footerHeight, 0, null, HPos.CENTER, VPos.TOP);
                if (backgroundNode != null) { // Same position & size as the mount node (if present)
                    layoutInArea(backgroundNode, 0, nodeY, width, height - nodeY - footerHeight, 0, null, HPos.CENTER, VPos.TOP);
                }
                if (dialogArea != null) { // Same position & size as the mount node (if present)
                    layoutInArea(dialogArea, 0, nodeY, width, height - nodeY - footerHeight, 0, null, HPos.CENTER, VPos.TOP);
                }
            }
        };
        mainFrameFooter = createMainFrameFooter();

        // To be aware: if backgroundNode is set to a WebView (which is actually its main purpose), then modifying the
        // mainFrame children again will cause the iFrame to reload in the web version, which is what we want to prevent
        // here: when the user is navigating back to the WebView, we want him to retrieve the WebView in the exact same
        // state as when he left it. So we try to not modify these children anymore once the backgroundNode is set.
        // That's why we encapsulated the mount node inside a container that won't change in that list.
        FXProperties.runNowAndOnPropertiesChange(() -> {
            backgroundNode = FXBackgroundNode.getBackgroundNode();
            mainFrame.getChildren().setAll(Collections.listOfRemoveNulls(
                    backgroundNode,     // may be a WebView
                    mountNodeContainer, // contains a standard mount node, or null if we want to display the backgroundNode
                    mainFrameFooter));  // the footer (front-office navigation buttons bar)
        }, FXBackgroundNode.backgroundNodeProperty());

        // Reacting to the mount node changes:
        FXProperties.runNowAndOnPropertiesChange(() -> {
            // Updating the mount node container with the new mount node
            Node mountNode = getMountNode();
            mountNodeContainer.setCenter(mountNode);
            // When the mount node is null, this is to indicate that we want to display the background node instead
            boolean displayBackgroundNode = mountNode == null;
            // We make the background node visible only when we want to display it
            if (backgroundNode != null)
                backgroundNode.setVisible(displayBackgroundNode);
            // Also when we display the background node, we need make the mount node container transparent to the mouse
            // (as the background node is behind) to allow the user to interact with it (ex: WebView).
            mountNodeContainer.setMouseTransparent(displayBackgroundNode);
            updateDialogArea();
        }, mountNodeProperty());

        // Requesting a layout for containerPane on layout mode changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            double footerHeight = Math.max(0.08 * (Math.min(mainFrame.getHeight(), mainFrame.getWidth())), 40);
            if (mainFrameFooter != null)
                mainFrameFooter.getChildrenUnmodifiable().forEach(n -> ((ScalePane) n).setPrefHeight(footerHeight));
        }, mainFrame.widthProperty(), mainFrame.heightProperty());

        setUpContextMenu(mainFrame, this::contextMenuActionGroup);
        return mainFrame;
    }

    private void updateDialogArea() {
        if (dialogArea != null)
            mainFrame.getChildren().remove(dialogArea);
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
        ObservableList<Node> mainFrameChildren = mainFrame.getChildren();
        if (dialogArea.getChildren().isEmpty())
            mainFrameChildren.remove(dialogArea);
        else if (!mainFrameChildren.contains(dialogArea)) {
            mainFrameChildren.add(dialogArea);
        }
    }

    protected ActionGroup contextMenuActionGroup() {
        return newActionGroup(
                ChangeLanguageRequestEmitter.getProvidedEmitters().stream()
                        .map(emitter -> newOperationAction(emitter::emitLanguageRequest))
                        .toArray(Action[]::new)
        );
    }

    @Override
    protected Region createMainFrameHeader() {
        return null;
    }

    @Override
    protected Region createMainFrameFooter() {
        Button[] buttons = RoutingActions.filterRoutingActions(this, this, sortedPossibleRoutingOperations)
                .stream().map(this::createRouteButton)
                .toArray(Button[]::new);
        ColumnsPane buttonBar = new ColumnsPane(Arrays.map(buttons, ModalityFrontOfficeMainFrameActivity::scaleButton, Node[]::new));
        buttonBar.getStyleClass().setAll("button-bar"); // Style class used in Modality.css to make buttons square (remove round corners)
        buttonBar.setEffect(new DropShadow());
        return buttonBar;
    }

    private Button createRouteButton(Action routeAction) {
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
        button.setTextFill(Color.web("#838788")); // Same color as SVG
        button.setBackground(Background.fill(Color.WHITE));
        button.setFont(Font.font("Montserrat", FontWeight.BOLD, 6));
        button.setStyle("-fx-font-family: Montserrat; -fx-font-weight: bold; -fx-font-size: 6px; -fx-background-color: white; -fx-background-radius: 0");
        button.setPadding(new Insets(5));
        return button;
    }

    private static ScalePane scaleButton(Button button) {
        ScalePane scalePane = new ScalePane(button);
        scalePane.setStretchWidth(true);
        scalePane.setStretchHeight(true);
        /* Commented as this is not good for mobiles other than iPad
        // Adding some bottom padding due to the iPad bar overlay at the bottom
        if (OperatingSystem.isMobile()) // Should we implement OperatingSystem.isIPad()?
            scalePane.setPadding(new Insets(0, 0, 10, 0));
        */
        scalePane.managedProperty().bind(button.managedProperty()); // Should it be in MonoPane?
        return scalePane;
    }
}
