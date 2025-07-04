package one.modality.base.frontoffice.operations.routes.account;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.extras.operation.HasOperationCode;
import one.modality.base.frontoffice.activities.account.routing.AccountFriendsAndFamilyEditRouting;

public class RouteToEditAccountFriendsAndFamilyRequest extends RoutePushRequest implements HasOperationCode {
    public RouteToEditAccountFriendsAndFamilyRequest(BrowsingHistory browsingHistory) {
        super(AccountFriendsAndFamilyEditRouting.getPath(), browsingHistory);
    }
    @Override
    public Object getOperationCode() {
        return "RouteToEditAccountFriendsAndFamily";
    }
}
