package one.modality.event.backoffice.activities.events;

import one.modality.event.backoffice.activities.events.routing.EventsRouting;
import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.router.util.PathBuilder;

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
