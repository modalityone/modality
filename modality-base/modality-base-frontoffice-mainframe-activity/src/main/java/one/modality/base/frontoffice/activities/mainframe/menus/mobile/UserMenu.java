package one.modality.base.frontoffice.activities.mainframe.menus.mobile;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.session.state.client.fx.FXLoggedIn;
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

    public static <A extends UiRouteActivityContext<?> & OperationActionFactoryMixin> Node createUserMenuIcon(Node languageMenuBar, A activity) {
        CollapsePane mobileRightUserMenuBar = MobileRightUserMenuBar.createMobileRightUserMenuBar(activity);
        SVGPath userIcon = SvgIcons.setSVGPathFill(SvgIcons.createUserIcon(), Brand.getBlueColor()); // Maybe too small
        MonoPane userButton = MenuBarFactory.setupSideMenuIconAndBar(userIcon, mobileRightUserMenuBar, languageMenuBar, MenuBarLayout.MOBILE_RIGHT);
        // We display the user button only when the user is logged in
        Layouts.bindManagedAndVisiblePropertiesTo(FXLoggedIn.loggedInProperty(), userButton);
        return userButton;
    }

}
