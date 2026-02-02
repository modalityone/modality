package one.modality.event.frontoffice.activities.audiolibrary;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class EventAudioLibraryRouting {

    static final String PATH_EVENT_ID_PARAMETER_NAME = "eventId";
    static final String PATH_ITEM_CODE_PARAMETER_NAME = "code";
    private static final String PATH_EVENT_ID_PARAMETER_TOKEN = ':' + PATH_EVENT_ID_PARAMETER_NAME;
    private static final String PATH_ITEM_CODE_PARAMETER_TOKEN = ':' + PATH_ITEM_CODE_PARAMETER_NAME;
    private static final String PATH = "/audio-library/" + PATH_EVENT_ID_PARAMETER_TOKEN + "/" + PATH_ITEM_CODE_PARAMETER_TOKEN;

    public static String getPath() {
        return PATH;
    }

    public static String getEventRecordingsPlaylistPath(Object eventId, String itemCode) {
        String path = ModalityRoutingUtil.interpolateParamInPath(PATH_EVENT_ID_PARAMETER_TOKEN, Entities.getPrimaryKey(eventId), PATH);
        path = ModalityRoutingUtil.interpolateParamInPath(PATH_ITEM_CODE_PARAMETER_TOKEN, itemCode, path);
        return path;
    }

    public static final class EventAudioLibraryUiRoute extends UiRouteImpl {

        public EventAudioLibraryUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(EventAudioLibraryRouting.getPath()
                , true
                , EventAudioLibraryActivity::new
                , ViewDomainActivityContextFinal::new
            );
        }
    }

}
