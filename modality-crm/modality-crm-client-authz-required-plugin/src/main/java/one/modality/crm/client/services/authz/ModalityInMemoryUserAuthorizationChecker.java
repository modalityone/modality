package one.modality.crm.client.services.authz;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.meta.Meta;
import dev.webfx.stack.shareddata.cache.CacheEntry;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.authz.client.operation.OperationAuthorizationRuleParser;
import dev.webfx.stack.authz.client.spi.impl.inmemory.InMemoryUserAuthorizationChecker;
import dev.webfx.stack.shareddata.cache.serial.SerialCache;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.sqlcompiler.mapping.QueryRowToEntityMapping;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.query_result_to_entities.QueryResultToEntitiesMapper;
import dev.webfx.stack.routing.router.auth.authz.RoutingAuthorizationRuleParser;
import dev.webfx.stack.session.state.LogoutUserId;
import dev.webfx.stack.session.state.client.fx.FXAuthorizationsChanged;
import dev.webfx.stack.session.state.client.fx.FXAuthorizationsReceived;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;

import java.util.List;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
final class ModalityInMemoryUserAuthorizationChecker extends InMemoryUserAuthorizationChecker {

    // TODO: share this constant with the server counterpart (ModalityAuthorizationServerServiceProvider).
    private final static String AUTHZ_QUERY_BASE = "select rule.rule,activityState.route,operation.(code,grantRoute,guest) from AuthorizationAssignment aa";

    private final Object userId;
    private final DataSourceModel dataSourceModel;
    private final CacheEntry<Object> authorizationPushCacheEntry = SerialCache.createCacheEntry("modality/crm/authz/push-query-result");
    private List<Entity> publicOrGuestOperationsWithGrantRoute;
    private Object lastPushObject;
    private List<Entity> lastPublicOrGuestOperationsWithGrantRoute;

    ModalityInMemoryUserAuthorizationChecker(Object userId, DataSourceModel dataSourceModel) {
        this.userId = userId;
        this.dataSourceModel = dataSourceModel;
        // Registering the authorization (requests and rules) parsers
        ruleRegistry.addAuthorizationRuleParser(new RoutingAuthorizationRuleParser());
        ruleRegistry.addAuthorizationRuleParser(new OperationAuthorizationRuleParser());
        // Getting initial authorizations from cache if present (only if the previous session was logged in as Modality user already)
        if (FXModalityUserPrincipal.getModalityUserPrincipal() != null) {
            authorizationPushCacheEntry.getValue()
                .onSuccess(cachedValue -> {
                    if (cachedValue != null)
                        onAuthorizationPush(cachedValue);
                });
        }
        // Note: the pushObject sent by the server contained all permissions specifically assigned to the user.
        // In addition, we may have public operations. Since they are public, they don't need authorizations, however,
        // some may have a route associated, and we need therefore to authorize those public routes.
        String officeType = Meta.isBackoffice() ? "backoffice" : "frontoffice";
        EntityStore.create(dataSourceModel)
            .executeQueryWithCache("modality/crm/" + officeType + "/authz/operations",
                """
                    select grantRoute, guest, public
                        from Operation
                        where grantRoute!=null
                            and (public or guest)
                            and officeType
                    """.replace("officeType", officeType))
            .onFailure(Console::log)
            .onCacheAndOrSuccess(this::onPublicOrGuestOperationsWithGrantRouteChanged);
    }

    private void onPublicOrGuestOperationsWithGrantRouteChanged(List<Entity> operations) {
        publicOrGuestOperationsWithGrantRoute = operations;
        onAuthorizationPush(lastPushObject);
    }

