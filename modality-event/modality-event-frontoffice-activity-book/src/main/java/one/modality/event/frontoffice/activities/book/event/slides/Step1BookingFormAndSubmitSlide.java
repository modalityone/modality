package one.modality.event.frontoffice.activities.book.event.slides;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.FlipPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.windowhistory.WindowHistory;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import one.modality.base.client.i18n.BaseI18nKeys;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.entity.EntityStore;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingHistoryHelper;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingform.BookingForm;
import one.modality.booking.frontoffice.bookingform.BookingFormActivityCallback;
import one.modality.booking.frontoffice.bookingform.GatewayPaymentForm;
import one.modality.event.frontoffice.activities.book.BookI18nKeys;
import one.modality.event.frontoffice.activities.book.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.book.event.BookEventActivity;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;
import one.modality.event.frontoffice.activities.book.fx.FXGuestToBook;

import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
final class Step1BookingFormAndSubmitSlide extends StepSlide implements BookingFormActivityCallback {

    private final Button defaultSubmitButton = Bootstrap.largeSuccessButton(I18nControls.newButton(BaseI18nKeys.Submit));

    private final BooleanProperty bookingFormPersonToBookRequiredProperty = new SimpleBooleanProperty();
    private final BooleanProperty bookingFormShowDefaultSubmitButtonProperty = new SimpleBooleanProperty();
    private final BooleanProperty bookingFormDisableSubmitButtonProperty = new SimpleBooleanProperty();
    private final BooleanProperty bookingFormTransitingProperty = new SimpleBooleanProperty();
    private final BooleanProperty readyToSubmitBookingProperty = new SimpleBooleanProperty();
    // Node property that will be managed by the sub-router to mount the CheckoutAccountActivity (when routed)
    private final ObjectProperty<Node> embeddedLoginMountNodeProperty = new SimpleObjectProperty<>();
    private final VBox loginTopVBox = new VBox(10, Bootstrap.textPrimary(Bootstrap.strong(I18nControls.newLabel(BookI18nKeys.LoginBeforeBooking))));
    // Guest checkout button - more prominent than a simple hyperlink
    private final Button continueAsGuestButton = Bootstrap.largePrimaryButton(I18nControls.newButton("ContinueAsGuest"));
    private final Label guestDescLabel = I18nControls.newLabel("GuestCheckoutDesc");
    private final Label orDividerLabel = I18nControls.newLabel("Or");
    private final VBox guestOptionBox = new VBox(12);
    private final FlipPane loginGuestFlipPane = new FlipPane();
    private final MonoPane loginContent = new MonoPane();
    private final GuestPanel guestPanel = new GuestPanel();
    private Button[] bookingFormSubmitButtons;
    private BookingForm currentBookingForm;

    public Step1BookingFormAndSubmitSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
        mainVbox.setSpacing(100);
        defaultSubmitButton.setOnAction(event -> submitBooking());
        loginGuestFlipPane.setAlignment(Pos.TOP_CENTER);
        loginContent.contentProperty().bind(embeddedLoginMountNodeProperty); // managed by sub-router
        // Adding the container that will display the CheckoutAccountActivity (and eventually the login page before)
        BorderPane signInContainer = new BorderPane();
        loginTopVBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(loginTopVBox, new Insets(0, 0, 20, 0));
        signInContainer.setTop(loginTopVBox);
        signInContainer.setCenter(loginContent);
        // Add the prominent guest option below the login form
        signInContainer.setBottom(guestOptionBox);
        BorderPane.setMargin(guestOptionBox, new Insets(10, 0, 0, 0));
        loginGuestFlipPane.setFront(signInContainer);
        FXProperties.runNowAndOnPropertiesChange(this::updateLoginAndSubmitVisibility,
            FXUserPerson.userPersonProperty(), bookingFormPersonToBookRequiredProperty, bookingFormShowDefaultSubmitButtonProperty, bookingFormDisableSubmitButtonProperty, bookingFormTransitingProperty);

        // Build the OR divider - a horizontal line with "OR" in the middle
        Line leftLine = new Line(0, 0, 80, 0);
        leftLine.setStroke(Color.LIGHTGRAY);
        Line rightLine = new Line(0, 0, 80, 0);
        rightLine.setStroke(Color.LIGHTGRAY);
        orDividerLabel.setStyle("-fx-text-fill: #999; -fx-font-weight: bold;");
        HBox orDivider = new HBox(15, leftLine, orDividerLabel, rightLine);
        orDivider.setAlignment(Pos.CENTER);
        orDivider.setPadding(new Insets(20, 0, 10, 0));

