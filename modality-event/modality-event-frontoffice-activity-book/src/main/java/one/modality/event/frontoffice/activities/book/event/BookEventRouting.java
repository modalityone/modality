package one.modality.event.frontoffice.activities.book.event;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class BookEventRouting {

    private final static String PATH = "/booking/event/:eventId";

    public static String getPath() {
        return PATH;
    }

    public static String getBookEventPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

    public static final class BookEventUiRoute extends UiRouteImpl {

        public BookEventUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(BookEventRouting.getPath()
                    , false
                    , BookEventActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }

    }
}