    void onAuthorizationPush(Object pushObject) {
        // May happen that it's the same input as last time, and there is no need to recompute everything
        if (Objects.equals(pushObject, lastPushObject) && lastPublicOrGuestOperationsWithGrantRoute == publicOrGuestOperationsWithGrantRoute) {
            // However, we need to fire the event; otherwise the requested page won't be displayed after second login from same user
            if (!FXAuthorizationsReceived.isAuthorizationsReceived())
                FXAuthorizationsChanged.fireAuthorizationsChanged();
            return;
        }

        // At this point, it's an actual change in the authorizations
        QueryResult queryResult = (QueryResult) pushObject;
        QueryRowToEntityMapping queryMapping = dataSourceModel.parseAndCompileSelect(AUTHZ_QUERY_BASE).getQueryMapping();
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        EntityList<Entity> assignments = QueryResultToEntitiesMapper.mapQueryResultToEntities(queryResult, queryMapping, entityStore, "assignments");

        // A bit hacky, but we try to detect if the pushObject is really for this user, because sometimes there is
        // confusion with the logout user. It looks like the server may still push authorizations for the logout user
        // just after the user logged in for some reason.
        // Firstly, we check that the user is still matching the current user
        Object currentUserId = FXUserId.getUserId();
        boolean userStillMatching = Objects.equals(userId, currentUserId) || LogoutUserId.isLogoutUserIdOrNull(userId) && LogoutUserId.isLogoutUserIdOrNull(currentUserId);
        if (!userStillMatching) {
            return;
        }
        // Secondly, if there is no assignment, it's likely a push for the logout user, so we ignore it (if this user is
        // not the logout user).
        if (assignments.isEmpty() && !LogoutUserId.isLogoutUserIdOrNull(userId) && lastPushObject != null) {
            return;
        }
        // TODO: find a better way to detect for which user the pushObject is intended - should probably be part of the
        //  pushObject itself, or at least mark the pushObject when intended for the logout user.

        lastPushObject = pushObject;
        lastPublicOrGuestOperationsWithGrantRoute = publicOrGuestOperationsWithGrantRoute;
        clearAllAuthorizationRulesAndGrantAuthorizedRoutesFromPublicOrGuestOperations();
        for (Entity assignment : assignments) {
            // Case of a rule (ex: "grant route:*" or "grant operation:*")
            Entity authorizationRule = assignment.getForeignEntity("rule");
            if (authorizationRule != null) // if yes, passing the rule as a string (will be parsed)
                ruleRegistry.registerAuthorizationRule(authorizationRule.getStringFieldValue("rule"));
/* Commented as activity state was experimental (not sure if we will use it)
            // Case when it's an activity state => automatically granting the route to it (when provided)
            Entity activityState = assignment.getForeignEntity("activityState");
            if (activityState != null) {
                String route = activityState.getStringFieldValue("route");
                if (route != null) {
                    route = Strings.replaceAll(route, "[id]", activityState.getPrimaryKey());
                    ruleRegistry.registerAuthorizationRule(new RoutingAuthorizationRule(AuthorizationRuleType.GRANT, route, false));
                }
            }
*/
            // Case of a specific operation
            Entity operation = assignment.getForeignEntity("operation");
            if (operation != null) {
                // Granting that specific operation
                ruleRegistry.registerAuthorizationRule("grant operation:" + operation.evaluate("code"));
                // Granting the associated route if provided
                String route = operation.getStringFieldValue("grantRoute");
                if (!Strings.isEmpty(route)) {
                    ruleRegistry.registerAuthorizationRule("grant route:" + route);
                }
            }
        }

        FXAuthorizationsChanged.fireAuthorizationsChanged();

        // Caching latest authorizations
        authorizationPushCacheEntry.putValue(pushObject);
    }

    private void clearAllAuthorizationRulesAndGrantAuthorizedRoutesFromPublicOrGuestOperations() {
        ruleRegistry.clearAllAuthorizationRules();
        if (publicOrGuestOperationsWithGrantRoute != null) {
            publicOrGuestOperationsWithGrantRoute.forEach(op -> {
                String route = op.getStringFieldValue("grantRoute");
                if (!Strings.isEmpty(route)) {
                    boolean isOperationPublic = op.getBooleanFieldValue("public");
                    boolean isOperationGuest = op.getBooleanFieldValue("guest");
                    boolean isUserAtLeastGuest = !LogoutUserId.isLogoutUserIdOrNull(FXUserId.getUserId());
                    if (isOperationPublic || isOperationGuest && isUserAtLeastGuest)
                        ruleRegistry.registerAuthorizationRule("grant route:" + route);
                }
            });
        }
    }
}
