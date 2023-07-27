package one.modality.base.backoffice2018.operations.routes.monitor;

import one.modality.base.backoffice2018.activities.monitor.routing.MonitorRouting;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToMonitorRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToMonitor";

    public RouteToMonitorRequest(BrowsingHistory history) {
        super(MonitorRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
