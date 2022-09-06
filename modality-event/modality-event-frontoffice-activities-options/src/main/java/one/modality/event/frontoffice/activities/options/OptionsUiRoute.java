package one.modality.event.frontoffice.activities.options;

import one.modality.event.frontoffice.activities.options.routing.OptionsRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class OptionsUiRoute extends UiRouteImpl {

    public OptionsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(OptionsRouting.getPath()
                , false
                , OptionsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
