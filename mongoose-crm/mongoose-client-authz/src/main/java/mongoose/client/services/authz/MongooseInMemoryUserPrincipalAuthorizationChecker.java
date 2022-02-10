package mongoose.client.services.authz;

import mongoose.client.services.authn.MongooseUserPrincipal;
import dev.webfx.framework.shared.operation.authz.OperationAuthorizationRuleParser;
import dev.webfx.framework.shared.orm.domainmodel.DataSourceModel;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.router.auth.authz.RoutingAuthorizationRule;
import dev.webfx.framework.shared.router.auth.authz.RoutingAuthorizationRuleParser;
import dev.webfx.framework.shared.services.authz.spi.impl.inmemory.AuthorizationRuleType;
import dev.webfx.framework.shared.services.authz.spi.impl.inmemory.InMemoryUserPrincipalAuthorizationChecker;
import dev.webfx.platform.shared.services.log.Logger;
import dev.webfx.platform.shared.util.Strings;

/**
 * @author Bruno Salmon
 */
final class MongooseInMemoryUserPrincipalAuthorizationChecker extends InMemoryUserPrincipalAuthorizationChecker {

    MongooseInMemoryUserPrincipalAuthorizationChecker(Object userPrincipal, DataSourceModel dataSourceModel) {
        super(userPrincipal);
        // userPrincipal must be a MongooseUserPrincipal
        MongooseUserPrincipal principal = (MongooseUserPrincipal) userPrincipal;
        // Registering the authorization (requests and rules) parsers
        ruleRegistry.addAuthorizationRuleParser(new RoutingAuthorizationRuleParser());
        ruleRegistry.addAuthorizationRuleParser(new OperationAuthorizationRuleParser());
        if (userPrincipal != null)
            setUpInMemoryAsyncRulesLoading(EntityStore.create(dataSourceModel).executeQuery("select rule.rule,activityState.route from AuthorizationAssignment where active and management.user=?", principal.getUserPersonId()), ar -> {
                if (ar.failed())
                    Logger.log(ar.cause());
                else // When successfully loaded, iterating over the assignments
                    for (Entity assignment: ar.result()) {
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
                    }
            });
    }
}
