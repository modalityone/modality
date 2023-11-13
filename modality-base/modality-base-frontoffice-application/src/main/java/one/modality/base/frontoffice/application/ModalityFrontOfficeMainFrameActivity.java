package one.modality.base.frontoffice.application;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.util.properties.FXProperties;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import one.modality.base.client.application.ModalityClientMainFrameActivity;
import one.modality.base.client.application.RoutingActions;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;

public class ModalityFrontOfficeMainFrameActivity extends ModalityClientMainFrameActivity {

    protected Pane mainFrame;
    //private Region mainFrameHeader;
    private Region mainFrameFooter;
    private Pane dialogArea;
    private Insets breathingPadding; // actual value will be computed depending on compact mode

    @Override
    public Node buildUi() {
        mainFrame = new Pane() { // Children are set later in updateMountNode()
            @Override
            protected void layoutChildren() {
                double width = getWidth(), height = getHeight();
                double headerHeight = 0; //mainFrameHeader.prefHeight(width);
                double footerHeight = mainFrameFooter.prefHeight(width);
                //layoutInArea(mainFrameHeader, 0, 0, width, headerHeight, 0, HPos.CENTER, VPos.TOP);
                layoutInArea(mainFrameFooter, 0, height - footerHeight, width, footerHeight, 0, HPos.CENTER, VPos.BOTTOM);
                double nodeY = /*FXLayoutMode.isCompactMode() ? 0 :*/ headerHeight; // Note: breathingPadding is passed as margin in layoutInArea() calls
                double nodeHeight = 0;
                if (dialogArea != null) {
                    layoutInArea(dialogArea, 0, nodeY, width, height - nodeY - footerHeight, 0, breathingPadding, HPos.CENTER, VPos.TOP);
                }
                Node mountNode = getMountNode();
                if (mountNode != null) {
                    nodeY += nodeHeight;
                    layoutInArea(mountNode, 0, nodeY, width, height - nodeY - footerHeight, 0, breathingPadding, HPos.CENTER, VPos.CENTER);
                }
            }
        };
        //mainFrameHeader = createMainFrameHeader();
        mainFrameFooter = createMainFrameFooter();
        FXProperties.runNowAndOnPropertiesChange(this::updateMountNode,
                mountNodeProperty());

        // Requesting a layout for containerPane on layout mode changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            /*boolean compactMode = false; //FXLayoutMode.isCompactMode();
            double hBreathing = compactMode ? 0 : 0.03 * mainFrame.getWidth();
            double vBreathing = compactMode ? 0 : 0.03 * mainFrame.getHeight();
            breathingPadding = new Insets(vBreathing, hBreathing, vBreathing, hBreathing);
            mainFrame.requestLayout();*/
            double footerHeight = Math.max(0.08 * (Math.min(mainFrame.getHeight(), mainFrame.getWidth())), 40);
            mainFrameFooter.getChildrenUnmodifiable().forEach(n -> ((ScalePane) n).setPrefHeight(footerHeight));
        }, mainFrame.widthProperty(), mainFrame.heightProperty());


        setUpContextMenu(mainFrame, this::contextMenuActionGroup);
        return mainFrame;
    }

    private void updateMountNode() {
        // Note: the order of the children is important in compact mode, where the container header overlaps the mount
        // node (as a transparent button bar on top of it) -> so the container header must be after the mount node,
        // otherwise it will be hidden.
        mainFrame.getChildren().setAll(Collections.listOfRemoveNulls(
                getMountNode(),
                //mainFrameHeader,
                mainFrameFooter));
        updateDialogArea();
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
        Button[] buttons = RoutingActions.filterRoutingActions(this, this,
                        "RouteToHome", "RouteToBooking", "RouteToAlerts", "RouteToAccount")
                .stream().map(this::createRouteButton)
                .toArray(Button[]::new);
        ColumnsPane buttonBar = new ColumnsPane(Arrays.map(buttons, ModalityFrontOfficeMainFrameActivity::scaleButton, Node[]::new));
        buttonBar.getStyleClass().setAll("button-bar"); // Style class used in Modality.css to make buttons square (remove round corners)
        return buttonBar;
    }

    private Button createRouteButton(Action routeAction) {
        Button button = new Button();
        button.setContentDisplay(ContentDisplay.TOP);
        button.setTextFill(Color.web("#838788")); // Same color as SVG
        button.setCursor(Cursor.HAND);
        ActionBinder.bindButtonToAction(button, routeAction);
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

    private static void setButtonPrefWidth(Button[] buttons, double prefWidth) {
        Arrays.forEach(buttons, b -> b.setPrefWidth(prefWidth));
    }
}
