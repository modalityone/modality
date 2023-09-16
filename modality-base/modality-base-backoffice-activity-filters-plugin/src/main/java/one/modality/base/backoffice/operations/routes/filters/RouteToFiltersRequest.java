package one.modality.base.backoffice.operations.routes.filters;

import one.modality.base.backoffice.activities.filters.routing.FiltersRouting;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

public final class RouteToFiltersRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToFilters";

    public RouteToFiltersRequest(BrowsingHistory history) {
        super(FiltersRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
