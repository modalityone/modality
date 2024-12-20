package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.operation.HasOperationCode;

public class UserProfileRouting {

    private final static String ANY_PATH = "/user-profile(/email-update/:token)?";
    private final static String PATH = "/user-profile";
    private final static String OPERATION_CODE = "RouteToUserProfile";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getPath() {
        return PATH;
    }

    public static class UserProfileUiRoute extends UiRouteImpl<ViewDomainActivityContextFinal> {

        public UserProfileUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<ViewDomainActivityContextFinal> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(UserProfileRouting.getAnyPath())
                    , true
                    , UserProfileActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToUserProfileRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToUserProfileRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return UserProfileI18nKeys.UserProfile;
        }
    }

    public static class RouteToUserProfileRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToUserProfileRequest(context.getHistory());
        }
    }
}
