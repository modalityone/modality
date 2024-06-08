package one.modality.base.client.operations.routes;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.client.activities.console.routing.ConsoleRouting;

public class RouteToConsoleRequest extends RoutePushRequest implements HasOperationCode {

    public RouteToConsoleRequest(BrowsingHistory browsingHistory) {
        super(ConsoleRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToConsole";
    }
}
