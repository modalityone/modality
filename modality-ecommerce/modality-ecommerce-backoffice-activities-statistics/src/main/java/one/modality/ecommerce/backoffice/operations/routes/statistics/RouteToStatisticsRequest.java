package one.modality.ecommerce.backoffice.operations.routes.statistics;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;

import one.modality.ecommerce.backoffice.activities.statistics.routing.StatisticsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToStatisticsRequest extends RoutePushRequest implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToStatistics";

    public RouteToStatisticsRequest(Object eventId, BrowsingHistory history) {
        super(StatisticsRouting.getEventStatisticsPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
