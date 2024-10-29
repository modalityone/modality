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
    private static final String PATH_EVENT_ID_PARAMETER_TOKEN = ':' + PATH_EVENT_ID_PARAMETER_NAME;
    private static final String PATH = "/recordings/event/" + PATH_EVENT_ID_PARAMETER_TOKEN;

    public static String getPath() {
        return PATH;
    }

    public static String getEventRecordingsPlaylistPath(Object eventId) {
        return ModalityRoutingUtil.interpolateParamInPath(PATH_EVENT_ID_PARAMETER_TOKEN, Entities.getPrimaryKey(eventId), PATH);
    }

    public static final class RecordingsOfEventUiRoute extends UiRouteImpl {

        public RecordingsOfEventUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(EventAudioPlaylistRouting.getPath()
                , false
                , EventAudioPlaylistActivity::new
                , ViewDomainActivityContextFinal::new
            );
        }
    }

}
