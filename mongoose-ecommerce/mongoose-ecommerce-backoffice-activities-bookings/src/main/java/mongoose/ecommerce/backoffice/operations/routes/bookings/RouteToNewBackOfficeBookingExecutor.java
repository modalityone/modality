package mongoose.ecommerce.backoffice.operations.routes.bookings;

import mongoose.base.client.aggregates.event.EventAggregate;
import mongoose.event.frontoffice.operations.fees.RouteToFeesRequest;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;
import dev.webfx.platform.shared.util.async.Future;

/**
 * @author Bruno Salmon
 */
final class RouteToNewBackOfficeBookingExecutor {

    static Future<Void> executeRequest(RouteToNewBackOfficeBookingRequest rq) {
        return execute(rq.getEventId(), rq.getHistory());
    }

    private static Future<Void> execute(Object eventId, BrowsingHistory history) {
        // When made in the back-office, we don't want to add the new booking to the last visited booking cart (as
        // opposed to the front-office), so we clear the reference to the active booking cart (if set) before routing
        EventAggregate eventAggregate = EventAggregate.get(eventId);
        if (eventAggregate != null)
            eventAggregate.setActiveCart(null);
        // Now that the current cart reference is cleared, we can route to the fees page
        new RouteToFeesRequest(eventId, history).execute();
        return Future.succeededFuture();
    }
}
