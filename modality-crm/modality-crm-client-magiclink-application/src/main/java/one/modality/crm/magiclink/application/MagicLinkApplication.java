package one.modality.crm.magiclink.application;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.authn.*;
import dev.webfx.stack.authn.login.ui.spi.impl.gateway.password.PasswordI18nKeys;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;


public class MagicLinkApplication extends Application implements dev.webfx.stack.ui.controls.MaterialFactoryMixin {

    private String token;
    private Stage stage;
    private Label loginTitleLabel;
    private Label successMessageLabel;
    private TextField textField;
    private Label infoMessageForTextFieldLabel;
    private Hyperlink hyperlink;
    private Button actionButton;
    private VBox mainVBox;
    private ScalePane checkMarkForActionButtonScalePane;

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
        String hash = "/en/2a9713db-fdad-ab95-2dfc-b29d8106725c";//WindowLocation.getHash();
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
        I18n.bindI18nTextProperty(loginTitleLabel.textProperty(), MagicLinkI18nKeys.Recovery);
        I18n.bindI18nTextProperty(successMessageLabel.textProperty(), MagicLinkI18nKeys.ChangeYourPassword);
        I18n.bindI18nTextProperty(actionButton.textProperty(),MagicLinkI18nKeys.ConfirmChange);
        //TODO: for some reason the bold doesn't work
        successMessageLabel.getStyleClass().setAll(Bootstrap.STRONG);
        infoMessageForTextFieldLabel.setVisible(false);
        actionButton.setGraphic(null);
        actionButton.setOnAction(l -> {
            AuthenticationService.updateCredentials(new MagicLinkPasswordUpdate(textField.getText()))
                .onFailure(e -> {
                    Console.log("Error Updating password: " + e);
                    //           errorMessage.setManaged(true);
                })
                .onSuccess(ignored -> {
                    Console.log("Password Updated");
                    //            successMessage.setManaged(true);
                });
        });
    }

    private void onFailure(Throwable e) {
        String technicalMessage = e.getMessage();
        Console.log("Technical error: " + technicalMessage);

        if (technicalMessage != null) {
            //The error Message are defined in ModalityMagicLinkAuthenticationGatewayProvider
            if (technicalMessage.contains("not found")) {
                I18n.bindI18nTextProperty(loginTitleLabel.textProperty(), MagicLinkI18nKeys.MagicLinkUnrecognisedErrorTitle);
                I18n.bindI18nTextProperty(successMessageLabel.textProperty(), MagicLinkI18nKeys.MagicLinkUnexpectedError);
            }
            else if (technicalMessage.contains("used")) {
                I18nControls.bindI18nProperties(loginTitleLabel, MagicLinkI18nKeys.MagicLinkAlreadyUsedErrorTitle);
                I18nControls.bindI18nProperties(successMessageLabel, MagicLinkI18nKeys.MagicLinkAlreadyUsedError);
                I18n.bindI18nTextProperty(actionButton.textProperty(),PasswordI18nKeys.SendLink);
                //TODO: for some reason the bold doesn't work
                successMessageLabel.getStyleClass().setAll(Bootstrap.STRONG);
                infoMessageForTextFieldLabel.setVisible(false);
                actionButton.setGraphic(null);
                actionButton.setOnAction(event -> {
                    Object credentials = new MagicLinkRenewalRequest(token);
                    OperationUtil.turnOnButtonsWaitMode(actionButton);
                    new AuthenticationRequest()
                        .setUserCredentials(credentials)
                        .executeAsync()
                        .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(actionButton)))
                        .onFailure(failure->Console.log("Fail to renew Magik Link:" + failure.getMessage() ))
                        .onSuccess(ignored -> UiScheduler.runInUiThread(() -> {
                            actionButton.setGraphic(checkMarkForActionButtonScalePane);
                            successMessageLabel.textProperty().unbind();
                            successMessageLabel.getStyleClass().setAll(Bootstrap.TEXT_SUCCESS);
                            successMessageLabel.setText(I18n.getI18nText(MagicLinkI18nKeys.MagicLinkSentCheckYourMailBox));
                            actionButton.setDisable(true);
                            textField.setDisable(true);
                        }));
                });
            }
            else if (technicalMessage.contains("expired")) {
                I18n.bindI18nTextProperty(loginTitleLabel.textProperty(), MagicLinkI18nKeys.MagicLinkExpiredErrorTitle);
                I18n.bindI18nTextProperty(successMessageLabel.textProperty(), MagicLinkI18nKeys.MagicLinkExpiredError);
                I18n.bindI18nTextProperty(actionButton.textProperty(), PasswordI18nKeys.SendLink);
                infoMessageForTextFieldLabel.setVisible(false);
                actionButton.setGraphic(null);
                actionButton.setOnAction(event -> {
                    Object credentials = new MagicLinkRenewalRequest(token);
                    OperationUtil.turnOnButtonsWaitMode(actionButton);
                    new AuthenticationRequest()
                        .setUserCredentials(credentials)
                        .executeAsync()
                        .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(actionButton)))
                        .onFailure(failure->Console.log("Fail to renew Magik Link:" + failure.getMessage() ))
                        .onSuccess(ignored -> UiScheduler.runInUiThread(() -> {
                            actionButton.setGraphic(checkMarkForActionButtonScalePane);
                            successMessageLabel.textProperty().unbind();
                            successMessageLabel.getStyleClass().setAll(Bootstrap.TEXT_SUCCESS);
                            successMessageLabel.setText(I18n.getI18nText(MagicLinkI18nKeys.MagicLinkSentCheckYourMailBox));
                            actionButton.setDisable(true);
                            textField.setDisable(true);
                            // content.getChildren().removeAll(emailTexField,sendLinkButton);
                        }));
                });
            }
            if (technicalMessage.contains("address")) {
                I18n.bindI18nTextProperty(loginTitleLabel.textProperty(), MagicLinkI18nKeys.MagicLinkPushErrorTitle);
                I18n.bindI18nTextProperty(successMessageLabel.textProperty(), MagicLinkI18nKeys.MagicLinkPushError);
            }
            if (technicalMessage.contains("closed")) {
                I18n.bindI18nTextProperty(loginTitleLabel.textProperty(), MagicLinkI18nKeys.MagicLinkBusClosedErrorTitle);
                I18n.bindI18nTextProperty(successMessageLabel.textProperty(), MagicLinkI18nKeys.MagicLinkBusClosedError);
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane loginWindowBorderPane = new BorderPane();

        String backgroundImageUrl = SourcesConfig.getSourcesRootConfig().childConfigAt("one.modality.crm.magiclink.application").getString("backgroundImageUrl");
        Image backgroundImage = new Image(backgroundImageUrl);
//        ScalePane headerImageScalePane = new ScalePane(headerImageView);
        //headerImageScalePane.setMaxHeight(1024);
        BackgroundImage background = new BackgroundImage(
            backgroundImage,
            BackgroundRepeat.NO_REPEAT,   // Repeat settings
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,    // Position of the image
            BackgroundSize.DEFAULT        // Size settings
        );
        loginWindowBorderPane.setBackground(new Background(background));

        HBox dharmaWheelAndTitleHbox = new HBox();
        dharmaWheelAndTitleHbox.setAlignment(Pos.CENTER);
        dharmaWheelAndTitleHbox.setSpacing(50);
        SVGPath dharmaWheel = SvgIcons.createDharmaWheelSVGPath();
        dharmaWheel.setFill(Color.web(ModalityStyle.primaryColor()));

        HtmlText kadampaTitleHTMLText = ModalityStyle.h1Primary(new HtmlText());
        I18n.bindI18nTextProperty(kadampaTitleHTMLText.textProperty(),PasswordI18nKeys.KadampaBookingSystem);
        dharmaWheelAndTitleHbox.getChildren().addAll(dharmaWheel,kadampaTitleHTMLText);

        VBox loginVBox = new VBox();
        loginVBox.setAlignment(Pos.CENTER);
        initialiseMainVBox(loginVBox);
        loginVBox.getChildren().addAll(dharmaWheelAndTitleHbox,mainVBox);
        loginVBox.setSpacing(60);

        loginWindowBorderPane.setCenter(loginVBox);

        stage = primaryStage;
        stage.setScene(new Scene(loginWindowBorderPane, 1440, 1024));
        stage.show();
    }

    private void initialiseMainVBox(VBox container) {
        mainVBox = new VBox();
        mainVBox.setMinWidth(container.getMinWidth());
        mainVBox.getStyleClass().add("login");
        mainVBox.setAlignment(Pos.TOP_CENTER);
        mainVBox.setMaxWidth(586);
        mainVBox.setMinHeight(506);

        int vSpacing = 45;
        loginTitleLabel = ModalityStyle.h2Primary(I18nControls.newLabel(MagicLinkI18nKeys.Recovery));
        loginTitleLabel.setPadding(new Insets(vSpacing,0,0,0));

        successMessageLabel = ModalityStyle.textSuccess(new Label("Success Message"));
        successMessageLabel.setPadding(new Insets(vSpacing,0,0,0));
        successMessageLabel.setVisible(true);
        successMessageLabel.setTextAlignment(TextAlignment.CENTER);
        successMessageLabel.setWrapText(true);

        VBox textFieldAndMessageVbox = new VBox(10);
        textField = newMaterialTextField(PasswordI18nKeys.Email);
        VBox.setMargin(textField,new Insets(vSpacing,0,0,0));
        textFieldAndMessageVbox.setMaxWidth(300);

        infoMessageForTextFieldLabel = Bootstrap.small(new Label("Info message"));
        infoMessageForTextFieldLabel.setVisible(true);
        textFieldAndMessageVbox.getChildren().addAll(textField,infoMessageForTextFieldLabel);

        hyperlink = new Hyperlink("hyperlink");
        hyperlink.setVisible(true);
        hyperlink.setPadding(new Insets(vSpacing+30,0,0,0));

        actionButton = Bootstrap.largePrimaryButton(I18nControls.newButton(MagicLinkI18nKeys.ConfirmChange));
        SVGPath checkMark = SvgIcons.createCheckMarkSVGPath();
        checkMark.setFill(Color.WHITE);
        checkMarkForActionButtonScalePane = new ScalePane(checkMark);

        VBox.setMargin(actionButton,new Insets(vSpacing-20,0,0,0));
        mainVBox.getChildren().addAll(loginTitleLabel,successMessageLabel,textFieldAndMessageVbox,hyperlink,actionButton);
    }


}