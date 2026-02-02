package one.modality.base.frontoffice.activities.mainframe.menus.desktop;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuBarFactory;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuBarLayout;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuConfig;

/**
 * @author Bruno Salmon
 */
public final class DesktopMainMenuBar {

    public static <A extends UiRouteActivityContext<?> & OperationActionFactoryMixin> MonoPane createDesktopTopMainMenuBar(A activity) {
        return MenuBarFactory.createMenuBar(
            MenuConfig.MAIN_MENU_OPERATION_CODES,
            MenuConfig.mainAndUserMenuItemGroup,
            false,
            MenuBarLayout.DESKTOP,
            activity);
    }

}
