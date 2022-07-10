package org.modality_project.event.frontoffice.activities.program;

import org.modality_project.event.frontoffice.activities.program.routing.ProgramRouting;
import dev.webfx.stack.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.impl.UiRouteImpl;

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
