package one.modality.crm.client.services.authz;

import dev.webfx.platform.util.Strings;
import dev.webfx.stack.authz.client.operation.OperationAuthorizationRuleParser;
import dev.webfx.stack.authz.client.spi.impl.inmemory.AuthorizationRuleType;
import dev.webfx.stack.authz.client.spi.impl.inmemory.InMemoryUserAuthorizationChecker;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.sqlcompiler.mapping.QueryRowToEntityMapping;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.query_result_to_entities.QueryResultToEntitiesMapper;
import dev.webfx.stack.routing.router.auth.authz.RoutingAuthorizationRule;
import dev.webfx.stack.routing.router.auth.authz.RoutingAuthorizationRuleParser;
import dev.webfx.stack.session.state.client.fx.FXAuthorizationsChanged;

/**
 * @author Bruno Salmon
 */
final class ModalityInMemoryUserAuthorizationChecker extends InMemoryUserAuthorizationChecker {

    // TODO: share this constant with the server counterpart.
    private final static String AUTHZ_QUERY_BASE = "select rule.rule,activityState.route,operation.operationCode from AuthorizationAssignment";

    private final DataSourceModel dataSourceModel;
    //private final Promise<Void> promise = Promise.promise();

    ModalityInMemoryUserAuthorizationChecker(DataSourceModel dataSourceModel) {
        super();
        this.dataSourceModel = dataSourceModel;
        // Registering the authorization (requests and rules) parsers
        ruleRegistry.addAuthorizationRuleParser(new RoutingAuthorizationRuleParser());
        ruleRegistry.addAuthorizationRuleParser(new OperationAuthorizationRuleParser());
    }

    void onAuthorizationPush(Object pushObject) {
        QueryResult queryResult = (QueryResult) pushObject;
        QueryRowToEntityMapping queryMapping = dataSourceModel.parseAndCompileSelect(AUTHZ_QUERY_BASE).getQueryMapping();
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        EntityList<Entity> assignments = QueryResultToEntitiesMapper.mapQueryResultToEntities(queryResult, queryMapping, entityStore, "assignments");
        ruleRegistry.clear();
        for (Entity assignment: assignments) {
            // If it is an authorization rule assignment, registering it
            Entity authorizationRule = assignment.getForeignEntity("rule");
            if (authorizationRule != null) // if yes, passing the rule as a string (will be parsed)
                ruleRegistry.registerAuthorizationRule(authorizationRule.getStringFieldValue("rule"));
            // If it is a shared activity state, automatically granting the route to it (when provided)
            Entity activityState = assignment.getForeignEntity("activityState");
            if (activityState != null) {
                String route = activityState.getStringFieldValue("route");
                if (route != null) {
                    route = Strings.replaceAll(route, "[id]", activityState.getPrimaryKey());
                    ruleRegistry.registerAuthorizationRule(new RoutingAuthorizationRule(AuthorizationRuleType.GRANT, route, false));
                }
            }
            Entity operation = assignment.getForeignEntity("operation");
            if (operation != null)
                ruleRegistry.registerAuthorizationRule("grant operation:" + operation.getStringFieldValue("operationCode"));
        }
        FXAuthorizationsChanged.fireAuthorizationsChanged();
    }
}
