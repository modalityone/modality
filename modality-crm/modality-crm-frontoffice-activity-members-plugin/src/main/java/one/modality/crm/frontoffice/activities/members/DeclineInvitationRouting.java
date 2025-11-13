package one.modality.crm.frontoffice.activities.members;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.router.util.PathBuilder;

/**
 * Routing for the decline invitation activity
 * Route: /members/decline/:token
 *
 * @author David Hello
 */
public final class DeclineInvitationRouting {

    private static final String PATH = "/members/decline/:token";
    static final String PATH_TOKEN_PARAMETER_NAME = "token";

    public static String getPath() {
        return PATH;
    }

    public static final class DeclineInvitationUiRoute extends UiRouteImpl {

        public DeclineInvitationUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(DeclineInvitationRouting.getPath())
                    , false // No authentication required - token-based access
                    , DeclineInvitationActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }
}
