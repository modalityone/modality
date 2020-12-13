package mongoose.backend.activities.statements;

import mongoose.backend.activities.statements.routing.StatementsRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;
import dev.webfx.framework.shared.router.util.PathBuilder;

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
