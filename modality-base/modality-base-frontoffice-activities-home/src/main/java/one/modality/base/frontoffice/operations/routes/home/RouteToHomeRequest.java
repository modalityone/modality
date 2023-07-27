package one.modality.base.frontoffice.operations.routes.home;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.frontoffice.activities.home.routing.HomeRouting;

public class RouteToHomeRequest extends RoutePushRequest implements HasOperationCode {

    public RouteToHomeRequest(BrowsingHistory browsingHistory) {
        super(HomeRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToHome";
    }
}
