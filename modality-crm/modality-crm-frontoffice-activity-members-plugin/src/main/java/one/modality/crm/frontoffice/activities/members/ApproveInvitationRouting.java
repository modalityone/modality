package one.modality.crm.frontoffice.activities.members;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.router.util.PathBuilder;

/**
 * Routing for the approve invitation activity
 * Route: /members/approve/:token
 *
 * @author David Hello
 */
public final class ApproveInvitationRouting {

    private static final String PATH = "/members/approve/:token";
    static final String PATH_TOKEN_PARAMETER_NAME = "token";

    public static String getPath() {
        return PATH;
    }

    public static final class ApproveInvitationUiRoute extends UiRouteImpl {

        public ApproveInvitationUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(ApproveInvitationRouting.getPath())
                    , false // No authentication required - token-based access
                    , ApproveInvitationActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }
}
