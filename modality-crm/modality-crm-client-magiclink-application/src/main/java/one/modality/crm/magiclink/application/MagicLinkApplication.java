package one.modality.crm.magiclink.application;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.authn.*;
import dev.webfx.stack.authn.login.ui.FXLoginContext;
import dev.webfx.stack.authn.login.ui.spi.impl.gateway.password.PasswordI18nKeys;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import one.modality.base.client.icons.SvgIcons;

public class MagicLinkApplication extends Application implements dev.webfx.stack.ui.controls.MaterialFactoryMixin {
    private String token;
    private final VBox content = new VBox(20);
    private Stage stage;

    @Override
    public void init() {
        readTokenAndSetLanguage();
        if (token == null) {
            I18n.bindI18nProperties(new Text(), MagicLinkI18nKeys.MagicLinkUnrecognisedError);
        } else {
            AuthenticationService.authenticate(new MagicLinkCredentials(token))
                .onFailure(e -> UiScheduler.runInUiThread(() -> onFailure(e)))
                .onSuccess(ignored -> UiScheduler.runInUiThread(this::onSuccess));
        }
    }

    private void readTokenAndSetLanguage() {
        String hash = "/en/b2e6b8bd-db9e-bf89-c8e8-8b73fb3d48ec";//WindowLocation.getHash();
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
        Label titleLabel = Bootstrap.textPrimary(I18nControls.newLabel(MagicLinkI18nKeys.Recovery));
        titleLabel.getStyleClass().add(Bootstrap.H2);
        Label message = new Label();
        message.setPrefWidth(300);
        Label successMessage = Bootstrap.textSuccess(I18nControls.newLabel(MagicLinkI18nKeys.PasswordChanged));
        successMessage.setManaged(false);
        successMessage.setGraphic(SvgIcons.createCheckMarkSVGPath());

        Label errorMessage = Bootstrap.textDanger(I18nControls.newLabel(MagicLinkI18nKeys.ErrorWhileUpdatingPassword));
        errorMessage.setManaged(false);

        message.setWrapText(true);
        I18n.bindI18nTextProperty(message.textProperty(), MagicLinkI18nKeys.MagicLinkSuccessMessage);
        TextField passwordField = newMaterialTextField(MagicLinkI18nKeys.NewPassword);
        passwordField.setMaxWidth(300);
        Button confirmButton = Bootstrap.largePrimaryButton(I18nControls.newButton(MagicLinkI18nKeys.ConfirmChange));
        confirmButton.setOnAction(l -> {
            AuthenticationService.updateCredentials(new MagicLinkPasswordUpdate(passwordField.getText()))
                .onFailure(e -> {
                    Console.log("Error Updating password: " + e);
                    errorMessage.setManaged(true);
                })
                .onSuccess(ignored -> {
                    Console.log("Password Updated");
                    successMessage.setManaged(true);
                });
        });
        content.getChildren().setAll(titleLabel,message, successMessage,passwordField, confirmButton);
    }

    private void onFailure(Throwable e) {
        String technicalMessage = e.getMessage();
        Console.log("Technical error: " + technicalMessage);
        Object i18nTitleKey = MagicLinkI18nKeys.MagicLinkUnexpectedError;
        Label titleLabel = Bootstrap.textPrimary(new Label());
        titleLabel.getStyleClass().add(Bootstrap.H2);

        Label message = new Label();

        message.setMaxWidth(300);
        message.setWrapText(true);
        Object i18nMessageKey = MagicLinkI18nKeys.MagicLinkUnexpectedError;

        if (technicalMessage != null) {
            //The error Message are defined in ModalityMagicLinkAuthenticationGatewayProvider
            if (technicalMessage.contains("not found")) {
                message.setWrapText(true);
                i18nTitleKey = MagicLinkI18nKeys.MagicLinkUnrecognisedErrorTitle;
                i18nMessageKey = MagicLinkI18nKeys.MagicLinkUnrecognisedError;
                content.getChildren().addAll(titleLabel,message);
            } else if (technicalMessage.contains("used")) {
                TextField emailTexField = newMaterialTextField(PasswordI18nKeys.Email);
                emailTexField.setMaxWidth(300);
                message.setWrapText(true);
                i18nTitleKey = MagicLinkI18nKeys.MagicLinkAlreadyUsedErrorTitle;
                i18nMessageKey = MagicLinkI18nKeys.MagicLinkAlreadyUsedError;
                Button sendLinkButton = createSendLinkButton(content,emailTexField,message);
                content.getChildren().addAll(titleLabel,message,emailTexField,sendLinkButton);
            } else if (technicalMessage.contains("expired")) {
                TextField emailTexField = newMaterialTextField(PasswordI18nKeys.Email);
                emailTexField.setMaxWidth(300);
                i18nTitleKey = MagicLinkI18nKeys.MagicLinkExpiredErrorTitle;
                i18nMessageKey = MagicLinkI18nKeys.MagicLinkExpiredError;
                Button sendLinkButton = createSendLinkButton(content,emailTexField,message);
                content.getChildren().addAll(titleLabel,message,emailTexField,sendLinkButton);
            } if (technicalMessage.contains("address")) {
                i18nTitleKey = MagicLinkI18nKeys.MagicLinkPushErrorTitle;
                i18nMessageKey = MagicLinkI18nKeys.MagicLinkPushError;
                content.getChildren().addAll(titleLabel,message);
            }
            if (technicalMessage.contains("closed")) {
                i18nTitleKey = MagicLinkI18nKeys.MagicLinkBusClosedErrorTitle;
                i18nMessageKey = MagicLinkI18nKeys.MagicLinkBusClosedError;
                content.getChildren().addAll(titleLabel,message);
            }
            I18n.bindI18nTextProperty(titleLabel.textProperty(), i18nTitleKey);
            I18n.bindI18nTextProperty(message.textProperty(), i18nMessageKey);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane loginWindow = new BorderPane();
        content.getStyleClass().add("login");
        loginWindow.setCenter(content);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setMaxHeight(400);
        stage = primaryStage;
        stage.setScene(new Scene(loginWindow, 800, 600));
        stage.show();



    }

    private Button createSendLinkButton(VBox container,TextField emailTexField, Label message) {
        Button sendLinkButton = Bootstrap.largePrimaryButton(I18nControls.newButton((PasswordI18nKeys.SendLink)));
        SVGPath checkMark = SvgIcons.createCheckMarkSVGPath();
        checkMark.setFill(Color.WHITE);
        ScalePane scalePane = new ScalePane(checkMark);
        sendLinkButton.setGraphic(null);
        sendLinkButton.setOnAction(event -> {
            //if (validationSupport.isValid())
            Object credentials = new MagicLinkRequest(emailTexField.getText(), WindowLocation.getOrigin(), I18n.getLanguage(), FXLoginContext.getLoginContext());
            OperationUtil.turnOnButtonsWaitMode(sendLinkButton);
            new AuthenticationRequest()
                .setUserCredentials(credentials)
                .executeAsync()
                .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(sendLinkButton)))
                .onFailure(failure->Console.log("Failure"))
                .onSuccess(ignored -> UiScheduler.runInUiThread(() -> {
                    sendLinkButton.setGraphic(scalePane);
                    message.textProperty().unbind();
                    message.getStyleClass().add(Bootstrap.TEXT_SUCCESS);
                    message.setText(I18n.getI18nText(MagicLinkI18nKeys.MagicLinkSentCheckYourMailBox));
                    content.getChildren().removeAll(emailTexField,sendLinkButton);
                }));
        });
        return sendLinkButton;
    }

}