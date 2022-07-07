package org.modality_project.base.backoffice.operations.routes.filters;

import org.modality_project.base.backoffice.activities.filters.routing.FiltersRouting;
import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

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
