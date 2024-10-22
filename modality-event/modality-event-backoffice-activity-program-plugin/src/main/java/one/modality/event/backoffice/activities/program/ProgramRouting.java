package one.modality.event.backoffice.activities.program;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.operation.HasOperationCode;

/**
 * @author Bruno Salmon
 */
public class ProgramRouting {

    private final static String PATH = "/program";
    private final static String OPERATION_CODE = "RouteToProgram";

    public static String getPath() {
        return PATH;
    }

    public static final class ProgramUiRoute extends UiRouteImpl {

        public ProgramUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(ProgramRouting.getPath()
                    , true
                    , ProgramActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToProgramRequest extends RoutePushRequest implements HasOperationCode {

        public RouteToProgramRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }
    }

    public static final class RouteToProgramRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToProgramRequest(context.getHistory());
        }
    }
}
