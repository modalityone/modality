package one.modality.crm.magiclink.application;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.MagicLinkCredentials;
import dev.webfx.stack.i18n.I18n;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MagicLinkApplication extends Application {

    private final Text text = I18n.bindI18nProperties(new Text(), "MagicLinkInitialMessage");
    private String token;

    @Override
    public void init() {
        readTokenAndSetLanguage();
        if (token == null) {
            I18n.bindI18nProperties(text, "MagicLinkUnrecognisedError");
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
    }

    private void onFailure(Throwable e) {
        String technicalMessage = e.getMessage();
        Console.log("Technical error: " + technicalMessage);
        Object i18nKey = "MagicLinkUnexpectedError";
        if (technicalMessage != null) {
            if (technicalMessage.contains("not found")) {
                i18nKey = "MagicLinkUnrecognisedError";
            } else if (technicalMessage.contains("used")) {
                i18nKey = "MagicLinkAlreadyUsedError";
            } else if (technicalMessage.contains("expired")) {
                i18nKey = "MagicLinkExpiredError";
            } if (technicalMessage.contains("address"))
                i18nKey = "MagicLinkPushError";
        }
        I18n.bindI18nProperties(text, i18nKey);
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane(text);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

}