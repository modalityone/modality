package one.modality.event.frontoffice.activities.booking;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.extras.operation.HasOperationCode;

public final class BookingRouting {

    private final static String PATH = "/booking";
    private final static String OPERATION_CODE = "RouteToBooking";

    public static String getPath() {
        return PATH;
    }

    public static final class BookingUiRoute extends UiRouteImpl {

        public BookingUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(BookingRouting.getPath()
                    , false
                    , BookingActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToBookingRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToBookingRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return BookingI18nKeys.Booking;
        }
    }

    public static final class RouteToBookingRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToBookingRequest(context.getHistory());
        }
    }
}
