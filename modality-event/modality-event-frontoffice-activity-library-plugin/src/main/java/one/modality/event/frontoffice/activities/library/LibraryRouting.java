package one.modality.event.frontoffice.activities.library;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.operation.HasOperationCode;

public final class LibraryRouting {

    private final static String PATH = "/library";
    private final static String OPERATION_CODE = "RouteToLibrary";

    public static String getPath() {
        return PATH;
    }

    public static class LibraryUiRoute extends UiRouteImpl {

        public LibraryUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(LibraryRouting.getPath()
                    , true
                    , LibraryActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToLibraryRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToLibraryRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return LibraryI18nKeys.LibraryMenu;
        }
    }

    public static class RouteToLibraryRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToLibraryRequest(context.getHistory());
        }
    }
}
