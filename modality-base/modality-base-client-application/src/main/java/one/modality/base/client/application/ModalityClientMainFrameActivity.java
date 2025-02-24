package one.modality.base.client.application;

import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.session.Session;
import dev.webfx.stack.session.state.client.fx.FXLoggedIn;
import dev.webfx.stack.session.state.client.fx.FXSession;
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

    { // I18n language storage management (using user session which is based on LocalStorage for now)
        // TODO Move this feature into WebFX Stack
        // Restoring the user language stored from the session
        FXProperties.runNowAndOnPropertyChange(session -> {
            if (session != null) { // The session may be null first time the user launches the application
                Object lang = session.get("lang");
                if (lang != null)
                    I18n.setLanguage(lang);
            }
        }, FXSession.sessionProperty());
        // Saving the user language into the session
        FXProperties.runOnPropertyChange(lang -> {
            Session session = FXSession.getSession();
            if (session != null) {
                session.put("lang", lang);
                session.store();
            }
        }, I18n.languageProperty());
        // This is to ensure LogoutRequest is registered in OperationActionRegistry (especially in front-office where
        // it is not explicitly called by just referred via operation code from configuration), so that the Logout
        // action can be displayed once the user is logged in (via authorization mechanism).
        newOperationAction(LogoutRequest::new);
    }

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
                    layoutInArea(backButton, x += w + 5, y, w = backButton.prefWidth(h), h, 0, HPos.LEFT, VPos.CENTER);
                if (forwardButton.isManaged())
                    layoutInArea(forwardButton, x += w + 3, y, w = forwardButton.prefWidth(h), h, 0, HPos.LEFT, VPos.CENTER);
                if (brandNode != null)
                    layoutInArea(brandNode, x += w + 12, y, w = brandNode.prefWidth(h), h, 0, Insets.EMPTY, false, false, HPos.LEFT, VPos.CENTER);
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
        FXProperties.runNowAndOnPropertiesChange(() -> headerButtonsBar.getChildren().setAll(Collections.listOfRemoveNulls(
            homeButton, backButton, forwardButton, brandNode, headerCenterItem, FXProfile.getProfileButton()))
            , FXProfile.profileButtonProperty(), FXProfile.profilePanelProperty());

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
        button.getStyleClass().add("main-frame-header-button");
        return button;
    }

    protected Node createBrandNode() {
        return null;
    }

    protected Node createMainFrameHeaderCenterItem() {
        return Layouts.createHSpace(0);
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
