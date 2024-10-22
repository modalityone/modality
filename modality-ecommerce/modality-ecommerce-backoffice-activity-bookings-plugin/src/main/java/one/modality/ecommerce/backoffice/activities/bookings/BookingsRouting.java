package one.modality.ecommerce.backoffice.activities.bookings;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.client.util.routing.ModalityRoutingUtil;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class BookingsRouting {

    // Would be better but pb retrieving named groups doesn't work with JavaScript RegExp (can't retrieve groups): private final static String ANY_PATH = "/bookings(/organization/:organizationId|/event/:eventId|/day/:day|/arrivals|/departures|/minday/:minDay|/maxday/:maxDay|/filter/:filter|/groupby|:groupBy|/orderby/:orderBy/columns/:columns|export/:activityStateId)*";
    private final static String ANY_PATH = "/bookings(/organization/:organizationId)?(/event/:eventId)?(/day/:day)?(/arrivals)?(/departures)?(/minday/:minDay)?(/maxday/:maxDay)?(/filter/:filter)?(/groupby/:groupBy)?(/orderby/:orderBy)?(/columns/:columns)?(/export/:activityStateId)?";
    private final static String EVENT_PATH = "/bookings/event/:eventId";
    private final static String OPERATION_CODE = "RouteToBookings";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getEventBookingsPath(Object eventId) {
        return eventId == null ? "/bookings" : ModalityRoutingUtil.interpolateEventIdInPath(eventId, EVENT_PATH);
    }

    public static LocalDate parseDayParam(String parameterValue) {
        if (parameterValue == null)
            return null;
        switch (parameterValue) {
            case "yesterday" : return LocalDate.now().minusDays(1);
            case "today":      return LocalDate.now();
            case "tomorrow" :  return LocalDate.now().plusDays(1);
            default:           return LocalDate.parse(parameterValue); // Expecting an iso date (yyyy-MM-dd)
        }
    }

    public static final class BookingsUiRoute extends UiRouteImpl {

        public BookingsUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(getAnyPath())
                    , true
                    , BookingsActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToBookingsRequest extends RoutePushRequest implements HasOperationCode {

        public RouteToBookingsRequest(Object eventId, BrowsingHistory history) {
            super(getEventBookingsPath(eventId), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

    }

    public static final class RouteToBookingsRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToBookingsRequest(context.getParameter("eventId"), context.getHistory());
        }
    }
}
