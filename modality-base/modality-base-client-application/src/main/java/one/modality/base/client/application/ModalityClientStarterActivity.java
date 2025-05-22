package one.modality.base.client.application;

import dev.webfx.platform.boot.ApplicationBooter;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.function.Factory;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.ViewDomainActivityContext;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.ViewDomainActivityContextMixin;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.activity.Activity;
import dev.webfx.stack.routing.activity.ActivityContext;
import dev.webfx.stack.routing.router.Router;
import dev.webfx.stack.routing.uirouter.UiRouter;

/**
 * @author Bruno Salmon
 */
public abstract class ModalityClientStarterActivity
        implements Activity<ViewDomainActivityContext>
        , ViewDomainActivityContextMixin {

    private final String defaultInitialHistoryPath;
    private final Factory<Activity<ViewDomainActivityContextFinal>> containerActivityFactory;
    private ViewDomainActivityContext context;

    public ModalityClientStarterActivity(String defaultInitialHistoryPath) {
        this(defaultInitialHistoryPath, ModalityClientMainFrameActivity::new);
    }

    public ModalityClientStarterActivity(String defaultInitialHistoryPath, Factory<Activity<ViewDomainActivityContextFinal>> containerActivityFactory) {
        this.defaultInitialHistoryPath = Objects.coalesce(Arrays.first(ApplicationBooter.getMainArgs()), defaultInitialHistoryPath);
        this.containerActivityFactory = containerActivityFactory;
    }

    @Override
    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    public void onCreate(ViewDomainActivityContext context) {
        this.context = context;
        // Language routing management (ex: /de/..., /fr/... etc...)
        Router router = getUiRouter().getRouter();
        router.routeWithRegex("/../.*").handler(event -> {
            String path = event.path();
            // Extracting the 2-letters code for the language
            I18n.setLanguage(path.substring(1, 3));
            // Redirecting to the standard path without the language prefix
            getHistory().replace(path.substring(3));
        });
        getUiRouter().routeAndMount("/", containerActivityFactory, setupContainedRouter(UiRouter.createSubRouter(context)));
    }

    protected UiRouter setupContainedRouter(UiRouter containedRouter) {
        return containedRouter.registerProvidedUiRoutes();
    }

    @Override
    public void onStart() {
        //ModalityActions.registerActions();
        UiRouter uiRouter = getUiRouter();
        uiRouter.setDefaultInitialHistoryPath(defaultInitialHistoryPath);
        uiRouter.start();
    }
}
