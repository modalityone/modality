package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.extras.operation.HasOperationCode;

/**
 * @author Bruno Salmon
 */
public final class VideoStreamingRouting {

    private final static String PATH = "/video-streaming";
    private final static String OPERATION_CODE = "RouteToVideoStreaming";

    public static String getPath() {
        return PATH;
    }

    public static final class VideoStreamingUiRoute extends UiRouteImpl {

        public VideoStreamingUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(VideoStreamingRouting.getPath()
                    , true
                    , VideoStreamingActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToVideoStreamingRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToVideoStreamingRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return VideoStreamingI18nKeys.VideoStreamingMenu;
        }
    }

    public static final class RouteToVideoStreamingRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToVideoStreamingRequest(context.getHistory());
        }
    }
}
