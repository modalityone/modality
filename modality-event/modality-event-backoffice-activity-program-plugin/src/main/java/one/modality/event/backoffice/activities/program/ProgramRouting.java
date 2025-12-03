package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

/**
 * Routing configuration for the Program activity.
 * This class defines the routing infrastructure for the Program module, including:
 * <ul>
 *   <li>URL path mapping ("/program")</li>
 *   <li>Route configuration and registration</li>
 *   <li>Navigation requests for routing to this activity</li>
 * </ul>
 *
 * <p><b>Route Path:</b> /program
 *
 * <p><b>Usage:</b>
 * The routing is automatically registered via Java service providers (SPI).
 * Navigation to this activity can be triggered by:
 * <ul>
 *   <li>Direct URL navigation: /program</li>
 *   <li>Programmatic navigation: new RouteToProgramRequest(history).execute()</li>
 *   <li>Menu/button actions linked to the "RouteToProgram" operation</li>
 * </ul>
 *
 * <p><b>Service Registration:</b>
 * The route and emitter are registered via META-INF/services files:
 * <ul>
 *   <li>dev.webfx.stack.routing.uirouter.UiRoute</li>
 *   <li>dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter</li>
 * </ul>
 *
 * @author Bruno Salmon
 *
 * @see ProgramActivity
 * @see ProgramUiRoute
 * @see RouteToProgramRequest
 */
public class ProgramRouting {

    /**
     * The URL path for the Program activity.
     * This is the path users navigate to in order to access the program management interface.
     */
    private final static String PATH = "/program";

    /**
     * Operation code for routing to the Program activity.
     * Used by the authorization system and operation framework to identify this navigation action.
     */
    private final static String OPERATION_CODE = "RouteToProgram";

    /**
     * Returns the URL path for the Program activity.
     *
     * @return The path string "/program"
     */
    public static String getPath() {
        return PATH;
    }

    /**
     * UI Route implementation for the Program activity.
     * This class registers the route configuration with the routing system, mapping
     * the "/program" path to the {@link ProgramActivity}.
     *
     * <p>The route is registered via Java SPI in:
     * META-INF/services/dev.webfx.stack.routing.uirouter.UiRoute
     */
    public static final class ProgramUiRoute extends UiRouteImpl {

        /**
         * Constructs a new ProgramUiRoute by delegating to the static route configuration.
         */
        public ProgramUiRoute() {
            super(uiRoute());
        }

        /**
         * Creates the route configuration for the Program activity.
         *
         * @return UiRoute configured with:
         *         <ul>
         *           <li>Path: /program</li>
         *           <li>Authentication required: true</li>
         *           <li>Activity factory: ProgramActivity::new</li>
         *           <li>Context factory: ViewDomainActivityContextFinal::new</li>
         *         </ul>
         */
        public static UiRoute<?> uiRoute() {
            return UiRoute.create(ProgramRouting.getPath()
                    , true  // Requires authentication
                    , ProgramActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    /**
     * Route request for navigating to the Program activity.
     * This request can be executed to navigate the user to the Program activity.
     * It implements {@link HasOperationCode} for authorization checks and
     * {@link HasI18nKey} for localized display in UI elements (menus, buttons).
     *
     * <p>Example usage:
     * <pre>
     * new RouteToProgramRequest(history).execute();
     * </pre>
     */
    public static final class RouteToProgramRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        /**
         * Constructs a new route request to navigate to the Program activity.
         *
         * @param browsingHistory The browser history for navigation
         */
        public RouteToProgramRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        /**
         * Returns the operation code for this route request.
         * Used by the authorization system to check if the user has permission
         * to access the Program activity.
         *
         * @return The operation code "RouteToProgram"
         */
        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        /**
         * Returns the i18n key for displaying this route in the UI.
         * Used for menu items, buttons, and other UI elements that navigate
         * to the Program activity. The key resolves to localized text like
         * "Program" in English or "Programme" in French.
         *
         * @return The i18n key from BackOfficeHomeI18nKeys.Program
         */
        @Override
        public Object getI18nKey() {
            return ProgramI18nKeys.ProgramMenu;
        }
    }

    /**
     * Route request emitter for the Program activity.
     * This emitter is used by the routing framework to create route requests
     * based on the current activity context. It's registered via Java SPI in:
     * META-INF/services/dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter
     *
     * <p>The emitter allows the framework to automatically create navigation
     * requests when needed (e.g., for menu generation or programmatic navigation).
     */
    public static final class RouteToProgramRequestEmitter implements RouteRequestEmitter {

        /**
         * Creates a new route request instance for navigating to the Program activity.
         *
         * @param context The current UI route activity context
         * @return A new RouteToProgramRequest configured with the context's browsing history
         */
        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToProgramRequest(context.getHistory());
        }
    }
}
