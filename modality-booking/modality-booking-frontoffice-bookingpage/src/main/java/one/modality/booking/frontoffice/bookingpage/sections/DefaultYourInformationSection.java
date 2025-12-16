package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.authn.AuthenticateWithUsernamePasswordCredentials;
import dev.webfx.stack.authn.AuthenticateWithVerificationCodeCredentials;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.SendMagicLinkCredentials;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

import java.util.function.Consumer;

/**
 * Default implementation of the "Your Information" section following an email-first login/registration flow.
 *
 * <p>State machine with 5 states:</p>
 * <ul>
 *   <li>EMAIL_INPUT: Initial email entry</li>
 *   <li>CHECKING_EMAIL: Loading while checking if email exists</li>
 *   <li>EXISTING_USER: Login with password (email found)</li>
 *   <li>NEW_USER: Registration form (email not found)</li>
 *   <li>FORGOT_PASSWORD: Verification code entry</li>
 * </ul>
 *
 * <p>This class can be extended to customize icons, text, or behavior.</p>
 *
 * @author Bruno Salmon
 */
public class DefaultYourInformationSection implements HasYourInformationSection {

    // === STATE MACHINE ===
    public enum FlowState {
        EMAIL_INPUT,
        CHECKING_EMAIL,
        EXISTING_USER,
        NEW_USER,
        FORGOT_PASSWORD
    }

    protected final ObjectProperty<FlowState> flowStateProperty = new SimpleObjectProperty<>(FlowState.EMAIL_INPUT);

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty emailValidProperty = new SimpleBooleanProperty(false);

    // === FORM DATA ===
    protected final StringProperty emailProperty = new SimpleStringProperty("");
    protected final StringProperty passwordProperty = new SimpleStringProperty("");
    protected final StringProperty firstNameProperty = new SimpleStringProperty("");
    protected final StringProperty lastNameProperty = new SimpleStringProperty("");
    protected final BooleanProperty createAccountProperty = new SimpleBooleanProperty(false);
    protected final StringProperty verificationCodeProperty = new SimpleStringProperty("");
    protected final BooleanProperty showPasswordProperty = new SimpleBooleanProperty(false);

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected final StackPane contentContainer = new StackPane();

    // Form fields (created once, reused across views)
    protected TextField emailField;
    protected PasswordField passwordField;
    protected TextField visiblePasswordField;
    protected TextField firstNameField;
    protected TextField lastNameField;
    protected TextField[] codeDigitFields;  // 6 separate digit input fields
    protected HBox codeDigitsContainer;     // Container for the 6 digit fields
    protected CheckBox createAccountCheckBox;
    protected Button signInButton;
    protected Hyperlink resendLink;           // Resend code link
    protected Timeline resendCountdownTimer;  // Timer for resend cooldown
    protected int resendSecondsRemaining;     // Countdown seconds remaining
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    // Rate limiting for login/code attempts (security against brute force)
    protected int loginAttemptCount = 0;
    protected long lastLoginAttemptTime = 0;
    protected int codeAttemptCount = 0;
    protected long lastCodeAttemptTime = 0;
    protected Timeline loginCooldownTimer;
    protected Timeline codeCooldownTimer;
    private static final int INITIAL_COOLDOWN_SECONDS = 5;    // First 5 attempts
    private static final int EXTENDED_COOLDOWN_SECONDS = 30;  // After 5 attempts
    private static final int ATTEMPT_THRESHOLD = 5;           // When to switch to extended cooldown

    // View containers
    protected VBox emailInputView;
    protected VBox existingUserView;
    protected VBox newUserView;
    protected VBox forgotPasswordView;
    protected VBox checkingView;

    // Error labels
    protected Label emailErrorLabel;
    protected Label passwordErrorLabel;
    protected Label firstNameErrorLabel;
    protected Label lastNameErrorLabel;
    protected Label codeErrorLabel;

    // Responsive design
    protected ResponsiveDesign responsiveDesign;
    protected GridPane nameFieldsGrid;

    // Create Account box - color-scheme-aware components
    protected SVGPath createAccountUserIcon;
    protected Label createAccountTitleLabel;
    protected SVGPath createAccountNoteIcon;
    protected java.util.List<SVGPath> benefitCheckIcons = new java.util.ArrayList<>();
    protected java.util.List<Label> benefitLabels = new java.util.ArrayList<>();

    // === CALLBACKS ===
    protected Consumer<Person> onLoginSuccess;
    protected Consumer<NewUserData> onNewUserContinue;
    protected Runnable onBackPressed;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;
    protected boolean emailExists;

    public DefaultYourInformationSection() {
        buildUI();
        setupStateListener();
        setupResponsiveDesign();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-your-information-section");

        // Create all form fields
        createFormFields();

        // Create all views
        emailInputView = buildEmailInputView();
        existingUserView = buildExistingUserView();
        newUserView = buildNewUserView();
        forgotPasswordView = buildForgotPasswordView();
        checkingView = buildCheckingView();

        // Initially show email input view
        contentContainer.getChildren().add(emailInputView);
        container.getChildren().add(contentContainer);
    }

