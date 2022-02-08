package mongoose.frontoffice.activities.terms;

import mongoose.frontoffice.activities.terms.routing.TermsRouting;
import dev.webfx.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

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
