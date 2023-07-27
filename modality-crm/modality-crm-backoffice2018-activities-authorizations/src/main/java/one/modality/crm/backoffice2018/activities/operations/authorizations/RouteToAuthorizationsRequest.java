package one.modality.crm.backoffice2018.activities.operations.authorizations;


import one.modality.crm.backoffice2018.activities.authorizations.routing.AuthorizationsRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToAuthorizationsRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToAuthorizations";

    public RouteToAuthorizationsRequest(BrowsingHistory history) {
        super(AuthorizationsRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
