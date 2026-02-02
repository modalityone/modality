package one.modality.event.frontoffice.activities.book.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.controlfactory.MaterialFactoryMixin;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import dev.webfx.extras.validation.ValidationSupport;
import one.modality.crm.client.i18n.CrmI18nKeys;

import java.util.function.Consumer;

/**
 * Guest checkout panel for booking without an account.
 * Features:
 * - First Name / Last Name side by side
 * - Email address field with existence check
 * - Optional "Create account" checkbox (account creation via email verification)
 *
 * @author Bruno Salmon
 */
final class GuestPanel implements MaterialFactoryMixin {

    // Color constants matching the HTML design
    private static final Color CREATE_ACCOUNT_BG = Color.web("#E7F3FF");
    private static final Color CREATE_ACCOUNT_BORDER = Color.web("#0096D6");
    private static final Color WARNING_BG = Color.web("#FFF3CD");
    private static final Color WARNING_BORDER = Color.web("#FFC107");

    private final BorderPane container = new BorderPane();
    private final VBox topVBox = new VBox(8);
    private final TextField firstNameTextField = newMaterialTextField(CrmI18nKeys.FirstName);
    private final TextField lastNameTextField = newMaterialTextField(CrmI18nKeys.LastName);
    private final TextField emailTextField = newMaterialTextField(CrmI18nKeys.Email);
    private final CheckBox createAccountCheckBox = new CheckBox();
    private final Button submitButton = Bootstrap.largePrimaryButton(I18nControls.newButton("SubmitAsGuest"));
    private final Hyperlink loginLink = I18nControls.newHyperlink("LogInHere");
    private final ValidationSupport validationSupport = new ValidationSupport();
    private final VBox emailAlertBox = new VBox();
    private final BooleanProperty emailExistsProperty = new SimpleBooleanProperty(false);
    private Runnable onLoginLinkClicked;
    private Consumer<String> emailCheckCallback;
    private String lastCheckedEmail;

