package one.modality.base.client.application;

import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.FXLuminanceMode;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.palette.FXPaletteMode;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.action.ActionGroup;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;

/**
 * @author Bruno Salmon
 */
public class ModalityClientMainFrameActivity extends ViewDomainActivityBase
        implements ModalityButtonFactoryMixin
        , OperationActionFactoryMixin {

    @Override
    public Node buildUi() {
        BorderPane mainFrame = new BorderPane();
        mainFrame.centerProperty().bind(mountNodeProperty());
        mainFrame.setTop(createMainFrameHeader());
        mainFrame.setBottom(createMainFrameFooter());
        return mainFrame;
    }

    protected Region createMainFrameHeader() {
        // mainFrameHeader consists of 1) headerButtonsBar on top, and eventually 2) headerTabsBar at the bottom.

        // 1) Building headerButtonsBar containing a home button, navigation buttons, customisable center item & logout button
        // Home button
        Button homeButton = routeOperationButton("RouteToHome");
        // Back navigation button (will be hidden in browsers as they already have one)
        Button backButton = routeOperationButton("RouteBackward");
        // Forward navigation button (will be hidden in browsers as they already have one)
        Button forwardButton = routeOperationButton("RouteForward");
        Node brandNode = createBrandNode();
        Node headerCenterItem = createMainFrameHeaderCenterItem();
        Button logoutButton = ActionBinder.bindButtonToAction(newButton(), newOperationAction(LogoutRequest::new)); //operationButton("Logout");
        Pane headerButtonsBar = new Pane(Arrays.nonNulls(Node[]::new, homeButton, backButton, forwardButton, brandNode, headerCenterItem, logoutButton)) {
            @Override
            protected void layoutChildren() {
                double width = getWidth(), height = getHeight();
                double x = 5, y = 3, w, h = height - 6;
                layoutInArea(homeButton, x, y, w = homeButton.prefWidth(h), h, 0, HPos.LEFT, VPos.CENTER);
                if (backButton.isManaged())
                    layoutInArea(backButton, x += w + 8, y, w = backButton.prefWidth(h), h, 0, HPos.LEFT, VPos.CENTER);
                if (forwardButton.isManaged())
                    layoutInArea(forwardButton, x += w + 3, y, w = forwardButton.prefWidth(h), h, 0, HPos.LEFT, VPos.CENTER);
                if (brandNode != null)
                    layoutInArea(brandNode, x += w + 8, y, w = brandNode.prefWidth(h), h, 0, HPos.LEFT, VPos.CENTER);
                layoutInArea(headerCenterItem, x += w + 5, y - 2, w = Math.max(width - 2 * x, headerCenterItem.prefWidth(h)), height, 0, HPos.CENTER, VPos.CENTER);
                if (logoutButton.isManaged())
                    layoutInArea(logoutButton, x += w + 8, y, width - 5 - x, h, 0, HPos.RIGHT, VPos.CENTER);
            }

            @Override
            protected double computePrefHeight(double width) {
                return super.computePrefHeight(width) + 6;
            }
        };

        // 2) Building a customisable headerTabsBar (used only in the backoffice so far)
        Region headerTabsBar = createHeaderTabsBar();

        // Assembling 1) & 2) into the mainFrameHeader
        Region mainFrameHeader = headerTabsBar == null ? headerButtonsBar : new VBox(headerButtonsBar, headerTabsBar);

        setUpContextMenu(headerButtonsBar, this::contextMenuActionGroup);
        LuminanceTheme.createApplicationFrameFacet(headerButtonsBar)
                .setBordered(true)
                .setOnMouseClicked(e -> { // Temporary for testing
                    if (e.isAltDown())
                        FXPaletteMode.setVariedPalette(!FXPaletteMode.isVariedPalette());
                    if (e.isShiftDown())
                        FXLuminanceMode.setDarkMode(!FXLuminanceMode.isDarkMode());
                    if (e.isMetaDown())
                        FXLayoutMode.setCompactMode(!FXLayoutMode.isCompactMode());
                })
                .style();
        // Hiding the center item in compact mode
        FXLayoutMode.layoutModeProperty().addListener(observable -> headerCenterItem.setVisible(!FXLayoutMode.isCompactMode()));
        return mainFrameHeader;
    }

    private Button routeOperationButton(String routeOperationCode) {
        // Creating the route button
        Button routeButton = ActionBinder.bindButtonToAction(newButton(), routeOperationCodeToAction(routeOperationCode));
        // Automatically removing the button from layout if not visible
        routeButton.managedProperty().bind(routeButton.visibleProperty());
        return routeButton;
    }

    protected Node createBrandNode() {
        return null;
    }

    protected Node createMainFrameHeaderCenterItem() {
        return LayoutUtil.createHSpace(0);
    }

    protected Region createHeaderTabsBar() {
        return null;
    }

    protected Region createMainFrameFooter() {
        return null;
    }

    private Action routeOperationCodeToAction(String operationCode) {
        return RoutingActions.routeOperationCodeToAction(operationCode, this, this);
    }

    protected ActionGroup contextMenuActionGroup() {
        return newActionGroup(
                ChangeLanguageRequestEmitter.getProvidedEmitters().stream()
                        .map(instantiator -> newOperationAction(instantiator::emitLanguageRequest))
                        .toArray(Action[]::new)
        );
    }
}
