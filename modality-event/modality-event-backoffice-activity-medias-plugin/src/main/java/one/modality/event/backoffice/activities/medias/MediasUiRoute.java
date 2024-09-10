package one.modality.event.backoffice.activities.medias;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.backoffice.activities.medias.routing.MediasRouting;

public final class MediasUiRoute extends UiRouteImpl {

    public MediasUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(MediasRouting.getPath()
                , true
                , MediasActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
