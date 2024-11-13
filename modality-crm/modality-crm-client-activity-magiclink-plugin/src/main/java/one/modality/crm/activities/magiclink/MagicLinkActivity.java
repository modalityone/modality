package one.modality.crm.activities.magiclink;

import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.authn.*;
import dev.webfx.stack.authn.login.ui.spi.impl.gateway.password.PasswordI18nKeys;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;

final class MagicLinkActivity extends ViewDomainActivityBase implements dev.webfx.stack.ui.controls.MaterialFactoryMixin {

    private final StringProperty tokenProperty = new SimpleStringProperty();
    private Stage stage;
    private Label loginTitleLabel;
    private Label successMessageLabel;
    private javafx.scene.control.TextField textField;
    private Label infoMessageForTextFieldLabel;
    private Hyperlink hyperlink;
    private Button actionButton;
    private VBox mainVBox;
    private ScalePane checkMarkForActionButtonScalePane;
    private VBox textFieldAndMessageVbox;
    private SVGPath checkMarkForLabel;
    private String pathToBeRedirected;
    private int vSpacing = 50;

    @Override
    protected void updateModelFromContextParameters() {
        tokenProperty.set(getParameter(MagicLinkRouting.PATH_TOKEN_PARAMETER_NAME));
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************
        String backgroundImageUrl = SourcesConfig.getSourcesRootConfig().childConfigAt("one.modality.crm.magiclink.application").getString("backgroundImageUrl");
        Image backgroundImage = new Image(backgroundImageUrl);
        BackgroundImage background = new BackgroundImage(
            backgroundImage,
            BackgroundRepeat.NO_REPEAT,   // Repeat settings
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,    // Position of the image
            BackgroundSize.DEFAULT        // Size settings
        );

        HBox dharmaWheelAndTitleHbox = new HBox();
        dharmaWheelAndTitleHbox.setAlignment(Pos.CENTER);
        dharmaWheelAndTitleHbox.setSpacing(50);
        SVGPath dharmaWheel = SvgIcons.createDharmaWheelSVGPath();
        dharmaWheel.setFill(Color.web(ModalityStyle.primaryColor()));

        HtmlText kadampaTitleHTMLText = ModalityStyle.h1Primary(new HtmlText());
        I18n.bindI18nTextProperty(kadampaTitleHTMLText.textProperty(), PasswordI18nKeys.KadampaBookingSystem);
        dharmaWheelAndTitleHbox.getChildren().addAll(dharmaWheel,kadampaTitleHTMLText);

        VBox loginVBox = new VBox();
        loginVBox.setAlignment(Pos.CENTER);
        initialiseMainVBox(loginVBox);
        loginVBox.getChildren().addAll(dharmaWheelAndTitleHbox,mainVBox);
        loginVBox.setSpacing(60);

        Text text = new Text();
        GoldenRatioPane container = new GoldenRatioPane(text);
        container.setMaxWidth(Double.MAX_VALUE); // so it fills the whole width of the main frame VBox (with text centered)
        container.setBackground(new Background(background));
        container.setContent(loginVBox);

        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        FXProperties.runNowAndOnPropertyChange(token -> {
            if (token == null) {
                I18n.bindI18nProperties(new Text(), MagicLinkI18nKeys.MagicLinkUnrecognisedError);
            } else {
                AuthenticationService.authenticate(new MagicLinkCredentials(token))
                    .onFailure(e -> UiScheduler.runInUiThread(() -> onFailure(e)))
                    .onSuccess(requestedPath -> {
                        UiScheduler.runInUiThread(this::onSuccess);
                        pathToBeRedirected = requestedPath.toString();
                    });
            }
        }, tokenProperty);

        // *************************************************************************************************************
        // ************************************* Returning final container *********************************************
        // *************************************************************************************************************

        return container;
    }

