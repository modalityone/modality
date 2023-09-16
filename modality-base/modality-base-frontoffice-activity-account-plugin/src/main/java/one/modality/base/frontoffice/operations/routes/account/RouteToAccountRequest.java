package one.modality.base.frontoffice.operations.routes.account;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.frontoffice.activities.account.routing.AccountRouting;

public class RouteToAccountRequest extends RoutePushRequest implements HasOperationCode {

    public RouteToAccountRequest(BrowsingHistory browsingHistory) {
        super(AccountRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToAccount";
    }
}
