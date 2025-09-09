package one.modality.crm.frontoffice.activities.members;

import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

/**
 * @author Bruno Salmon
 */
public final class MembersRouting {

    private final static String PATH = "/members";
    private final static String OPERATION_CODE = "RouteToMembers";

    public static String getPath() {
        return PATH;
    }

    public static final class MembersUiRoute extends UiRouteImpl {

        public MembersUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(MembersRouting.getPath()
                    , false
                    , MembersActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToMembersRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToMembersRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return MembersI18nKeys.MembersMenu;
        }
    }

    public static class RouteToUserProfileRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToMembersRequest(context.getHistory());
        }
    }


}
