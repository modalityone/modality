package one.modality.crm.server.services.authz;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.auth.authn.AuthenticationService;
import dev.webfx.stack.auth.authz.server.spi.AuthorizationServerServiceProvider;
import dev.webfx.stack.com.bus.DeliveryOptions;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.push.server.PushServerService;

/**
 * @author Bruno Salmon
 */
public final class ModalityAuthorizationServerServiceProvider implements AuthorizationServerServiceProvider {

    // To be shared by the client counterpart.
    private final static String CLIENT_AUTHZ_SERVICE_ADDRESS = "modality/service/authz";
    private final static String AUTHZ_QUERY_BASE = "select rule.rule,activityState.route from AuthorizationAssignment";

    @Override
    public Future<Void> pushAuthorizations(Object userId, Object runId) {
        // Step 1: we are asking the user claims, so we can identify the user by his email
        AuthenticationService.getUserClaims(userId)
                // Step 2: we are loading the user authorizations from the database
                .compose(userClaims -> QueryService.executeQuery(new QueryArgument(
                                null,
                                DataSourceModelService.getDefaultDataSourceId(),
                                null,
                                "DQL",
                                AUTHZ_QUERY_BASE + " where active and management.user.email=?",
                                userClaims.getEmail()))
                        // Step 3: we are pushing the query result to the Modality authorization client counterpart
                        .compose(queryResult -> PushServerService.push(
                                CLIENT_AUTHZ_SERVICE_ADDRESS,
                                queryResult,
                                new DeliveryOptions(),
                                runId)));
        return null;
    }
}
