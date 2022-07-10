package org.modality_project.all.backoffice.activities.event.clone.openjfx;

import org.modality_project.event.backoffice.activities.cloneevent.routing.CloneEventRouting;
import dev.webfx.stack.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;

/**
 * @author Bruno Salmon
 */
public final class FxCloneEventRouting {

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(CloneEventRouting.getPath()
                , false
                , FxCloneEventPresentationActivity::new
                , DomainPresentationActivityContextFinal::new
        );
    }

}
