package mongoose.event.frontoffice.activities.program;

import mongoose.event.frontoffice.activities.program.routing.ProgramRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class ProgramUiRoute extends UiRouteImpl {

    public ProgramUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(ProgramRouting.getPath()
                , false
                , ProgramActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
