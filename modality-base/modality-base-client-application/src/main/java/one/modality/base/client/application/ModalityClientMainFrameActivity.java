package one.modality.base.client.application;

import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.session.state.client.fx.FXLoggedIn;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.profile.fx.FXProfile;

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
        headerCenterItem.visibleProperty().bind(FXLoggedIn.loggedInProperty());

        Pane headerButtonsBar = new Pane() { // Children will be set just after
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
                    layoutInArea(brandNode, x += w + 8, y, w = brandNode.prefWidth(h), h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
                layoutInArea(headerCenterItem, x += w + 5, y, w = Math.max(width - 2 * x, headerCenterItem.prefWidth(h)), h, 0, Insets.EMPTY, false, false, HPos.CENTER, VPos.CENTER);
                Node profileButton = FXProfile.getProfileButton();
                if (profileButton != null && profileButton.isManaged())
                    layoutInArea(profileButton, x += w + 8, y, width - 5 - x, h, 0, HPos.RIGHT, VPos.CENTER);
            }

            @Override
            protected double computePrefHeight(double width) {
                return super.computePrefHeight(width) + 6;
            }
        };

        // The profile button can be customized (ex: ModalityClientProfileInitJob)
        if (FXProfile.getProfileButton() == null) { // If not, we just display a logout button instead
            Button button = actionButton(newOperationAction(LogoutRequest::new));
            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY); // We display just the icon, not the text
            FXProfile.setProfileButton(button);
        }
        // Setting all children, including the profile button
        FXProperties.runNowAndOnPropertiesChange(() -> {
            headerButtonsBar.getChildren().setAll(Collections.listOfRemoveNulls(homeButton, backButton, forwardButton, brandNode, headerCenterItem, FXProfile.getProfileButton()));
        }, FXProfile.profileButtonProperty(), FXProfile.profilePanelProperty());

        // 2) Building a customisable headerTabsBar (used only in the backoffice so far)
        Region headerTabsBar = createHeaderTabsBar();

        // Assembling 1) & 2) into the mainFrameHeader
        Region mainFrameHeader = headerTabsBar == null ? headerButtonsBar : new VBox(headerButtonsBar, headerTabsBar);

        LuminanceTheme.createApplicationFrameFacet(headerButtonsBar)
                .setBordered(true)
                .style();
        // Hiding the center item in compact mode
        // Commented as headerCenterItem.visibleProperty() is bound
        //FXLayoutMode.layoutModeProperty().addListener(observable -> headerCenterItem.setVisible(!FXLayoutMode.isCompactMode()));
        return mainFrameHeader;
    }

    private Button routeOperationButton(String routeOperationCode) {
        return actionButton(routeOperationCodeToAction(routeOperationCode));
    }

    private Button actionButton(Action action) {
        return ActionBinder.bindButtonToAction(newButton(), action);
    }

    @Override
    public Button newButton() {
        Button button = ModalityButtonFactoryMixin.super.newButton();
        button.setPadding(new Insets(5));
        return button;
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
}
