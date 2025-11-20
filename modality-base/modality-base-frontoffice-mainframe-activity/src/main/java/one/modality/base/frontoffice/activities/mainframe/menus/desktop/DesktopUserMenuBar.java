package one.modality.base.frontoffice.activities.mainframe.menus.desktop;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.session.state.client.fx.FXLoggedIn;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuBarFactory;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuBarLayout;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuConfig;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;

/**
 * @author Bruno Salmon
 */
public final class DesktopUserMenuBar {

    public static <A extends UiRouteActivityContext<?> & OperationActionFactoryMixin> CollapsePane createDesktopUserMenuBar(A activity) {
        CollapsePane userMenuButtonBar = MenuBarFactory.createMenuBar(
            MenuConfig.USER_MENU_OPERATION_CODES,
            MenuConfig.mainAndUserMenuItemGroup,
            true,
            MenuBarLayout.DESKTOP,
            activity);
        userMenuButtonBar.setAnimate(false);
        userMenuButtonBar.collapsedProperty().bind(FXLoggedIn.loggedInProperty().not().or(FXCollapseMenu.collapseMenuProperty()));
        userMenuButtonBar.setAnimate(true);
        return userMenuButtonBar;
    }

}
