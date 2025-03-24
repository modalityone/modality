package one.modality.event.frontoffice.activities.videos;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class Level2EventDaysWithVideoRouting {

    static final String PATH_EVENT_ID_PARAMETER_NAME = "eventId";
    private static final String PATH_EVENT_ID_PARAMETER_TOKEN = ':' + PATH_EVENT_ID_PARAMETER_NAME;
    private static final String PATH_EVENT_WITH_RECORDINGS = "/videos/event/" + PATH_EVENT_ID_PARAMETER_TOKEN;

    public static String getPath() {
        return PATH_EVENT_WITH_RECORDINGS;
    }

    public static String getEventVideosWallPath(Object eventId) {
        return ModalityRoutingUtil.interpolateParamInPath(PATH_EVENT_ID_PARAMETER_TOKEN, Entities.getPrimaryKey(eventId), PATH_EVENT_WITH_RECORDINGS);
    }

    public static String getLivestreamOnlyPath(Object eventId) {
        return ModalityRoutingUtil.interpolateParamInPath(PATH_EVENT_ID_PARAMETER_TOKEN, Entities.getPrimaryKey(eventId), PATH_EVENT_WITH_RECORDINGS);
    }

    public static final class EventVideosWallUiRoute extends UiRouteImpl {

        public EventVideosWallUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(Level2EventDaysWithVideoRouting.getPath()
                , true
                , Level2EventDaysWithVideoActivity::new
                , ViewDomainActivityContextFinal::new
            );
        }
    }

}
