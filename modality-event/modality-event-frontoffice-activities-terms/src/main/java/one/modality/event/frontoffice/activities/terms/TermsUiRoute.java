package one.modality.event.frontoffice.activities.terms;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

import one.modality.event.frontoffice.activities.terms.routing.TermsRouting;

/**
 * @author Bruno Salmon
 */
public final class TermsUiRoute extends UiRouteImpl {

    public TermsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(
                TermsRouting.getPath(),
                false,
                TermsActivity::new,
                DomainPresentationActivityContextFinal::new);
    }
}
