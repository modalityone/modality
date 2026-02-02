package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.controlfactory.MaterialFactoryMixin;
import dev.webfx.extras.controlfactory.button.ButtonFactory;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.control.HtmlInputAutocomplete;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.authn.AuthenticateWithUsernamePasswordCredentials;
import dev.webfx.stack.authn.AuthenticationRequest;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.UpdatePasswordCredentials;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.shared.entities.Person;
import one.modality.crm.frontoffice.activities.createaccount.CreateAccountI18nKeys;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

import static one.modality.crm.frontoffice.activities.userprofile.UserProfileCssSelectors.*;

/**
 * @author David Hello
 */
final class ChangePasswordUI implements MaterialFactoryMixin {

    private final PasswordField passwordField;
    private final PasswordField newPasswordField;
    private final PasswordField newPasswordRepeatedField;
    private final VBox container = new VBox();
    private final ValidationSupport validationSupport = new ValidationSupport();
    private final Button actionButton;
    private final Button closeButton;
    private Person currentUser;
    private final Label infoMessage = Bootstrap.textDanger(new Label());
    private final Label resultMessage = Bootstrap.textDanger(new Label());
    private String emailAddress = "";
    private DialogCallback callback;

    public ChangePasswordUI() {
        Label title = Bootstrap.textPrimary(Bootstrap.h2(I18nControls.newLabel(UserProfileI18nKeys.ChangePassword)));
        title.setTextAlignment(TextAlignment.CENTER);
        title.setPadding(new Insets(0, 0, 50, 0));
        Controls.setupTextWrapping(title, true, false);

        Label description = Bootstrap.strong(I18nControls.newLabel(UserProfileI18nKeys.UpdatePasswordDesc));
        description.setTextAlignment(TextAlignment.CENTER);
        description.setPadding(new Insets(0, 0, 10, 0));
        Controls.setupTextWrapping(description, true, false);

        passwordField = newMaterialPasswordField(UserProfileI18nKeys.CurrentPassword);
        Controls.setHtmlInputAutocomplete(passwordField, HtmlInputAutocomplete.CURRENT_PASSWORD);
        MaterialUtil.getMaterialTextField(passwordField).setAnimateLabel(false);

        newPasswordField = newMaterialPasswordField(UserProfileI18nKeys.NewPassword);
        Controls.setHtmlInputAutocomplete(newPasswordField, HtmlInputAutocomplete.NEW_PASSWORD);
        MaterialUtil.getMaterialTextField(newPasswordField).setAnimateLabel(false);

        newPasswordRepeatedField = newMaterialPasswordField(UserProfileI18nKeys.NewPasswordAgain);
        Controls.setHtmlInputAutocomplete(newPasswordRepeatedField, HtmlInputAutocomplete.NEW_PASSWORD);
        MaterialUtil.getMaterialTextField(newPasswordRepeatedField).setAnimateLabel(false);

        actionButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.Confirm));
        closeButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.Close));
        closeButton.setOnAction(evt -> {
            callback.closeDialog();
            resetToInitialState();
        });
        //We display the close button only when the confirm button is not visible
        closeButton.setVisible(false);
        closeButton.setManaged(false);

        passwordField.disableProperty().bind(closeButton.visibleProperty());
        newPasswordField.disableProperty().bind(closeButton.visibleProperty());
        newPasswordRepeatedField.disableProperty().bind(closeButton.visibleProperty());

        FXProperties.runNowAndOnPropertyChange(user -> {
            //We reload in case we changed the email address recently
            //FXUserPerson.reloadUserPerson();
            currentUser = FXUserPerson.getUserPerson();
            if (currentUser != null)
                emailAddress = currentUser.getEmail();

            ButtonFactory.resetDefaultButton(actionButton);
            actionButton.setOnAction(e -> {
                if (validateForm()) {
                    // First, we check if the password entered is the correct
                    Object credentials = new AuthenticateWithUsernamePasswordCredentials(emailAddress, passwordField.getText().trim());
                    AsyncSpinner.displayButtonSpinner(actionButton);
                    new AuthenticationRequest()
                        .setUserCredentials(credentials)
                        .executeAsync()
                        .inUiThread()
                        .onComplete(ar -> AsyncSpinner.hideButtonSpinner(actionButton))
                        .onFailure(failure -> {
                            Animations.shake(container);
                            showInfoMessage(UserProfileI18nKeys.IncorrectPassword, Bootstrap.TEXT_DANGER);
                        })
                        .onSuccess(ignored -> {
                            //Here we send an email
                            //TODO : Change the function when it's ready
                            Object updatePasswordCredentials = new UpdatePasswordCredentials(passwordField.getText().trim(), newPasswordField.getText().trim());
                            AsyncSpinner.displayButtonSpinner(actionButton);
                            AuthenticationService.updateCredentials(updatePasswordCredentials)
                                .inUiThread()
                                .onComplete(ar -> AsyncSpinner.hideButtonSpinner(actionButton))
                                .onFailure(failure -> {
                                    //  showResultMessage(failure.getMessage(), Bootstrap.TEXT_DANGER);
                                    showInfoMessage(failure.getMessage(), Bootstrap.TEXT_DANGER);
                                    displayCloseButton();

                                    // transitionPane.transitToContent(messageVBox);
                                })
                                .onSuccess(s -> {
                                    showResultMessage(UserProfileI18nKeys.PasswordSuccessfullyChanged, Bootstrap.TEXT_SUCCESS);
                                    showInfoMessage(UserProfileI18nKeys.PasswordSuccessfullyChanged, Bootstrap.TEXT_SUCCESS);
                                    displayCloseButton();
                                    // transitionPane.transitToContent(messageVBox);
                                });
                        });
                }
            });
        }, FXUserPerson.userPersonProperty());

        infoMessage.setVisible(false);
        infoMessage.setWrapText(true);
        resultMessage.setWrapText(true);

        container.getChildren().setAll(title, description, passwordField, newPasswordField, newPasswordRepeatedField, infoMessage, actionButton, closeButton);
        setupModalVBox(container);

        closeButton.setOnAction(evt -> {
            callback.closeDialog();
            resetToInitialState();
        });

        //We prevent the mouse click event to pass through the Pane
        container.setOnMouseClicked(Event::consume);
    }

    private void displayCloseButton() {
        closeButton.setVisible(true);
        closeButton.setManaged(true);
        actionButton.setVisible(false);
        actionButton.setManaged(false);
    }

    public static void setupModalVBox(VBox vBox) {
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.getStyleClass().add(user_profile_modal_window);
        vBox.setPadding(new Insets(60));
        vBox.setMinWidth(300);
    }

    public Region getView() {
        return container;
    }

    public void resetToInitialState() {
        passwordField.setText("");
        newPasswordField.setText("");
        newPasswordRepeatedField.setText("");
        showResultMessage("", "");
        showInfoMessage("", "");
        Layouts.setManagedAndVisibleProperties(closeButton, false);
        Layouts.setManagedAndVisibleProperties(actionButton, true);
    }

    public void showResultMessage(Object errorI18nKey, String cssClass) {
        I18nControls.bindI18nProperties(resultMessage, errorI18nKey);
        resultMessage.getStyleClass().setAll(cssClass);
    }

    public void showInfoMessage(Object errorI18nKey, String cssClass) {
        I18nControls.bindI18nProperties(infoMessage, errorI18nKey);
        infoMessage.getStyleClass().setAll(cssClass);
        infoMessage.setVisible(true);
    }

    /**
     * This method is used to initialize the parameters for the form validation
     */
    private void initFormValidation() {
        if (validationSupport.isEmpty()) {
            validationSupport.addRequiredInput(passwordField);
            validationSupport.addPasswordStrengthValidation(newPasswordField, I18n.i18nTextProperty(CreateAccountI18nKeys.PasswordStrength));
            validationSupport.addPasswordMatchValidation(newPasswordField, newPasswordRepeatedField, I18n.i18nTextProperty(CreateAccountI18nKeys.PasswordNotMatchingError));
        }
    }

    /**
     * We validate the form
     *
     * @return true if all the validation is success, false otherwise
     */
    public boolean validateForm() {
        initFormValidation(); // do nothing if not yet initialised
        return validationSupport.isValid();
    }

    public void setDialogCallback(DialogCallback callback) {
        this.callback = callback;
    }
}
