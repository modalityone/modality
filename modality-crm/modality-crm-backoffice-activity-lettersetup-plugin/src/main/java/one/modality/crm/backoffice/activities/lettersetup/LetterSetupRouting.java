package one.modality.crm.backoffice.activities.lettersetup;

import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

/**
 * @author Bruno Salmon
 */
public final class LetterSetupRouting {

    private final static String PATH = "/letter-setup";
    private final static String OPERATION_CODE = "RouteToLetterSetup";
    public static String getPath() {
        return PATH;
    }

    public static final class LetterSetupUiRoute extends UiRouteImpl {

        public LetterSetupUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(LetterSetupRouting.getPath()
                    , true
                    , LetterSetupActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToLetterSetupRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToLetterSetupRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return LetterSetupI18nKeys.LetterSetupTile;
        }
    }

    public static class RouteToLetterSetupRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToLetterSetupRequest(context.getHistory());
        }
    }
}
