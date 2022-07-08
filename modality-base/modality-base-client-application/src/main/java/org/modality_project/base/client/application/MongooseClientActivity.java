package org.modality_project.base.client.application;

import org.modality_project.base.client.actions.MongooseActions;
import dev.webfx.framework.client.activity.Activity;
import dev.webfx.framework.client.activity.ActivityContext;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.ViewDomainActivityContext;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.ViewDomainActivityContextMixin;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRouter;
import dev.webfx.platform.shared.util.function.Factory;

/**
 * @author Bruno Salmon
 */
public abstract class MongooseClientActivity
        implements Activity<ViewDomainActivityContext>
        , ViewDomainActivityContextMixin {

    private final String defaultInitialHistoryPath;
    private ViewDomainActivityContext context;

    public MongooseClientActivity(String defaultInitialHistoryPath) {
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
        return MongooseClientContainerActivity::new;
    }

    protected UiRouter setupContainedRouter(UiRouter containedRouter) {
        return containedRouter.registerProvidedUiRoutes();
    }

    @Override
    public void onStart() {
        MongooseActions.registerActions();
        UiRouter uiRouter = getUiRouter();
        uiRouter.setDefaultInitialHistoryPath(defaultInitialHistoryPath);
        uiRouter.start();
    }
}
