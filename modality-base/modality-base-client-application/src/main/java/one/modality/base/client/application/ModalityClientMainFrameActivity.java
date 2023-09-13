package one.modality.base.client.application;

import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.FXLuminanceMode;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.palette.FXPaletteMode;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.action.ActionGroup;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
        Node headerCenterItem = createMainFrameHeaderCenterItem();
        HBox headerButtonsBar = new HBox(
                // Home button
                ActionBinder.bindButtonToAction(newButton(), routeOperationCodeToAction("RouteToHome")),
                LayoutUtil.createHSpace(6),
                // Back navigation button (will be hidden in browsers as they already have one)
                ActionBinder.bindButtonToAction(newButton(), routeOperationCodeToAction("RouteBackward")),
                LayoutUtil.createHSpace(3),
                // Forward navigation button (will be hidden in browsers as they already have one)
                ActionBinder.bindButtonToAction(newButton(), routeOperationCodeToAction("RouteForward")),
                // Horizontal space
                LayoutUtil.createHGrowable(),
                headerCenterItem,
                LayoutUtil.createHGrowable(),
                // Logout button
                ActionBinder.bindButtonToAction(newButton(), newOperationAction(LogoutRequest::new))
        );
        headerButtonsBar.setAlignment(Pos.CENTER_LEFT);
        headerButtonsBar.setPadding(new Insets(5));

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
