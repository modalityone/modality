package one.modality.crm.activities.magiclink;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

public final class MagicLinkRouting {

    static final String PATH_TOKEN_PARAMETER_NAME = "token";

    private final static String PATH = "/magic-link/:" + PATH_TOKEN_PARAMETER_NAME;

    public static String getPath() {
        return PATH;
    }

    public static class MagicLinkUiRoute extends UiRouteImpl {

        public MagicLinkUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(MagicLinkRouting.getPath()
                    , false
                    , MagicLinkActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }
}
