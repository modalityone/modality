package one.modality.crm.frontoffice.activities.createaccount;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class CreateAccountRouting {

    private final static String PATH = "/create-account/:token";

    public static String getPath() {
        return PATH;
    }

    public static final class CreateAccountUiRoute extends UiRouteImpl {

        public CreateAccountUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(CreateAccountRouting.getPath())
                    , false
                    , CreateAccountActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

}
