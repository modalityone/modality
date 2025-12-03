package one.modality.event.backoffice.activities.medias;

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
public final class MediasRouting {

    private final static String PATH = "/medias";
    private final static String OPERATION_CODE = "RouteToMedias";

    public static String getPath() {
        return PATH;
    }

    public static final class MediasUiRoute extends UiRouteImpl {

        public MediasUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(MediasRouting.getPath()
                    , true
                    , MediasActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToMediasRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToMediasRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return MediasI18nKeys.MediasMenu;
        }
    }

    public static final class RouteToMediasRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToMediasRequest(context.getHistory());
        }
    }
}
