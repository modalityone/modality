package one.modality.ecommerce.backoffice2018.activities.statements;

import one.modality.ecommerce.backoffice2018.activities.statements.routing.StatementsRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.router.util.PathBuilder;

/**
 * @author Bruno Salmon
 */
public final class StatementsUiRoute extends UiRouteImpl {

    public StatementsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(StatementsRouting.getPath())
                , false
                , StatementsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
