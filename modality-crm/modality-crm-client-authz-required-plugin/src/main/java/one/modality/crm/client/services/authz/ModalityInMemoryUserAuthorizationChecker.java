package one.modality.crm.client.services.authz;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.authz.client.operation.OperationAuthorizationRuleParser;
import dev.webfx.stack.authz.client.spi.impl.inmemory.InMemoryUserAuthorizationChecker;
import dev.webfx.stack.cache.CacheEntry;
import dev.webfx.stack.cache.client.LocalStorageCache;
import dev.webfx.stack.cache.client.SessionClientCache;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.sqlcompiler.mapping.QueryRowToEntityMapping;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.query_result_to_entities.QueryResultToEntitiesMapper;
import dev.webfx.stack.routing.router.auth.authz.RoutingAuthorizationRuleParser;
import dev.webfx.stack.session.state.client.fx.FXAuthorizationsChanged;
import one.modality.base.client.conf.ModalityClientConfig;

import java.util.List;

/**
 * @author Bruno Salmon
 */
final class ModalityInMemoryUserAuthorizationChecker extends InMemoryUserAuthorizationChecker {

    // TODO: share this constant with the server counterpart (ModalityAuthorizationServerServiceProvider).
    private final static String AUTHZ_QUERY_BASE = "select rule.rule,activityState.route,operation.(code, grantRoute) from AuthorizationAssignment";

    private final DataSourceModel dataSourceModel;
    private final CacheEntry<Object> authorizationPushCashEntry = SessionClientCache.get().getCacheEntry("cache-authz-push");

    ModalityInMemoryUserAuthorizationChecker(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
        // Registering the authorization (requests and rules) parsers
        ruleRegistry.addAuthorizationRuleParser(new RoutingAuthorizationRuleParser());
        ruleRegistry.addAuthorizationRuleParser(new OperationAuthorizationRuleParser());
        // Getting initial authorizations from cache if present
        Object cachedValue = authorizationPushCashEntry.getValue();
        if (cachedValue != null)
            onAuthorizationPush(cachedValue);
        // Note: the pushObject sent by the server contained all permissions specifically assigned to the user.
        // In addition, we may have public operations. Since they are public, they don't need authorizations, however
        // some may have a route associated, and we need therefore to authorize those public routes.
        EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                .executeCachedQuery(
                        LocalStorageCache.get().getCacheEntry("cache-authz-operations"), this::registerOperationRoutes,
                        "select grantRoute from Operation where public and grantRoute!=null and " + (ModalityClientConfig.isBackOffice() ? "backoffice" : "frontoffice"))
                .onFailure(Console::log)
                .onSuccess(this::registerOperationRoutes);
    }

    void onAuthorizationPush(Object pushObject) {
        QueryResult queryResult = (QueryResult) pushObject;
        QueryRowToEntityMapping queryMapping = dataSourceModel.parseAndCompileSelect(AUTHZ_QUERY_BASE).getQueryMapping();
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        EntityList<Entity> assignments = QueryResultToEntitiesMapper.mapQueryResultToEntities(queryResult, queryMapping, entityStore, "assignments");
        ruleRegistry.clear();
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

    private void registerOperationRoutes(List<Entity> operations) {
        // When this method is called, this overrides all previous rules. For example, if the first rules come from the
        // cache, and subsequently the server pushes the most recent authorization rules, this second call overrides all
        // previous rules initially coming from the cache.
        ruleRegistry.clearAllAuthorizationRules();
        operations.forEach(op -> {
            String route = op.getStringFieldValue("grantRoute");
            if (!Strings.isEmpty(route)) {
                ruleRegistry.registerAuthorizationRule("grant route:" + route);
            }
        });
    }
}
