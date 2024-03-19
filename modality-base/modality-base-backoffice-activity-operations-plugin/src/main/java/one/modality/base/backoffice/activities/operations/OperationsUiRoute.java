package one.modality.base.backoffice.activities.operations;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.backoffice.activities.operations.routing.OperationsRouting;

/**
 * @author Bruno Salmon
 */
public final class OperationsUiRoute extends UiRouteImpl {

    public OperationsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(OperationsRouting.getPath()
                , true
                , OperationsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
