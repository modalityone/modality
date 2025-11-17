package one.modality.crm.client.services.authz;

import dev.webfx.platform.console.Console;
import dev.webfx.stack.authz.client.operation.OperationAuthorizationRuleParser;
import dev.webfx.stack.authz.client.spi.impl.inmemory.InMemoryUserAuthorizationChecker;
import dev.webfx.stack.routing.router.auth.authz.RoutingAuthorizationRuleParser;
import dev.webfx.stack.session.state.LogoutUserId;
import dev.webfx.stack.session.state.client.fx.FXAuthorizationsChanged;
import dev.webfx.stack.session.state.client.fx.FXAuthorizationsReceived;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import dev.webfx.stack.shareddata.cache.CacheEntry;
import dev.webfx.stack.shareddata.cache.serial.SerialCache;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
final class ModalityInMemoryUserAuthorizationChecker extends InMemoryUserAuthorizationChecker {

    private final Object userId;
    private final CacheEntry<Object> authorizationPushObjectCacheEntry = SerialCache.createCacheEntry("modality/crm/authz/pushObject");
    private Object lastPushObject;

    ModalityInMemoryUserAuthorizationChecker(Object userId) {
        this.userId = userId;
        // Registering the authorization (requests and rules) parsers
        ruleRegistry.addAuthorizationRuleParser(new RoutingAuthorizationRuleParser());
        ruleRegistry.addAuthorizationRuleParser(new OperationAuthorizationRuleParser());
        // Getting initial authorizations from cache if present (only if the previous session was logged in as Modality user already)
        if (FXModalityUserPrincipal.getModalityUserPrincipal() != null) {
            authorizationPushObjectCacheEntry.getValue()
                .onSuccess(cachedValue -> {
                    if (cachedValue != null)
                        onAuthorizationPush(cachedValue, false);
                });
        }
    }

    void onAuthorizationPush(Object pushObject, boolean cachePushObject) {
        // May happen that it's the same input as last time, and there is no need to recompute everything
        if (Objects.equals(pushObject, lastPushObject)) {
            // However, we need to fire the event; otherwise the requested page won't be displayed after second login from same user
            if (!FXAuthorizationsReceived.isAuthorizationsReceived())
                FXAuthorizationsChanged.fireAuthorizationsChanged();
            return;
        }

        // A bit hacky, but we try to detect if the pushObject is really for this user, because sometimes there is
        // confusion with the logout user. It looks like the server may still push authorizations for the logout user
        // just after the user logged in for some reason.
        // Firstly, we check that the user is still matching the current user
        Object currentUserId = FXUserId.getUserId();
        boolean userStillMatching = Objects.equals(userId, currentUserId) || LogoutUserId.isLogoutUserIdOrNull(userId) && LogoutUserId.isLogoutUserIdOrNull(currentUserId);
        if (!userStillMatching) {
            return;
        }

        if (pushObject instanceof String authorizationRules) { // Expecting a String containing the authorization rules
            Console.log("Received authorizations for user " + userId + ": " + authorizationRules);
            // Secondly, if there is no assignment, it's likely a push for the logout user, so we ignore it (if this user is
            // not the logout user).
            if (authorizationRules.startsWith("logout") && !LogoutUserId.isLogoutUserIdOrNull(userId) && lastPushObject != null) {
                return;
            }
            ruleRegistry.clearAllAuthorizationRules();
            authorizationRules.lines().forEach(ruleRegistry::registerAuthorizationRule);
            lastPushObject = pushObject;
        }

        FXAuthorizationsChanged.fireAuthorizationsChanged();

        // Caching latest authorizations
        if (cachePushObject)
            authorizationPushObjectCacheEntry.putValue(pushObject);
    }
}
