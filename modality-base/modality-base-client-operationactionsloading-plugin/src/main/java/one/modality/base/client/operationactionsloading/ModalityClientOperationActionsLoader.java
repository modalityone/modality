package one.modality.base.client.operationactionsloading;

import dev.webfx.platform.boot.spi.ApplicationModuleBooter;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.ConfigLoader;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.stack.authz.client.factory.AuthorizationFactory;
import dev.webfx.stack.cache.client.SessionClientCache;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionFactoryMixin;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import dev.webfx.stack.ui.operation.action.OperationActionRegistry;
import one.modality.base.client.conf.ModalityClientConfig;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public class ModalityClientOperationActionsLoader implements ApplicationModuleBooter,
        OperationActionFactoryMixin,
        ActionFactoryMixin {

    private final static String CONFIG_PATH = "modality.base.client.operationactionsloading";

    private boolean hideUnauthorizedRouteOperationActions;
    private boolean hideUnauthorizedOtherOperationActions;


    @Override
    public String getModuleName() {
        return "modality-base-client-operationactionsloading";
    }

    @Override
    public int getBootLevel() {
        return COMMUNICATION_ALL_BOOT_LEVEL;
    }

    @Override
    public void bootModule() {
        Config config = ConfigLoader.getRootConfig().childConfigAt(CONFIG_PATH);
        hideUnauthorizedRouteOperationActions = config.getBoolean("hideUnauthorizedRouteOperationActions", false);
        hideUnauthorizedOtherOperationActions = config.getBoolean("hideUnauthorizedOtherOperationActions", true);

        EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                .executeCachedQuery(SessionClientCache.get().getCacheEntry("clientOperationsCache"), this::registerOperations,
                        "select code,i18nCode,public from Operation where " + (ModalityClientConfig.isBackOffice() ? "backoffice" : "frontoffice"))
                .onSuccess(this::registerOperations)
                .onFailure(cause -> {
                    Console.log("Failed loading operations", cause);
                    // Schedule a retry, as the client won't work anyway without a successful load
                    Scheduler.scheduleDeferred(this::bootModule);
                });
    }

    private void registerOperations(List<Entity> operations) {
        OperationActionRegistry registry = getOperationActionRegistry();
        // Registering graphical properties for all loaded operations
        for (Entity operation : operations) {
            String operationCode = (String) operation.evaluate("code");
            String i18nCode = operation.getStringFieldValue("i18nCode");
            boolean isPublic = operation.getBooleanFieldValue("public");
            Object i18nKey = new ModalityOperationI18nKey(i18nCode, operationCode);
            Action operationGraphicalAction;
            if (isPublic) {
                operationGraphicalAction = newAction(i18nKey);
            } else {
                boolean isRoute = operationCode.startsWith("RouteTo");
                boolean hideUnauthorizedAction = isRoute ? hideUnauthorizedRouteOperationActions : hideUnauthorizedOtherOperationActions;
                operationGraphicalAction = newAuthAction(
                        i18nKey,
                        registry.authorizedOperationActionProperty(operationCode, AuthorizationFactory::isAuthorized),
                        hideUnauthorizedAction);
            }
            operationGraphicalAction.setUserData(i18nKey);
            registry.registerOperationGraphicalAction(operationCode, operationGraphicalAction);
        }
        // Telling the registry how to update the graphical properties when needed (ex: ToggleCancel actions
        // text needs to be updated to say 'Cancel' or 'Uncancel' on selection change)
        registry.setOperationActionGraphicalPropertiesUpdater(operationAction -> {
            // Actually since text and graphic properties come from I18n, we just need to inform it about the
            // change, and it will refresh all translations, including therefore these graphical properties.
            // The possible expressions used by operations like ToggleCancel will be recomputed through this
            // refresh thanks to the I18n evaluation system.
            // Instantiating an operation request just to have the request class or operation code
            Object operationRequest = registry.newOperationActionRequest(operationAction);
            // Then getting the graphical action from it
            Action graphicalAction = registry.getGraphicalActionFromOperationRequest(operationRequest);
            if (graphicalAction != null) {
                Object i18nKey = graphicalAction.getUserData();
                if (i18nKey instanceof ModalityOperationI18nKey)
                    ((ModalityOperationI18nKey) i18nKey).setOperationRequest(operationRequest);
                I18n.refreshMessageTokenProperties(i18nKey);
            }
        });
    }

}
