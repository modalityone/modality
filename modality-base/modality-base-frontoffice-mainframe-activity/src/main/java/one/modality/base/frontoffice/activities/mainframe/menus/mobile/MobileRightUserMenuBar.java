package one.modality.base.frontoffice.activities.mainframe.menus.mobile;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuBarFactory;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuBarLayout;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuConfig;

/**
 * @author Bruno Salmon
 */
public final class MobileRightUserMenuBar {

    public static <A extends UiRouteActivityContext<?> & OperationActionFactoryMixin> CollapsePane createMobileRightUserMenuBar(A activity) {
        CollapsePane userMenuBar = MenuBarFactory.createMenuBar(
            MenuConfig.USER_MENU_OPERATION_CODES,
            MenuConfig.mainAndUserMenuItemGroup,
            true,
            MenuBarLayout.MOBILE_RIGHT,
            activity);
        userMenuBar.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(userMenuBar, Pos.CENTER_RIGHT);
        return userMenuBar;
    }

}
