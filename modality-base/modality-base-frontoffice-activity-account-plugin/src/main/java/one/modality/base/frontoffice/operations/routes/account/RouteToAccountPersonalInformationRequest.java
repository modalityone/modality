package one.modality.base.frontoffice.operations.routes.account;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.extras.operation.HasOperationCode;
import one.modality.base.frontoffice.activities.account.personalinfo.AccountPersonalInformationRouting;

public class RouteToAccountPersonalInformationRequest extends RoutePushRequest implements HasOperationCode {
    public RouteToAccountPersonalInformationRequest(BrowsingHistory browsingHistory) {
        super(AccountPersonalInformationRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToAccountPersonalInformation";
    }
}
