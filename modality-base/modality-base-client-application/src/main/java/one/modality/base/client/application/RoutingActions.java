package one.modality.base.client.application;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBuilder;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public final class RoutingActions {

    private final static Collection<RouteRequestEmitter> providedEmitters = RouteRequestEmitter.getProvidedEmitters();

    private final static Action invisibleVoidAction = new ActionBuilder().setVisibleProperty(new SimpleBooleanProperty(false)).build();

    public static Collection<Action> filterRoutingActions(UiRouteActivityContext context, OperationActionFactoryMixin mixin, String... sortedPossibleRoutingOperations) {
        return filterRoutingActions(code -> routeOperationCodeToAction(code, context, mixin), sortedPossibleRoutingOperations);
    }

    public static Collection<Action> filterRoutingActions(Function<String, Action> operationCodeToActionFunction, String... sortedPossibleRoutingOperations) {
        return Arrays.stream(sortedPossibleRoutingOperations)
                .map(operationCodeToActionFunction::apply)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Action routeOperationCodeToAction(String operationCode, UiRouteActivityContext context, OperationActionFactoryMixin mixin) {
        Optional<RouteRequestEmitter> routeRequestEmitter = providedEmitters.stream()
                .filter(instantiator -> hasRequestOperationCode(instantiator.instantiateRouteRequest(context), operationCode))
                .findFirst();
        return routeRequestEmitter.isEmpty() ? invisibleVoidAction : mixin.newOperationAction(() -> routeRequestEmitter.get().instantiateRouteRequest(context));
    }

    public static Action getRouteEmitterAction(RouteRequestEmitter routeRequestEmitter, UiRouteActivityContext context, OperationActionFactoryMixin mixin) {
        return mixin.newOperationAction(() -> {
            RouteRequest routeRequest = routeRequestEmitter.instantiateRouteRequest(context);
            if (routeRequest instanceof RoutePushRequest)
                ((RoutePushRequest) routeRequest).setReplace(true);
            return routeRequest;
        });
    }

    public static RouteRequestEmitter findRouteRequestEmitter(String operationCode, UiRouteActivityContext context) {
        return RouteRequestEmitter.getProvidedEmitters().stream()
                .filter(instantiator -> hasRequestOperationCode(instantiator.instantiateRouteRequest(context), operationCode))
                .findFirst().orElse(null);
    }

    private static boolean hasRequestOperationCode(Object request, Object operationCode) {
        return request instanceof HasOperationCode && operationCode.equals(((HasOperationCode) request).getOperationCode());
    }

}
