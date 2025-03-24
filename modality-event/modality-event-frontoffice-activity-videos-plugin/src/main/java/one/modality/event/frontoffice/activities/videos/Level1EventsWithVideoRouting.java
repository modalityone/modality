package one.modality.event.frontoffice.activities.videos;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.operation.HasOperationCode;

/**
 * @author Bruno Salmon
 */
public final class Level1EventsWithVideoRouting {

    private final static String PATH = "/videos";
    private final static String OPERATION_CODE = "RouteToVideos";

    public static String getPath() {
        return PATH;
    }

    public static final class VideosUiRoute extends UiRouteImpl {

        public VideosUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(Level1EventsWithVideoRouting.getPath()
                    , true
                    , Level1EventsWithVideoActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToVideosRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToVideosRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return VideosI18nKeys.VideosMenu;
        }
    }

    public static final class RouteToVideosRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToVideosRequest(context.getHistory());
        }
    }
}
