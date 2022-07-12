package org.modality_project.base.client.application;

import org.modality_project.base.client.actions.ModalityActions;
import dev.webfx.stack.routing.activity.Activity;
import dev.webfx.stack.routing.activity.ActivityContext;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.ViewDomainActivityContext;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.ViewDomainActivityContextMixin;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRouter;
import dev.webfx.platform.util.function.Factory;

/**
 * @author Bruno Salmon
 */
public abstract class ModalityClientActivity
        implements Activity<ViewDomainActivityContext>
        , ViewDomainActivityContextMixin {

    private final String defaultInitialHistoryPath;
    private ViewDomainActivityContext context;

    public ModalityClientActivity(String defaultInitialHistoryPath) {
        this.defaultInitialHistoryPath = defaultInitialHistoryPath;
    }

    @Override
    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    public void onCreate(ViewDomainActivityContext context) {
        this.context = context;
        getUiRouter().routeAndMount("/", getContainerActivityFactory(), setupContainedRouter(UiRouter.createSubRouter(context)));
    }

    protected Factory<Activity<ViewDomainActivityContextFinal>> getContainerActivityFactory() {
        return ModalityClientContainerActivity::new;
    }

    protected UiRouter setupContainedRouter(UiRouter containedRouter) {
        return containedRouter.registerProvidedUiRoutes();
    }

    @Override
    public void onStart() {
        ModalityActions.registerActions();
        UiRouter uiRouter = getUiRouter();
        uiRouter.setDefaultInitialHistoryPath(defaultInitialHistoryPath);
        uiRouter.start();
    }
}
