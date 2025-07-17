package one.modality.event.frontoffice.activities.book.account;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public class CheckoutAccountRouting {

    private final static String PATH = "/booking/account";

    public static String getPath() {
        return PATH;
    }

    /**
     * @author Bruno Salmon
     */
    public static class CheckoutAccountUiRoute extends UiRouteImpl {

        public CheckoutAccountUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create("/" // only path in the sub-router (under /booking/account)
                    , true
                    , CheckoutAccountActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }
}
