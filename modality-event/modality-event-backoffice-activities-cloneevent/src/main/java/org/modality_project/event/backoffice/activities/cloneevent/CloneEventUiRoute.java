package org.modality_project.event.backoffice.activities.cloneevent;

import org.modality_project.event.backoffice.activities.cloneevent.routing.CloneEventRouting;
import dev.webfx.stack.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class CloneEventUiRoute extends UiRouteImpl {

    public CloneEventUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(CloneEventRouting.getPath()
                , false
                , CloneEventActivity::new
                , DomainPresentationActivityContextFinal::new
        );
    }
}
