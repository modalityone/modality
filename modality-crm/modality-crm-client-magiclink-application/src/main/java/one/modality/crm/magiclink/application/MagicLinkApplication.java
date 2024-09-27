package one.modality.crm.magiclink.application;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.MagicLinkCredentials;
import dev.webfx.stack.authn.MagicLinkPasswordUpdate;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MagicLinkApplication extends Application {

    private final Text text = I18n.bindI18nProperties(new Text(), MagicLinkI18nKeys.MagicLinkInitialMessage);
    private String token;
    private final VBox content = new VBox(20, text);
    private Stage stage;

    @Override
    public void init() {
        readTokenAndSetLanguage();
        if (token == null) {
            I18n.bindI18nProperties(text, MagicLinkI18nKeys.MagicLinkUnrecognisedError);
        } else {
            AuthenticationService.authenticate(new MagicLinkCredentials(token))
                .onFailure(e -> UiScheduler.runInUiThread(() -> onFailure(e)))
                .onSuccess(ignored -> UiScheduler.runInUiThread(this::onSuccess));
        }
    }

    private void readTokenAndSetLanguage() {
        String hash = WindowLocation.getHash();
        String route = hash != null ? hash : WindowLocation.getPath();
        if (route != null) {
            int tokenSlashIndex = route.lastIndexOf('/');
            if (tokenSlashIndex != -1) {
                token = route.substring(tokenSlashIndex + 1);
                int previousSlashIndex = route.substring(0, tokenSlashIndex).lastIndexOf('/');
                if (previousSlashIndex != -1) {
                    String lang = route.substring(previousSlashIndex + 1, tokenSlashIndex);
                    I18n.setLanguage(lang);
                }
            }
        }
    }

    private void onSuccess() {
        I18n.bindI18nProperties(text, "MagicLinkSuccessMessage");
        Label titleLabel = Bootstrap.textPrimary(I18nControls.bindI18nTextProperty(new Label(), MagicLinkI18nKeys.ChangeYourPassword));
        Label passwordLabel = Bootstrap.textSecondary(I18nControls.bindI18nTextProperty(new Label(), MagicLinkI18nKeys.NewPassword));
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(250);
        Button confirmButton = Bootstrap.successButton(I18nControls.bindI18nProperties(new Button(), "Confirm"));
        confirmButton.setPrefWidth(250);
        confirmButton.setOnAction(l -> {
            AuthenticationService.updateCredentials(new MagicLinkPasswordUpdate(passwordField.getText()))
                .onFailure(e -> Console.log("Error Updating password: " + e))
                .onSuccess(ignored -> Console.log("Password Updated"));
        });
        content.getChildren().setAll(text, titleLabel ,passwordLabel, passwordField, confirmButton);
    }

    private void onFailure(Throwable e) {
        String technicalMessage = e.getMessage();
        Console.log("Technical error: " + technicalMessage);
        Object i18nKey = MagicLinkI18nKeys.MagicLinkUnexpectedError;
        if (technicalMessage != null) {
            if (technicalMessage.contains("not found")) {
                i18nKey = MagicLinkI18nKeys.MagicLinkUnrecognisedError;
            } else if (technicalMessage.contains("used")) {
                i18nKey = MagicLinkI18nKeys.MagicLinkAlreadyUsedError;
            } else if (technicalMessage.contains("expired")) {
                i18nKey = MagicLinkI18nKeys.MagicLinkExpiredError;
            } if (technicalMessage.contains("address"))
                i18nKey = MagicLinkI18nKeys.MagicLinkPushError;
        }
        I18n.bindI18nProperties(text, i18nKey);
    }

    @Override
    public void start(Stage primaryStage) {
        content.setAlignment(Pos.CENTER);
        stage = primaryStage;
        stage.setScene(new Scene(content, 800, 600));
        stage.show();
    }

}