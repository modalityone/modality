package one.modality.base.client.activities.console;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.activity.view.impl.ViewActivityContextFinal;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RouteI18nKeys;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.operation.HasOperationCode;

public final class ConsoleRouting {

    private final static String PATH = "/console";
    private final static String OPERATION_CODE = "RouteToConsole";

    public static String getPath() {
        return PATH;
    }

    public static class ConsoleUiRoute extends UiRouteImpl {

        public ConsoleUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(ConsoleRouting.getPath()
                    , false
                    , ConsoleActivity::new
                    , ViewActivityContextFinal::new
            );
        }
    }

    public static class RouteToConsoleRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToConsoleRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return ConsoleI18nKeys.Console;
        }
    }

    public static class RouteToConsoleRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToConsoleRequest(context.getHistory());
        }

    }
}
