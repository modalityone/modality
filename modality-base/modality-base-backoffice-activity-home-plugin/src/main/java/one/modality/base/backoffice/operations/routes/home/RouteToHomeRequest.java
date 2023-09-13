package one.modality.base.backoffice.operations.routes.home;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.backoffice.activities.home.routing.HomeRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToHomeRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToHome";

    public RouteToHomeRequest(BrowsingHistory history) {
        super(HomeRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
