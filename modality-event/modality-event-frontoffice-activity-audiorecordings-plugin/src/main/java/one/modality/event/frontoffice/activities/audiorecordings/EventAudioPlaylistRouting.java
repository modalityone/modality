package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class EventAudioPlaylistRouting {

    static final String PATH_EVENT_ID_PARAMETER_NAME = "eventId";
    static final String PATH_ITEM_CODE_PARAMETER_NAME = "code";
    private static final String PATH_EVENT_ID_PARAMETER_TOKEN = ':' + PATH_EVENT_ID_PARAMETER_NAME;
    private static final String PATH_ITEM_CODE_PARAMETER_TOKEN = ':' + PATH_ITEM_CODE_PARAMETER_NAME;
    private static final String PATH = "/recordings/" + PATH_EVENT_ID_PARAMETER_TOKEN + "/" + PATH_ITEM_CODE_PARAMETER_TOKEN;

    public static String getPath() {
        return PATH;
    }

    public static String getEventRecordingsPlaylistPath(Object eventId, String itemCode) {
        String path = ModalityRoutingUtil.interpolateParamInPath(PATH_EVENT_ID_PARAMETER_TOKEN, Entities.getPrimaryKey(eventId), PATH);
        path = ModalityRoutingUtil.interpolateParamInPath(PATH_ITEM_CODE_PARAMETER_TOKEN, itemCode, path);
        return path;
    }

    public static final class RecordingsOfEventUiRoute extends UiRouteImpl {

        public RecordingsOfEventUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(EventAudioPlaylistRouting.getPath()
                , true
                , EventAudioPlaylistActivity::new
                , ViewDomainActivityContextFinal::new
            );
        }
    }

}
