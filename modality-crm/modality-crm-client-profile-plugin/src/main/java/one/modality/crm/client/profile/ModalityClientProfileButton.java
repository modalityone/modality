package one.modality.crm.client.profile;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.authn.UserClaims;
import dev.webfx.stack.session.state.client.fx.FXUserClaims;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import one.modality.base.client.profile.fx.FXProfile;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

/**
 * @author Bruno Salmon
 */
final class ModalityClientProfileButton {

    public static Node createProfileButton() {
        Text buttonText = new Text();
        buttonText.setFill(Color.WHITE);
        CirclePane circleButton = new CirclePane(buttonText);
        circleButton.setMinSize(32, 32);
        circleButton.setBackground(Background.fill(Color.rgb(0, 150, 214)));
        FXProperties.runNowAndOnPropertiesChange(() -> updateButtonText(buttonText), FXUserPerson.userPersonProperty(), FXUserClaims.userClaimsProperty());
        FXProfile.setProfileButton(circleButton);
        circleButton.setOnMouseClicked(e -> FXProfile.toggleShowProfilePanel());
        return circleButton;
    }

    private static void updateButtonText(Text buttonText) {
        String identity;
        Person userPerson = FXUserPerson.getUserPerson();
        if (userPerson != null)
            identity = userPerson.getFirstName();
        else {
            UserClaims userClaims = FXUserClaims.getUserClaims();
            identity = userClaims == null ? null : userClaims.getEmail();
        }
        buttonText.setText(identity == null ? "" : getFirstLetter(identity));
    }

    private static String getFirstLetter(String... texts) {
        for (String text : texts) {
            if (Strings.isNotEmpty(text))
                return text.toUpperCase().substring(0, 1);
        }
        return "?";
    }

}
