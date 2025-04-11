package one.modality.event.frontoffice.activities.videos;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class Page3LivestreamPlayerRouting {

    static final String EVENT_ID_PARAMETER_NAME = "eventId";
    private static final String EVENT_ID_PARAMETER_TOKEN = ':' + EVENT_ID_PARAMETER_NAME;
    //We need to add : after /livestream/ so the server understand he needs to evaluate this token
    private static final String PATH = "/livestream/" + EVENT_ID_PARAMETER_TOKEN;

    public static String getPath() {
        return PATH;
    }

    public static String getLivestreamPath(Object eventId) {
        return ModalityRoutingUtil.interpolateParamInPath(EVENT_ID_PARAMETER_TOKEN, Entities.getPrimaryKey(eventId), PATH);
    }

    public static final class LivestreamUiRoute extends UiRouteImpl {

        public LivestreamUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(Page3LivestreamPlayerRouting.getPath()
                , true
                , Page3LivestreamPlayerActivity::new
                , ViewDomainActivityContextFinal::new
            );
        }
    }

}
