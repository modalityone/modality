package one.modality.event.frontoffice.operations.routes.alerts;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import one.modality.base.frontoffice.states.GeneralPM;

public class RouteToAlertsRequest extends RoutePushRequest implements dev.webfx.stack.ui.operation.HasOperationCode {

    public RouteToAlertsRequest(BrowsingHistory browsingHistory) {
        super(GeneralPM.ALERTS_PATH, browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToAlerts";
    }
}
