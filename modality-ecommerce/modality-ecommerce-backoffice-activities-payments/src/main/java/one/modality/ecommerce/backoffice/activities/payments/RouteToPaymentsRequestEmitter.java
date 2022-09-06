package one.modality.ecommerce.backoffice.activities.payments;

import one.modality.ecommerce.backoffice.operations.routes.payments.RouteToPaymentsRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToPaymentsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToPaymentsRequest(context.getParameter("eventId"), context.getHistory());
    }
}
