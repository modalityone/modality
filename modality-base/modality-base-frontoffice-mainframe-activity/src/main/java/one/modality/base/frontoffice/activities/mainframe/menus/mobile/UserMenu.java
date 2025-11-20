package one.modality.base.frontoffice.activities.mainframe.menus.mobile;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.brand.Brand;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuBarFactory;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuBarLayout;

/**
 * @author Bruno Salmon
 */
public class UserMenu {

    public static <A extends UiRouteActivityContext<?> & OperationActionFactoryMixin> Node createUserMenuIcon(A activity) {
        SVGPath userIcon = SvgIcons.setSVGPathFill(SvgIcons.createUserIcon(), Brand.getBlueColor());
        CollapsePane mobileRightUserMenuBar = MobileRightUserMenuBar.createMobileRightUserMenuBar(activity);
        return MenuBarFactory.setupSideMenuIconAndBar(userIcon, mobileRightUserMenuBar, MenuBarLayout.MOBILE_RIGHT);
    }

}
