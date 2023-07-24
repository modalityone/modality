package one.modality.base.backoffice.operations.routes.filters;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.backoffice.activities.filters.routing.FiltersRouting;

public final class RouteToFiltersRequest extends RoutePushRequest implements HasOperationCode {

  private static final String OPERATION_CODE = "RouteToFilters";

  public RouteToFiltersRequest(BrowsingHistory history) {
    super(FiltersRouting.getPath(), history);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
