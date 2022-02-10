package mongoose.backoffice.activities.cloneevent;

import mongoose.backoffice.activities.cloneevent.routing.CloneEventRouting;
import dev.webfx.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

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