        // Style the guest description
        guestDescLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Build the guest option box
        guestOptionBox.setAlignment(Pos.CENTER);
        guestOptionBox.getChildren().addAll(orDivider, continueAsGuestButton, guestDescLabel);
        continueAsGuestButton.setMaxWidth(300);
        continueAsGuestButton.getStyleClass().add("guest-checkout-btn");

        // Guest button action
        continueAsGuestButton.setOnAction(e -> {
            loginGuestFlipPane.flipToBack();
            guestPanel.onShowing();
        });

        // Handle "Log in here" link click from the GuestPanel
        guestPanel.setOnLoginLinkClicked(() -> {
            loginGuestFlipPane.flipToFront();
            guestPanel.onHiding();
        });

        // Set up email existence check callback for guest checkout
        guestPanel.setEmailCheckCallback(email -> {
            EntityStore.create(getBookEventActivity().getDataSourceModel())
                .<Person>executeQuery("select id from Person where email=? and owner=true and removed!=true limit 1", email)
                .onFailure(e -> Console.log("Error checking email existence: " + e.getMessage()))
                .onSuccess(persons -> UiScheduler.runInUiThread(() -> {
                    if (persons.isEmpty()) {
                        guestPanel.hideEmailExistsAlert();
                    } else {
                        guestPanel.showEmailExistsAlert();
                    }
                }));
        });