    public GuestPanel() {
        Controls.setHtmlInputTypeAndAutocompleteToEmail(emailTextField);

        // Title section - "Guest Checkout" with subtitle
        Label titleLabel = Bootstrap.textPrimary(Bootstrap.strong(I18nControls.newLabel("GuestCheckout")));
        titleLabel.setStyle("-fx-font-size: 20px;");
        Label subtitleLabel = I18nControls.newLabel("GuestCheckoutSubtitle");
        subtitleLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
        subtitleLabel.setWrapText(true);

        topVBox.getChildren().addAll(titleLabel, subtitleLabel);
        topVBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(topVBox, new Insets(0, 0, 24, 0));
        container.setTop(topVBox);

        // Build the form content
        VBox formContent = new VBox(20);
        formContent.setAlignment(Pos.TOP_CENTER);

        // First Name / Last Name row (side by side) - Material text fields have integrated labels
        HBox nameRow = new HBox(16);
        nameRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(firstNameTextField, Priority.ALWAYS);
        HBox.setHgrow(lastNameTextField, Priority.ALWAYS);
        firstNameTextField.setMaxWidth(Double.MAX_VALUE);
        lastNameTextField.setMaxWidth(Double.MAX_VALUE);
        nameRow.getChildren().addAll(firstNameTextField, lastNameTextField);

        // Email field - Material text field has integrated label
        emailTextField.setMaxWidth(Double.MAX_VALUE);

        // Email existence alert (hidden by default)
        buildEmailExistsAlert();
        Layouts.setManagedAndVisibleProperties(emailAlertBox, false);

        // Check email on blur
        emailTextField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                checkEmailExists();
            }
        });

        // Email container with alert
        VBox emailContainer = new VBox(12, emailTextField, emailAlertBox);

        // Create account option - highlighted blue box
        VBox createAccountBox = buildCreateAccountBox();

        // Submit button - always primary (blue)
        Layouts.setMaxWidthToInfinite(submitButton);
        VBox.setMargin(submitButton, new Insets(8, 0, 0, 0));

        // Disable submit when email exists
        submitButton.disableProperty().bind(emailExistsProperty);

        // Update button text based on checkbox state (button stays primary)
        createAccountCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                I18nControls.bindI18nProperties(submitButton, "SubmitAndCreateAccount");
            } else {
                I18nControls.bindI18nProperties(submitButton, "SubmitAsGuest");
            }
        });

        formContent.getChildren().addAll(nameRow, emailContainer, createAccountBox, submitButton);

        // Main form card with styling
        VBox formCard = new VBox(formContent);
        formCard.setMaxWidth(450);
        formCard.setPadding(new Insets(32));
        formCard.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(12), null)));
        formCard.setBorder(new Border(new BorderStroke(Color.gray(0.88), BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(2))));
        formCard.setEffect(new DropShadow(8, Color.gray(0.85)));
        formCard.getStyleClass().add("login");
        container.setCenter(formCard);

        // Bottom section - "Already have an account? Log in here"
        VBox bottomBox = buildBottomSection();
        container.setBottom(bottomBox);

        // Validation
        validationSupport.addRequiredInput(firstNameTextField, I18n.i18nTextProperty(CrmI18nKeys.FirstName));
        validationSupport.addRequiredInput(lastNameTextField, I18n.i18nTextProperty(CrmI18nKeys.LastName));
        validationSupport.addEmailValidation(emailTextField, emailTextField, I18n.i18nTextProperty(CrmI18nKeys.Email));
    }

    private void buildEmailExistsAlert() {
        emailAlertBox.setBackground(new Background(new BackgroundFill(WARNING_BG, new CornerRadii(8), null)));
        emailAlertBox.setBorder(new Border(new BorderStroke(WARNING_BORDER, BorderStrokeStyle.SOLID, new CornerRadii(8), BorderStroke.THIN)));
        emailAlertBox.setPadding(new Insets(16));
        emailAlertBox.setSpacing(8);

        // Warning title
        Label warningTitle = I18nControls.newLabel("EmailAlreadyRegisteredTitle");
        warningTitle.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #856404;");

        // Warning message
        Label warningMessage = I18nControls.newLabel("EmailAlreadyRegisteredDesc");
        warningMessage.setStyle("-fx-font-size: 13px; -fx-text-fill: #856404;");
        warningMessage.setWrapText(true);

        // Switch to login button
        Button switchToLoginButton = Bootstrap.primaryButton(I18nControls.newButton("SwitchToLogin"));
        switchToLoginButton.setOnAction(e -> {
            if (onLoginLinkClicked != null) {
                onLoginLinkClicked.run();
            }
        });

        // Forgot password link
        Hyperlink forgotPasswordLink = I18nControls.newHyperlink("ForgotPassword");
        forgotPasswordLink.setStyle("-fx-text-fill: #0096D6;");

        HBox buttonsRow = new HBox(12, switchToLoginButton, forgotPasswordLink);
        buttonsRow.setAlignment(Pos.CENTER_LEFT);

        emailAlertBox.getChildren().addAll(warningTitle, warningMessage, buttonsRow);
    }

    private void checkEmailExists() {
        String email = emailTextField.getText();
        if (email == null || email.isEmpty() || !email.contains("@")) {
            hideEmailExistsAlert();
            return;
        }

        // Don't re-check the same email
        if (email.equals(lastCheckedEmail)) {
            return;
        }
        lastCheckedEmail = email;

        // Call the external check callback if set
        if (emailCheckCallback != null) {
            emailCheckCallback.accept(email);
        }
    }

    /**
     * Sets the callback to check if email exists in the database.
     * The callback should call showEmailExistsAlert() or hideEmailExistsAlert() based on result.
     */
    public void setEmailCheckCallback(Consumer<String> callback) {
        this.emailCheckCallback = callback;
    }

    /**
     * Shows the email already exists alert
     */
    public void showEmailExistsAlert() {
        emailExistsProperty.set(true);
        Layouts.setManagedAndVisibleProperties(emailAlertBox, true);
    }

    /**
     * Hides the email already exists alert
     */
    public void hideEmailExistsAlert() {
        emailExistsProperty.set(false);
        Layouts.setManagedAndVisibleProperties(emailAlertBox, false);
    }

    private VBox buildCreateAccountBox() {
        // Main container with blue background
        VBox createAccountContainer = new VBox(0);
        createAccountContainer.setBackground(new Background(new BackgroundFill(CREATE_ACCOUNT_BG, new CornerRadii(8), null)));
        createAccountContainer.setBorder(new Border(new BorderStroke(CREATE_ACCOUNT_BORDER, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2))));
        createAccountContainer.setPadding(new Insets(20));

        // Checkbox with title and description
        Label titleLabel = I18nControls.newLabel("CreateAccountTitle");
        titleLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 15px; -fx-text-fill: #292A33;");
        titleLabel.setWrapText(true);

        Label descLabel = I18nControls.newLabel("CreateAccountDesc");
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #838788; -fx-line-spacing: 4px;");
        descLabel.setWrapText(true);

        // Additional note about email verification
        Label emailNoteLabel = I18nControls.newLabel("CreateAccountEmailNote");
        emailNoteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #0096D6; -fx-font-style: italic;");
        emailNoteLabel.setWrapText(true);

        VBox textContent = new VBox(6, titleLabel, descLabel, emailNoteLabel);
        textContent.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        HBox checkboxRow = new HBox(12);
        checkboxRow.setAlignment(Pos.TOP_LEFT);
        VBox.setMargin(createAccountCheckBox, new Insets(4, 0, 0, 0));
        checkboxRow.getChildren().addAll(createAccountCheckBox, textContent);

        createAccountContainer.getChildren().add(checkboxRow);

        return createAccountContainer;
    }

    private VBox buildBottomSection() {
        // OR divider
        Line leftLine = new Line(0, 0, 60, 0);
        leftLine.setStroke(Color.LIGHTGRAY);
        Line rightLine = new Line(0, 0, 60, 0);
        rightLine.setStroke(Color.LIGHTGRAY);
        Label orLabel = I18nControls.newLabel("Or");
        orLabel.setStyle("-fx-text-fill: #999; -fx-font-weight: bold;");
        HBox orDivider = new HBox(12, leftLine, orLabel, rightLine);
        orDivider.setAlignment(Pos.CENTER);

        // "Already have an account? Log in here"
        Label alreadyHaveAccountLabel = I18nControls.newLabel("AlreadyHaveAccount");
        alreadyHaveAccountLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        loginLink.setOnAction(e -> {
            if (onLoginLinkClicked != null) {
                onLoginLinkClicked.run();
            }
        });

        HBox loginRow = new HBox(5, alreadyHaveAccountLabel, loginLink);
        loginRow.setAlignment(Pos.CENTER);

        VBox bottomBox = new VBox(15, orDivider, loginRow);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(24, 0, 0, 0));

        return bottomBox;
    }

    public void setOnLoginLinkClicked(Runnable handler) {
        this.onLoginLinkClicked = handler;
    }

    public void addTopNode(Node topNode) {
        topVBox.getChildren().add(topNode);
    }

    public void setOnSubmit(EventHandler<ActionEvent> submitHandler) {
        submitButton.setOnAction(event -> {
            if (emailExistsProperty.get()) {
                Animations.shake(emailAlertBox);
                return;
            }
            if (validationSupport.isValid()) {
                submitHandler.handle(event);
            } else {
                Animations.shake(container);
            }
        });
    }

    public void onShowing() {
        submitButton.setDefaultButton(true);
        UiScheduler.scheduleDelay(500, () -> SceneUtil.autoFocusIfEnabled(firstNameTextField));
    }

    public void onHiding() {
        submitButton.setDefaultButton(false);
    }

    public void turnOnButtonWaitMode() {
        StepSlide.turnOnButtonWaitMode(submitButton);
    }

    public void turnOffButtonWaitMode() {
        String key = createAccountCheckBox.isSelected() ? "SubmitAndCreateAccount" : "SubmitAsGuest";
        StepSlide.turnOffButtonWaitMode(submitButton, key);
    }

    public Node getContainer() {
        return container;
    }

    public Button getSubmitButton() {
        return submitButton;
    }

    public String getFirstName() {
        return firstNameTextField.getText();
    }

    public String getLastName() {
        return lastNameTextField.getText();
    }

    public String getEmail() {
        return emailTextField.getText();
    }

    public boolean wantsToCreateAccount() {
        return createAccountCheckBox.isSelected();
    }

    /**
     * Returns an observable property that is true when all required fields are filled
     * and valid (first name, last name, valid email, email doesn't already exist).
     */
    public ObservableBooleanValue validProperty() {
        return Bindings.createBooleanBinding(
            () -> {
                String firstName = firstNameTextField.getText();
                String lastName = lastNameTextField.getText();
                String email = emailTextField.getText();
                return firstName != null && !firstName.trim().isEmpty()
                    && lastName != null && !lastName.trim().isEmpty()
                    && email != null && !email.trim().isEmpty() && email.contains("@");
            },
            firstNameTextField.textProperty(),
            lastNameTextField.textProperty(),
            emailTextField.textProperty()
        ).and(emailExistsProperty.not());
    }
}
