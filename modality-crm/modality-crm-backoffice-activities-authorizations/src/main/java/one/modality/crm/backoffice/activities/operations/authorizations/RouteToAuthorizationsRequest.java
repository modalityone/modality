package one.modality.crm.backoffice.activities.operations.authorizations;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.crm.backoffice.activities.authorizations.routing.AuthorizationsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToAuthorizationsRequest extends RoutePushRequest
    implements HasOperationCode {

  private static final String OPERATION_CODE = "RouteToAuthorizations";

  public RouteToAuthorizationsRequest(BrowsingHistory history) {
    super(AuthorizationsRouting.getPath(), history);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
