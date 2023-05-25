package one.modality.hotel.backoffice.operations.routes.household;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.hotel.backoffice.activities.household.routing.HouseholdRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToHouseholdRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToHousehold";

    public RouteToHouseholdRequest(BrowsingHistory history) {
        super(HouseholdRouting.getAnyPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
