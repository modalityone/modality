package one.modality.all.backoffice.activities.event.clone.openjfx;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;

import one.modality.event.backoffice.activities.cloneevent.routing.CloneEventRouting;

/**
 * @author Bruno Salmon
 */
public final class FxCloneEventRouting {

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(
                CloneEventRouting.getPath(),
                false,
                FxCloneEventPresentationActivity::new,
                DomainPresentationActivityContextFinal::new);
    }
}
