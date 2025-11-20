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
public final class BurgerMenu {

    private static final String BURGER_PATH = "M0,0 L32,0 M0,12 L32,12 M0,24 L32,24";

    public static <A extends UiRouteActivityContext<?> & OperationActionFactoryMixin> Node createBurgerMenuIcon(A activity) {
        SVGPath burgerIcon = SvgIcons.createStrokeSVGPath(BURGER_PATH, Brand.getBlueColor(), 3);
        CollapsePane mobileLeftMainMenuBar = MobileLeftMainMenuBar.createMobileLeftMainMenuBar(activity);
        return MenuBarFactory.setupSideMenuIconAndBar(burgerIcon, mobileLeftMainMenuBar, MenuBarLayout.MOBILE_LEFT);
    }

}
