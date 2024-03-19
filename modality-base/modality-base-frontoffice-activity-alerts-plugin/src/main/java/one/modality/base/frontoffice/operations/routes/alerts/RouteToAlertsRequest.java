package one.modality.base.frontoffice.operations.routes.alerts;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import one.modality.base.frontoffice.activities.alerts.routing.AlertsRouting;

public class RouteToAlertsRequest extends RoutePushRequest implements dev.webfx.stack.ui.operation.HasOperationCode {

    public RouteToAlertsRequest(BrowsingHistory browsingHistory) {
        super(AlertsRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToAlerts";
    }
}
