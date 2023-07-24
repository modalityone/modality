package one.modality.base.client.application;

import dev.webfx.platform.util.function.Factory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.ViewDomainActivityContext;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.ViewDomainActivityContextMixin;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.activity.Activity;
import dev.webfx.stack.routing.activity.ActivityContext;
import dev.webfx.stack.routing.uirouter.UiRouter;
import one.modality.base.client.actions.ModalityActions;

/**
 * @author Bruno Salmon
 */
public abstract class ModalityClientStarterActivity
    implements Activity<ViewDomainActivityContext>, ViewDomainActivityContextMixin {

  private final String defaultInitialHistoryPath;
  private final Factory<Activity<ViewDomainActivityContextFinal>> containerActivityFactory;
  private ViewDomainActivityContext context;

  public ModalityClientStarterActivity(String defaultInitialHistoryPath) {
    this(defaultInitialHistoryPath, ModalityClientFrameContainerActivity::new);
  }

  public ModalityClientStarterActivity(
      String defaultInitialHistoryPath,
      Factory<Activity<ViewDomainActivityContextFinal>> containerActivityFactory) {
    this.defaultInitialHistoryPath = defaultInitialHistoryPath;
    this.containerActivityFactory = containerActivityFactory;
  }

  @Override
  public ActivityContext getActivityContext() {
    return context;
  }

  @Override
  public void onCreate(ViewDomainActivityContext context) {
    this.context = context;
    getUiRouter()
        .routeAndMount(
            "/", containerActivityFactory, setupContainedRouter(UiRouter.createSubRouter(context)));
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
