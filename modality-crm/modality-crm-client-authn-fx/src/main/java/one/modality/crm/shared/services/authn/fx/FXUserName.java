package one.modality.crm.shared.services.authn.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Objects;
import dev.webfx.stack.authn.UserClaims;
import dev.webfx.stack.session.state.client.fx.FXUserClaims;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import one.modality.base.shared.entities.Person;

/**
 * @author Bruno Salmon
 */
public final class FXUserName {

    private final static StringProperty userNameProperty = new SimpleStringProperty();

    public static String getUserName() {
        return userNameProperty.get();
    }

    public static ReadOnlyStringProperty userNameProperty() {
        return userNameProperty;
    }

    private final static StringProperty userInitialsProperty = new SimpleStringProperty();

    public static String getUserInitials() {
        return userInitialsProperty.get();
    }

    public static ReadOnlyStringProperty userInitialsProperty() {
        return userInitialsProperty;
    }

    static {
        // TODO: see if we can optimize this code to not systematically use FXUserClaims (which causes a server call)
        //  if userPerson is already present. Also see if we should add FXModalityGuestPrincipal (to create)
        FXProperties.runNowAndOnPropertiesChange(() -> {
            String username = null;
            Person userPerson = FXUserPerson.getUserPerson();
            if (userPerson != null) {
                username = userPerson.getFullName();
            } else {
                UserClaims userClaims = FXUserClaims.getUserClaims();
                if (userClaims != null)
                    username = Objects.coalesce(userClaims.getUsername(), userClaims.getEmail(), userClaims.getPhone());
            }
            userNameProperty.set(username);
            userInitialsProperty.set(username != null ? getInitials(username) : null);
        }, FXUserPerson.userPersonProperty(), FXUserClaims.userClaimsProperty());
    }

    private static String getInitials(String name) {
        StringBuilder sb = new StringBuilder();
        for (String text : name.split(" ")) {
             sb.append(text.toUpperCase().charAt(0));
        }
        return sb.toString();
    }

}
