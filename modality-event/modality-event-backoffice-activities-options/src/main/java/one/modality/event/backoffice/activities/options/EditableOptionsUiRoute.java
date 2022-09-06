package one.modality.event.backoffice.activities.options;

import one.modality.event.frontoffice.activities.options.routing.OptionsRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class EditableOptionsUiRoute extends UiRouteImpl<ViewDomainActivityContextFinal> {

    public EditableOptionsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<ViewDomainActivityContextFinal> uiRoute() {
        return UiRoute.create(OptionsRouting.getPath()
                , false
                , EditableOptionsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }

}
