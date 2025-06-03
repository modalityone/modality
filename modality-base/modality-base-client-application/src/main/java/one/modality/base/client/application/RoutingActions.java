package one.modality.base.client.application;

import dev.webfx.platform.util.Strings;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.platform.windowhistory.spi.BrowsingHistoryLocation;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.action.OperationAction;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.value.ObservableValue;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public final class RoutingActions {

    private final static Map<Object /* operationCode */, RouteRequestEmitter> PROVIDED_OPERATION_CODE_EMITTERS = new HashMap<>();

    public static RouteRequestEmitter findRouteRequestEmitterWithOperationCode(String operationCode, UiRouteActivityContext context) {
        // Initial population
        if (PROVIDED_OPERATION_CODE_EMITTERS.isEmpty()) {
            RouteRequestEmitter.getProvidedEmitters().forEach(emitter -> {
                RouteRequest routeRequest = emitter.instantiateRouteRequest(context);
                if (routeRequest instanceof HasOperationCode) {
                    PROVIDED_OPERATION_CODE_EMITTERS.put(((HasOperationCode) routeRequest).getOperationCode(), emitter);
                }
            });
        }
        return PROVIDED_OPERATION_CODE_EMITTERS.get(operationCode);
    }

    public static Collection<Action> filterRoutingActions(UiRouteActivityContext context, OperationActionFactoryMixin mixin, String... sortedPossibleRoutingOperations) {
        return filterRoutingActions(code -> routeOperationCodeToAction(code, context, mixin), sortedPossibleRoutingOperations);
    }

    public static Collection<Action> filterRoutingActions(Function<String, Action> operationCodeToActionFunction, String... sortedPossibleRoutingOperations) {
        return Arrays.stream(sortedPossibleRoutingOperations)
            .map(operationCodeToActionFunction)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static Action routeOperationCodeToAction(String operationCode, UiRouteActivityContext context, OperationActionFactoryMixin mixin) {
        RouteRequestEmitter routeRequestEmitter = findRouteRequestEmitterWithOperationCode(operationCode, context);
        if (routeRequestEmitter != null)
            return getRouteEmitterAction(routeRequestEmitter, context, mixin);
        // Maybe an unregistered route (such as RouteToConsole if the console activity plugin was not added), or the
        // code refers to an operation that is not a route (ex: Logout).
        return mixin.getOperationActionRegistry().getOrWaitOperationAction(operationCode);
    }

    public static Action getRouteEmitterAction(RouteRequestEmitter routeRequestEmitter, UiRouteActivityContext context, OperationActionFactoryMixin mixin) {
        return mixin.newOperationAction(() -> {
            RouteRequest routeRequest = routeRequestEmitter.instantiateRouteRequest(context);
            /* Commented as this prevents the whole history to work in the browser (after refactoring this class)
               but there was probably a good reason to do this for some specific cases TODO investigate which cases
            if (routeRequest instanceof RoutePushRequest)
                ((RoutePushRequest) routeRequest).setReplace(true);*/
            return routeRequest;
        });
    }

    public static <Rq> OperationAction<Rq, ?> newRoutingAction(Function<BrowsingHistory, Rq> routeRequestFactory, Supplier<BrowsingHistory> historySupplier, OperationActionFactoryMixin mixin, ObservableValue<?>... graphicalDependencies) {
        return mixin.newOperationAction(e -> routeRequestFactory.apply(historySupplier.get()), graphicalDependencies);
    }

    public static <Rq> OperationAction<Rq, ?> newRoutingAction(Function<BrowsingHistory, Rq> routeRequestFactory, UiRouteActivityContext context, OperationActionFactoryMixin mixin, ObservableValue<?>... graphicalDependencies) {
        return newRoutingAction(routeRequestFactory, context::getHistory, mixin, graphicalDependencies);
    }

    public static <Rq, M extends UiRouteActivityContext & OperationActionFactoryMixin> OperationAction<Rq, ?> newRoutingAction(Function<BrowsingHistory, Rq> routeRequestFactory, M mixin, ObservableValue<?>... graphicalDependencies) {
        return newRoutingAction(routeRequestFactory, mixin, mixin, graphicalDependencies);
    }

    public static RoutePushRequest getRoutingActionRoutePushRequest(Action action) {
        if (action instanceof OperationAction<?,?> operationAction) {
            Object req = operationAction.getOperationRequestFactory().apply(null);
            if (req instanceof RoutePushRequest routePushRequest) {
                return routePushRequest;
            }
        }
        return null;
    }

    public static boolean isCurrentRouteMatchingRoutingAction(Action action) {
        RoutePushRequest routePushRequest = getRoutingActionRoutePushRequest(action);
        if (routePushRequest != null) {
            BrowsingHistoryLocation currentLocation = routePushRequest.getHistory().getCurrentLocation();
            String currentPath = currentLocation.getHash();
            if (Strings.isEmpty(currentPath))
                currentPath = currentLocation.getPath();
            else
                currentPath = Strings.removePrefix(currentPath, "#");
            return Strings.startsWith(currentPath, routePushRequest.getRoutePath());
        }
        return false;
    }

}