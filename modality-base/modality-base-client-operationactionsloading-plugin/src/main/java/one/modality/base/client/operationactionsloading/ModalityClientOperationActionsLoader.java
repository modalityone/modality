package one.modality.base.client.operationactionsloading;

import dev.webfx.extras.action.Action;
import dev.webfx.extras.action.ActionFactoryMixin;
import dev.webfx.extras.exceptions.UserCancellationException;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.operation.action.OperationAction;
import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.operation.action.OperationActionRegistry;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.platform.boot.spi.ApplicationModuleBooter;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.ConfigLoader;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.meta.Meta;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.stack.authz.client.binder.AuthorizationBinder;
import dev.webfx.stack.authz.client.factory.AuthorizationFactory;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class ModalityClientOperationActionsLoader implements ApplicationModuleBooter,
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
        hideUnauthorizedRouteOperationActions = config.getBoolean("hideUnauthorizedRouteOperationActions");
        hideUnauthorizedOtherOperationActions = config.getBoolean("hideUnauthorizedOtherOperationActions");

        OperationActionRegistry.setAuthorizer(AuthorizationBinder::authorizedOperationProperty);

        String officeType = Meta.isBackoffice() ? "backoffice" : "frontoffice";
        EntityStore.create()
            .executeQueryWithCache("modality/base/" + officeType + "-operations",
                """
                    select code, i18nCode, public
                        from Operation
                        where officeType
                    """.replace("officeType", officeType))
            .onFailure(cause -> {
                Console.log("Failed loading operations", cause);
                // Schedule a retry, as the client won't work anyway without a successful load
                Scheduler.scheduleDeferred(this::bootModule);
            })
            .onCacheAndOrSuccess(this::registerOperations);
    }

    private void registerOperations(List<Entity> operations) {
        OperationActionRegistry registry = getOperationActionRegistry();
        // Registering graphical properties for all loaded operations
        for (Entity operation : operations) {
            // Extracting all info about the operation from the database
            String operationCode = operation.evaluate("code");
            String i18nCode = operation.getStringFieldValue("i18nCode");
            boolean isPublic = operation.getBooleanFieldValue("public");
            // Note: if an i18nCode is read from the database, it should be considered as the first choice, before the default
            // i18n key provided by the software (via operation request). This is part of the Modality customization
            // features. This will indeed happen here because ModalityOperationI18nKey implements HasDictionaryMessageKey,
            // and getDictionaryMessageKey() will return that passed i18nCode. However, if no i18nCode is read from
            // the database (i.e., i18nCode is null or empty), getDictionaryMessageKey() should return the default i18n
            // key instead. This can't happen immediately because we don't have an operation request instance at this
            // stage to read that default i18n key. We will do it through setOperationActionGraphicalPropertiesUpdater()
            // below. Until this happens, getDictionaryMessageKey() will return null.
            ModalityOperationI18nKey i18nKey = new ModalityOperationI18nKey(i18nCode);
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
            // Actually, since the text and graphic properties come from I18n, we just need to inform it about the
            // change, and it will refresh all translations, including therefore these graphical properties.
            // The possible expressions used by operations like ToggleCancel will be recomputed through this
            // refresh thanks to the I18n evaluation system.
            // Instantiating an operation request just to have the request class or operation code
            Object operationRequest = registry.newOperationActionRequest(operationAction);
            // Then getting the graphical action from it
            Action graphicalAction = registry.getGraphicalActionFromOperationRequest(operationRequest);
            if (graphicalAction != null) {
                Object i18nKey = graphicalAction.getUserData();
                if (i18nKey instanceof ModalityOperationI18nKey) {
                    // Refreshing the operation request instance before asking an I18n refresh.
                    // Note: This call will also set the default i18nKey provided by the software (via operation request)
                    // if no i18nCode was read from the database.
                    ((ModalityOperationI18nKey) i18nKey).setOperationRequest(operationRequest);
                }
                I18n.refreshMessageTokenProperties(i18nKey);
            }
        });
        registry.setLoaded(true);
    }

    static {
        OperationAction.setActionExecutingIconFactory(operationRequest -> {
            // We don't show an executing icon for route requests
            if (operationRequest instanceof RouteRequest) {
                return null;
            }
            return Controls.createSpinner(16);
        });

        OperationAction.setActionExecutedIconFactory((operationRequest, throwable) -> {
            if (operationRequest instanceof RouteRequest) {
                return null;
            }
            Object i18nKey;
            if (throwable == null) {
                i18nKey = ModalityOperationI18nKeys.ExecutedSuccessfullyActionIcon;
            } else if (throwable instanceof UserCancellationException) {
                i18nKey = ModalityOperationI18nKeys.ExecutedCancelledByUserActionIcon;
            } else {
                i18nKey = ModalityOperationI18nKeys.ExecutedErrorActionIcon;
            }
            return I18n.getI18nGraphic(i18nKey);
        });
    }

}