        guestPanel.setOnSubmit(event -> {
            Document document = getWorkingBooking().getLastestDocumentAggregate().getDocument();
            document.setFirstName(guestPanel.getFirstName());
            document.setLastName(guestPanel.getLastName());
            document.setEmail(guestPanel.getEmail());
            document.setCountry(getEvent().getOrganization().getCountry());
            FXGuestToBook.setGuestToBook(document);
            // Call onGuestSubmitted() which allows multi-page forms to navigate to review
            if (currentBookingForm != null) {
                currentBookingForm.onGuestSubmitted();
            } else {
                submitBooking(); // Fallback to direct submission
            }
        });
    }

    // Exposing accountMountNodeProperty for the sub-routing binding (done in SlideController)
    ObjectProperty<Node> accountMountNodeProperty() {
        return embeddedLoginMountNodeProperty;
    }

    @Override
    protected void buildSlideUi() {
        // The content is actually set by the setBookingForm()
    }

    void setBookingForm(BookingForm bookingForm) {
        this.currentBookingForm = bookingForm;
        bookingForm.setActivityCallback(this);

        Node bookingFormUi = bookingForm.buildUi();
        if (bookingFormUi == null) {
            mainVbox.getChildren().clear();
            return;
        }

        EventBookingFormSettings settings = (EventBookingFormSettings) bookingForm.getSettings();
        boolean bookAsAGuestAllowed = settings.bookAsAGuestAllowed();
        if (bookAsAGuestAllowed)
            loginGuestFlipPane.setBack(guestPanel.getContainer());
        // Show/hide the prominent guest checkout option below login
        Layouts.setManagedAndVisibleProperties(guestOptionBox, bookAsAGuestAllowed);

        // The booking form decides at which point to show the default submitButton
        showDefaultSubmitButton(false);
        setPersonToBookRequired(false);
        disableSubmitButton(false);
        bookingFormTransitingProperty.bind(bookingForm.transitingProperty());

        // and if it should be disabled or not, but in addition to that, we disable it if we show the login
        defaultSubmitButton.disableProperty().bind(readyToSubmitBookingProperty.not());
        // Guest submit button disabled until all required fields are filled AND form allows submission
        guestPanel.getSubmitButton().disableProperty().bind(
            bookingFormDisableSubmitButtonProperty.or(Bindings.not(guestPanel.validProperty())));

        mainVbox.getChildren().setAll(
            bookingFormUi,
            defaultSubmitButton
        );
    }

    /**
     * Sets a modification view to display instead of the normal booking form.
     * Used when the user is modifying an existing booking to add options.
     *
     * @param modificationView The modification view to display
     */
    void setModificationView(Node modificationView) {
        this.currentBookingForm = null; // No booking form for modification view

        if (modificationView == null) {
            mainVbox.getChildren().clear();
            return;
        }

        // For modification view, we don't need the login/submit logic -
        // the modification form handles its own navigation and payment
        mainVbox.getChildren().setAll(modificationView);
    }

    private void updateLoginAndSubmitVisibility() {
        boolean transiting = bookingFormTransitingProperty.get();
        // Updating login visibility
        Person userPerson = FXUserPerson.getUserPerson();
        boolean showLogin = bookingFormPersonToBookRequiredProperty.get() && userPerson == null; // Means that the user is logged in with an account in Modality
        if (showLogin && loginContent.getContent() == null && !transiting) {
            WindowHistory.getProvider().push(CheckoutAccountRouting.getPath());
        }
        if (!transiting || showLogin)
            Layouts.setManagedAndVisibleProperties(loginGuestFlipPane, showLogin);
        Animations.animateProperty(loginGuestFlipPane.opacityProperty(), showLogin ? 1 : 0);
        if (!transiting) {
            Layouts.setManagedAndVisibleProperties(defaultSubmitButton, bookingFormShowDefaultSubmitButtonProperty.get() && !showLogin);
        }

        // Updating submit button visibility
        boolean readyToSubmitButton = !bookingFormDisableSubmitButtonProperty.get() && !showLogin;
        if (!readyToSubmitButton || !transiting)
            readyToSubmitBookingProperty.set(readyToSubmitButton);
        defaultSubmitButton.setDefaultButton(!showLogin);
    }

    @Override
    void turnOnWaitMode() {
        turnOnButtonWaitMode(defaultSubmitButton);
        turnOnButtonWaitMode(bookingFormSubmitButtons);
        guestPanel.turnOnButtonWaitMode();
    }

    @Override
    void turnOffWaitMode() {
        turnOffButtonWaitMode(defaultSubmitButton, BaseI18nKeys.Submit);
        if (!Arrays.isEmpty(bookingFormSubmitButtons))
            turnOffButtonWaitMode(bookingFormSubmitButtons[0], BaseI18nKeys.Submit); // TODO: get the correct i18n key from the booking form
        guestPanel.turnOffButtonWaitMode();
    }

    // Callback (called by the booking form or its pages)

    @Override
    public void setPersonToBookRequired(boolean required) {
        bookingFormPersonToBookRequiredProperty.set(required);
    }

    @Override
    public void showDefaultSubmitButton(boolean show) {
        bookingFormShowDefaultSubmitButtonProperty.set(show);
    }

    @Override
    public void disableSubmitButton(boolean disable) {
        bookingFormDisableSubmitButtonProperty.set(disable);
    }

    @Override
    public Region getEmbeddedLoginNode() {
        return loginGuestFlipPane;
    }

    @Override
    public BooleanExpression readyToSubmitBookingProperty() {
        return readyToSubmitBookingProperty;
    }

    private void submitBooking() {
        submitBooking(getWorkingBookingProperties().getBalance());
    }

    @Override
    public void submitBooking(int paymentDeposit, Consumer<GatewayPaymentForm> gatewayPaymentFormDisplayer, Button... submitButtons) {
        bookingFormSubmitButtons = submitButtons;
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = getWorkingBooking();
        // Three cases here:
        // 1) we pay an old balance with no new option, the currentBooking has no changes
        if (workingBooking.hasNoChanges()) {
            payOrThankYou(paymentDeposit, gatewayPaymentFormDisplayer);
        } else {
            // 2) the currentBooking has a new option
            turnOnWaitMode();
            // We look at the changes to fill the history
            String historyComment = WorkingBookingHistoryHelper.generateHistoryComment(workingBooking);
            workingBooking.submitChanges(historyComment, false)
                .inUiThread()
                // Turning off the wait mode in all cases - Might be turned on again in success if payment is required
                .onComplete(ar -> turnOffWaitMode())
                .onFailure(throwable -> {
                    displayErrorMessage(BookI18nKeys.ErrorWhileInsertingBooking);
                    Console.log(throwable);
                })
                .onSuccess(result -> {
                    workingBookingProperties.setBookingReference(result.documentRef());
                    payOrThankYou(paymentDeposit, gatewayPaymentFormDisplayer);
                });
        }
    }

    private void payOrThankYou(int paymentDeposit, Consumer<GatewayPaymentForm> gatewayPaymentFormDisplayer) {
        // If a payment is required, we initiate the payment and display the payment slide
        if (paymentDeposit > 0) {
            initiateNewPaymentAndDisplayPaymentSlide(paymentDeposit, gatewayPaymentFormDisplayer); // will turn on wait mode again
        } else { // if no payment is required, we display the thank-you slide
            displayThankYouSlide();
        }
    }

    @Override
    public void onEndReached() {
        getBookEventActivity().onEndSlideReached();
    }

    @Override
    public void navigateToLogin() {
        // Ensure the flip pane shows the login form (front), not the guest panel (back)
        loginGuestFlipPane.flipToFront();
        WindowHistory.getProvider().push(CheckoutAccountRouting.getPath());
    }
}
