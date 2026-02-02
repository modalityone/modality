package one.modality.base.client.error;

import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.Error;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

/**
 * @author Bruno Salmon
 */
public final class ErrorReporter {

    public static void reportError(Throwable throwable) {
        reportError(throwable.getMessage());
    }

    public static void reportError(String message) {
        UpdateStore updateStore = UpdateStore.create();
        Error error = updateStore.insertEntity(Error.class);
        error.setMessage(message);
        error.setUserPerson(FXUserPersonId.getUserPersonId());
        error.setUserAgent(UserAgent.getUserAgentName());
        error.setRoute(WindowLocation.getPath());
        updateStore.submitChanges();
    }

}
