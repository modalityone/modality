package one.modality.event.frontoffice.activities.videos;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class Level3SessionVideoPlayerRouting {

    static final String SCHEDULED_VIDEO_ITEM_ID_PARAMETER_NAME = "scheduledVideoItemId";
    private static final String SCHEDULED_VIDEO_ITEM_ID_PARAMETER_TOKEN = ':' + SCHEDULED_VIDEO_ITEM_ID_PARAMETER_NAME;
    private static final String PATH = "/videos/session/" + SCHEDULED_VIDEO_ITEM_ID_PARAMETER_TOKEN;

    public static String getPath() {
        return PATH;
    }

    public static String getVideoOfSessionPath(Object videoScheduledItemId) {
        return ModalityRoutingUtil.interpolateParamInPath(SCHEDULED_VIDEO_ITEM_ID_PARAMETER_TOKEN, Entities.getPrimaryKey(videoScheduledItemId), PATH);
    }

    public static final class VideOfSessionUiRoute extends UiRouteImpl {

        public VideOfSessionUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(Level3SessionVideoPlayerRouting.getPath()
                , true
                , Level3SessionVideoPlayerActivity::new
                , ViewDomainActivityContextFinal::new
            );
        }
    }

}
