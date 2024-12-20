package one.modality.hotel.backoffice.activities.accommodation;

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
import one.modality.base.backoffice.activities.home.BackOfficeHomeI18nKeys;

/**
 * @author Bruno Salmon
 */
public final class AccommodationRouting {

    private final static String ANY_PATH = "/accommodation";
    private final static String OPERATION_CODE = "RouteToAccommodation";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static final class AccommodationUiRoute extends UiRouteImpl {

        public AccommodationUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(getAnyPath())
                    , true
                    , AccommodationActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToAccommodationRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToAccommodationRequest(BrowsingHistory history) {
            super(getAnyPath(), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return BackOfficeHomeI18nKeys.Accommodation;
        }
    }

    public static final class RouteToAccommodationRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToAccommodationRequest(context.getHistory());
        }
    }
}
