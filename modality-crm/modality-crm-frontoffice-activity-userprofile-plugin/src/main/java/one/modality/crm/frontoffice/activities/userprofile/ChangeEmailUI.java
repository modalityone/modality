package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.authn.AuthenticateWithUsernamePasswordCredentials;
import dev.webfx.stack.authn.AuthenticationRequest;
import dev.webfx.stack.authn.InitiateEmailUpdateCredentials;
import dev.webfx.stack.authn.login.ui.FXLoginContext;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.controls.MaterialFactoryMixin;
import dev.webfx.stack.ui.controls.button.ButtonFactory;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.operation.OperationUtil;
import dev.webfx.stack.ui.validation.ValidationSupport;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

public class ChangeEmailUI implements MaterialFactoryMixin {

    protected PasswordField passwordField;
    protected TextField emailField;
    private final VBox changeEmailVBox = new VBox();
    private final TransitionPane transitionPane = new TransitionPane(changeEmailVBox);
    protected ScalePane container = new ScalePane(transitionPane);
    private final ValidationSupport validationSupport = new ValidationSupport();
    private Person currentUser;
    protected Label infoMessage = Bootstrap.textDanger(new Label());
    private final UserProfileActivity parentActivity;
    private String emailAddress = "";
    private DialogCallback callback;

    public ChangeEmailUI(UserProfileActivity activity) {
        parentActivity = activity;
        javafx.scene.control.Label title = Bootstrap.textPrimary(Bootstrap.h2(I18nControls.newLabel(UserProfileI18nKeys.ChangeEmailAddress)));
        changeEmailVBox.setPadding(new Insets(20, 0, 0, 0));
        title.setPadding(new Insets(0, 0, 100, 0));

        passwordField = newMaterialPasswordField(UserProfileI18nKeys.CurrentPassword);
        MaterialUtil.getMaterialTextField(passwordField).setAnimateLabel(false);

        emailField = newMaterialTextField(UserProfileI18nKeys.NewEmailAddress);
        MaterialUtil.getMaterialTextField(emailField).setAnimateLabel(false);
        Controls.setHtmlInputTypeAndAutocompleteToEmail(emailField);

        infoMessage.setVisible(false);
        infoMessage.setWrapText(true);

        Button actionButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.Confirm));

        FXProperties.runNowAndOnPropertyChange(user -> {
            //We reload in case the user changed his email address in the last action
            //FXUserPerson.reloadUserPerson();
            currentUser = FXUserPerson.getUserPerson();
            if (currentUser != null)
                emailAddress = currentUser.getEmail();

            ButtonFactory.resetDefaultButton(actionButton);
            actionButton.setOnAction(e -> {
                if (validateForm()) {
                    //First we check if the password entered is the correct
                    Object credentials = new AuthenticateWithUsernamePasswordCredentials(emailAddress, passwordField.getText().trim());
                    OperationUtil.turnOnButtonsWaitMode(actionButton);
                    new AuthenticationRequest()
                        .setUserCredentials(credentials)
                        .executeAsync()
                        .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(actionButton)))
                        .onFailure(failure -> Platform.runLater(() -> {
                            showMessage(UserProfileI18nKeys.IncorrectPassword, Bootstrap.TEXT_DANGER);
                            Animations.shake(container);
                        }))
                        .onSuccess(ignored -> {
                            //Here we send an email
                            Object credentials2 = new InitiateEmailUpdateCredentials(emailField.getText().trim(), WindowLocation.getOrigin(), WindowLocation.getPath(), I18n.getLanguage(), FXLoginContext.getLoginContext());
                            UiScheduler.runInUiThread(() -> {
                                OperationUtil.turnOnButtonsWaitMode(actionButton);
                                new AuthenticationRequest()
                                    .setUserCredentials(credentials2)
                                    .executeAsync()
                                    .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(actionButton)))
                                    .onFailure(failure -> {
                                        // callback.notifyUserLoginFailed(failure);
                                        UiScheduler.runInUiThread(() -> {
                                            showMessage(failure.getMessage(), Bootstrap.TEXT_DANGER);
                                            actionButton.setDisable(true);
                                        });
                                    })
                                    .onSuccess(s -> UiScheduler.runInUiThread(() -> {
                                        showMessage(UserProfileI18nKeys.EmailSentForEmailChange, Bootstrap.TEXT_SUCCESS);
                                        actionButton.setDisable(true);
                                        passwordField.setDisable(true);
                                    }));
                            });
                        });
                }
            });
        }, FXUserPerson.userPersonProperty());

        changeEmailVBox.getChildren().setAll(title, passwordField, emailField,infoMessage, actionButton);
        changeEmailVBox.setMaxWidth(UserProfileActivity.MODAL_WINDOWS_MAX_WIDTH);
        changeEmailVBox.setMaxHeight(UserProfileActivity.MODAL_WINDOWS_MAX_WIDTH+100);

        changeEmailVBox.setSpacing(20);
        ChangePasswordUI.setupModalVBox(changeEmailVBox);
    }

    public ScalePane getView() {
        return container;
    }

    public void showMessage(String errorMessageKey, String cssClass) {
        I18nControls.bindI18nProperties(infoMessage, errorMessageKey);
        infoMessage.getStyleClass().setAll(cssClass);
        infoMessage.setVisible(true);
    }

    /**
     * This method is used to initialise the parameters for the form validation
     */
    private void initFormValidation() {
        if (validationSupport.isEmpty()) {
            validationSupport.addRequiredInput(passwordField);
            validationSupport.addRequiredInput(emailField);
            validationSupport.addEmailValidation(emailField,emailField,I18n.i18nTextProperty(UserProfileI18nKeys.EmailFormatIncorrect));
        }
    }

    public void resetToInitialState() {
    }
    
    public void setDialogCallback(DialogCallback callback) {
        this.callback = callback;
    }

    /**
     * We validate the form
     *
     * @return true if all the validation is success, false otherwise
     */
    public boolean validateForm() {
        initFormValidation();
        return validationSupport.isValid();
    }

}
