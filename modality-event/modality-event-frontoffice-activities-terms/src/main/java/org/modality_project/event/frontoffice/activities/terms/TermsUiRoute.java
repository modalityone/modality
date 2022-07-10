package org.modality_project.event.frontoffice.activities.terms;

import org.modality_project.event.frontoffice.activities.terms.routing.TermsRouting;
import dev.webfx.stack.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class TermsUiRoute extends UiRouteImpl {

    public TermsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(TermsRouting.getPath()
                , false
                , TermsActivity::new
                , DomainPresentationActivityContextFinal::new
        );
    }
}
