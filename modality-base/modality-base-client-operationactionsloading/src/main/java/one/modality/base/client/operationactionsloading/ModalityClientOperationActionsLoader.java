package one.modality.base.client.operationactionsloading;

import dev.webfx.platform.boot.spi.ApplicationModuleBooter;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.auth.authz.client.factory.AuthorizationFactory;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.session.state.client.fx.FXUserPrincipal;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionFactoryMixin;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import dev.webfx.stack.ui.operation.action.OperationActionRegistry;

/**
 * @author Bruno Salmon
 */
public class ModalityClientOperationActionsLoader implements ApplicationModuleBooter,
        OperationActionFactoryMixin,
        ActionFactoryMixin {

    @Override
    public String getModuleName() {
        return "modality-base-client-operationactionsloading";
    }

    @Override
    public int getBootLevel() {
        return COMMUNICATION_ANY_BOOT_LEVEL;
    }

    @Override
    public void bootModule() {
        // Temporary load only the operations for the backend (ie back-office) for the demo (because backend and frontend fields are not considered by authz so far)
        EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                .executeQuery("select operationCode,i18nCode,public from Operation where backend")
                .onFailure(cause -> Console.log("Failed loading operations", cause))
                .onSuccess(operations -> {
                    OperationActionRegistry registry = getOperationActionRegistry();
                    // Registering graphical properties for all loaded operations
                    for (Entity operation : operations) {
                        String operationCode = operation.getStringFieldValue("operationCode");
                        String i18nCode = operation.getStringFieldValue("i18nCode");
                        boolean isPublic = operation.getBooleanFieldValue("public");
                        Object i18nKey = new ModalityOperationI18nKey(i18nCode);
                        Action operationGraphicalAction = isPublic ? newAction(i18nKey) : newAuthAction(i18nKey, registry.authorizedOperationActionProperty(operationCode, FXUserPrincipal.userPrincipalProperty(), AuthorizationFactory::isAuthorized));
                        operationGraphicalAction.setUserData(i18nKey);
                        registry.registerOperationGraphicalAction(operationCode, operationGraphicalAction);
                    }
                    // Telling the registry how to update the graphical properties when needed (ex: ToggleCancel actions text
                    // needs to be updated to say 'Cancel' or 'Uncancel' on selection change)
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
                });
    }

}
