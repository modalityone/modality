package org.modality_project.event.backoffice.activities.events;

import org.modality_project.event.backoffice.activities.events.routing.EventsRouting;
import dev.webfx.stack.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.framework.shared.router.util.PathBuilder;

/**
 * @author Bruno Salmon
 */
public final class EventsUiRoute extends UiRouteImpl {

    public EventsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(
                PathBuilder.toRegexPath(EventsRouting.getAnyPath())
                , false
                , EventsActivity::new
                , DomainPresentationActivityContextFinal::new
        );
    }
}
