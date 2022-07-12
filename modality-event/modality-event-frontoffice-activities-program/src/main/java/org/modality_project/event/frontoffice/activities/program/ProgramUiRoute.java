package org.modality_project.event.frontoffice.activities.program;

import org.modality_project.event.frontoffice.activities.program.routing.ProgramRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

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
