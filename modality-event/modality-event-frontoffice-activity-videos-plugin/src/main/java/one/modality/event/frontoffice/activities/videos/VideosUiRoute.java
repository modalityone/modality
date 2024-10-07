package one.modality.event.frontoffice.activities.videos;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.frontoffice.activities.videos.routing.VideosRouting;

public final class VideosUiRoute extends UiRouteImpl {

    public VideosUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(VideosRouting.getPath()
                , false
                , VideosActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
