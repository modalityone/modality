package org.modality_project.event.backoffice.activities.options;

import org.modality_project.event.frontoffice.activities.options.routing.OptionsRouting;
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
