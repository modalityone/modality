package one.modality.event.frontoffice.operations.routes.account;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.frontoffice.states.GeneralPM;

public class RouteToAccountSettingsRequest extends RoutePushRequest implements HasOperationCode {
    public RouteToAccountSettingsRequest(BrowsingHistory browsingHistory) {
        super(GeneralPM.ACCOUNT_SETTINGS_PATH, browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToAccountSettings";
    }
}
