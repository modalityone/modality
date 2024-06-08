package one.modality.base.client.activities.console;

import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.view.impl.ViewActivityContextFinal;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.client.activities.console.routing.ConsoleRouting;

/**
 * @author Bruno Salmon
 */
public class ConsoleUiRoute extends UiRouteImpl {

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
