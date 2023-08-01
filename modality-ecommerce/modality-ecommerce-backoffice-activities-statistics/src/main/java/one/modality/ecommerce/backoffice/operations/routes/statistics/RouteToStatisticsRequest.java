package one.modality.ecommerce.backoffice.operations.routes.statistics;

import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import one.modality.ecommerce.backoffice.activities.statistics.routing.StatisticsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToStatisticsRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToStatistics";

    public RouteToStatisticsRequest(Object eventId, BrowsingHistory history) {
        super(StatisticsRouting.getEventStatisticsPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
