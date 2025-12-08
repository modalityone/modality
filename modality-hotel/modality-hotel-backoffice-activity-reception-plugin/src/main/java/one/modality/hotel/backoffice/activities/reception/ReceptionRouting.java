package one.modality.hotel.backoffice.activities.reception;

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
public final class ReceptionRouting {

    private final static String PATH = "/reception";
    private final static String OPERATION_CODE = "RouteToReception";
    public static String getPath() {
        return PATH;
    }

    public static final class ReceptionUiRoute extends UiRouteImpl {

        public ReceptionUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(ReceptionRouting.getPath()
                    , true
                    , ReceptionActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToLetterSetupRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToLetterSetupRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return ReceptionI18nKeys.ReceptionTile;
        }
    }

    public static class RouteToReceptionRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToLetterSetupRequest(context.getHistory());
        }
    }
}
