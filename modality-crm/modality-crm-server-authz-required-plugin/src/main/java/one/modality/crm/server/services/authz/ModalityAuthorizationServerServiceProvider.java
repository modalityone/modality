package one.modality.crm.server.services.authz;

import dev.webfx.platform.async.Batch;
import dev.webfx.platform.async.CompositeFuture;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authz.server.spi.AuthorizationServerServiceProvider;
import dev.webfx.stack.com.bus.DeliveryOptions;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.push.server.PushServerService;
import dev.webfx.stack.session.state.LogoutUserId;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.*;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class ModalityAuthorizationServerServiceProvider implements AuthorizationServerServiceProvider {

    // TODO: Share these constants with the client counterpart (ModalityInMemoryUserAuthorizationChecker).
    private final static String CLIENT_AUTHZ_SERVICE_ADDRESS = "modality/service/authz";

    @Override
    public Future<Void> pushAuthorizations() {
        // Capturing userId and runId from the thread local state holder (won't be present on later async callbacks)
        Object userId = ThreadLocalStateHolder.getUserId();
        String runId = ThreadLocalStateHolder.getRunId();
        boolean backoffice = ThreadLocalStateHolder.isBackoffice();
        // Returning an empty result set when the user is logged out or null (i.e., not logged in)
        if (LogoutUserId.isLogoutUserIdOrNull(userId)) {
            EntityStore entityStore = EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());
            return entityStore.<Operation>executeQuery("select operationCode, grantRoute from Operation op where ($1 and backoffice or !$1 and frontoffice) and public", backoffice)
                .compose(operations ->
                    pushAuthorizationsObject(grantOperations(operations, new StringBuilder("logout\n")).toString(), runId));
        }
        // Otherwise reading the authorizations from the database and pushing the result set to the client:
        // Step 1: we ask the user claims, so we can identify the user by his email
        return AuthenticationService.getUserClaims()
            // Step 2: we load the user authorizations from the database
            .compose(userClaims ->
                loadAndPushUserAuthorizations(userClaims.email(), backoffice, runId)
            );
    }

    private <T> Future<T> loadAndPushUserAuthorizations(String userEmail, boolean backoffice, Object runId) {
        EntityStore entityStore = EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());
        return Future.all(
            // Loading guest operations granted to everyone who is logged in
            entityStore.<Operation>executeQuery("select operationCode, grantRoute from Operation op where ($1 and backoffice or !$1 and frontoffice) and guest", backoffice)
                .map(operations -> grantOperations(operations, new StringBuilder()).toString()),
            // Loading operations and rules granted to the user
            entityStore.<AuthorizationOrganizationUserAccess>executeQuery(
                    "select organization.id,event.id,role.id from AuthorizationOrganizationUserAccess where user.email=$1 order by organization.id,event..id", userEmail)
                .compose(userAccesses -> {
                    int n = userAccesses.size();
                    return new Batch<>(userAccesses.toArray(new AuthorizationOrganizationUserAccess[n]))
                        .executeParallel(CompositeFuture[]::new, userAccess ->
                            Future.all(
                                entityStore.executeQuery("select operationCode, grantRoute from Operation op where ($1 and backoffice or !$1 and frontoffice) and exists(select AuthorizationRoleOperation ro where ro.role=$2 and (ro.operation = op or ro.operationGroup = op.group))", backoffice, userAccess.getRole()),
                                entityStore.executeQuery("select rule from AuthorizationRule where role=$1", userAccess.getRole())
                            )
                        ).map(batch -> {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < n; i++) {
                                AuthorizationOrganizationUserAccess userAccess = userAccesses.get(i);
                                EntityList<Operation> operations = batch.get(i).resultAt(0);
                                EntityList<AuthorizationRule> rules = batch.get(i).resultAt(1);
                                sb.append("context:organizationId=").append(Entities.getPrimaryKey(userAccess.getOrganizationId()));
                                Object eventId = Entities.getPrimaryKey(userAccess.getEventId());
                                if (eventId != null)
                                    sb.append(",eventId=").append(eventId);
                                sb.append("\n");
                                grantOperations(operations, sb);
                                rules.forEach(rule -> sb.append(rule.getRule()).append("\n"));
                            }
                            return sb.toString();
                        });
                }),
            // If the user is also the admin of an organization, we grant him the permissions to manage the organization
            entityStore.<AuthorizationOrganizationAdmin>executeQuery("select organization.id from AuthorizationOrganizationAdmin where admin.email=$1 order by organization.id", userEmail)
                .map(organizationAdmins -> {
                    StringBuilder sb = new StringBuilder();
                    organizationAdmins.forEach(organizationAdmin -> {
                        sb.append("context:organizationId=").append(Entities.getPrimaryKey(organizationAdmin.getOrganizationId())).append("\n");
                        sb.append("grant operation:RouteToAdmin\n");
                        sb.append("grant route:/admin*\n");
                    });
                    return sb.toString();
                }),
            // If the user is a super admin, we grant him universal permissions
            entityStore.<AuthorizationSuperAdmin>executeQuery("select AuthorizationSuperAdmin where superAdmin.email=$1 limit 1", userEmail)
                .map(superAdmins -> {
                    if (superAdmins.isEmpty())
                        return "";
                    return """
                        context:any
                        grant operation:*
                        grant route:*
                        """;
                })
        ).compose(compositeFuture -> {
            String guestGrants = (String) compositeFuture.list().get(0);
            String userGrants = (String) compositeFuture.list().get(1);
            String adminGrants = (String) compositeFuture.list().get(2);
            String superAdminGrants = (String) compositeFuture.list().get(3);
            return pushAuthorizationsObject(superAdminGrants.isEmpty() ? guestGrants + userGrants + adminGrants : superAdminGrants, runId);
        });
    }

    private StringBuilder grantOperations(List<Operation> operations, StringBuilder sb) {
        operations.forEach(operation -> {
            sb.append("grant operation:").append(operation.getOperationCode()).append("\n");
            String grantRoute = operation.getGrantRoute();
            if (!Strings.isEmpty(grantRoute))
                sb.append("grant route:").append(grantRoute).append("\n");
        });
        return sb;
    }

    private <T> Future<T> pushAuthorizationsObject(Object pushObject, Object runId) {
        return PushServerService.push(
            CLIENT_AUTHZ_SERVICE_ADDRESS,
            pushObject,
            new DeliveryOptions(),
            runId);
    }
}
