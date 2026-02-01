package one.modality.booking.frontoffice.bookingpage.sections.user;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.browser.Browser;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.authn.*;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
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
import one.modality.base.client.entities.Labels;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.FrontendAccount;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.util.Objects;
import java.util.function.Consumer;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;

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
    protected final BooleanProperty forceAccountCreationProperty = new SimpleBooleanProperty(false);
    protected final ObjectProperty<Boolean> genderProperty = new SimpleObjectProperty<>(null); // null | false (Female) | true (Male)
    protected final BooleanProperty showGenderFieldProperty = new SimpleBooleanProperty(false);
    protected final BooleanProperty ageTermsAcceptedProperty = new SimpleBooleanProperty(false);
    protected final BooleanProperty emailVerificationCompleteProperty = new SimpleBooleanProperty(false);
    protected final BooleanProperty verificationCodeSentProperty = new SimpleBooleanProperty(false);

    // Password fields for account creation
    protected final StringProperty createAccountPasswordProperty = new SimpleStringProperty("");
    protected final StringProperty createAccountConfirmPasswordProperty = new SimpleStringProperty("");
    protected final BooleanProperty showCreateAccountPasswordProperty = new SimpleBooleanProperty(false);

    // Validation support
    protected final ValidationSupport accountCreationValidationSupport = new ValidationSupport();

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected final StackPane contentContainer = new StackPane();

    // Form fields (created once, reused across views)
    protected TextField emailField;
    protected PasswordField passwordField;
    protected TextField visiblePasswordField;
    protected TextField firstNameField;
    protected TextField lastNameField;
    protected TextField[] codeDigitFields;  // 6 separate digit input fields (for forgot password)
    protected HBox codeDigitsContainer;     // Container for the 6 digit fields (for forgot password)
    protected TextField[] accountVerificationDigitFields;  // 6 separate digit input fields (for account verification)
    protected HBox accountVerificationDigitsContainer;     // Container for the 6 digit fields (for account verification)
    // Password fields for account creation
    protected PasswordField createAccountPasswordField;
    protected PasswordField createAccountConfirmPasswordField;
    protected TextField visibleCreateAccountPasswordField;
    protected TextField visibleCreateAccountConfirmPasswordField;
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

    // Page-level back button (for navigating to previous page)
    protected Button emailInputBackButton;

    // Gender selection components
    protected Button femaleButton;
    protected Button maleButton;
    protected HBox genderButtonsContainer;

    // Age/Terms agreement components
    protected HBox ageTermsToggleContainer;
    protected HBox termsLine;

    // Email verification for account creation
    protected Button sendVerificationButton;
    protected HBox verificationSuccessBox;
    protected Hyperlink resendVerificationLink;

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

        // Reset verification state when email changes
        emailProperty.addListener((obs, oldEmail, newEmail) -> {
            if (!Objects.equals(oldEmail, newEmail)) {
                ageTermsAcceptedProperty.set(false);
                verificationCodeSentProperty.set(false);
                emailVerificationCompleteProperty.set(false);
                clearAccountVerificationCodeDigitFields();
            }
        });

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

        // Verification code fields for forgot password flow
        BookingPageUIBuilder.VerificationCodeResult forgotPasswordCodeFields =
                BookingPageUIBuilder.createVerificationCodeFields(
                        this::updateVerificationCodeFromFields,
                        this::distributePastedCode
                );
        codeDigitFields = forgotPasswordCodeFields.getDigitFields();
        codeDigitsContainer = forgotPasswordCodeFields.getContainer();

        // Verification code fields for account verification flow (separate instance to avoid JavaFX parent conflicts)
        BookingPageUIBuilder.VerificationCodeResult accountVerificationCodeFields =
                BookingPageUIBuilder.createVerificationCodeFields(
                        this::updateAccountVerificationCodeFromFields,
                        this::distributeAccountVerificationPastedCode
                );
        accountVerificationDigitFields = accountVerificationCodeFields.getDigitFields();
        accountVerificationDigitsContainer = accountVerificationCodeFields.getContainer();

        codeErrorLabel = createErrorLabel();

        // Gender selection buttons
        genderButtonsContainer = new HBox(8);
        genderButtonsContainer.setAlignment(Pos.CENTER_LEFT);

        femaleButton = new Button();
        I18n.bindI18nTextProperty(femaleButton.textProperty(), BookingPageI18nKeys.Female);
        femaleButton.setPadding(new Insets(14, 20, 14, 20));
        femaleButton.setMinWidth(80);
        femaleButton.getStyleClass().add(bookingpage_gender_button);
        femaleButton.setOnAction(e -> {
            genderProperty.set(false);
            femaleButton.getStyleClass().add(selected);
            maleButton.getStyleClass().remove(selected);
        });

        maleButton = new Button();
        I18n.bindI18nTextProperty(maleButton.textProperty(), BookingPageI18nKeys.Male);
        maleButton.setPadding(new Insets(14, 20, 14, 20));
        maleButton.setMinWidth(80);
        maleButton.getStyleClass().add(bookingpage_gender_button);
        maleButton.setOnAction(e -> {
            genderProperty.set(true);
            maleButton.getStyleClass().add(selected);
            femaleButton.getStyleClass().remove(selected);
        });

        genderButtonsContainer.getChildren().addAll(femaleButton, maleButton);

        // Password field for account creation
        createAccountPasswordField = new PasswordField();
        I18n.bindI18nPromptProperty(createAccountPasswordField.promptTextProperty(), BookingPageI18nKeys.CreatePassword);
        createAccountPasswordField.textProperty().bindBidirectional(createAccountPasswordProperty);
        styleInput(createAccountPasswordField);

        visibleCreateAccountPasswordField = new TextField();
        I18n.bindI18nPromptProperty(visibleCreateAccountPasswordField.promptTextProperty(), BookingPageI18nKeys.CreatePassword);
        visibleCreateAccountPasswordField.textProperty().bindBidirectional(createAccountPasswordProperty);
        styleInput(visibleCreateAccountPasswordField);
        visibleCreateAccountPasswordField.setVisible(false);
        visibleCreateAccountPasswordField.setManaged(false);

        // Confirm password field
        createAccountConfirmPasswordField = new PasswordField();
        I18n.bindI18nPromptProperty(createAccountConfirmPasswordField.promptTextProperty(), BookingPageI18nKeys.ConfirmPassword);
        createAccountConfirmPasswordField.textProperty().bindBidirectional(createAccountConfirmPasswordProperty);
        styleInput(createAccountConfirmPasswordField);

        visibleCreateAccountConfirmPasswordField = new TextField();
        I18n.bindI18nPromptProperty(visibleCreateAccountConfirmPasswordField.promptTextProperty(), BookingPageI18nKeys.ConfirmPassword);
        visibleCreateAccountConfirmPasswordField.textProperty().bindBidirectional(createAccountConfirmPasswordProperty);
        styleInput(visibleCreateAccountConfirmPasswordField);
        visibleCreateAccountConfirmPasswordField.setVisible(false);
        visibleCreateAccountConfirmPasswordField.setManaged(false);

        // Bind visibility toggle for password fields
        showCreateAccountPasswordProperty.addListener((obs, old, show) -> {
            createAccountPasswordField.setVisible(!show);
            createAccountPasswordField.setManaged(!show);
            visibleCreateAccountPasswordField.setVisible(show);
            visibleCreateAccountPasswordField.setManaged(show);
            createAccountConfirmPasswordField.setVisible(!show);
            createAccountConfirmPasswordField.setManaged(!show);
            visibleCreateAccountConfirmPasswordField.setVisible(show);
            visibleCreateAccountConfirmPasswordField.setManaged(show);
        });

        // Age & Terms agreement checkbox
        ageTermsToggleContainer = new HBox(12);
        ageTermsToggleContainer.setAlignment(Pos.TOP_LEFT);

        // Checkbox for agreement
        CheckBox ageTermsCheckBox = new CheckBox();
        ageTermsCheckBox.selectedProperty().bindBidirectional(ageTermsAcceptedProperty);
        ageTermsCheckBox.setPadding(new Insets(2, 0, 0, 0)); // Slight top padding for alignment

        // Checkbox labels with bullet list
        VBox toggleLabels = new VBox(6);
        Label confirmLabel = I18nControls.newLabel(BookingPageI18nKeys.IConfirmThat);
        confirmLabel.getStyleClass().add(bookingpage_toggle_label);

        // Bullet list (simulating HTML <ul>)
        VBox bulletList = new VBox(4);
        bulletList.setPadding(new Insets(0, 0, 0, 20)); // Left padding like paddingLeft: 20px in JSX

        // First bullet item - Age confirmation
        HBox age18Line = new HBox(6);
        age18Line.setAlignment(Pos.TOP_LEFT);
        Label age18Bullet = new Label("•");
        age18Bullet.getStyleClass().add(bookingpage_toggle_item);
        age18Bullet.setMinWidth(10);
        Label age18Label = I18nControls.newLabel(BookingPageI18nKeys.IAm18YearsOrOlder);
        age18Label.getStyleClass().add(bookingpage_toggle_item);
        age18Label.setWrapText(true);
        age18Line.getChildren().addAll(age18Bullet, age18Label);
        HBox.setHgrow(age18Label, Priority.ALWAYS);

        // Second bullet item - Terms agreement
        termsLine = new HBox(6);
        termsLine.setAlignment(Pos.TOP_LEFT);
        Label termsBullet = new Label("•");
        termsBullet.getStyleClass().add(bookingpage_toggle_item);
        termsBullet.setMinWidth(10);

        HBox termsContent = new HBox(4);
        termsContent.setAlignment(Pos.CENTER_LEFT);
        Label iAgreeLabel = I18nControls.newLabel(BookingPageI18nKeys.IAgreeToThe);
        iAgreeLabel.getStyleClass().add(bookingpage_toggle_item);
        Hyperlink termsLink = I18nControls.newHyperlink(BookingPageI18nKeys.TermsAndConditions);
        termsLink.setOnAction(e -> openTermsAndConditions());
        termsLink.getStyleClass().add(bookingpage_terms_link);
        termsContent.getChildren().addAll(iAgreeLabel, termsLink);

        termsLine.getChildren().addAll(termsBullet, termsContent);
        HBox.setHgrow(termsContent, Priority.ALWAYS);

        bulletList.getChildren().addAll(age18Line, termsLine);
        toggleLabels.getChildren().addAll(confirmLabel, bulletList);
        HBox.setHgrow(toggleLabels, Priority.ALWAYS);

        ageTermsToggleContainer.getChildren().addAll(ageTermsCheckBox, toggleLabels);

        // Email verification - Send button with spinner
        sendVerificationButton = BookingPageUIBuilder.createPrimaryButton(BookingPageI18nKeys.SendVerificationCode, colorScheme);
        sendVerificationButton.setOnAction(e -> handleSendVerificationCode());

        // Email verification - Success box
        verificationSuccessBox = new HBox(12);
        verificationSuccessBox.setAlignment(Pos.CENTER_LEFT);

        Region checkIconContainer = new Region();
        checkIconContainer.setPrefSize(32, 32);
        checkIconContainer.setMaxSize(32, 32);
        checkIconContainer.getStyleClass().add(bookingpage_verification_success_icon);

        VBox successLabels = new VBox(4);
        Label verifiedLabel = I18nControls.newLabel(BookingPageI18nKeys.EmailVerified);
        verifiedLabel.getStyleClass().add(bookingpage_success_title);
        successLabels.getChildren().add(verifiedLabel);

        verificationSuccessBox.getChildren().addAll(checkIconContainer, successLabels);

        // Resend verification code link
        resendVerificationLink = I18nControls.newHyperlink(BookingPageI18nKeys.ResendCode);
        resendVerificationLink.setOnAction(e -> handleResendVerificationCode());
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
        emailInputBackButton = createBackButton();
        emailInputBackButton.setOnAction(e -> {
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
        buttonRow.getChildren().addAll(emailInputBackButton, spacer, continueButton);

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
        banner.getStyleClass().add(bookingpage_info_box_info);

        // Checkmark circle
        StackPane checkCircle = createCheckmarkCircle();

        // Content
        VBox content = new VBox(4);
        Label foundLabel = I18nControls.newLabel(BookingPageI18nKeys.AccountFound);
        foundLabel.getStyleClass().addAll(bookingpage_text_md, bookingpage_font_semibold, bookingpage_text_dark);

        Label emailDisplayLabel = new Label();
        emailDisplayLabel.textProperty().bind(emailProperty);
        emailDisplayLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_muted);

        Hyperlink changeEmailLink = createHyperlink(BookingPageI18nKeys.UseDifferentEmail);
        changeEmailLink.setOnAction(e -> resetToEmailInput());

        content.getChildren().addAll(foundLabel, emailDisplayLabel, changeEmailLink);
        HBox.setHgrow(content, Priority.ALWAYS);

        banner.getChildren().addAll(checkCircle, content);

        // CSS class handles theming - no listener needed
        banner.getStyleClass().add(bookingpage_info_box_info);

        return banner;
    }

    protected StackPane createCheckmarkCircle() {
        // Uses primary solid circle with white checkmark - CSS handles theming
        StackPane circle = BookingPageUIBuilder.createThemedIconCircle(24);
        circle.getStyleClass().add(bookingpage_icon_circle_primary);

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
        btn.getStyleClass().add(bookingpage_btn_link);
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

        // Gender field (conditionally shown)
        VBox genderContainer = new VBox(8);
        Label genderLabel = createFieldLabel(BookingPageI18nKeys.Gender);
        genderContainer.getChildren().addAll(genderLabel, genderButtonsContainer);
        genderContainer.visibleProperty().bind(showGenderFieldProperty);
        genderContainer.managedProperty().bind(showGenderFieldProperty);

        // Add to grid (will be rearranged by responsive design)
        nameFieldsGrid.add(firstNameContainer, 0, 0);
        nameFieldsGrid.add(lastNameContainer, 1, 0);
        nameFieldsGrid.add(genderContainer, 0, 1, 2, 1); // Span 2 columns
        nameFieldsGrid.getColumnConstraints().addAll(createColumnConstraint(), createColumnConstraint());

        nameCard.getChildren().add(nameFieldsGrid);

        // Create account container (dynamically shows checkbox or info box based on forceAccountCreation)
        StackPane createAccountBox = buildAccountCreationContainer();

        // Navigation buttons
        HBox buttonRow = createNavigationButtonRow();
        Button backButton = createBackButton();
        backButton.setOnAction(e -> resetToEmailInput());

        Button continueButton = createPrimaryButton(BookingPageI18nKeys.Continue);
        continueButton.setOnAction(e -> handleNewUserContinue());

        // Complex validation binding
        BooleanProperty canContinue = new SimpleBooleanProperty(false);
        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean namesValid = firstNameProperty.get().trim().length() >= 2
                               && lastNameProperty.get().trim().length() >= 2;

            boolean genderValid = !showGenderFieldProperty.get()
                               || genderProperty.get() != null;

            boolean accountCreationValid = !createAccountProperty.get()
                || (ageTermsAcceptedProperty.get() && emailVerificationCompleteProperty.get());

            canContinue.set(namesValid && genderValid && accountCreationValid);
        }, firstNameProperty, lastNameProperty, genderProperty, showGenderFieldProperty,
           createAccountProperty, ageTermsAcceptedProperty, emailVerificationCompleteProperty);

        continueButton.disableProperty().bind(canContinue.not());

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
        box.getStyleClass().add(bookingpage_card_light);

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
        emailDisplay.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_secondary);
        HBox.setHgrow(emailDisplay, Priority.ALWAYS);

        Hyperlink changeLink = createHyperlink(BookingPageI18nKeys.Change);
        changeLink.setOnAction(e -> resetToEmailInput());

        box.getChildren().addAll(emailIcon, emailDisplay, changeLink);
        return box;
    }

    /**
     * Builds a container that dynamically shows either the optional "Create Account" checkbox
     * or the "Account Required" info box based on the forceAccountCreation property.
     * The account creation requirements (age/terms, password, verification) are shared between both views.
     */
    protected StackPane buildAccountCreationContainer() {
        StackPane container = new StackPane();
        container.setAlignment(Pos.TOP_LEFT);

        // Build the shared account requirements section
        VBox accountRequirements = buildAccountCreationRequirements();

        // Build both header versions (these methods now return headers WITHOUT the requirements)
        VBox optionalCheckboxView = buildCreateAccountBoxHeaderOnly();
        VBox requiredInfoBoxView = buildAccountRequiredBoxHeaderOnly();

        // Find the content VBox inside the optional checkbox view where requirements should be added
        VBox optionalContent = findContentVBox(optionalCheckboxView);

        // Update visibility and move accountRequirements to correct parent based on property
        Runnable updateLayout = () -> {
            boolean forced = forceAccountCreationProperty.get();
            optionalCheckboxView.setVisible(!forced);
            optionalCheckboxView.setManaged(!forced);
            requiredInfoBoxView.setVisible(forced);
            requiredInfoBoxView.setManaged(forced);

            // Move accountRequirements to the correct parent (JavaFX nodes can only have one parent)
            if (forced) {
                // Remove from optional view and add to required view
                if (optionalContent != null) {
                    optionalContent.getChildren().remove(accountRequirements);
                }
                if (!requiredInfoBoxView.getChildren().contains(accountRequirements)) {
                    requiredInfoBoxView.getChildren().add(accountRequirements);
                }
                // In forced mode, requirements are always visible
                accountRequirements.visibleProperty().unbind();
                accountRequirements.managedProperty().unbind();
                accountRequirements.setVisible(true);
                accountRequirements.setManaged(true);
            } else {
                // Remove from required view and add to optional view content
                requiredInfoBoxView.getChildren().remove(accountRequirements);
                if (optionalContent != null && !optionalContent.getChildren().contains(accountRequirements)) {
                    optionalContent.getChildren().add(accountRequirements);
                }
                // In optional mode, requirements are visible only when checkbox is checked
                accountRequirements.visibleProperty().bind(createAccountProperty);
                accountRequirements.managedProperty().bind(createAccountProperty);
            }
        };

        forceAccountCreationProperty.addListener((obs, old, newVal) -> updateLayout.run());
        updateLayout.run();

        container.getChildren().addAll(optionalCheckboxView, requiredInfoBoxView);
        return container;
    }

    /**
     * Helper to find the content VBox inside the optional checkbox header.
     * The structure is: header (VBox) -> contentRow (HBox) -> [checkbox, content (VBox)]
     */
    private VBox findContentVBox(VBox header) {
        for (Node child : header.getChildren()) {
            if (child instanceof HBox) {
                HBox contentRow = (HBox) child;
                for (Node rowChild : contentRow.getChildren()) {
                    if (rowChild instanceof VBox) {
                        return (VBox) rowChild;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Builds the shared account creation requirements section (age/terms, password, verification).
     * This section is shared between the optional and required account views.
     */
    protected VBox buildAccountCreationRequirements() {
        VBox accountRequirements = new VBox(16);
        accountRequirements.setPadding(new Insets(16, 0, 0, 0));

        // Age/Terms container (white card with toggle)
        VBox ageTermsCard = new VBox(0);
        ageTermsCard.setPadding(new Insets(16));
        ageTermsCard.getStyleClass().addAll(bookingpage_card, bookingpage_rounded);
        ageTermsCard.setOnMouseClicked(e -> e.consume()); // Prevent toggling main checkbox
        ageTermsCard.getChildren().add(ageTermsToggleContainer);
        accountRequirements.getChildren().add(ageTermsCard);

        // Password fields section (shown when age/terms accepted)
        VBox passwordSection = new VBox(16);
        passwordSection.setPadding(new Insets(16, 0, 0, 0));
        passwordSection.visibleProperty().bind(ageTermsAcceptedProperty);
        passwordSection.managedProperty().bind(ageTermsAcceptedProperty);

        // Password field with label
        VBox passwordFieldContainer = new VBox(8);
        HBox passwordLabelBox = createRequiredFieldLabel(BookingPageI18nKeys.CreatePassword);
        StackPane pwdStack = new StackPane();
        pwdStack.getChildren().addAll(createAccountPasswordField, visibleCreateAccountPasswordField);
        HBox pwdRow = new HBox(8);
        pwdRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(pwdStack, Priority.ALWAYS);
        Button pwdEyeBtn = createEyeToggleButton();
        pwdEyeBtn.setOnAction(e -> showCreateAccountPasswordProperty.set(!showCreateAccountPasswordProperty.get()));
        pwdRow.getChildren().addAll(pwdStack, pwdEyeBtn);
        passwordFieldContainer.getChildren().addAll(passwordLabelBox, pwdRow);

        // Password strength hint
        Label passwordStrengthHint = I18nControls.newLabel(BookingPageI18nKeys.PasswordStrengthHint);
        passwordStrengthHint.getStyleClass().addAll(bookingpage_text_xs, bookingpage_text_muted);
        passwordStrengthHint.setWrapText(true);

        // Confirm password field with label
        VBox confirmPasswordContainer = new VBox(8);
        HBox confirmPasswordLabelBox = createRequiredFieldLabel(BookingPageI18nKeys.ConfirmPassword);
        StackPane confirmPwdStack = new StackPane();
        confirmPwdStack.getChildren().addAll(createAccountConfirmPasswordField, visibleCreateAccountConfirmPasswordField);
        confirmPasswordContainer.getChildren().addAll(confirmPasswordLabelBox, confirmPwdStack);

        passwordSection.getChildren().addAll(passwordFieldContainer, passwordStrengthHint, confirmPasswordContainer);
        accountRequirements.getChildren().add(passwordSection);

        // Email verification section (shown when age/terms accepted)
        VBox verificationSection = new VBox(16);
        verificationSection.setPadding(new Insets(16, 0, 0, 0));
        verificationSection.visibleProperty().bind(ageTermsAcceptedProperty);
        verificationSection.managedProperty().bind(ageTermsAcceptedProperty);

        // Send verification button state
        VBox sendCodeContainer = new VBox(8);
        sendCodeContainer.setAlignment(Pos.CENTER);
        Label verifyPrompt = I18nControls.newLabel(BookingPageI18nKeys.ToCreateAccountVerifyEmail);
        verifyPrompt.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
        verifyPrompt.setWrapText(true);
        verifyPrompt.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        sendCodeContainer.getChildren().addAll(verifyPrompt, sendVerificationButton);
        sendCodeContainer.visibleProperty().bind(verificationCodeSentProperty.not());
        sendCodeContainer.managedProperty().bind(verificationCodeSentProperty.not());

        // Code entry state
        VBox codeEntryContainer = new VBox(12);

        // Email confirmation banner
        HBox emailConfirmBanner = new HBox(10);
        emailConfirmBanner.setPadding(new Insets(12, 16, 12, 16));
        emailConfirmBanner.setAlignment(Pos.TOP_LEFT);
        emailConfirmBanner.getStyleClass().addAll(bookingpage_info_banner);

        SVGPath emailIcon = new SVGPath();
        emailIcon.setContent("M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z M22 6l-10 7L2 6");
        emailIcon.getStyleClass().add(bookingpage_icon_primary);
        emailIcon.setStrokeWidth(2);
        emailIcon.setFill(Color.TRANSPARENT);
        emailIcon.setScaleX(0.7);
        emailIcon.setScaleY(0.7);

        VBox emailConfirmText = new VBox(4);
        Label codeSentLabel = I18nControls.newLabel(BookingPageI18nKeys.WeSentVerificationCodeTo);
        codeSentLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
        Label emailAddressLabel = new Label();
        emailAddressLabel.textProperty().bind(emailProperty);
        emailAddressLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_font_semibold);
        emailConfirmText.getChildren().addAll(codeSentLabel, emailAddressLabel);

        emailConfirmBanner.getChildren().addAll(emailIcon, emailConfirmText);
        HBox.setHgrow(emailConfirmText, Priority.ALWAYS);

        // Code input label
        Label codeLabel = I18nControls.newLabel(BookingPageI18nKeys.EnterVerificationCode);
        codeLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_font_medium);

        // Resend/hint row
        HBox resendRow = new HBox(12);
        resendRow.setAlignment(Pos.CENTER_LEFT);
        Label didntReceiveLabel = I18nControls.newLabel(BookingPageI18nKeys.DidntReceiveIt);
        didntReceiveLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
        resendRow.getChildren().addAll(didntReceiveLabel, resendVerificationLink);

        codeEntryContainer.getChildren().addAll(emailConfirmBanner, codeLabel, accountVerificationDigitsContainer, codeErrorLabel, resendRow);
        codeEntryContainer.visibleProperty().bind(verificationCodeSentProperty.and(emailVerificationCompleteProperty.not()));
        codeEntryContainer.managedProperty().bind(verificationCodeSentProperty.and(emailVerificationCompleteProperty.not()));

        // Success state
        verificationSuccessBox.visibleProperty().bind(emailVerificationCompleteProperty);
        verificationSuccessBox.managedProperty().bind(emailVerificationCompleteProperty);

        verificationSection.getChildren().addAll(sendCodeContainer, codeEntryContainer, verificationSuccessBox);
        accountRequirements.getChildren().add(verificationSection);

        return accountRequirements;
    }

    /**
     * Builds an info box explaining that account creation is required (header only).
     * The account requirements are added dynamically by buildAccountCreationContainer().
     * Used when forceAccountCreation is true.
     */
    protected VBox buildAccountRequiredBoxHeaderOnly() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        box.getStyleClass().add(bookingpage_info_box_info);

        // Header row with icon and title/description
        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.TOP_LEFT);

        // Info icon circle
        StackPane iconCircle = BookingPageUIBuilder.createThemedIconCircle(28);
        iconCircle.getStyleClass().add(bookingpage_icon_circle_primary);

        // Info "i" icon
        SVGPath infoIcon = new SVGPath();
        infoIcon.setContent("M12 16v-4 M12 8h.01");
        infoIcon.setStroke(Color.WHITE);
        infoIcon.setStrokeWidth(2.5);
        infoIcon.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        infoIcon.setFill(Color.TRANSPARENT);
        infoIcon.setScaleX(0.9);
        infoIcon.setScaleY(0.9);
        iconCircle.getChildren().add(infoIcon);

        // Title and description
        VBox headerContent = new VBox(8);
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.AccountRequired);
        titleLabel.getStyleClass().addAll(bookingpage_text_lg, bookingpage_font_bold, bookingpage_text_dark);

        Label descLabel = I18nControls.newLabel(BookingPageI18nKeys.AccountRequiredDescription);
        descLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_muted);
        descLabel.setWrapText(true);

        headerContent.getChildren().addAll(titleLabel, descLabel);
        headerRow.getChildren().addAll(iconCircle, headerContent);
        HBox.setHgrow(headerContent, Priority.ALWAYS);

        box.getChildren().add(headerRow);
        // Note: accountRequirements is added dynamically by buildAccountCreationContainer()

        return box;
    }

    /**
     * Builds the optional "Create an Account" checkbox box (header only).
     * The account requirements are added dynamically by buildAccountCreationContainer().
     * Used when forceAccountCreation is false.
     */
    protected VBox buildCreateAccountBoxHeaderOnly() {
        BookingFormColorScheme colors = colorScheme.get();

        // Clear any existing references
        benefitCheckIcons.clear();
        benefitLabels.clear();

        VBox box = new VBox(0);
        box.setPadding(new Insets(24));
        updateCreateAccountBoxStyle(box);

        HBox contentRow = new HBox(16);
        contentRow.setAlignment(Pos.TOP_LEFT);

        // Custom checkbox (clickable)
        StackPane checkbox = createCustomCheckbox();

        // Content
        VBox content = new VBox(8);

        // Title with icon
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        createAccountUserIcon = new SVGPath();
        createAccountUserIcon.setContent("M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2 M12 3a4 4 0 100 8 4 4 0 000-8z");
        createAccountUserIcon.getStyleClass().add(bookingpage_icon_primary);
        createAccountUserIcon.setStrokeWidth(2);
        createAccountUserIcon.setFill(Color.TRANSPARENT);
        createAccountUserIcon.setScaleX(0.8);
        createAccountUserIcon.setScaleY(0.8);

        createAccountTitleLabel = I18nControls.newLabel(BookingPageI18nKeys.CreateAnAccount);
        createAccountTitleLabel.getStyleClass().addAll(bookingpage_text_lg, bookingpage_font_bold, bookingpage_text_dark);

        titleRow.getChildren().addAll(createAccountUserIcon, createAccountTitleLabel);

        // Description
        Label descLabel = I18nControls.newLabel(BookingPageI18nKeys.CreateAccountBenefit);
        descLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_muted);
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
        // Note: accountRequirements is added dynamically by buildAccountCreationContainer()

        contentRow.getChildren().addAll(checkbox, content);
        HBox.setHgrow(content, Priority.ALWAYS);
        box.getChildren().add(contentRow);

        // Update style when checkbox changes
        createAccountProperty.addListener((obs, old, checked) -> updateCreateAccountBoxStyle(box));

        return box;
    }

    protected void updateCreateAccountBoxStyle(VBox box) {
        boolean checked = createAccountProperty.get();
        // Use CSS class with theme variables - toggle 'selected' class for checked state
        box.getStyleClass().removeAll(bookingpage_selectable_card, selected);
        box.getStyleClass().add(bookingpage_selectable_card);
        if (checked) {
            box.getStyleClass().add(selected);
        }
    }

    protected StackPane createCustomCheckbox() {
        StackPane checkbox = new StackPane();
        checkbox.setMinSize(24, 24);
        checkbox.setMaxSize(24, 24);
        checkbox.setCursor(Cursor.HAND);

        // Make checkbox clickable
        checkbox.setOnMouseClicked(e -> {
            e.consume(); // Prevent event propagation
            createAccountProperty.set(!createAccountProperty.get());
        });

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

            checkbox.getStyleClass().removeAll(bookingpage_checkbox, bookingpage_checkbox_selected);

            if (checked) {
                checkbox.getStyleClass().add(bookingpage_checkbox_selected);
                if (!checkbox.getChildren().contains(checkIcon)) {
                    checkbox.getChildren().add(checkIcon);
                }
            } else {
                checkbox.getStyleClass().add(bookingpage_checkbox);
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
        tag.getStyleClass().add(bookingpage_benefit_tag);

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
        label.getStyleClass().addAll(bookingpage_text_xs, bookingpage_text_dark);

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
        didntReceiveLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_muted);
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
        box.getStyleClass().add(bookingpage_warning_box);

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
        text1.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_warning);

        Label emailLabel = new Label();
        emailLabel.textProperty().bind(emailProperty);
        emailLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_warning);

        emailContent.getChildren().addAll(text1, emailLabel);
        emailRow.getChildren().addAll(emailIcon, emailContent);

        // Second row: Warning about timing (no icon, just indented text)
        Label warningText = I18nControls.newLabel(BookingPageI18nKeys.EmailMayTakeUpTo1Minute);
        warningText.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_warning);
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
        checkingLabel.getStyleClass().addAll(bookingpage_text_lg, bookingpage_text_muted);

        view.getChildren().addAll(spinner, checkingLabel);
        return view;
    }

    // ========================================
    // STYLING HELPERS
    // ========================================

    protected void styleInput(TextInputControl input) {
        input.setPadding(new Insets(14, 16, 14, 16));
        input.setMaxWidth(Double.MAX_VALUE);
        input.getStyleClass().addAll(bookingpage_input_bordered, bookingpage_text_base);

        // Focus styling - toggle CSS class on focus
        input.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                input.getStyleClass().remove(bookingpage_input_bordered);
                input.getStyleClass().add(bookingpage_input_focused);
                input.setEffect(BookingPageUIBuilder.createFocusShadow());
            } else {
                input.getStyleClass().remove(bookingpage_input_focused);
                input.getStyleClass().add(bookingpage_input_bordered);
                input.setEffect(null);
            }
        });
    }

    protected Label createPageTitle(Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().addAll(bookingpage_text_2xl, bookingpage_font_bold, bookingpage_text_dark);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    protected Label createPageSubtitle(Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_muted);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    protected Label createFieldLabel(Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().addAll(bookingpage_text_base, bookingpage_font_semibold, bookingpage_text_dark);
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
        label.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_muted);
        label.setWrapText(true);
        VBox.setMargin(label, new Insets(6, 0, 0, 0));
        return label;
    }

    protected Label createErrorLabel() {
        Label label = new Label();
        label.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_danger);
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
        card.getStyleClass().addAll(bookingpage_card_static, bookingpage_rounded_lg);
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
            .<Person>executeQuery("select id from Person where lower(email)=$1 and owner=true and removed!=true limit 1", email)
            .onFailure(error -> UiScheduler.runInUiThread(() -> {
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
                    genderProperty.get(),
                    createAccountProperty.get(),
                    emailVerificationCompleteProperty.get()
            );
            onNewUserContinue.accept(data);
        }
    }

    protected void handleForgotPassword() {
        String email = emailProperty.get();
        if (email == null || email.trim().isEmpty()) {
            return;
        }
        email = email.trim().toLowerCase();

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

        AuthenticationService.authenticate(credentials);

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
        if (email == null || email.trim().isEmpty()) {
            return;
        }
        email = email.trim().toLowerCase();

        SendMagicLinkCredentials credentials = new SendMagicLinkCredentials(
            email,
            WindowLocation.getOrigin(),
            null,
            I18n.getLanguage(),
            true,
            null
        );

        AuthenticationService.authenticate(credentials);

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
        resendLink.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_disabled);
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
                    resendLink.getStyleClass().remove(bookingpage_text_disabled);
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
     * Starts the resend cooldown timer for account verification resend link.
     */
    protected void startAccountVerificationResendCooldown() {
        if (resendVerificationLink == null) {
            return;
        }

        // Stop any existing timer
        if (resendCountdownTimer != null) {
            resendCountdownTimer.stop();
        }

        // Initialize countdown
        resendSecondsRemaining = RESEND_COOLDOWN_SECONDS;

        // Unbind the i18n binding so we can set text directly
        resendVerificationLink.textProperty().unbind();

        // Disable the link and show initial countdown
        resendVerificationLink.setDisable(true);
        resendVerificationLink.setText(I18n.getI18nText(BookingPageI18nKeys.ResendInSeconds, resendSecondsRemaining));
        resendVerificationLink.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_disabled);
        resendVerificationLink.setTextFill(Color.web("#6c757d")); // Muted gray for disabled state

        // Create countdown timer (fires every second)
        resendCountdownTimer = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                resendSecondsRemaining--;
                if (resendSecondsRemaining > 0) {
                    // Update countdown text
                    resendVerificationLink.setText(I18n.getI18nText(BookingPageI18nKeys.ResendInSeconds, resendSecondsRemaining));
                } else {
                    // Cooldown complete - re-enable the link
                    resendCountdownTimer.stop();
                    resendVerificationLink.setDisable(false);
                    resendVerificationLink.getStyleClass().remove(bookingpage_text_disabled);
                    I18nControls.bindI18nTextProperty(resendVerificationLink, BookingPageI18nKeys.ResendCode);
                    BookingFormColorScheme colors = colorScheme.get();
                    Color linkColor = colors != null ? colors.getPrimary() : Color.web("#0d6efd");
                    resendVerificationLink.setTextFill(linkColor);
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
     * Clears all account verification digit fields.
     */
    protected void clearAccountVerificationCodeDigitFields() {
        verificationCodeProperty.set("");
        if (accountVerificationDigitFields != null) {
            for (TextField field : accountVerificationDigitFields) {
                field.setText("");
            }
            // Focus first field
            if (accountVerificationDigitFields.length > 0) {
                accountVerificationDigitFields[0].requestFocus();
            }
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
     * Updates the verificationCodeProperty from the individual digit fields (forgot password flow)
     * and triggers auto-validation when all 6 digits are entered.
     */
    protected void updateVerificationCodeFromFields() {
        StringBuilder code = new StringBuilder();
        for (TextField field : codeDigitFields) {
            code.append(field.getText());
        }
        String fullCode = code.toString();
        verificationCodeProperty.set(fullCode);

        // Auto-validate when all 6 digits are entered (forgot password flow)
        if (fullCode.length() == 6) {
            UiScheduler.runInUiThread(this::handleVerifyCode);
        }
    }

    /**
     * Distributes pasted digits across account verification digit fields.
     */
    protected void distributeAccountVerificationPastedCode(String digits, int startIndex) {
        // Distribute digits across fields (listeners on each field will call updateAccountVerificationCodeFromFields)
        for (int i = 0; i < digits.length() && (startIndex + i) < 6; i++) {
            accountVerificationDigitFields[startIndex + i].setText(String.valueOf(digits.charAt(i)));
        }
        // Focus the last filled field or the next empty one
        int lastIndex = Math.min(startIndex + digits.length() - 1, 5);
        if (lastIndex < 5 && digits.length() < (6 - startIndex)) {
            accountVerificationDigitFields[lastIndex + 1].requestFocus();
        } else {
            accountVerificationDigitFields[lastIndex].requestFocus();
        }
    }

    /**
     * Updates the verificationCodeProperty from the account verification digit fields
     * and triggers auto-validation when all 6 digits are entered.
     */
    protected void updateAccountVerificationCodeFromFields() {
        StringBuilder code = new StringBuilder();
        for (TextField field : accountVerificationDigitFields) {
            code.append(field.getText());
        }
        String fullCode = code.toString();
        verificationCodeProperty.set(fullCode);

        // Auto-validate when all 6 digits are entered (account verification flow)
        if (fullCode.length() == 6) {
            UiScheduler.runInUiThread(this::handleVerifyAccountCode);
        }
    }

    @Override
    public void resetToEmailInput() {
        emailProperty.set("");
        passwordProperty.set("");
        firstNameProperty.set("");
        lastNameProperty.set("");
        createAccountProperty.set(forceAccountCreationProperty.get());
        verificationCodeProperty.set("");
        // Reset account creation password fields
        createAccountPasswordProperty.set("");
        createAccountConfirmPasswordProperty.set("");
        accountCreationValidationSupport.reset();
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
    // HELPER METHODS FOR NEW FEATURES
    // ========================================

    protected void openTermsAndConditions() {
        String privacyUrl = getOrganizationPrivacyUrl();
        if (privacyUrl != null && !privacyUrl.isEmpty()) {
            try {
                Browser.launchExternalBrowser(privacyUrl);
            } catch (Exception e) {
                Console.log(e);
            }
        }
    }

    /**
     * Gets the privacy URL from the organization associated with the event.
     * The URL is stored in a Label entity with i18n fields (en, fr, etc.).
     * Returns the URL for the current interface language, falling back to other languages if not available.
     * Returns null if no privacy URL is configured.
     */
    protected String getOrganizationPrivacyUrl() {
        if (workingBookingProperties != null) {
            WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
            if (workingBooking != null && workingBooking.getEvent() != null) {
                Organization organization = workingBooking.getPolicyAggregate().getEvent().getOrganization();
                if (organization != null) {
                    one.modality.base.shared.entities.Label privacyUrlLabel = organization.getPrivacyUrlLabel();
                    return Labels.instantTranslateLabel(privacyUrlLabel);
                }
            }
        }
        return null;
    }

    /**
     * Updates the visibility of the terms line based on whether the organization has a privacy URL.
     */
    protected void updateTermsLineVisibility() {
        if (termsLine != null) {
            String privacyUrl = getOrganizationPrivacyUrl();
            boolean hasPrivacyUrl = privacyUrl != null && !privacyUrl.isEmpty();
            termsLine.setVisible(hasPrivacyUrl);
            termsLine.setManaged(hasPrivacyUrl);
        }
    }

    /**
     * Initializes validation rules for account creation.
     * Called before sending verification code.
     */
    protected void initAccountCreationValidation() {
        if (accountCreationValidationSupport.isEmpty()) {
            // First name required (min 2 chars)
            accountCreationValidationSupport.addValidationRule(
                Bindings.createBooleanBinding(
                    () -> firstNameProperty.get().trim().length() >= 2,
                    firstNameProperty
                ),
                firstNameField,
                I18n.i18nTextProperty(BookingPageI18nKeys.FirstNameRequired),
                true
            );

            // Last name required (min 2 chars)
            accountCreationValidationSupport.addValidationRule(
                Bindings.createBooleanBinding(
                    () -> lastNameProperty.get().trim().length() >= 2,
                    lastNameProperty
                ),
                lastNameField,
                I18n.i18nTextProperty(BookingPageI18nKeys.LastNameRequired),
                true
            );

            // Password strength validation
            accountCreationValidationSupport.addPasswordStrengthValidation(
                createAccountPasswordField,
                I18n.i18nTextProperty(BookingPageI18nKeys.PasswordStrengthHint)
            );

            // Password match validation
            accountCreationValidationSupport.addPasswordMatchValidation(
                createAccountPasswordField,
                createAccountConfirmPasswordField,
                I18n.i18nTextProperty(BookingPageI18nKeys.PasswordsDoNotMatch)
            );
        }
    }

    protected void handleSendVerificationCode() {
        // Initialize and run validation
        initAccountCreationValidation();
        if (!accountCreationValidationSupport.isValid()) {
            return; // Validation failed - error popover shown automatically
        }

        String email = emailProperty.get().trim().toLowerCase();

        // Use InitiateAccountCreationCredentials for new account creation
        // with verificationCodeOnly=true to receive a 6-digit code instead of magic link
        InitiateAccountCreationCredentials credentials = new InitiateAccountCreationCredentials(
                email,
                WindowLocation.getOrigin(),
                WindowLocation.getPath(),
                I18n.getLanguage(),
                true,  // verificationCodeOnly - send 6-digit code, not magic link
                null   // No context needed
        );

        AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                new AuthenticationRequest()
                        .setUserCredentials(credentials)
                        .executeAsync()
                        .inUiThread()
                        .onFailure(error -> {
                            showError(codeErrorLabel, I18n.getI18nText(BookingPageI18nKeys.ErrorSendingCode));
                        })
                        .onSuccess(ar -> {
                            verificationCodeSentProperty.set(true);
                            clearError(codeErrorLabel);
                            // Start cooldown timer for resend link
                            UiScheduler.scheduleDelay(100, this::startAccountVerificationResendCooldown);
                        }),
                sendVerificationButton
        );
    }

    protected void handleVerifyAccountCode() {
        String code = verificationCodeProperty.get().trim();

        if (code.length() != 6) {
            return; // Should not happen due to validation
        }

        String password = createAccountPasswordProperty.get();

        // Create account with password using FinaliseAccountCreationCredentials
        new AuthenticationRequest()
                .setUserCredentials(new FinaliseAccountCreationCredentials(code, password))
                .executeAsync()
                .inUiThread()
                .onFailure(error -> {
                    Console.log("Account creation failed: " + error.getMessage());
                    showError(codeErrorLabel, I18n.getI18nText(BookingPageI18nKeys.InvalidOrExpiredCode));
                    playShakeAnimation(accountVerificationDigitsContainer);
                    clearAccountVerificationCodeDigitFields(); // Clear for retry
                })
                .onSuccess(accountPk -> {
                    Console.log("FrontendAccount created successfully, now creating Person...");

                    // Create UpdateStore for Person and FrontendAccount update
                    UpdateStore updateStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());

                    // Create Person entity linked to the account (like UserAccountUI.java)
                    Person person = updateStore.insertEntity(Person.class);
                    person.setFrontendAccount(accountPk);
                    person.setEmail(emailProperty.get().trim().toLowerCase());
                    person.setFirstName(firstNameProperty.get().trim());
                    person.setLastName(lastNameProperty.get().trim());

                    // Set gender if entered (genderProperty: null=not set, false=Female, true=Male)
                    Boolean gender = genderProperty.get();
                    if (gender != null) {
                        person.setMale(gender);
                    }

                    // Mark as account owner (critical to avoid duplicate in Member Selection!)
                    person.setOwner(true);

                    // Update FrontendAccount with current language
                    FrontendAccount fa = updateStore.updateEntity(FrontendAccount.class, accountPk);
                    fa.setLang(I18n.getLanguage().toString());

                    // Submit Person and FrontendAccount update
                    updateStore.submitChanges()
                        .inUiThread()
                        .onFailure(failure -> {
                            Console.log("Error creating Person: " + failure.getMessage());
                            showError(codeErrorLabel, I18n.getI18nText(BookingPageI18nKeys.InvalidOrExpiredCode));
                        })
                        .onSuccess(success -> {
                            Console.log("Person created successfully, now authenticating...");

                            // Authenticate using the verification code to log user in
                            new AuthenticationRequest()
                                .setUserCredentials(new AuthenticateWithMagicLinkCredentials(code))
                                .executeAsync()
                                .inUiThread()
                                .onFailure(authError -> {
                                    // Person is created, but auth failed - still allow to continue
                                    Console.log("Authentication after account creation failed: " + authError.getMessage());
                                    emailVerificationCompleteProperty.set(true);
                                    clearError(codeErrorLabel);
                                })
                                .onSuccess(requestedPath -> {
                                    Console.log("Account created and user authenticated successfully");
                                    emailVerificationCompleteProperty.set(true);
                                    clearError(codeErrorLabel);

                                    // User is now logged in - notify callback when FXUserPerson is available
                                    if (onLoginSuccess != null) {
                                        Person userPerson = FXUserPerson.getUserPerson();
                                        if (userPerson != null) {
                                            onLoginSuccess.accept(userPerson);
                                        } else {
                                            FXProperties.runOnPropertyChange(p -> {
                                                if (p != null) {
                                                    onLoginSuccess.accept(p);
                                                }
                                            }, FXUserPerson.userPersonProperty());
                                        }
                                    }
                                });
                        });
                });
    }

    protected void handleResendVerificationCode() {
        emailVerificationCompleteProperty.set(false);
        clearAccountVerificationCodeDigitFields();
        handleSendVerificationCode();
        // Refocus first digit field
        if (accountVerificationDigitFields.length > 0) {
            accountVerificationDigitFields[0].requestFocus();
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

        if (workingBookingProperties != null) {
            // Configure account creation requirement from event's noAccountBooking field
            Event event = workingBookingProperties.getEvent();
            if (event != null) {
                Boolean noAccountBooking = event.isNoAccountBooking();
                // If noAccountBooking is null or false, force account creation
                // If noAccountBooking is true, guest checkout is allowed
                setForceAccountCreation(noAccountBooking == null || !noAccountBooking);
            }

            // Configure gender field requirement from item policies
            updateGenderFieldRequirement();

            // Update terms line visibility based on organization's privacy URL
            updateTermsLineVisibility();
        }
    }

    /**
     * Updates the gender field visibility based on selected items' ItemPolicy.genderInfoRequired.
     * Called when workingBookingProperties is set or when booking selections change.
     */
    protected void updateGenderFieldRequirement() {
        if (workingBookingProperties == null) return;

        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
        if (policyAggregate == null) return;

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        DocumentAggregate documentAggregate = workingBooking != null ?
            workingBooking.getLastestDocumentAggregate() : null;

        if (documentAggregate != null && documentAggregate.getDocumentLines() != null) {
            boolean genderRequired = documentAggregate.getDocumentLines().stream()
                .map(line -> policyAggregate.getItemPolicy(line.getItem()))
                .filter(Objects::nonNull)
                .anyMatch(policy -> Boolean.TRUE.equals(policy.isGenderInfoRequired()));

            setShowGenderField(genderRequired);
        }
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

    /**
     * Sets the visibility of the page-level back button (in the email input view).
     * Use this to hide the back button when Your Information is the first step.
     *
     * @param visible true to show, false to hide
     */
    public void setBackButtonVisible(boolean visible) {
        if (emailInputBackButton != null) {
            emailInputBackButton.setVisible(visible);
            emailInputBackButton.setManaged(visible);
        }
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

    @Override
    public void setForceAccountCreation(boolean force) {
        forceAccountCreationProperty.set(force);
        if (force) {
            // When forced, always set createAccount to true
            createAccountProperty.set(true);
        }
    }

    @Override
    public boolean isForceAccountCreation() {
        return forceAccountCreationProperty.get();
    }

    public void setShowGenderField(boolean show) {
        showGenderFieldProperty.set(show);
    }

    public boolean isShowGenderField() {
        return showGenderFieldProperty.get();
    }
}
