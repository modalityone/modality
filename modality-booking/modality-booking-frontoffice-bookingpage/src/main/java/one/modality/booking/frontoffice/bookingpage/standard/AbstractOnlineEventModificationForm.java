package one.modality.booking.frontoffice.bookingpage.standard;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.platform.async.Future;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.markers.HasPersonalDetails;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.sections.audio.DefaultAudioRecordingSection;
import one.modality.booking.frontoffice.bookingpage.sections.confirmation.HasConfirmationSection;
import one.modality.booking.frontoffice.bookingpage.sections.payment.DefaultPaymentSection;
import one.modality.booking.frontoffice.bookingpage.sections.payment.HasPaymentSection;
import one.modality.booking.frontoffice.bookingpage.sections.summary.DefaultExistingBookingSummarySection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.Set;

/**
 * Abstract base class for booking modification forms.
 *
 * <p>A "Booking Modification" form allows adding options (like audio recordings)
 * to an existing booking. It implements a 3-step flow:</p>
 * <ol>
 *   <li>Select Options - Choose additional items to add</li>
 *   <li>Payment - Select payment amount, method, accept terms, pay</li>
 *   <li>Confirmation - Success message</li>
 * </ol>
 *
 * <p>This is a standalone form that does NOT use StandardBookingFormBuilder.
 * It manages its own step navigation and UI.</p>
 *
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #getColorScheme()} - returns the color theme for the form</li>
 *   <li>{@link #createConfirmationSection()} - creates the confirmation section</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public abstract class AbstractOnlineEventModificationForm {

    // === Step Constants ===
    protected static final int STEP_OPTIONS = 1;
    protected static final int STEP_PAYMENT = 2;
    protected static final int STEP_CONFIRMATION = 3;

    // === Data ===
    protected final WorkingBookingProperties workingBookingProperties;
    protected final BookingFormColorScheme colorScheme;

    // === State ===
    protected final IntegerProperty currentStep = new SimpleIntegerProperty(STEP_OPTIONS);
    protected final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    protected final BooleanProperty hasNewSelectionsProperty = new SimpleBooleanProperty(false);

    // === Sections ===
    protected DefaultExistingBookingSummarySection bookingSummarySection;
    protected DefaultAudioRecordingSection audioRecordingSection;
    protected DefaultPaymentSection paymentSection;
    protected HasConfirmationSection confirmationSection;

    // === UI ===
    protected final VBox rootContainer;
    protected final VBox contentContainer;
    protected final VBox stepProgressContainer;

    // === Callbacks ===
    protected Runnable onComplete;
    protected Runnable onCancel;
    protected Runnable onProceedToPayment;

    // ========================================
    // Abstract Methods (Required)
    // ========================================

    /**
     * Returns the color scheme for this form.
     *
     * @return the color scheme to use for theming
     */
    protected abstract BookingFormColorScheme getColorScheme();

    /**
     * Creates the confirmation section.
     * The section should implement {@link HasConfirmationSection}.
     *
     * @return the confirmation section
     */
    protected abstract HasConfirmationSection createConfirmationSection();

    // ========================================
    // Override Points (Optional)
    // ========================================

    /**
     * Configures the booking summary section.
     * Default hides the package row (not relevant for modification).
     *
     * @param section the section to configure
     */
    protected void configureBookingSummarySection(DefaultExistingBookingSummarySection section) {
        section.setShowPackage(false);
    }

    /**
     * Configures the audio recording section.
     * Default does nothing.
     *
     * @param section the section to configure
     */
    protected void configureAudioRecordingSection(DefaultAudioRecordingSection section) {
        // Default: no additional configuration
    }

    /**
     * Configures the payment section.
     * Default sets full payment only mode.
     *
     * @param section the section to configure
     */
    protected void configurePaymentSection(DefaultPaymentSection section) {
        section.setFullPaymentOnly(true);
    }

    /**
     * Returns the i18n key for the "no options available" warning message.
     * Override to provide a custom message.
     *
     * @return the i18n key for the warning message
     */
    protected Object getNoOptionsAvailableMessageKey() {
        return BookingPageI18nKeys.NoRecordingsAvailable;
    }

    /**
     * Returns the i18n key for the "all options already purchased" warning message.
     * Override to provide a custom message.
     *
     * @return the i18n key for the warning message
     */
    protected Object getAllOptionsPurchasedMessageKey() {
        return BookingPageI18nKeys.AllRecordingsAlreadyPurchased;
    }

    // ========================================
    // Constructor
    // ========================================

    /**
     * Creates a new booking modification form.
     *
     * @param workingBookingProperties the working booking properties
     */
    protected AbstractOnlineEventModificationForm(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
        this.colorScheme = getColorScheme();

        this.rootContainer = new VBox(0);
        this.contentContainer = new VBox(24);
        this.stepProgressContainer = new VBox(0);

        initializeSections();
        buildUI();
    }

    // ========================================
    // Section Initialization
    // ========================================

    protected void initializeSections() {
        // Create sections
        bookingSummarySection = new DefaultExistingBookingSummarySection();
        audioRecordingSection = new DefaultAudioRecordingSection();
        paymentSection = new DefaultPaymentSection();
        confirmationSection = createConfirmationSection();

        // Set color scheme
        bookingSummarySection.setColorScheme(colorScheme);
        audioRecordingSection.setColorScheme(colorScheme);
        paymentSection.setColorScheme(colorScheme);
        confirmationSection.setColorScheme(colorScheme);

        // Configure sections
        configureBookingSummarySection(bookingSummarySection);
        configureAudioRecordingSection(audioRecordingSection);
        configurePaymentSection(paymentSection);

        // Wire up sections with WorkingBookingProperties
        bookingSummarySection.setWorkingBookingProperties(workingBookingProperties);
        audioRecordingSection.setWorkingBookingProperties(workingBookingProperties);
        paymentSection.setWorkingBookingProperties(workingBookingProperties);
        confirmationSection.setWorkingBookingProperties(workingBookingProperties);

        // Configure payment section callbacks
        paymentSection.setOnBackPressed(() -> showStep(STEP_OPTIONS));
        paymentSection.setOnPaymentSubmit(this::handlePaymentSubmit);

        // Listen to audio recording selection changes
        audioRecordingSection.setOnSelectionChanged(this::onAudioSelectionChanged);
    }

    protected void onAudioSelectionChanged(Set<Item> selectedItems) {
        // Check if there are new selections beyond locked items
        Set<Item> lockedItems = audioRecordingSection.getLockedItems();
        boolean hasNew = false;
        for (Item item : selectedItems) {
            if (!lockedItems.contains(item)) {
                hasNew = true;
                break;
            }
        }
        hasNewSelectionsProperty.set(hasNew);

        // Update price calculation
        workingBookingProperties.updateAll();
    }

    // ========================================
    // UI Building
    // ========================================

    protected void buildUI() {
        rootContainer.setAlignment(Pos.TOP_CENTER);
        rootContainer.getStyleClass().addAll("booking-form-container", "theme-" + colorScheme.getId());

        // Container with max-width
        VBox innerContainer = new VBox(0);
        innerContainer.setMaxWidth(800);
        innerContainer.setPadding(new Insets(32, 24, 32, 24));

        // Step progress header
        innerContainer.getChildren().add(stepProgressContainer);

        // Content area
        innerContainer.getChildren().add(contentContainer);

        // Loading overlay
        MonoPane loadingOverlay = new MonoPane();
        loadingOverlay.setAlignment(Pos.CENTER);
        loadingOverlay.getStyleClass().addAll("booking-form-loading-overlay", "bookingpage-loading-overlay");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(48, 48);
        loadingOverlay.setContent(spinner);

        loadingOverlay.visibleProperty().bind(isLoading);
        loadingOverlay.managedProperty().bind(isLoading);

        // StackPane for overlay
        StackPane mainContent = new StackPane(innerContainer, loadingOverlay);
        rootContainer.getChildren().add(mainContent);

        // Show initial step
        showStep(STEP_OPTIONS);
    }

    // ========================================
    // Step Navigation
    // ========================================

    protected void showStep(int step) {
        currentStep.set(step);
        contentContainer.getChildren().clear();

        updateStepProgress();

        switch (step) {
            case STEP_OPTIONS:
                showOptionsStep();
                break;
            case STEP_PAYMENT:
                showPaymentStep();
                break;
            case STEP_CONFIRMATION:
                showConfirmationStep();
                break;
        }
    }

    protected void updateStepProgress() {
        stepProgressContainer.getChildren().clear();

        // Hide on confirmation step
        if (currentStep.get() == STEP_CONFIRMATION) {
            return;
        }

        VBox progress = createStepProgress();
        stepProgressContainer.getChildren().add(progress);
    }

    protected VBox createStepProgress() {
        VBox wrapper = new VBox(0);
        wrapper.setPadding(new Insets(0, 0, 25, 0));
        wrapper.getStyleClass().add("booking-form-step-progress-wrapper");

        String[] stepNames = {"Options", "Payment", "Confirmation"};
        int current = currentStep.get();

        StackPane container = new StackPane();
        container.setPadding(new Insets(25, 0, 25, 0));
        container.getStyleClass().add("booking-form-step-progress");

        // Progress line
        Line progressLine = new Line();
        progressLine.setStroke(Color.web("#e0e0e0"));
        progressLine.setStrokeWidth(2);
        progressLine.endXProperty().bind(container.widthProperty().subtract(100));
        progressLine.setStartX(0);
        StackPane.setAlignment(progressLine, Pos.TOP_CENTER);
        StackPane.setMargin(progressLine, new Insets(20, 0, 0, 0));

        // Steps row
        HBox stepsRow = new HBox();
        stepsRow.setAlignment(Pos.CENTER);
        stepsRow.setFillHeight(true);

        for (int i = 1; i <= 3; i++) {
            boolean isActive = i == current;
            boolean isCompleted = i < current;

            VBox stepBox = createStepIndicator(i, stepNames[i - 1], isActive, isCompleted);
            HBox.setHgrow(stepBox, Priority.ALWAYS);
            stepsRow.getChildren().add(stepBox);
        }

        container.getChildren().addAll(progressLine, stepsRow);

        // Divider
        Region dividerLine = new Region();
        dividerLine.setMinHeight(1);
        dividerLine.setPrefHeight(1);
        dividerLine.setMaxHeight(1);
        dividerLine.getStyleClass().add("bookingpage-divider-light");

        wrapper.getChildren().addAll(container, dividerLine);
        return wrapper;
    }

    protected VBox createStepIndicator(int stepNum, String label, boolean isActive, boolean isCompleted) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("step-progress-item");

        if (isCompleted) {
            box.getStyleClass().add("completed");
        } else if (isActive) {
            box.getStyleClass().add("active");
        }

        StackPane circle = new StackPane();
        circle.getStyleClass().add("step-bubble");

        Label numLabel;
        if (isCompleted) {
            numLabel = new Label("\u2713");
        } else {
            numLabel = new Label(String.valueOf(stepNum));
        }
        numLabel.getStyleClass().add("step-number");
        circle.getChildren().add(numLabel);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("step-progress-label");

        box.getChildren().addAll(circle, labelNode);
        return box;
    }

    // ========================================
    // Step 1: Options
    // ========================================

    protected void showOptionsStep() {
        // Booking summary
        VBox summarySection = new VBox(0);
        summarySection.getChildren().add(bookingSummarySection.getView());

        // Audio recordings
        VBox audioSection = new VBox(0);
        audioSection.getChildren().add(audioRecordingSection.getView());

        // Warning box if no options available
        Node warningBox = null;
        if (audioRecordingSection.hasAnyRecordings() && !audioRecordingSection.hasAvailableRecordings()) {
            warningBox = BookingPageUIBuilder.createInfoBox(
                getAllOptionsPurchasedMessageKey(),
                BookingPageUIBuilder.InfoBoxType.WARNING
            );
        } else if (!audioRecordingSection.hasAnyRecordings()) {
            warningBox = BookingPageUIBuilder.createInfoBox(
                getNoOptionsAvailableMessageKey(),
                BookingPageUIBuilder.InfoBoxType.WARNING
            );
        }

        // Continue button
        Button continueBtn = BookingPageUIBuilder.createPrimaryButton(BookingPageI18nKeys.Continue);
        continueBtn.setMaxWidth(Double.MAX_VALUE);
        continueBtn.setOnAction(e -> showStep(STEP_PAYMENT));
        continueBtn.disableProperty().bind(hasNewSelectionsProperty.not());

        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().add(continueBtn);
        VBox.setMargin(buttonRow, new Insets(24, 0, 0, 0));

        if (warningBox != null) {
            VBox.setMargin(warningBox, new Insets(0, 0, 16, 0));
            contentContainer.getChildren().addAll(summarySection, audioSection, warningBox, buttonRow);
        } else {
            contentContainer.getChildren().addAll(summarySection, audioSection, buttonRow);
        }
    }

    // ========================================
    // Step 2: Payment
    // ========================================

    protected void showPaymentStep() {
        updatePaymentFromSelections();

        contentContainer.getChildren().add(paymentSection.getView());

        // Button row
        HBox buttonRow = BookingPageUIBuilder.createNavigationButtonRow();
        VBox.setMargin(buttonRow, new Insets(24, 0, 0, 0));

        // Back button
        Button backBtn = BookingPageUIBuilder.createBackButton(BookingPageI18nKeys.Back);
        backBtn.setOnAction(e -> showStep(STEP_OPTIONS));

        // Pay button
        Button payBtn = new Button();
        payBtn.textProperty().bind(paymentSection.payButtonTextProperty());
        payBtn.disableProperty().bind(paymentSection.payButtonDisabledProperty());
        payBtn.setCursor(javafx.scene.Cursor.HAND);
        payBtn.getStyleClass().addAll("booking-form-primary-btn", "booking-form-primary-btn-text");
        payBtn.setOnAction(e -> paymentSection.submitPaymentAsync());
        payBtn.setGraphicTextGap(16);

        buttonRow.getChildren().addAll(backBtn, payBtn);
        contentContainer.getChildren().add(buttonRow);
    }

    protected void updatePaymentFromSelections() {
        paymentSection.clearBookingItems();

        String attendeeName = getAttendeeName();
        if (attendeeName == null || attendeeName.isEmpty()) {
            attendeeName = "Additional Options";
        }

        int totalInCents = workingBookingProperties.getBalance();

        paymentSection.addBookingItem(new HasPaymentSection.PaymentBookingItem(
            workingBookingProperties.getWorkingBooking().getDocument(),
            attendeeName,
            "Audio Recording Options",
            totalInCents
        ));

        paymentSection.setTotalAmount(totalInCents);
        paymentSection.setDepositAmount(totalInCents / 10);
    }

    protected Future<Void> handlePaymentSubmit(HasPaymentSection.PaymentResult result) {
        if (onProceedToPayment != null) {
            onProceedToPayment.run();
        } else {
            showStep(STEP_CONFIRMATION);
        }
        return Future.succeededFuture();
    }

    // ========================================
    // Step 3: Confirmation
    // ========================================

    protected void showConfirmationStep() {
        confirmationSection.clearConfirmedBookings();

        String bookingRef = bookingSummarySection.getBookingReference();
        String attendeeName = getAttendeeName();
        String email = getAttendeeEmail();

        if (bookingRef != null) {
            confirmationSection.addConfirmedBooking(
                new HasConfirmationSection.ConfirmedBooking(
                    attendeeName != null ? attendeeName : "Guest",
                    email != null ? email : "",
                    bookingRef
                )
            );
        }

        String eventName = getEventName();
        if (eventName != null) {
            confirmationSection.setEventName(eventName);
        }

        int totalInCents = workingBookingProperties.getBalance();
        confirmationSection.setPaymentAmounts(totalInCents, 0, totalInCents);

        contentContainer.getChildren().add(confirmationSection.getView());

        // Action button
        Button viewBookingsBtn = I18nControls.newButton(BookingPageI18nKeys.ViewMyBookings);
        viewBookingsBtn.setCursor(javafx.scene.Cursor.HAND);
        viewBookingsBtn.getStyleClass().addAll("booking-form-primary-btn", "booking-form-primary-btn-text");
        viewBookingsBtn.setOnAction(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        VBox.setMargin(viewBookingsBtn, new Insets(24, 0, 0, 0));

        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().add(viewBookingsBtn);
        contentContainer.getChildren().add(buttonRow);
    }

    // ========================================
    // Helper Methods
    // ========================================

    protected HasPersonalDetails getAttendeePersonalDetails() {
        Document document = workingBookingProperties.getWorkingBooking().getDocument();
        Person person = document.getPerson();
        return person != null ? person : document;
    }

    protected String getAttendeeName() {
        return getAttendeePersonalDetails().getFullName();
    }

    protected String getAttendeeEmail() {
        return getAttendeePersonalDetails().getEmail();
    }

    protected String getEventName() {
        if (workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null) {
            var event = workingBookingProperties.getWorkingBooking().getEvent();
            if (event != null) {
                return event.getName();
            }
        }
        return null;
    }

    // ========================================
    // Public API
    // ========================================

    /**
     * Returns the root UI node for this form.
     */
    public Node getView() {
        return rootContainer;
    }

    /**
     * Sets a callback to run when the modification is complete.
     */
    public void setOnComplete(Runnable callback) {
        this.onComplete = callback;
    }

    /**
     * Sets a callback to run when the user cancels the modification.
     */
    public void setOnCancel(Runnable callback) {
        this.onCancel = callback;
    }

    /**
     * Sets a callback to run when proceeding to payment.
     */
    public void setOnProceedToPayment(Runnable callback) {
        this.onProceedToPayment = callback;
    }

    /**
     * Shows the confirmation step (call after successful payment).
     */
    public void showConfirmation() {
        showStep(STEP_CONFIRMATION);
    }

    /**
     * Returns the current step number (1-3).
     */
    public int getCurrentStep() {
        return currentStep.get();
    }

    /**
     * Returns the current step property for binding.
     */
    public ReadOnlyIntegerProperty currentStepProperty() {
        return currentStep;
    }

    /**
     * Returns the loading state property for binding.
     */
    public ReadOnlyBooleanProperty loadingProperty() {
        return isLoading;
    }

    /**
     * Returns the WorkingBookingProperties for this form.
     */
    public WorkingBookingProperties getWorkingBookingProperties() {
        return workingBookingProperties;
    }
}
