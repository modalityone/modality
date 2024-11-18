package one.modality.ecommerce.backoffice.activities.statistics;

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
import one.modality.base.backoffice.activities.home.HomeI18nKeys;
import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class StatisticsRouting {

    private final static String ANY_PATH = "/statistics(/event/:eventId)?";
    private final static String EVENT_PATH = "/statistics/event/:eventId";
    private final static String OPERATION_CODE = "RouteToFinancesAndStats";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getEventStatisticsPath(Object eventId) {
        return eventId == null ? "/statistics" : ModalityRoutingUtil.interpolateEventIdInPath(eventId, EVENT_PATH);
    }

    public static final class StatisticsUiRoute extends UiRouteImpl {

        public StatisticsUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(getAnyPath())
                    , true
                    , StatisticsActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToStatisticsRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToStatisticsRequest(Object eventId, BrowsingHistory history) {
            super(getEventStatisticsPath(eventId), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return HomeI18nKeys.FinancesAndStats;
        }
    }

    public static final class RouteToStatisticsRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToStatisticsRequest(context.getParameter("eventId"), context.getHistory());
        }
    }
}
