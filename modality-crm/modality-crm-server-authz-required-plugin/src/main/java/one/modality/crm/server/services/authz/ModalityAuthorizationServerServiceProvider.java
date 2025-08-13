package one.modality.crm.server.services.authz;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authz.server.spi.AuthorizationServerServiceProvider;
import dev.webfx.stack.com.bus.DeliveryOptions;
import dev.webfx.stack.db.query.QueryResultBuilder;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.entity.DqlQueries;
import dev.webfx.stack.push.server.PushServerService;
import dev.webfx.stack.session.state.LogoutUserId;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;

/**
 * @author Bruno Salmon
 */
public final class ModalityAuthorizationServerServiceProvider implements AuthorizationServerServiceProvider {

    // TODO: Share these constants with the client counterpart (ModalityInMemoryUserAuthorizationChecker).
    private final static String CLIENT_AUTHZ_SERVICE_ADDRESS = "modality/service/authz";
    private final static String AUTHZ_QUERY_BASE = "select rule.rule,activityState.route,operation.(code,grantRoute,guest) from AuthorizationAssignment aa";

    @Override
    public Future<Void> pushAuthorizations() {
        // Capturing userId and runId from the thread local state holder (won't be present on subsequent async callbacks)
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
        // Otherwise reading the authorizations from the database and pushing the result set to the client:
        // Step 1: we ask the user claims, so we can identify the user by his email
        return AuthenticationService.getUserClaims()
            // Step 2: we load the user authorizations from the database
            .compose(userClaims ->
                QueryService.executeQuery(DqlQueries.newQueryArgumentForDefaultDataSource(
                        AUTHZ_QUERY_BASE + " where aa.active and (" +
                        // Authorizations granted to everybody (ex: Logout)
                        "aa..management = null and aa..role = null" +
                        // Authorizations granted to guests (at this point everybody is at least a guest)
                        " or aa..operation..guest" +
                        // Authorization directly granted to the user
                        " or aa..management != null and aa..management..user..email = $1 and aa..role = null" +
                        // Authorization granted to the user through a role he has been assigned
                        " or aa..management = null and aa..role != null and exists(select AuthorizationAssignment aa2 where aa2.active and aa2..management..user..email=$1 and aa2..role=aa..role)" +
                        ")", userClaims.getEmail()))
                    // Step 3: we push the query result to the Modality authorization client counterpart
                    .compose(queryResult ->
                        PushServerService.push(
                            CLIENT_AUTHZ_SERVICE_ADDRESS,
                            queryResult,
                            new DeliveryOptions(),
                            runId)
                    )
            );
    }
}
