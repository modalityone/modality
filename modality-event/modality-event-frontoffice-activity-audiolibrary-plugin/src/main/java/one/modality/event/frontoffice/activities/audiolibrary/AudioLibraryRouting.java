package one.modality.event.frontoffice.activities.audiolibrary;

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
public final class AudioLibraryRouting {

    private final static String PATH = "/audio-library";
    private final static String OPERATION_CODE = "RouteToAudioLibrary";

    public static String getPath() {
        return PATH;
    }

    public static final class AudioLibraryUiRoute extends UiRouteImpl {

        public AudioLibraryUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(AudioLibraryRouting.getPath()
                    , true
                    , AudioLibraryActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToAudioLibraryRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToAudioLibraryRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return AudioLibraryI18nKeys.AudioLibraryMenu;
        }
    }

    public static final class RouteToAudioLibraryRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToAudioLibraryRequest(context.getHistory());
        }
    }
}