    protected void createFormFields() {
        // Email field
        emailField = new TextField();
        I18n.bindI18nPromptProperty(emailField.promptTextProperty(), BookingPageI18nKeys.EmailPlaceholder);
        emailField.textProperty().bindBidirectional(emailProperty);
        styleInput(emailField);

        // Update email validity when email changes
        emailProperty.addListener((obs, oldVal, newVal) -> emailValidProperty.set(isValidEmail(newVal)));

        emailErrorLabel = createErrorLabel();

        // Password fields (both visible and hidden versions)
        passwordField = new PasswordField();
        I18n.bindI18nPromptProperty(passwordField.promptTextProperty(), BookingPageI18nKeys.EnterYourPassword);
        passwordField.textProperty().bindBidirectional(passwordProperty);
        styleInput(passwordField);

        visiblePasswordField = new TextField();
        I18n.bindI18nPromptProperty(visiblePasswordField.promptTextProperty(), BookingPageI18nKeys.EnterYourPassword);
        visiblePasswordField.textProperty().bindBidirectional(passwordProperty);
        styleInput(visiblePasswordField);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        // Bind visibility to showPassword property
        showPasswordProperty.addListener((obs, old, show) -> {
            passwordField.setVisible(!show);
            passwordField.setManaged(!show);
            visiblePasswordField.setVisible(show);
            visiblePasswordField.setManaged(show);
        });

        passwordErrorLabel = createErrorLabel();

        // Name fields
        firstNameField = new TextField();
        I18n.bindI18nPromptProperty(firstNameField.promptTextProperty(), BookingPageI18nKeys.FirstNamePlaceholder);
        firstNameField.textProperty().bindBidirectional(firstNameProperty);
        styleInput(firstNameField);
        firstNameErrorLabel = createErrorLabel();

        lastNameField = new TextField();
        I18n.bindI18nPromptProperty(lastNameField.promptTextProperty(), BookingPageI18nKeys.LastNamePlaceholder);
        lastNameField.textProperty().bindBidirectional(lastNameProperty);
        styleInput(lastNameField);
        lastNameErrorLabel = createErrorLabel();

        // Create account checkbox
        createAccountCheckBox = new CheckBox();
        createAccountCheckBox.selectedProperty().bindBidirectional(createAccountProperty);

        // Verification code fields - 6 separate digit boxes
        codeDigitFields = new TextField[6];
        codeDigitsContainer = new HBox(8);
        codeDigitsContainer.setAlignment(Pos.CENTER);

        for (int i = 0; i < 6; i++) {
            final int index = i;
            TextField digitField = new TextField();
            digitField.setPrefWidth(48);
            digitField.setMaxWidth(48);
            digitField.setAlignment(Pos.CENTER);
            digitField.getStyleClass().add("bookingpage-digit-field");
            digitField.setPadding(new Insets(12, 0, 12, 0));

            // Handle input - only allow single digit
            digitField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    // Handle paste of multiple digits
                    String digits = newVal.replaceAll("\\D", "");
                    if (digits.length() > 1) {
                        // Distribute pasted digits across fields
                        distributePastedCode(digits, index);
                        return;
                    }
                    // Single digit - keep only first digit
                    if (digits.length() == 1) {
                        if (!digits.equals(newVal)) {
                            digitField.setText(digits);
                        }
                        // Auto-advance to next field
                        if (index < 5) {
                            codeDigitFields[index + 1].requestFocus();
                        }
                        // Update combined code and check for auto-validation
                        updateVerificationCodeFromFields();
                    } else {
                        digitField.setText("");
                    }
                } else {
                    updateVerificationCodeFromFields();
                }
            });

            // Handle backspace to go to previous field
            digitField.setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.BACK_SPACE && digitField.getText().isEmpty() && index > 0) {
                    codeDigitFields[index - 1].requestFocus();
                    codeDigitFields[index - 1].clear();
                }
            });

            // Set initial border styling via CSS class
            digitField.getStyleClass().add("bookingpage-input-bordered");

            // Focus styling - toggle CSS class on focus (theme colors via CSS variables)
            digitField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    digitField.getStyleClass().remove("bookingpage-input-bordered");
                    digitField.getStyleClass().add("bookingpage-input-focused");
                } else {
                    digitField.getStyleClass().remove("bookingpage-input-focused");
                    digitField.getStyleClass().add("bookingpage-input-bordered");
                }
            });

            codeDigitFields[i] = digitField;
            codeDigitsContainer.getChildren().add(digitField);
        }

        codeErrorLabel = createErrorLabel();
    }

    protected void setupStateListener() {
        flowStateProperty.addListener((obs, oldState, newState) -> {
            contentContainer.getChildren().clear();

            switch (newState) {
                case EMAIL_INPUT:
                    contentContainer.getChildren().add(emailInputView);
                    clearErrors();
                    break;
                case CHECKING_EMAIL:
                    contentContainer.getChildren().add(checkingView);
                    break;
                case EXISTING_USER:
                    contentContainer.getChildren().add(existingUserView);
                    clearErrors();
                    passwordProperty.set("");
                    break;
                case NEW_USER:
                    contentContainer.getChildren().add(newUserView);
                    clearErrors();
                    break;
                case FORGOT_PASSWORD:
                    contentContainer.getChildren().add(forgotPasswordView);
                    clearErrors();
                    clearCodeDigitFields();
                    break;
            }

            updateValidity();
        });
    }

    protected void setupResponsiveDesign() {
        responsiveDesign = new ResponsiveDesign(container);

        // Desktop: Two-column name fields (width >= 600)
        responsiveDesign.addResponsiveLayout(
                width -> width >= 600,
                () -> {
                    if (nameFieldsGrid != null) {
                        nameFieldsGrid.getColumnConstraints().clear();
                        nameFieldsGrid.getColumnConstraints().addAll(
                                createColumnConstraint(),
                                createColumnConstraint()
                        );
                    }
                }
        );

        // Mobile: Single-column name fields (width < 600)
        responsiveDesign.addResponsiveLayout(
                width -> width < 600,
                () -> {
                    if (nameFieldsGrid != null) {
                        nameFieldsGrid.getColumnConstraints().clear();
                        nameFieldsGrid.getColumnConstraints().add(createColumnConstraint());
                    }
                }
        );

        responsiveDesign.start();
    }

    protected ColumnConstraints createColumnConstraint() {
        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        col.setHalignment(HPos.LEFT);
        col.setFillWidth(true);
        return col;
    }

    // ========================================
    // EMAIL INPUT VIEW
    // ========================================
    protected VBox buildEmailInputView() {
        VBox view = new VBox(0);
        view.setAlignment(Pos.TOP_CENTER);

        // Title
        Label title = createPageTitle(BookingPageI18nKeys.YourInformation);

        // Subtitle
        Label subtitle = createPageSubtitle(BookingPageI18nKeys.EnterYourEmailToGetStarted);

        // Email card
        VBox emailCard = createCard();
        VBox emailFieldContainer = new VBox(8);
        Label emailLabel = createFieldLabel(BookingPageI18nKeys.EmailAddress);
        emailFieldContainer.getChildren().addAll(emailLabel, emailField, emailErrorLabel);

        // Hint text
        Label hintLabel = createHintLabel(BookingPageI18nKeys.WeWillCheckIfYouHaveAnExistingAccount);

        emailCard.getChildren().addAll(emailFieldContainer, hintLabel);

        // Navigation buttons
        HBox buttonRow = createNavigationButtonRow();
        Button backButton = createBackButton();
        backButton.setOnAction(e -> {
            if (onBackPressed != null) onBackPressed.run();
        });

        Button continueButton = createPrimaryButton(BookingPageI18nKeys.Continue);
        continueButton.setOnAction(e -> handleEmailCheck());
        // Disable continue button when email is invalid
        continueButton.disableProperty().bind(emailValidProperty.not());

        // Allow Enter key to trigger continue
        emailField.setOnAction(e -> {
            if (emailValidProperty.get()) {
                handleEmailCheck();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonRow.getChildren().addAll(backButton, spacer, continueButton);

        view.getChildren().addAll(title, subtitle, emailCard, buttonRow);
        VBox.setMargin(subtitle, new Insets(0, 0, 40, 0));
        VBox.setMargin(emailCard, new Insets(0, 0, 24, 0));
        VBox.setMargin(buttonRow, new Insets(24, 0, 0, 0));

        return view;
    }

    // ========================================
    // EXISTING USER (LOGIN) VIEW
    // ========================================
    protected VBox buildExistingUserView() {
        VBox view = new VBox(0);
        view.setAlignment(Pos.TOP_CENTER);

        // Title
        Label title = createPageTitle(BookingPageI18nKeys.WelcomeBack);

        // Subtitle
        Label subtitle = createPageSubtitle(BookingPageI18nKeys.SignInToContinueWithYourBooking);

        // Account found banner
        HBox accountFoundBanner = buildAccountFoundBanner();

        // Password card
        VBox passwordCard = createCard();

        VBox passwordFieldContainer = new VBox(8);
        Label passwordLabel = createFieldLabel(BookingPageI18nKeys.Password);

        // Password field with eye toggle
        HBox passwordRow = new HBox(0);
        passwordRow.setAlignment(Pos.CENTER_LEFT);
        StackPane passwordStack = new StackPane();
        passwordStack.getChildren().addAll(passwordField, visiblePasswordField);
        HBox.setHgrow(passwordStack, Priority.ALWAYS);

        Button eyeButton = createEyeToggleButton();
        passwordRow.getChildren().addAll(passwordStack, eyeButton);

        passwordFieldContainer.getChildren().addAll(passwordLabel, passwordRow, passwordErrorLabel);

        // Forgot password link
        Hyperlink forgotLink = createHyperlink(BookingPageI18nKeys.ForgotYourPassword);
        forgotLink.setOnAction(e -> handleForgotPassword());

        passwordCard.getChildren().addAll(passwordFieldContainer, forgotLink);

        // Navigation buttons
        HBox buttonRow = createNavigationButtonRow();
        Button backButton = createBackButton();
        backButton.setOnAction(e -> resetToEmailInput());

        signInButton = createPrimaryButton(BookingPageI18nKeys.SignIn);
        signInButton.setOnAction(e -> handleLogin());
        // Disable sign in when password is empty
        signInButton.disableProperty().bind(passwordProperty.isEmpty());

        // Allow Enter key to trigger sign in
        passwordField.setOnAction(e -> {
            if (!passwordProperty.get().isEmpty()) {
                handleLogin();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonRow.getChildren().addAll(backButton, spacer, signInButton);

        view.getChildren().addAll(title, subtitle, accountFoundBanner, passwordCard, buttonRow);
        VBox.setMargin(subtitle, new Insets(0, 0, 40, 0));
        VBox.setMargin(accountFoundBanner, new Insets(0, 0, 24, 0));
        VBox.setMargin(passwordCard, new Insets(0, 0, 24, 0));
        VBox.setMargin(buttonRow, new Insets(24, 0, 0, 0));

        return view;
    }

    protected HBox buildAccountFoundBanner() {

        HBox banner = new HBox(12);
        banner.setAlignment(Pos.TOP_LEFT);
        banner.setPadding(new Insets(16, 20, 16, 20));
        // CSS class handles theming - no method call needed
        banner.getStyleClass().add("bookingpage-info-box-info");

        // Checkmark circle
        StackPane checkCircle = createCheckmarkCircle();

        // Content
        VBox content = new VBox(4);
        Label foundLabel = I18nControls.newLabel(BookingPageI18nKeys.AccountFound);
        foundLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label emailDisplayLabel = new Label();
        emailDisplayLabel.textProperty().bind(emailProperty);
        emailDisplayLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");

        Hyperlink changeEmailLink = createHyperlink(BookingPageI18nKeys.UseDifferentEmail);
        changeEmailLink.setOnAction(e -> resetToEmailInput());

        content.getChildren().addAll(foundLabel, emailDisplayLabel, changeEmailLink);
        HBox.setHgrow(content, Priority.ALWAYS);

        banner.getChildren().addAll(checkCircle, content);

        // CSS class handles theming - no listener needed
        banner.getStyleClass().add("bookingpage-info-box-info");

        return banner;
    }

    protected StackPane createCheckmarkCircle() {
        // Uses primary solid circle with white checkmark - CSS handles theming
        StackPane circle = BookingPageUIBuilder.createThemedIconCircle(24);
        circle.getStyleClass().add("bookingpage-icon-circle-primary");

        SVGPath checkmark = new SVGPath();
        checkmark.setContent("M20 6L9 17l-5-5");
        checkmark.setStroke(Color.WHITE);
        checkmark.setStrokeWidth(3);
        checkmark.setFill(Color.TRANSPARENT);
        checkmark.setScaleX(0.5);
        checkmark.setScaleY(0.5);

        circle.getChildren().add(checkmark);
        return circle;
    }

    protected Button createEyeToggleButton() {
        Button btn = new Button();
        btn.getStyleClass().add("bookingpage-btn-link");
        btn.setPadding(new Insets(4, 12, 4, 12));

        // SVG eye icon
        SVGPath eyeOpen = new SVGPath();
        eyeOpen.setContent("M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z M12 9a3 3 0 100 6 3 3 0 000-6z");
        eyeOpen.setStroke(Color.web("#6c757d"));
        eyeOpen.setStrokeWidth(2);
        eyeOpen.setFill(Color.TRANSPARENT);
        eyeOpen.setScaleX(0.8);
        eyeOpen.setScaleY(0.8);

        btn.setGraphic(eyeOpen);
        btn.setOnAction(e -> showPasswordProperty.set(!showPasswordProperty.get()));

        return btn;
    }

    // ========================================
    // NEW USER (REGISTRATION) VIEW
    // ========================================
    protected VBox buildNewUserView() {
        VBox view = new VBox(0);
        view.setAlignment(Pos.TOP_CENTER);

        // Title
        Label title = createPageTitle(BookingPageI18nKeys.YourDetails);

        // Subtitle
        Label subtitle = createPageSubtitle(BookingPageI18nKeys.CompleteYourInformationToContinue);

        // Email display box
        HBox emailDisplayBox = buildEmailDisplayBox();

        // Name fields card
        VBox nameCard = createCard();
        nameFieldsGrid = new GridPane();
        nameFieldsGrid.setHgap(20);
        nameFieldsGrid.setVgap(0);

        // First name column
        VBox firstNameContainer = new VBox(8);
        HBox firstNameLabelBox = createRequiredFieldLabel(BookingPageI18nKeys.FirstName);
        firstNameContainer.getChildren().addAll(firstNameLabelBox, firstNameField, firstNameErrorLabel);

        // Last name column
        VBox lastNameContainer = new VBox(8);
        HBox lastNameLabelBox = createRequiredFieldLabel(BookingPageI18nKeys.LastName);
        lastNameContainer.getChildren().addAll(lastNameLabelBox, lastNameField, lastNameErrorLabel);

        // Add to grid (will be rearranged by responsive design)
        nameFieldsGrid.add(firstNameContainer, 0, 0);
        nameFieldsGrid.add(lastNameContainer, 1, 0);
        nameFieldsGrid.getColumnConstraints().addAll(createColumnConstraint(), createColumnConstraint());

        nameCard.getChildren().add(nameFieldsGrid);

        // Create account option box
        VBox createAccountBox = buildCreateAccountBox();

        // Navigation buttons
        HBox buttonRow = createNavigationButtonRow();
        Button backButton = createBackButton();
        backButton.setOnAction(e -> resetToEmailInput());

        Button continueButton = createPrimaryButton(BookingPageI18nKeys.Continue);
        continueButton.setOnAction(e -> handleNewUserContinue());
        // Disable continue when names are not filled (min 2 chars each)
        BooleanProperty namesValid = new SimpleBooleanProperty(false);
        FXProperties.runNowAndOnPropertiesChange(() -> namesValid.set(
                firstNameProperty.get().trim().length() >= 2 &&
                        lastNameProperty.get().trim().length() >= 2
        ), firstNameProperty, lastNameProperty);
        continueButton.disableProperty().bind(namesValid.not());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonRow.getChildren().addAll(backButton, spacer, continueButton);

        view.getChildren().addAll(title, subtitle, emailDisplayBox, nameCard, createAccountBox, buttonRow);
        VBox.setMargin(subtitle, new Insets(0, 0, 40, 0));
        VBox.setMargin(emailDisplayBox, new Insets(0, 0, 24, 0));
        VBox.setMargin(nameCard, new Insets(0, 0, 24, 0));
        VBox.setMargin(createAccountBox, new Insets(0, 0, 24, 0));
        VBox.setMargin(buttonRow, new Insets(24, 0, 0, 0));

        return view;
    }

    protected HBox buildEmailDisplayBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 18, 14, 18));
        box.getStyleClass().add("bookingpage-card-light");

        // Email icon
        SVGPath emailIcon = new SVGPath();
        emailIcon.setContent("M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z M22 6l-10 7L2 6");
        emailIcon.setStroke(Color.web("#6c757d"));
        emailIcon.setStrokeWidth(2);
        emailIcon.setFill(Color.TRANSPARENT);
        emailIcon.setScaleX(0.7);
        emailIcon.setScaleY(0.7);

        Label emailDisplay = new Label();
        emailDisplay.textProperty().bind(emailProperty);
        emailDisplay.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-secondary");
        HBox.setHgrow(emailDisplay, Priority.ALWAYS);

        Hyperlink changeLink = createHyperlink(BookingPageI18nKeys.Change);
        changeLink.setOnAction(e -> resetToEmailInput());

        box.getChildren().addAll(emailIcon, emailDisplay, changeLink);
        return box;
    }

    protected VBox buildCreateAccountBox() {
        BookingFormColorScheme colors = colorScheme.get();

        // Clear any existing references
        benefitCheckIcons.clear();
        benefitLabels.clear();

        VBox box = new VBox(0);
        box.setPadding(new Insets(24));
        box.setCursor(Cursor.HAND);
        updateCreateAccountBoxStyle(box);

        // Make clickable
        box.setOnMouseClicked(e -> createAccountProperty.set(!createAccountProperty.get()));

        HBox contentRow = new HBox(16);
        contentRow.setAlignment(Pos.TOP_LEFT);

        // Custom checkbox
        StackPane checkbox = createCustomCheckbox();

        // Content
        VBox content = new VBox(8);

        // Title with icon
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        createAccountUserIcon = new SVGPath();
        createAccountUserIcon.setContent("M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2 M12 3a4 4 0 100 8 4 4 0 000-8z");
        createAccountUserIcon.getStyleClass().add("bookingpage-icon-primary");
        createAccountUserIcon.setStrokeWidth(2);
        createAccountUserIcon.setFill(Color.TRANSPARENT);
        createAccountUserIcon.setScaleX(0.8);
        createAccountUserIcon.setScaleY(0.8);

        createAccountTitleLabel = I18nControls.newLabel(BookingPageI18nKeys.CreateAnAccount);
        createAccountTitleLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");

        titleRow.getChildren().addAll(createAccountUserIcon, createAccountTitleLabel);

        // Description
        Label descLabel = I18nControls.newLabel(BookingPageI18nKeys.CreateAccountBenefit);
        descLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");
        descLabel.setWrapText(true);

        // Benefits tags
        HBox benefitsRow = new HBox(8);
        benefitsRow.setAlignment(Pos.CENTER_LEFT);
        benefitsRow.getChildren().addAll(
                createBenefitTag(BookingPageI18nKeys.SkipFormsNextTime, colors),
                createBenefitTag(BookingPageI18nKeys.BookForFamilyAndFriends, colors),
                createBenefitTag(BookingPageI18nKeys.ViewBookingHistory, colors)
        );

        content.getChildren().addAll(titleRow, descLabel, benefitsRow);

        // Email note (shown when checked)
        VBox emailNote = new VBox(0);
        emailNote.setPadding(new Insets(16, 0, 0, 0));
        emailNote.visibleProperty().bind(createAccountProperty);
        emailNote.managedProperty().bind(createAccountProperty);

        HBox noteBox = new HBox(10);
        noteBox.setPadding(new Insets(12, 14, 12, 14));
        noteBox.getStyleClass().addAll("bookingpage-card", "bookingpage-rounded");
        noteBox.setAlignment(Pos.TOP_LEFT);

        createAccountNoteIcon = new SVGPath();
        createAccountNoteIcon.setContent("M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z M22 6l-10 7L2 6");
        createAccountNoteIcon.getStyleClass().add("bookingpage-icon-primary");
        createAccountNoteIcon.setStrokeWidth(2);
        createAccountNoteIcon.setFill(Color.TRANSPARENT);
        createAccountNoteIcon.setScaleX(0.65);
        createAccountNoteIcon.setScaleY(0.65);

        Label noteText = I18nControls.newLabel(BookingPageI18nKeys.WeWillSendEmailToSetPassword);
        noteText.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-secondary");
        noteText.setWrapText(true);

        noteBox.getChildren().addAll(createAccountNoteIcon, noteText);
        HBox.setHgrow(noteText, Priority.ALWAYS);

        emailNote.getChildren().add(noteBox);
        content.getChildren().add(emailNote);

        contentRow.getChildren().addAll(checkbox, content);
        HBox.setHgrow(content, Priority.ALWAYS);
        box.getChildren().add(contentRow);

        // Update style when checkbox changes
        createAccountProperty.addListener((obs, old, checked) -> updateCreateAccountBoxStyle(box));

        // Note: Color scheme listener removed - CSS handles theme changes via CSS variables

        return box;
    }

    protected void updateCreateAccountBoxStyle(VBox box) {
        boolean checked = createAccountProperty.get();
        // Use CSS class with theme variables - toggle 'selected' class for checked state
        box.getStyleClass().removeAll("bookingpage-selectable-card", "selected");
        box.getStyleClass().add("bookingpage-selectable-card");
        if (checked) {
            box.getStyleClass().add("selected");
        }
    }

    protected StackPane createCustomCheckbox() {
        StackPane checkbox = new StackPane();
        checkbox.setMinSize(24, 24);
        checkbox.setMaxSize(24, 24);

        // Checkmark icon (always present, shown/hidden via parent style)
        SVGPath checkIcon = new SVGPath();
        checkIcon.setContent("M20 6L9 17l-5-5");
        checkIcon.setStroke(Color.WHITE);
        checkIcon.setStrokeWidth(3);
        checkIcon.setFill(Color.TRANSPARENT);
        checkIcon.setScaleX(0.5);
        checkIcon.setScaleY(0.5);

        // Update checkbox appearance based on state using CSS classes
        Runnable updateCheckbox = () -> {
            boolean checked = createAccountProperty.get();

            checkbox.getStyleClass().removeAll("bookingpage-checkbox", "bookingpage-checkbox-selected");

            if (checked) {
                checkbox.getStyleClass().add("bookingpage-checkbox-selected");
                if (!checkbox.getChildren().contains(checkIcon)) {
                    checkbox.getChildren().add(checkIcon);
                }
            } else {
                checkbox.getStyleClass().add("bookingpage-checkbox");
                checkbox.getChildren().remove(checkIcon);
            }
        };

        createAccountProperty.addListener((obs, old, checked) -> updateCheckbox.run());
        updateCheckbox.run();

        return checkbox;
    }

    protected HBox createBenefitTag(Object i18nKey, BookingFormColorScheme colors) {
        HBox tag = new HBox(6);
        tag.setAlignment(Pos.CENTER_LEFT);
        tag.setPadding(new Insets(4, 10, 4, 10));
        tag.getStyleClass().add("bookingpage-benefit-tag");

        SVGPath check = new SVGPath();
        check.setContent("M20 6L9 17l-5-5");
        check.setStroke(colors.getPrimary());
        check.setStrokeWidth(3);
        check.setFill(Color.TRANSPARENT);
        check.setScaleX(0.4);
        check.setScaleY(0.4);

        // Store reference for color scheme updates
        benefitCheckIcons.add(check);

        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-dark");

        // Store reference for potential updates
        benefitLabels.add(label);

        tag.getChildren().addAll(check, label);
        return tag;
    }

    // ========================================
    // FORGOT PASSWORD VIEW
    // ========================================
    protected VBox buildForgotPasswordView() {
        VBox view = new VBox(0);
        view.setAlignment(Pos.TOP_CENTER);

        // Title
        Label title = createPageTitle(BookingPageI18nKeys.CheckYourEmail);

        // Subtitle
        Label subtitle = createPageSubtitle(BookingPageI18nKeys.EnterVerificationCodeWeSentYou);

        // Combined info/warning box
        VBox emailInfoBox = buildEmailInfoBox();

        // Code input card
        VBox codeCard = createCard();

        VBox codeFieldContainer = new VBox(12);
        codeFieldContainer.setAlignment(Pos.CENTER);
        Label codeLabel = createFieldLabel(BookingPageI18nKeys.EnterVerificationCode);
        codeLabel.setAlignment(Pos.CENTER);
        codeLabel.setMaxWidth(Double.MAX_VALUE);
        codeFieldContainer.getChildren().addAll(codeLabel, codeDigitsContainer, codeErrorLabel);

        // Hint
        Label hintLabel = createHintLabel(BookingPageI18nKeys.EnterThe6DigitCodeFromYourEmail);
        hintLabel.setAlignment(Pos.CENTER);
        hintLabel.setMaxWidth(Double.MAX_VALUE);

        // Resend link with cooldown
        HBox resendRow = new HBox(4);
        resendRow.setAlignment(Pos.CENTER);
        Label didntReceiveLabel = I18nControls.newLabel(BookingPageI18nKeys.DidntReceiveIt);
        didntReceiveLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        resendLink = createHyperlink(BookingPageI18nKeys.ResendCode);
        resendLink.setOnAction(e -> {
            if (!resendLink.isDisabled()) {
                handleResendCode();
            }
        });
        resendRow.getChildren().addAll(didntReceiveLabel, resendLink);

        codeCard.getChildren().addAll(codeFieldContainer, hintLabel, resendRow);

        // Navigation buttons (only back button - verification is automatic)
        HBox buttonRow = createNavigationButtonRow();
        Button backButton = createBackButton();
        backButton.setOnAction(e -> flowStateProperty.set(FlowState.EXISTING_USER));
        buttonRow.getChildren().add(backButton);

        view.getChildren().addAll(title, subtitle, emailInfoBox, codeCard, buttonRow);
        VBox.setMargin(subtitle, new Insets(0, 0, 40, 0));
        VBox.setMargin(emailInfoBox, new Insets(0, 0, 24, 0));
        VBox.setMargin(codeCard, new Insets(0, 0, 24, 0));
        VBox.setMargin(buttonRow, new Insets(24, 0, 0, 0));

        return view;
    }

    /**
     * Builds a combined email info box with verification code info and warning.
     * Yellow/amber warning style.
     */
    protected VBox buildEmailInfoBox() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16, 20, 16, 20));
        box.getStyleClass().add("bookingpage-warning-box");

        // First row: Email sent info
        HBox emailRow = new HBox(10);
        emailRow.setAlignment(Pos.CENTER_LEFT);

        SVGPath emailIcon = new SVGPath();
        emailIcon.setContent("M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z M22 6l-10 7L2 6");
        emailIcon.setStroke(Color.web("#D97706"));
        emailIcon.setStrokeWidth(2);
        emailIcon.setFill(Color.TRANSPARENT);
        emailIcon.setScaleX(0.8);
        emailIcon.setScaleY(0.8);

        VBox emailContent = new VBox(2);
        Label text1 = I18nControls.newLabel(BookingPageI18nKeys.WeSentVerificationCodeTo);
        text1.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-warning");

        Label emailLabel = new Label();
        emailLabel.textProperty().bind(emailProperty);
        emailLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-warning");

        emailContent.getChildren().addAll(text1, emailLabel);
        emailRow.getChildren().addAll(emailIcon, emailContent);

        // Second row: Warning about timing (no icon, just indented text)
        Label warningText = I18nControls.newLabel(BookingPageI18nKeys.EmailMayTakeUpTo1Minute);
        warningText.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-warning");
        warningText.setWrapText(true);
        // Indent to align with the text above (after the email icon)
        VBox.setMargin(warningText, new Insets(0, 0, 0, 34));

        box.getChildren().addAll(emailRow, warningText);
        return box;
    }

    // ========================================
    // CHECKING VIEW (Loading)
    // ========================================
    protected VBox buildCheckingView() {
        VBox view = new VBox(20);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(60, 0, 60, 0));

        // Spinner
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(48, 48);

        Label checkingLabel = I18nControls.newLabel(BookingPageI18nKeys.Checking);
        checkingLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-text-muted");

        view.getChildren().addAll(spinner, checkingLabel);
        return view;
    }

    // ========================================
    // STYLING HELPERS
    // ========================================

    protected void styleInput(TextInputControl input) {
        input.setPadding(new Insets(14, 16, 14, 16));
        input.setMaxWidth(Double.MAX_VALUE);
        input.getStyleClass().addAll("bookingpage-input-bordered", "bookingpage-text-base");

        // Focus styling - toggle CSS class on focus
        input.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                input.getStyleClass().remove("bookingpage-input-bordered");
                input.getStyleClass().add("bookingpage-input-focused");
                input.setEffect(BookingPageUIBuilder.createFocusShadow());
            } else {
                input.getStyleClass().remove("bookingpage-input-focused");
                input.getStyleClass().add("bookingpage-input-bordered");
                input.setEffect(null);
            }
        });
    }

    protected Label createPageTitle(Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-bold", "bookingpage-text-dark");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    protected Label createPageSubtitle(Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    protected Label createFieldLabel(Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");
        return label;
    }

    /**
     * Creates an HBox containing the label followed by a red asterisk for required fields.
     */
    protected HBox createRequiredFieldLabel(Object i18nKey) {
        Label label = createFieldLabel(i18nKey);
        Text asterisk = new Text(" *");
        asterisk.setFill(Color.web("#dc3545")); // Danger red
        asterisk.setFont(Font.font(null, FontWeight.SEMI_BOLD, 14));
        HBox wrapper = new HBox(label, asterisk);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        return wrapper;
    }

    protected Label createHintLabel(Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        label.setWrapText(true);
        VBox.setMargin(label, new Insets(6, 0, 0, 0));
        return label;
    }

    protected Label createErrorLabel() {
        Label label = new Label();
        label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-danger");
        label.setWrapText(true);
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    protected Hyperlink createHyperlink(Object i18nKey) {
        Hyperlink link = BookingPageUIBuilder.createThemedHyperlink(i18nKey);
        return link;
    }

    protected VBox createCard() {
        VBox card = new VBox(24);
        card.setPadding(new Insets(32));
        card.getStyleClass().addAll("bookingpage-card-static", "bookingpage-rounded-lg");
        return card;
    }

    /**
     * Plays a subtle shake animation on a node to indicate an error.
     * The animation moves the node horizontally 3 times with decreasing amplitude.
     */
    protected void playShakeAnimation(Node node) {
        Timeline shake = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), -10, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), 10, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), -8, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), 8, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(250), new KeyValue(node.translateXProperty(), -4, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), 0, Interpolator.EASE_BOTH))
        );
        shake.play();
    }

    /**
     * Gets the cooldown duration in seconds based on the number of failed attempts.
     */
    protected int getCooldownSeconds(int attemptCount) {
        return attemptCount >= ATTEMPT_THRESHOLD ? EXTENDED_COOLDOWN_SECONDS : INITIAL_COOLDOWN_SECONDS;
    }

    /**
     * Checks if a login attempt is allowed based on rate limiting.
     * Returns the number of seconds to wait, or 0 if attempt is allowed.
     */
    protected int getLoginCooldownRemaining() {
        if (loginAttemptCount == 0) {
            return 0;
        }
        int cooldownSeconds = getCooldownSeconds(loginAttemptCount);
        long elapsedSeconds = (System.currentTimeMillis() - lastLoginAttemptTime) / 1000;
        int remaining = (int) (cooldownSeconds - elapsedSeconds);
        return Math.max(0, remaining);
    }

    /**
     * Checks if a code verification attempt is allowed based on rate limiting.
     * Returns the number of seconds to wait, or 0 if attempt is allowed.
     */
    protected int getCodeCooldownRemaining() {
        if (codeAttemptCount == 0) {
            return 0;
        }
        int cooldownSeconds = getCooldownSeconds(codeAttemptCount);
        long elapsedSeconds = (System.currentTimeMillis() - lastCodeAttemptTime) / 1000;
        int remaining = (int) (cooldownSeconds - elapsedSeconds);
        return Math.max(0, remaining);
    }

    /**
     * Starts a cooldown timer that disables the sign-in button and shows countdown.
     */
    protected void startLoginCooldown() {
        int cooldownSeconds = getCooldownSeconds(loginAttemptCount);

        // Stop any existing timer
        if (loginCooldownTimer != null) {
            loginCooldownTimer.stop();
        }

        // Disable button and show countdown
        signInButton.setDisable(true);
        final int[] secondsRemaining = {cooldownSeconds};

        // Update button text with countdown
        Runnable updateButtonText = () -> signInButton.setText(I18n.getI18nText(BookingPageI18nKeys.WaitSeconds, secondsRemaining[0]));
        updateButtonText.run();

        loginCooldownTimer = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                secondsRemaining[0]--;
                if (secondsRemaining[0] > 0) {
                    updateButtonText.run();
                } else {
                    loginCooldownTimer.stop();
                    signInButton.setDisable(false);
                    I18nControls.bindI18nTextProperty(signInButton, BookingPageI18nKeys.SignIn);
                }
            })
        );
        loginCooldownTimer.setCycleCount(cooldownSeconds);
        loginCooldownTimer.play();
    }

    /**
     * Starts a cooldown timer for code verification that disables input fields.
     */
    protected void startCodeCooldown() {
        int cooldownSeconds = getCooldownSeconds(codeAttemptCount);

        // Stop any existing timer
        if (codeCooldownTimer != null) {
            codeCooldownTimer.stop();
        }

        // Disable digit fields
        for (TextField field : codeDigitFields) {
            field.setDisable(true);
        }

        final int[] secondsRemaining = {cooldownSeconds};

        // Show countdown in error label
        Runnable updateMessage = () -> showError(codeErrorLabel, I18n.getI18nText(BookingPageI18nKeys.NextAttemptIn, secondsRemaining[0]));
        updateMessage.run();

        codeCooldownTimer = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                secondsRemaining[0]--;
                if (secondsRemaining[0] > 0) {
                    updateMessage.run();
                } else {
                    codeCooldownTimer.stop();
                    // Re-enable digit fields
                    for (TextField field : codeDigitFields) {
                        field.setDisable(false);
                    }
                    clearError(codeErrorLabel);
                    codeDigitFields[0].requestFocus();
                }
            })
        );
        codeCooldownTimer.setCycleCount(cooldownSeconds);
        codeCooldownTimer.play();
    }

    /**
     * Resets login attempt counter (called on successful login).
     */
    protected void resetLoginAttempts() {
        loginAttemptCount = 0;
        lastLoginAttemptTime = 0;
        if (loginCooldownTimer != null) {
            loginCooldownTimer.stop();
        }
    }

    /**
     * Resets code attempt counter (called on successful verification).
     */
    protected void resetCodeAttempts() {
        codeAttemptCount = 0;
        lastCodeAttemptTime = 0;
        if (codeCooldownTimer != null) {
            codeCooldownTimer.stop();
        }
    }

    protected HBox createNavigationButtonRow() {
        return BookingPageUIBuilder.createNavigationButtonRow();
    }

    protected Button createBackButton() {
        return BookingPageUIBuilder.createBackButton(BookingPageI18nKeys.Back);
    }

    protected Button createPrimaryButton(Object i18nKey) {
        return BookingPageUIBuilder.createPrimaryButton(i18nKey, colorScheme);
    }

    // ========================================
    // ACTION HANDLERS
    // ========================================

    public void handleEmailCheck() {
        String email = emailProperty.get().trim().toLowerCase();

        // Validate email
        if (email.isEmpty() || !isValidEmail(email)) {
            showError(emailErrorLabel, I18n.getI18nText(BookingPageI18nKeys.InvalidEmail));
            return;
        }

        clearError(emailErrorLabel);
        flowStateProperty.set(FlowState.CHECKING_EMAIL);

        // Check if email exists in database (Person with owner=true means they have an account)
        EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
            .<Person>executeQuery("select id from Person where lower(email)=? and owner=true and removed!=true limit 1", email)
            .onFailure(error -> UiScheduler.runInUiThread(() -> {
                Console.log("Error checking email existence: " + error.getMessage());
                // On error, default to new user flow (guest checkout)
                emailExists = false;
                flowStateProperty.set(FlowState.NEW_USER);
            }))
            .onSuccess(persons -> UiScheduler.runInUiThread(() -> {
                emailExists = !persons.isEmpty();
                flowStateProperty.set(emailExists ? FlowState.EXISTING_USER : FlowState.NEW_USER);
            }));
    }

    public void handleLogin() {
        String password = passwordProperty.get();

        if (password == null || password.isEmpty()) {
            showError(passwordErrorLabel, I18n.getI18nText(BookingPageI18nKeys.PasswordRequired));
            return;
        }

        // Check rate limiting
        int cooldownRemaining = getLoginCooldownRemaining();
        if (cooldownRemaining > 0) {
            showError(passwordErrorLabel, I18n.getI18nText(BookingPageI18nKeys.WaitSeconds, cooldownRemaining));
            playShakeAnimation(signInButton);
            return;
        }

        clearError(passwordErrorLabel);

        // Authenticate
        AuthenticateWithUsernamePasswordCredentials credentials = new AuthenticateWithUsernamePasswordCredentials(
                emailProperty.get(), password);

        // Unbind the signInButton's disableProperty so AsyncSpinner can set it
        signInButton.disableProperty().unbind();
        signInButton.textProperty().unbind();

        // Wrap authentication with spinner
        AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                AuthenticationService.authenticate(credentials)
                        .onFailure(error -> UiScheduler.runInUiThread(() -> {
                            // Track failed attempt
                            loginAttemptCount++;
                            lastLoginAttemptTime = System.currentTimeMillis();

                            showError(passwordErrorLabel, I18n.getI18nText(BookingPageI18nKeys.IncorrectPassword));
                            playShakeAnimation(passwordField.isVisible() ? passwordField : visiblePasswordField);

                            // Start cooldown timer - defer to ensure it runs AFTER AsyncSpinner restores button state
                            UiScheduler.scheduleDeferred(this::startLoginCooldown);
                        }))
                        .onSuccess(userId -> UiScheduler.runInUiThread(() -> {
                            // Reset rate limiting on success
                            resetLoginAttempts();

                            passwordProperty.set(""); // Clear password
                            signInButton.disableProperty().bind(passwordProperty.isEmpty());
                            if (onLoginSuccess != null) {
                                Person userPerson = FXUserPerson.getUserPerson();
                                if (userPerson != null) {
                                    onLoginSuccess.accept(userPerson);
                                } else {
                                    FXProperties.runOnPropertyChange(person -> {
                                        if (person != null) {
                                            onLoginSuccess.accept(person);
                                        }
                                    }, FXUserPerson.userPersonProperty());
                                }
                            }
                        })),
                signInButton);
    }

    public void handleNewUserContinue() {
        String firstName = firstNameProperty.get().trim();
        String lastName = lastNameProperty.get().trim();
        boolean hasError = false;

        if (firstName.length() < 2) {
            showError(firstNameErrorLabel, I18n.getI18nText(BookingPageI18nKeys.FirstNameRequired));
            hasError = true;
        } else {
            clearError(firstNameErrorLabel);
        }

        if (lastName.length() < 2) {
            showError(lastNameErrorLabel, I18n.getI18nText(BookingPageI18nKeys.LastNameRequired));
            hasError = true;
        } else {
            clearError(lastNameErrorLabel);
        }

        if (hasError) return;

        // Callback with user data
        if (onNewUserContinue != null) {
            NewUserData data = new NewUserData(
                    emailProperty.get(),
                    firstName,
                    lastName,
                    createAccountProperty.get()
            );
            onNewUserContinue.accept(data);
        }
    }

    protected void handleForgotPassword() {
        String email = emailProperty.get();
        if (email == null || email.isEmpty()) {
            return;
        }

        // Show the verification code input form
        flowStateProperty.set(FlowState.FORGOT_PASSWORD);

        // Send the magic link / verification code email
        SendMagicLinkCredentials credentials = new SendMagicLinkCredentials(
            email,
            WindowLocation.getOrigin(),   // clientOrigin
            null,                          // requestedPath
            I18n.getLanguage(),           // language
            true,                          // verificationCodeOnly (user will enter 6-digit code)
            null                           // context
        );

        AuthenticationService.authenticate(credentials)
            .onFailure(error -> Console.log("Failed to send verification code: " + error.getMessage()));

        // Start the cooldown timer (email is being sent, so user should wait before resending)
        // Delay slightly to ensure the view and resendLink are ready
        UiScheduler.scheduleDelay(100, this::startResendCooldown);
    }

    public void handleVerifyCode() {
        String code = verificationCodeProperty.get();

        if (code == null || code.length() != 6) {
            showError(codeErrorLabel, I18n.getI18nText(BookingPageI18nKeys.VerificationCodeRequired));
            return;
        }

        // Check rate limiting
        int cooldownRemaining = getCodeCooldownRemaining();
        if (cooldownRemaining > 0) {
            showError(codeErrorLabel, I18n.getI18nText(BookingPageI18nKeys.WaitSeconds, cooldownRemaining));
            playShakeAnimation(codeDigitsContainer);
            return;
        }

        clearError(codeErrorLabel);

        // Verify code on server
        AuthenticateWithVerificationCodeCredentials credentials =
            new AuthenticateWithVerificationCodeCredentials(code);

        AuthenticationService.authenticate(credentials)
            .onFailure(error -> {
                Console.log("Verification failed: " + error.getMessage());
                UiScheduler.runInUiThread(() -> {
                    // Track failed attempt
                    codeAttemptCount++;
                    lastCodeAttemptTime = System.currentTimeMillis();

                    showError(codeErrorLabel, I18n.getI18nText(BookingPageI18nKeys.InvalidOrExpiredCode));
                    playShakeAnimation(codeDigitsContainer);
                    clearCodeDigitFields();

                    // Start cooldown timer
                    startCodeCooldown();
                });
            })
            .onSuccess(userId -> {
                Console.log("Verification successful, user authenticated");
                UiScheduler.runInUiThread(() -> {
                    // Reset rate limiting on success
                    resetCodeAttempts();

                    // User is now authenticated - notify via onLoginSuccess callback
                    if (onLoginSuccess != null) {
                        Person userPerson = FXUserPerson.getUserPerson();
                        if (userPerson != null) {
                            onLoginSuccess.accept(userPerson);
                        } else {
                            // Wait for user person to be loaded
                            FXProperties.runOnPropertyChange(person -> {
                                if (person != null) {
                                    onLoginSuccess.accept(person);
                                }
                            }, FXUserPerson.userPersonProperty());
                        }
                    }
                });
            });
    }

    protected void handleResendCode() {
        // Clear all digit fields
        clearCodeDigitFields();

        // Resend the verification code email
        String email = emailProperty.get();
        if (email == null || email.isEmpty()) {
            return;
        }

        SendMagicLinkCredentials credentials = new SendMagicLinkCredentials(
            email,
            WindowLocation.getOrigin(),
            null,
            I18n.getLanguage(),
            true,
            null
        );

        AuthenticationService.authenticate(credentials)
            .onFailure(error -> Console.log("Failed to resend verification code: " + error.getMessage()));

        // Start the cooldown timer
        startResendCooldown();
    }

    /**
     * Starts the 60-second cooldown timer for the resend link.
     * Shows a countdown that reduces anxiety by showing the user exactly how long to wait.
     * Uses calming text "Resend in Xs" rather than aggressive countdown styling.
     */
    protected void startResendCooldown() {
        if (resendLink == null) {
            return;
        }

        // Stop any existing timer
        if (resendCountdownTimer != null) {
            resendCountdownTimer.stop();
        }

        // Initialize countdown
        resendSecondsRemaining = RESEND_COOLDOWN_SECONDS;

        // Unbind the i18n binding so we can set text directly
        resendLink.textProperty().unbind();

        // Disable the link and show initial countdown
        resendLink.setDisable(true);
        resendLink.setText(I18n.getI18nText(BookingPageI18nKeys.ResendInSeconds, resendSecondsRemaining));
        resendLink.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-disabled");
        resendLink.setTextFill(Color.web("#6c757d")); // Muted gray for disabled state

        // Create countdown timer (fires every second)
        resendCountdownTimer = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                resendSecondsRemaining--;
                if (resendSecondsRemaining > 0) {
                    // Update countdown text
                    resendLink.setText(I18n.getI18nText(BookingPageI18nKeys.ResendInSeconds, resendSecondsRemaining));
                } else {
                    // Cooldown complete - re-enable the link
                    resendCountdownTimer.stop();
                    resendLink.setDisable(false);
                    resendLink.getStyleClass().remove("bookingpage-text-disabled");
                    I18nControls.bindI18nTextProperty(resendLink, BookingPageI18nKeys.ResendCode);
                    BookingFormColorScheme colors = colorScheme.get();
                    Color linkColor = colors != null ? colors.getPrimary() : Color.web("#0d6efd");
                    resendLink.setTextFill(linkColor);
                }
            })
        );
        resendCountdownTimer.setCycleCount(RESEND_COOLDOWN_SECONDS);
        resendCountdownTimer.play();
    }

    /**
     * Clears all verification code digit fields.
     */
    protected void clearCodeDigitFields() {
        verificationCodeProperty.set("");
        if (codeDigitFields != null) {
            for (TextField field : codeDigitFields) {
                field.setText("");
            }
            // Focus first field
            codeDigitFields[0].requestFocus();
        }
    }

    /**
     * Distributes a pasted code across the digit fields starting from the given index.
     */
    protected void distributePastedCode(String digits, int startIndex) {
        // Distribute digits across fields
        for (int i = 0; i < digits.length() && (startIndex + i) < 6; i++) {
            codeDigitFields[startIndex + i].setText(String.valueOf(digits.charAt(i)));
        }
        // Focus the last filled field or the next empty one
        int lastIndex = Math.min(startIndex + digits.length() - 1, 5);
        if (lastIndex < 5 && digits.length() < (6 - startIndex)) {
            codeDigitFields[lastIndex + 1].requestFocus();
        } else {
            codeDigitFields[lastIndex].requestFocus();
        }
        updateVerificationCodeFromFields();
    }

    /**
     * Updates the verificationCodeProperty from the individual digit fields
     * and triggers auto-validation when all 6 digits are entered.
     */
    protected void updateVerificationCodeFromFields() {
        StringBuilder code = new StringBuilder();
        for (TextField field : codeDigitFields) {
            code.append(field.getText());
        }
        String fullCode = code.toString();
        verificationCodeProperty.set(fullCode);

        // Auto-validate when all 6 digits are entered
        if (fullCode.length() == 6) {
            // Use Platform.runLater to allow the UI to update first
            UiScheduler.runInUiThread(this::handleVerifyCode);
        }
    }

    @Override
    public void resetToEmailInput() {
        emailProperty.set("");
        passwordProperty.set("");
        firstNameProperty.set("");
        lastNameProperty.set("");
        createAccountProperty.set(false);
        verificationCodeProperty.set("");
        emailExists = false;
        flowStateProperty.set(FlowState.EMAIL_INPUT);
    }

    @Override
    public void goBack() {
        FlowState currentState = flowStateProperty.get();

        switch (currentState) {
            case EXISTING_USER:
            case NEW_USER:
                resetToEmailInput();
                break;
            case FORGOT_PASSWORD:
                flowStateProperty.set(FlowState.EXISTING_USER);
                break;
            default:
                if (onBackPressed != null) {
                    onBackPressed.run();
                }
                break;
        }
    }

    // ========================================
    // VALIDATION & UTILITY
    // ========================================

    protected boolean isValidEmail(String email) {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    protected void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    protected void clearError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    protected void clearErrors() {
        clearError(emailErrorLabel);
        clearError(passwordErrorLabel);
        clearError(firstNameErrorLabel);
        clearError(lastNameErrorLabel);
        clearError(codeErrorLabel);
    }

    protected void updateValidity() {
        FlowState state = flowStateProperty.get();

        switch (state) {
            case EMAIL_INPUT:
            case CHECKING_EMAIL:
                validProperty.set(false);
                break;
            case EXISTING_USER:
                validProperty.set(!passwordProperty.get().isEmpty());
                break;
            case NEW_USER:
                validProperty.set(
                        firstNameProperty.get().length() >= 2 &&
                                lastNameProperty.get().length() >= 2
                );
                break;
            case FORGOT_PASSWORD:
                validProperty.set(verificationCodeProperty.get().length() == 6);
                break;
        }
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.YourInformation;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    /**
     * This section is only applicable for new bookings where user needs to login/register.
     * For existing bookings or when user is already logged in, this section is skipped.
     */
    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        // Skip for existing bookings - user is already logged in and member already selected
        if (workingBooking != null && !workingBooking.isNewBooking()) {
            return false;
        }
        // Skip if user is already logged in
        if (FXUserPerson.getUserPerson() != null) {
            return false;
        }
        // Show for new bookings where user is not logged in
        return true;
    }

    // ========================================
    // HasYourInformationSection INTERFACE
    // ========================================

    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public void setOnLoginSuccess(Consumer<Person> callback) {
        this.onLoginSuccess = callback;
    }

    @Override
    public void setOnNewUserContinue(Consumer<NewUserData> callback) {
        this.onNewUserContinue = callback;
    }

    @Override
    public void setOnBackPressed(Runnable callback) {
        this.onBackPressed = callback;
    }

    @Override
    public String getEmail() {
        return emailProperty.get();
    }

    @Override
    public String getFirstName() {
        return firstNameProperty.get();
    }

    @Override
    public String getLastName() {
        return lastNameProperty.get();
    }

    @Override
    public boolean isCreateAccount() {
        return createAccountProperty.get();
    }
}
