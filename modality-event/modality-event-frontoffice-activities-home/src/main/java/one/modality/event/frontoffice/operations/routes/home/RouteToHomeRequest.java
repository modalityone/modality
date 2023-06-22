package one.modality.event.frontoffice.operations.routes.home;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import one.modality.base.frontoffice.states.GeneralPM;
import dev.webfx.stack.ui.operation.HasOperationCode;

public class RouteToHomeRequest extends RoutePushRequest implements HasOperationCode {
    public RouteToHomeRequest(BrowsingHistory browsingHistory) {
        super(GeneralPM.HOME_PATH, browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToHome";
    }
}
