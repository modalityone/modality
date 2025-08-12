package one.modality.crm.client.services.authz;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.meta.Meta;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.authz.client.operation.OperationAuthorizationRuleParser;
import dev.webfx.stack.authz.client.spi.impl.inmemory.InMemoryUserAuthorizationChecker;
import dev.webfx.stack.cache.CacheEntry;
import dev.webfx.stack.cache.client.LocalStorageCache;
import dev.webfx.stack.cache.client.SessionClientCache;
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

    private final DataSourceModel dataSourceModel;
    private final CacheEntry<Object> authorizationPushCashEntry = SessionClientCache.get().getCacheEntry("cache-authz-push");
    private List<Entity> publicOrGuestOperationsWithGrantRoute;
    private Object lastPushObject;
    private List<Entity> lastPublicOrGuestOperationsWithGrantRoute;

    ModalityInMemoryUserAuthorizationChecker(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
        // Registering the authorization (requests and rules) parsers
        ruleRegistry.addAuthorizationRuleParser(new RoutingAuthorizationRuleParser());
        ruleRegistry.addAuthorizationRuleParser(new OperationAuthorizationRuleParser());
        // Getting initial authorizations from cache if present (only if previous session was logged in as Modality user already)
        if (FXModalityUserPrincipal.getModalityUserPrincipal() != null) {
            Object cachedValue = authorizationPushCashEntry.getValue();
            if (cachedValue != null)
                onAuthorizationPush(cachedValue);
        }
        // Note: the pushObject sent by the server contained all permissions specifically assigned to the user.
        // In addition, we may have public operations. Since they are public, they don't need authorizations, however
        // some may have a route associated, and we need therefore to authorize those public routes.
        EntityStore.create(dataSourceModel)
                .executeQueryWithCache(
                        LocalStorageCache.get().getCacheEntry("cache-authz-operations"),
                    "select grantRoute,guest,public from Operation where grantRoute!=null and (public or guest) and " + (Meta.isBackoffice() ? "backoffice" : "frontoffice"))
                .onFailure(Console::log)
                .onSuccess(this::onPublicOrGuestOperationsWithGrantRouteChanged);
    }

    private void onPublicOrGuestOperationsWithGrantRouteChanged(List<Entity> operations) {
        publicOrGuestOperationsWithGrantRoute = operations;
        onAuthorizationPush(lastPushObject);
    }

    void onAuthorizationPush(Object pushObject) {
        // May happen that it's the same input as last time, and there is no need to recompute everything
        if (Objects.equals(pushObject, lastPushObject) && lastPublicOrGuestOperationsWithGrantRoute == publicOrGuestOperationsWithGrantRoute) {
            // However, we need to fire the event, otherwise the requested page won't be displayed after second login from same user
            if (!FXAuthorizationsReceived.isAuthorizationsReceived())
                FXAuthorizationsChanged.fireAuthorizationsChanged();
            return;
        }
        lastPushObject = pushObject;
        lastPublicOrGuestOperationsWithGrantRoute = publicOrGuestOperationsWithGrantRoute;

        // At this point, it's an actual change in the authorizations
        QueryResult queryResult = (QueryResult) pushObject;
        QueryRowToEntityMapping queryMapping = dataSourceModel.parseAndCompileSelect(AUTHZ_QUERY_BASE).getQueryMapping();
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        EntityList<Entity> assignments = QueryResultToEntitiesMapper.mapQueryResultToEntities(queryResult, queryMapping, entityStore, "assignments");
        clearAllAuthorizationRulesAndGrantAuthorizedRoutesFromPublicOrGuestOperations();
        for (Entity assignment: assignments) {
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
        authorizationPushCashEntry.putValue(pushObject);
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
