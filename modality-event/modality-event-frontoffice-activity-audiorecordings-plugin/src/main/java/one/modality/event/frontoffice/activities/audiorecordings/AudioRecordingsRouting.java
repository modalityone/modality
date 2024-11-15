package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
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
public final class AudioRecordingsRouting {

    private final static String PATH = "/recordings";
    private final static String OPERATION_CODE = "RouteToAudioRecordings";

    public static String getPath() {
        return PATH;
    }

    public static final class AudioRecordingsUiRoute extends UiRouteImpl {

        public AudioRecordingsUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(AudioRecordingsRouting.getPath()
                    , true
                    , AudioRecordingsActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToAudioRecordingsRequest extends RoutePushRequest implements HasOperationCode {

        public RouteToAudioRecordingsRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }
    }

    public static final class RouteToAudioRecordingsRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToAudioRecordingsRequest(context.getHistory());
        }
    }
}
