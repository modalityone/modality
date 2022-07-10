package org.modality_project.catering.backoffice.activities.diningareas;

import org.modality_project.catering.backoffice.activities.diningareas.routing.DiningAreasRouting;
import dev.webfx.stack.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.framework.shared.router.util.PathBuilder;

/**
 * @author Bruno Salmon
 */
public final class DiningAreasUiRoute extends UiRouteImpl {

    public DiningAreasUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(DiningAreasRouting.getPath())
                , false
                , DiningAreasActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
