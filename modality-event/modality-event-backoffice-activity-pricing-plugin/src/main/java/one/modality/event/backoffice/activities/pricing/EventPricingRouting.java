package one.modality.event.backoffice.activities.pricing;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.extras.operation.HasOperationCode;
import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class EventPricingRouting {

    private final static String ANY_PATH = "/event-pricing(/event/:eventId)?";
    private final static String EVENT_PATH = "/event-pricing/event/:eventId";
    private final static String OPERATION_CODE = "RouteToEventPricing";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getEventPricingPath(Object eventId) {
        return eventId == null ? "/event-pricing" : ModalityRoutingUtil.interpolateEventIdInPath(eventId, EVENT_PATH);
    }

    public static final class EventPricingUiRoute extends UiRouteImpl {

        public EventPricingUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(EventPricingRouting.getAnyPath())
                    , true
                    , EventPricingActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToEventPricingRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToEventPricingRequest(Object eventId, BrowsingHistory history) {
            super(getEventPricingPath(eventId), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return EventPricingI18nKeys.EventPricing;
        }
    }

    public static final class RouteToEventPricingRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToEventPricingRequest(context.getParameter("eventId"), context.getHistory());
        }
    }
}
