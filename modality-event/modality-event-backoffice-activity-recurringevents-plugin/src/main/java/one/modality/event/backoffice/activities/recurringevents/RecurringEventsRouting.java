package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.operation.HasOperationCode;

public final class RecurringEventsRouting {

    private final static String PATH = "/recurring-events";
    private final static String OPERATION_CODE = "RouteToRecurringEvents";

    public static String getPath() {
        return PATH;
    }

    public static final class RecurringEventsUiRoute extends UiRouteImpl {

        public RecurringEventsUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(RecurringEventsRouting.getPath()
                    , true
                    , RecurringEventsActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToRecurringEventsRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToRecurringEventsRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return RecurringEventsI18nKeys.RecurringEvents;
        }
    }

    public static final class RouteToRecurringEventsRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToRecurringEventsRequest(context.getHistory());
        }
    }
}
