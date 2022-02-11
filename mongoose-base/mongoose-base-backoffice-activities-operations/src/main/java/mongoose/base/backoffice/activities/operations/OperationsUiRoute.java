package mongoose.base.backoffice.activities.operations;

import mongoose.base.backoffice.activities.operations.routing.OperationsRouting;
import dev.webfx.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class OperationsUiRoute extends UiRouteImpl {

    public OperationsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(OperationsRouting.getPath()
                , false
                , OperationsActivity::new
                , DomainPresentationActivityContextFinal::new
        );
    }
}
