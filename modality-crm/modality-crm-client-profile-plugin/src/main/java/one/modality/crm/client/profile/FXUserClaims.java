package one.modality.crm.client.profile;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.UserClaims;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * @author Bruno Salmon
 */
public final class FXUserClaims {

    private final static ObjectProperty<UserClaims> userClaimsProperty = new SimpleObjectProperty<>();

    static {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            setUserClaims(null);
            AuthenticationService.getUserClaims()
                    .onFailure(Console::log)
                    .onSuccess(userClaims -> UiScheduler.runInUiThread(() -> setUserClaims(userClaims)));
        }, FXUserId.userIdProperty());
    }

    public static UserClaims getUserClaims() {
        return userClaimsProperty.get();
    }

    public static ObjectProperty<UserClaims> userClaimsProperty() {
        return userClaimsProperty;
    }

    public static void setUserClaims(UserClaims userClaims) {
        userClaimsProperty.set(userClaims);
    }
}
