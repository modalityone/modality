package one.modality.base.frontoffice.operations.routes.account;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.extras.operation.HasOperationCode;
import one.modality.base.frontoffice.activities.account.AccountI18nKeys;
import one.modality.base.frontoffice.activities.account.AccountRouting;

public class RouteToAccountRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

    private static final String OPERATION_CODE = "RouteToAccount";

    public RouteToAccountRequest(BrowsingHistory browsingHistory) {
        super(AccountRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return AccountI18nKeys.Account;
    }

}
