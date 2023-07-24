package one.modality.crm.server.services.authz;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authz.server.spi.AuthorizationServerServiceProvider;
import dev.webfx.stack.com.bus.DeliveryOptions;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.db.query.QueryResultBuilder;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.push.server.PushServerService;
import dev.webfx.stack.session.state.LogoutUserId;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;

/**
 * @author Bruno Salmon
 */
public final class ModalityAuthorizationServerServiceProvider
    implements AuthorizationServerServiceProvider {

  // TODO: Share this with the client counterpart.
  private static final String CLIENT_AUTHZ_SERVICE_ADDRESS = "modality/service/authz";
  private static final String AUTHZ_QUERY_BASE =
      "select rule.rule,activityState.route,operation.operationCode from AuthorizationAssignment aa";

  @Override
  public Future<Void> pushAuthorizations() {
    // Capturing userId and runId from the thread local state holder (won't be present on subsequent
    // async callbacks)
    Object userId = ThreadLocalStateHolder.getUserId();
    String runId = ThreadLocalStateHolder.getRunId();
    // Returning an empty result set when user is logged out or null (ie not logged in)
    if (LogoutUserId.isLogoutUserIdOrNull(userId)) {
      return PushServerService.push(
          CLIENT_AUTHZ_SERVICE_ADDRESS,
          QueryResultBuilder.create(0, 0).build(),
          new DeliveryOptions(),
          runId);
    }
    // Otherwise reading the authorizations from the database, and pushing the result set to the
    // client:
    // Step 1: we ask the user claims, so we can identify the user by his email
    return AuthenticationService.getUserClaims()
        // Step 2: we load the user authorizations from the database
        .compose(
            userClaims ->
                QueryService.executeQuery(
                        new QueryArgumentBuilder()
                            .setLanguage("DQL")
                            .setStatement(
                                AUTHZ_QUERY_BASE
                                    + " where active and (management=null or aa..management..user..email=?)")
                            .setParameters(userClaims.getEmail())
                            .setDataSourceId(DataSourceModelService.getDefaultDataSourceId())
                            .build())
                    // Step 3: we push the query result to the Modality authorization client
                    // counterpart
                    .compose(
                        queryResult ->
                            PushServerService.push(
                                CLIENT_AUTHZ_SERVICE_ADDRESS,
                                queryResult,
                                new DeliveryOptions(),
                                runId)));
  }
}
