package one.modality.event.frontoffice.operations.routes.account;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.frontoffice.states.GeneralPM;

public class RouteToAccountFriendsAndFamilyRequest extends RoutePushRequest implements HasOperationCode {
    public RouteToAccountFriendsAndFamilyRequest(BrowsingHistory browsingHistory) {
        super(GeneralPM.ACCOUNT_FRIENDS_AND_FAMILY_PATH, browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToAccountFriendsAndFamily";
    }
}
