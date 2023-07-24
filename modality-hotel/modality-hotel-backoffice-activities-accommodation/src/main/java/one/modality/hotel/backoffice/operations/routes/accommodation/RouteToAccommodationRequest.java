package one.modality.hotel.backoffice.operations.routes.accommodation;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;

import one.modality.hotel.backoffice.activities.accommodation.routing.AccommodationRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToAccommodationRequest extends RoutePushRequest
        implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToAccommodation";

    public RouteToAccommodationRequest(BrowsingHistory history) {
        super(AccommodationRouting.getAnyPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
