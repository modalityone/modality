package one.modality.base.frontoffice.activities.mainframe.menus.mobile;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuBarFactory;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuConfig;

/**
 * @author Bruno Salmon
 */
public class MobileBottomMainMenuBar {

    public static <A extends UiRouteActivityContext<?> & OperationActionFactoryMixin> CollapsePane createMobileBottomMainMenuBar(A activity) {
        return MenuBarFactory.createMenuBar(
            MenuConfig.MAIN_MENU_OPERATION_CODES,
            MenuConfig.mobileMenuItemGroup,
            false,
            true,
            activity);
    }

}