    private void onSuccess() {
        I18n.bindI18nTextProperty(loginTitleLabel.textProperty(), MagicLinkI18nKeys.Recovery);
        I18n.bindI18nTextProperty(successMessageLabel.textProperty(), MagicLinkI18nKeys.ChangeYourPassword);
        I18n.bindI18nTextProperty(actionButton.textProperty(),MagicLinkI18nKeys.ConfirmChange);
        textField = newMaterialPasswordField(MagicLinkI18nKeys.NewPassword);
        textField.getStyleClass().clear();
        textField.getStyleClass().add("transparent-input");
        VBox.setMargin (textField,new Insets(vSpacing,0,0,0));
        textFieldAndMessageVbox.getChildren().setAll(textField,infoMessageForTextFieldLabel);
        successMessageLabel.getStyleClass().setAll(Bootstrap.STRONG);
        successMessageLabel.setVisible(true);
        infoMessageForTextFieldLabel.setVisible(true);
        hyperlink.setVisible(false);
        actionButton.setGraphic(null);
        actionButton.setOnAction(l -> {
            AuthenticationService.updateCredentials(new MagicLinkPasswordUpdate(textField.getText()))
                .onFailure(e -> {
                    Console.log("Error Updating password: " + e);
                    //           errorMessage.setManaged(true);
                })
                .onSuccess(ignored -> {
                    Console.log("Password Updated");
                    Platform.runLater(()->{
                        I18n.bindI18nTextProperty(successMessageLabel.textProperty(), MagicLinkI18nKeys.PasswordChanged);
                        successMessageLabel.getStyleClass().setAll(Bootstrap.TEXT_SUCCESS);
                        successMessageLabel.setGraphic(checkMarkForLabel);
                        successMessageLabel.setVisible(true);
                        actionButton.setDisable(true);
                        textField.setDisable(true);
                        I18n.bindI18nTextProperty(hyperlink.textProperty(), MagicLinkI18nKeys.BackToNavigation);
                        hyperlink.setVisible(true);
                        hyperlink.setOnAction(e2-> {
                            getHistory().replace(pathToBeRedirected);
                        });
                    });
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
                infoMessageForTextFieldLabel.setVisible(false);
            }
            else if (technicalMessage.contains("used")) {
                I18nControls.bindI18nProperties(loginTitleLabel, MagicLinkI18nKeys.MagicLinkAlreadyUsedErrorTitle);
                I18nControls.bindI18nProperties(successMessageLabel, MagicLinkI18nKeys.MagicLinkAlreadyUsedError);
                I18n.bindI18nTextProperty(actionButton.textProperty(),PasswordI18nKeys.SendLink);
                successMessageLabel.setVisible(true);
                hyperlink.setVisible(false);
                successMessageLabel.getStyleClass().setAll(Bootstrap.STRONG);
                infoMessageForTextFieldLabel.setVisible(false);
                actionButton.setDisable(false);
                actionButton.setGraphic(null);
                actionButton.setOnAction(event -> {
                    Object credentials = new MagicLinkRenewalRequest(tokenProperty.get());
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
                            hyperlink.setVisible(true);
                            
                        }));
                });
            }
            else if (technicalMessage.contains("expired")) {
                I18n.bindI18nTextProperty(loginTitleLabel.textProperty(), MagicLinkI18nKeys.MagicLinkExpiredErrorTitle);
                I18n.bindI18nTextProperty(successMessageLabel.textProperty(), MagicLinkI18nKeys.MagicLinkExpiredError);
                I18n.bindI18nTextProperty(actionButton.textProperty(), PasswordI18nKeys.SendLink);
                successMessageLabel.setVisible(true);
                successMessageLabel.getStyleClass().setAll(Bootstrap.STRONG);
                hyperlink.setVisible(false);
                infoMessageForTextFieldLabel.setVisible(false);
                actionButton.setGraphic(null);
                actionButton.setOnAction(event -> {
                    Object credentials = new MagicLinkRenewalRequest(tokenProperty.get());
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
                            successMessageLabel.setGraphic(checkMarkForLabel);
                            I18nControls.bindI18nProperties(hyperlink,MagicLinkI18nKeys.GoToLogin);
                            hyperlink.setVisible(true);
                            hyperlink.setOnAction(e2-> {
                                getHistory().replace("/account");
                            });
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

    private void initialiseMainVBox(VBox container) {
        mainVBox = new VBox();
        mainVBox.setMinWidth(container.getMinWidth());
        mainVBox.getStyleClass().add("login");
        mainVBox.setAlignment(Pos.TOP_CENTER);
        mainVBox.setMaxWidth(586);
        mainVBox.setMinHeight(506);


        loginTitleLabel = ModalityStyle.h2Primary(I18nControls.newLabel(MagicLinkI18nKeys.Recovery));
        loginTitleLabel.setPadding(new Insets(vSpacing,0,0,0));

        successMessageLabel = ModalityStyle.textSuccess(new javafx.scene.control.Label("Success Message"));
        successMessageLabel.setPadding(new Insets(vSpacing,0,0,0));
        successMessageLabel.setVisible(false);
        successMessageLabel.setTextAlignment(TextAlignment.CENTER);
        successMessageLabel.setWrapText(true);
        successMessageLabel.setGraphicTextGap(15);

        checkMarkForLabel = SvgIcons.createCheckMarkSVGPath();
        checkMarkForLabel.setFill(Color.web(ModalityStyle.successColor()));

        textFieldAndMessageVbox = new VBox(10);
        textField = newMaterialTextField(PasswordI18nKeys.Email);
        textField.getStyleClass().clear();
        textField.getStyleClass().add("transparent-input");
        VBox.setMargin(textField,new Insets(vSpacing,0,0,0));
        textFieldAndMessageVbox.setMaxWidth(300);

        infoMessageForTextFieldLabel = Bootstrap.small(I18nControls.newLabel(MagicLinkI18nKeys.CaseSensitive));
        infoMessageForTextFieldLabel.setVisible(true);
        textFieldAndMessageVbox.getChildren().addAll(textField,infoMessageForTextFieldLabel);

        hyperlink = new Hyperlink();
        I18nControls.bindI18nProperties(hyperlink,MagicLinkI18nKeys.GoToLogin);
        hyperlink.getStyleClass().setAll(Bootstrap.TEXT_SECONDARY);
        hyperlink.setVisible(true);
        hyperlink.setOnAction(e-> {
            getHistory().replace("/account");
        });
        VBox.setMargin(hyperlink,new Insets(vSpacing,0,0,0));

        actionButton = Bootstrap.largePrimaryButton(I18nControls.newButton(PasswordI18nKeys.SendLink));
        SVGPath checkMark = SvgIcons.createCheckMarkSVGPath();
        checkMark.setFill(Color.WHITE);
        checkMarkForActionButtonScalePane = new ScalePane(checkMark);

        VBox.setMargin(actionButton,new Insets(vSpacing-20,0,0,0));
        mainVBox.getChildren().addAll(loginTitleLabel,successMessageLabel,textFieldAndMessageVbox,hyperlink,actionButton);
    }
}
