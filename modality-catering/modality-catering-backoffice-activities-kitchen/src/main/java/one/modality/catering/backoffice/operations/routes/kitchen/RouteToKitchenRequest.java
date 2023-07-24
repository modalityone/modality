package one.modality.catering.backoffice.operations.routes.kitchen;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;

import one.modality.catering.backoffice.activities.kitchen.routing.KitchenRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToKitchenRequest extends RoutePushRequest implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToKitchen";

    public RouteToKitchenRequest(BrowsingHistory history) {
        super(KitchenRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
