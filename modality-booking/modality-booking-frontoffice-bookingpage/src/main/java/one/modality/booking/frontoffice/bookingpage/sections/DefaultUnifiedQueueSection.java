package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import one.modality.base.client.time.ModalityDates;
import one.modality.base.shared.entities.Event;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.components.price.UnifiedPriceDisplay;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.util.Attendances;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.Arrays;
import java.util.List;

/**
 * Unified section that handles both pre-countdown waiting and queue processing states.
 * This consolidates the functionality of DefaultRegistrationWaitingSection and
 * DefaultQueueProcessingSection into a single page with dynamic content switching.
 *
 * <p>UI Structure (from mockup):</p>
 * <ul>
 *   <li>Danger banner: "Do Not Refresh This Page" warning</li>
 *   <li>Status header: Icon badge + title (changes based on state)</li>
 *   <li>Queue explanation: "How the Queue Works" with 3 numbered steps</li>
 *   <li>Temporary notice: Optional amber warning about new booking system</li>
 *   <li>Dynamic content area: Countdown timer OR progress bar</li>
 *   <li>Registration summary: Uses existing OrderDetails component</li>
 *   <li>Action button: "Leave Queue and Edit Booking" (only during countdown)</li>
 * </ul>
 *
 * <p>States:</p>
 * <ul>
 *   <li>PRE_COUNTDOWN: Waiting for registration to open (shows countdown timer)</li>
 *   <li>QUEUE_PROCESSING: Registration is being processed (shows progress bar)</li>
 * </ul>
 *
 * @author Claude Code
 */
public class DefaultUnifiedQueueSection implements BookingFormSection {

    /**
     * States for the unified queue page.
     */
    public enum QueueState {
        /** Waiting for registration to open - shows countdown timer */
        PRE_COUNTDOWN,
        /** Registration is being processed - shows progress bar */
        QUEUE_PROCESSING
    }

    // SVG Icons
    private static final String ICON_WARNING_TRIANGLE = "M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z M12 9v4 M12 17h.01";
    private static final String ICON_CHECKMARK_CIRCLE = "M22 11.08V12a10 10 0 1 1-5.93-9.14 M22 4L12 14.01l-3-3";
    private static final String ICON_CLOCK = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67V7z";
    private static final String ICON_INFO = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z";

    // Status messages for rotation during processing
    private static final List<Object> STATUS_MESSAGE_KEYS = Arrays.asList(
        BookingPageI18nKeys.StatusReceiving,
        BookingPageI18nKeys.StatusJoiningQueue,
        BookingPageI18nKeys.StatusProcessing,
        BookingPageI18nKeys.StatusCheckingAvailability,
        BookingPageI18nKeys.StatusAllocating,
        BookingPageI18nKeys.StatusAlmostThere
    );

    // State
    private final ObjectProperty<QueueState> currentState = new SimpleObjectProperty<>(QueueState.PRE_COUNTDOWN);
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    private final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);

    // Callbacks
    private Runnable onCountdownComplete;
    private Runnable onLeaveQueueAndEdit;

    // Data
    private WorkingBookingProperties workingBookingProperties;
    private Event event;
    private UnifiedPriceDisplay unifiedPriceDisplay;

    // UI Components - Main container
    private final VBox container = new VBox();

    // Dynamic content areas (swapped based on state)
    private VBox countdownContent;
    private VBox progressContent;
    private StackPane dynamicContentArea;

    // Countdown timer components
    private Label hoursLabel;
    private Label minutesLabel;
    private Label secondsLabel;
    private Timeline countdownTimer;
    private int remainingSeconds = 0;

    // Progress bar components
    private StackPane progressBarFill;
    private Label progressPercentLabel;
    private Label positionLabel;
    private Label totalLabel;
    private Label statusMessageLabel;
    private Timeline statusMessageTimer;
    private int currentStatusIndex = 0;

    // Status header components (for state-based icon changes)
    private StackPane statusIconBadge;
    private Label statusTitle;
    private Label statusSubtitle;

    // Action button (only visible during countdown)
    private HBox actionButtonContainer;

    // Summary container
    private VBox summaryContainer;

    public DefaultUnifiedQueueSection() {
        buildUI();
    }

    private void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(20);
        container.setMaxWidth(720);
        container.setPadding(new Insets(32, 24, 32, 24));
        container.getStyleClass().add("registration-queue-container");

        // Build all components
        container.getChildren().addAll(
            createDangerBanner(),
            createStatusHeader(),
            createQueueExplanation(),
            createTemporaryNotice(),
            createDynamicContentArea(),
            createRegistrationSummary(),
            createActionButton()
        );

        // Set initial state
        updateUIForState(QueueState.PRE_COUNTDOWN);
    }

    // ===============================
    // UI COMPONENT BUILDERS
    // ===============================

    /**
     * Creates the danger banner warning users not to refresh.
     */
    private HBox createDangerBanner() {
        HBox banner = new HBox(14);
        banner.setAlignment(Pos.TOP_LEFT);
        banner.setPadding(new Insets(16, 20, 16, 20));
        banner.getStyleClass().add("registration-queue-danger-banner");

        // Warning icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(40, 40);
        iconCircle.setMaxSize(40, 40);
        iconCircle.getStyleClass().add("registration-queue-danger-icon");

        SVGPath warningIcon = new SVGPath();
        warningIcon.setContent(ICON_WARNING_TRIANGLE);
        warningIcon.setScaleX(0.9);
        warningIcon.setScaleY(0.9);
        warningIcon.getStyleClass().add("registration-queue-danger-icon-svg");
        iconCircle.getChildren().add(warningIcon);

        // Text content
        VBox textContent = new VBox(6);
        textContent.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = I18nControls.newLabel("DoNotRefreshBannerTitle");
        title.getStyleClass().add("registration-queue-danger-title");

        Label message = I18nControls.newLabel("DoNotRefreshBannerMessage");
        message.setWrapText(true);
        message.getStyleClass().add("registration-queue-danger-message");

        textContent.getChildren().addAll(title, message);
        banner.getChildren().addAll(iconCircle, textContent);

        return banner;
    }

    /**
     * Creates the status header with icon badge and title.
     * Icon and text change based on state.
     */
    private VBox createStatusHeader() {
        VBox header = new VBox(16);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.getStyleClass().add("registration-queue-status-header");

        // Icon badge (56px circle) - will contain checkmark or clock based on state
        statusIconBadge = new StackPane();
        statusIconBadge.setMinSize(56, 56);
        statusIconBadge.setMaxSize(56, 56);
        statusIconBadge.getStyleClass().add("registration-queue-icon-badge");

        // Title and subtitle
        statusTitle = new Label();
        statusTitle.getStyleClass().add("registration-queue-status-title");

        statusSubtitle = new Label();
        statusSubtitle.setWrapText(true);
        statusSubtitle.getStyleClass().add("registration-queue-status-subtitle");

        header.getChildren().addAll(statusIconBadge, statusTitle, statusSubtitle);

        return header;
    }

    /**
     * Creates the "How the Queue Works" explanation card with 3 numbered steps.
     */
    private VBox createQueueExplanation() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("registration-queue-explanation");

        // Header with info icon
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        StackPane infoIconBadge = new StackPane();
        infoIconBadge.setMinSize(28, 28);
        infoIconBadge.setMaxSize(28, 28);
        infoIconBadge.getStyleClass().add("registration-queue-explanation-icon");

        SVGPath infoIcon = new SVGPath();
        infoIcon.setContent(ICON_INFO);
        infoIcon.setScaleX(0.6);
        infoIcon.setScaleY(0.6);
        infoIcon.getStyleClass().add("registration-queue-explanation-icon-svg");
        infoIconBadge.getChildren().add(infoIcon);

        Label headerLabel = I18nControls.newLabel("HowTheQueueWorks");
        headerLabel.getStyleClass().add("registration-queue-explanation-header");

        headerRow.getChildren().addAll(infoIconBadge, headerLabel);

        // Steps
        VBox stepsContainer = new VBox(12);
        stepsContainer.getChildren().addAll(
            createExplanationStep("1", "QueueStep1Title", "QueueStep1Desc"),
            createExplanationStep("2", "QueueStep2Title", "QueueStep2Desc"),
            createExplanationStep("3", "QueueStep3Title", "QueueStep3Desc")
        );

        card.getChildren().addAll(headerRow, stepsContainer);

        return card;
    }

    /**
     * Creates a single explanation step with number badge and text.
     */
    private HBox createExplanationStep(String number, Object titleKey, Object descKey) {
        HBox step = new HBox(12);
        step.setAlignment(Pos.TOP_LEFT);
        step.getStyleClass().add("registration-queue-explanation-step");

        // Number badge
        StackPane numberBadge = new StackPane();
        numberBadge.setMinSize(24, 24);
        numberBadge.setMaxSize(24, 24);
        numberBadge.getStyleClass().add("registration-queue-explanation-number");

        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("registration-queue-explanation-number-text");
        numberBadge.getChildren().add(numberLabel);

        // Text content
        VBox textContent = new VBox(2);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = I18nControls.newLabel(titleKey);
        title.getStyleClass().add("registration-queue-explanation-step-title");

        Label desc = I18nControls.newLabel(descKey);
        desc.setWrapText(true);
        desc.getStyleClass().add("registration-queue-explanation-step-desc");

        textContent.getChildren().addAll(title, desc);
        step.getChildren().addAll(numberBadge, textContent);

        return step;
    }

    /**
     * Creates the optional amber temporary notice about new booking system.
     */
    private HBox createTemporaryNotice() {
        HBox notice = new HBox(12);
        notice.setAlignment(Pos.TOP_LEFT);
        notice.setPadding(new Insets(14, 18, 14, 18));
        notice.getStyleClass().add("registration-queue-notice");

        // Info icon badge
        StackPane iconBadge = new StackPane();
        iconBadge.setMinSize(24, 24);
        iconBadge.setMaxSize(24, 24);
        iconBadge.getStyleClass().add("registration-queue-notice-icon");

        SVGPath infoIcon = new SVGPath();
        infoIcon.setContent(ICON_INFO);
        infoIcon.setScaleX(0.6);
        infoIcon.setScaleY(0.6);
        infoIcon.getStyleClass().add("registration-queue-notice-icon-svg");
        iconBadge.getChildren().add(infoIcon);

        // Text content
        VBox textContent = new VBox(4);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = I18nControls.newLabel("NewBookingSystemNotice");
        title.getStyleClass().add("registration-queue-notice-title");

        Label desc = I18nControls.newLabel("NewBookingSystemNoticeDesc");
        desc.setWrapText(true);
        desc.getStyleClass().add("registration-queue-notice-desc");

        textContent.getChildren().addAll(title, desc);
        notice.getChildren().addAll(iconBadge, textContent);

        return notice;
    }

    /**
     * Creates the dynamic content area that swaps between countdown and progress.
     */
    private StackPane createDynamicContentArea() {
        dynamicContentArea = new StackPane();
        dynamicContentArea.getStyleClass().add("registration-queue-dynamic-area");

        // Create both content variants
        countdownContent = createCountdownContent();
        progressContent = createProgressContent();

        // Add both to stack (visibility controlled by state)
        dynamicContentArea.getChildren().addAll(countdownContent, progressContent);

        return dynamicContentArea;
    }

    /**
     * Creates the countdown timer content (PRE_COUNTDOWN state).
     */
    private VBox createCountdownContent() {
        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));
        content.getStyleClass().add("registration-queue-countdown");

        // "Registration opens in" label
        Label opensInLabel = I18nControls.newLabel(BookingPageI18nKeys.RegistrationOpensIn);
        opensInLabel.getStyleClass().add("registration-queue-countdown-label");

        // Timer display: HH : MM : SS
        HBox timerDisplay = new HBox(12);
        timerDisplay.setAlignment(Pos.CENTER);

        // Hours
        VBox hoursBox = createTimerDigitBox("00", BookingPageI18nKeys.Hours);
        hoursLabel = (Label) ((StackPane) hoursBox.getChildren().get(0)).getChildren().get(0);

        // Colon separator
        Label colon1 = new Label(":");
        colon1.getStyleClass().add("registration-queue-countdown-colon");
        VBox.setMargin(colon1, new Insets(0, 0, 24, 0));

        // Minutes
        VBox minutesBox = createTimerDigitBox("00", BookingPageI18nKeys.Minutes);
        minutesLabel = (Label) ((StackPane) minutesBox.getChildren().get(0)).getChildren().get(0);

        // Colon separator
        Label colon2 = new Label(":");
        colon2.getStyleClass().add("registration-queue-countdown-colon");
        VBox.setMargin(colon2, new Insets(0, 0, 24, 0));

        // Seconds
        VBox secondsBox = createTimerDigitBox("00", BookingPageI18nKeys.Seconds);
        secondsLabel = (Label) ((StackPane) secondsBox.getChildren().get(0)).getChildren().get(0);

        timerDisplay.getChildren().addAll(hoursBox, colon1, minutesBox, colon2, secondsBox);

        content.getChildren().addAll(opensInLabel, timerDisplay);

        return content;
    }

    /**
     * Creates a timer digit box with unit label below.
     */
    private VBox createTimerDigitBox(String initialValue, Object unitKey) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);

        // Digit display
        Label digitLabel = new Label(initialValue);
        digitLabel.setMinWidth(80);
        digitLabel.setAlignment(Pos.CENTER);
        digitLabel.getStyleClass().add("registration-queue-countdown-digit");

        StackPane digitPane = new StackPane(digitLabel);
        digitPane.setPadding(new Insets(16, 24, 16, 24));
        digitPane.getStyleClass().add("registration-queue-countdown-digit-box");

        // Unit label below
        Label unitLabel = I18nControls.newLabel(unitKey);
        unitLabel.getStyleClass().add("registration-queue-countdown-unit");

        box.getChildren().addAll(digitPane, unitLabel);

        return box;
    }

    /**
     * Creates the progress bar content (QUEUE_PROCESSING state).
     */
    private VBox createProgressContent() {
        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));
        content.getStyleClass().add("registration-queue-progress");

        // "Processing Now" header
        Label processingLabel = I18nControls.newLabel("ProcessingNow");
        processingLabel.getStyleClass().add("registration-queue-progress-header");

        // Progress bar section
        VBox progressSection = new VBox(8);

        // Progress label row
        HBox progressLabelRow = new HBox();
        progressLabelRow.setAlignment(Pos.CENTER_LEFT);

        Label progressLabel = I18nControls.newLabel(BookingPageI18nKeys.Progress);
        progressLabel.getStyleClass().add("registration-queue-progress-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        progressPercentLabel = new Label("0%");
        progressPercentLabel.getStyleClass().add("registration-queue-progress-percent");

        progressLabelRow.getChildren().addAll(progressLabel, spacer, progressPercentLabel);

        // Progress bar track and fill
        StackPane progressBar = new StackPane();
        progressBar.setMinHeight(14);
        progressBar.setMaxHeight(14);
        progressBar.setAlignment(Pos.CENTER_LEFT);

        Region track = new Region();
        track.setMinHeight(14);
        track.setMaxHeight(14);
        track.getStyleClass().add("registration-queue-progress-track");
        HBox.setHgrow(track, Priority.ALWAYS);

        progressBarFill = new StackPane();
        progressBarFill.setMinHeight(14);
        progressBarFill.setMaxHeight(14);
        progressBarFill.setMaxWidth(0);
        progressBarFill.getStyleClass().add("registration-queue-progress-fill");

        progressBar.getChildren().addAll(track, progressBarFill);

        progressSection.getChildren().addAll(progressLabelRow, progressBar);

        // Queue position display
        HBox positionRow = new HBox(6);
        positionRow.setAlignment(Pos.CENTER);
        positionRow.setPadding(new Insets(12, 16, 12, 16));
        positionRow.getStyleClass().add("registration-queue-position");

        Label processingBookingLabel = I18nControls.newLabel("CurrentlyProcessingBooking");
        processingBookingLabel.getStyleClass().add("registration-queue-position-text");

        positionLabel = new Label("0");
        positionLabel.getStyleClass().add("registration-queue-position-number");

        Label ofLabel = I18nControls.newLabel("Of");
        ofLabel.getStyleClass().add("registration-queue-position-text");

        totalLabel = new Label("0");
        totalLabel.getStyleClass().add("registration-queue-position-number");

        positionRow.getChildren().addAll(processingBookingLabel, positionLabel, ofLabel, totalLabel);

        // Status message (rotating)
        statusMessageLabel = new Label();
        statusMessageLabel.getStyleClass().add("registration-queue-status-message");

        content.getChildren().addAll(processingLabel, progressSection, positionRow, statusMessageLabel);

        return content;
    }

    /**
     * Creates the registration summary section (same styling as DefaultSummarySection).
     */
    private VBox createRegistrationSummary() {
        VBox section = new VBox(0);

        // Section header using StyledSectionHeader (same as DefaultSummarySection)
        HBox header = new StyledSectionHeader(BookingPageI18nKeys.YourRegistration, StyledSectionHeader.ICON_CLIPBOARD);

        // Content box using createPassiveCard (same as DefaultSummarySection)
        VBox contentBox = BookingPageUIBuilder.createPassiveCard();

        // Price lines container (will be populated when workingBookingProperties is set)
        summaryContainer = new VBox(0);

        Label placeholder = new Label("Loading...");
        placeholder.getStyleClass().add("bookingpage-text-muted");
        summaryContainer.getChildren().add(placeholder);

        contentBox.getChildren().add(summaryContainer);
        section.getChildren().addAll(header, contentBox);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));

        return section;
    }

    /**
     * Creates the "Leave Queue and Edit Booking" action button.
     * Only visible during PRE_COUNTDOWN state.
     */
    private HBox createActionButton() {
        actionButtonContainer = new HBox();
        actionButtonContainer.setAlignment(Pos.CENTER);
        actionButtonContainer.setPadding(new Insets(24, 0, 0, 0));

        Label buttonLabel = I18nControls.newLabel("LeaveQueueAndEditBooking");

        HBox button = new HBox(8);
        button.setAlignment(Pos.CENTER);
        button.setPadding(new Insets(12, 24, 12, 24));
        button.getStyleClass().add("registration-queue-action-button");
        button.setOnMouseClicked(e -> {
            if (onLeaveQueueAndEdit != null) {
                onLeaveQueueAndEdit.run();
            }
        });

        button.getChildren().add(buttonLabel);
        actionButtonContainer.getChildren().add(button);

        return actionButtonContainer;
    }

    // ===============================
    // STATE MANAGEMENT
    // ===============================

    /**
     * Sets the current state and updates UI accordingly.
     */
    public void setState(QueueState state) {
        currentState.set(state);
        updateUIForState(state);
    }

    /**
     * Gets the current state.
     */
    public QueueState getState() {
        return currentState.get();
    }

    /**
     * Transitions from PRE_COUNTDOWN to QUEUE_PROCESSING state.
     */
    public void transitionToProcessing() {
        stopCountdown();
        setState(QueueState.QUEUE_PROCESSING);
        startStatusMessageRotation();
    }

    /**
     * Updates the UI for the given state.
     */
    private void updateUIForState(QueueState state) {
        boolean isProcessing = state == QueueState.QUEUE_PROCESSING;

        // Update status header icon
        statusIconBadge.getChildren().clear();
        statusIconBadge.getStyleClass().remove("registration-queue-icon-badge-pulse");

        SVGPath statusIcon = new SVGPath();
        if (isProcessing) {
            statusIcon.setContent(ICON_CLOCK);
            statusIconBadge.getStyleClass().add("registration-queue-icon-badge-pulse");
        } else {
            statusIcon.setContent(ICON_CHECKMARK_CIRCLE);
        }
        statusIcon.setScaleX(1.2);
        statusIcon.setScaleY(1.2);
        statusIcon.getStyleClass().add("registration-queue-icon-badge-svg");
        statusIconBadge.getChildren().add(statusIcon);

        // Update status title and subtitle
        if (isProcessing) {
            I18n.bindI18nTextProperty(statusTitle.textProperty(), "ProcessingYourRegistration");
            I18n.bindI18nTextProperty(statusSubtitle.textProperty(), "ProcessingSubtitle");
        } else {
            I18n.bindI18nTextProperty(statusTitle.textProperty(), "RegistrationSubmittedTitle");
            I18n.bindI18nTextProperty(statusSubtitle.textProperty(), "RegistrationSubmittedSubtitle");
        }

        // Show/hide content areas
        countdownContent.setVisible(!isProcessing);
        countdownContent.setManaged(!isProcessing);
        progressContent.setVisible(isProcessing);
        progressContent.setManaged(isProcessing);

        // Show/hide action button (only visible during countdown)
        actionButtonContainer.setVisible(!isProcessing);
        actionButtonContainer.setManaged(!isProcessing);
    }

    // ===============================
    // COUNTDOWN TIMER METHODS
    // ===============================

    /**
     * Starts the countdown timer from the given number of seconds.
     */
    public void startCountdown(int seconds) {
        stopCountdown();
        remainingSeconds = Math.max(0, seconds);
        updateTimerDisplay();

        if (remainingSeconds > 0) {
            countdownTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    remainingSeconds--;
                    updateTimerDisplay();

                    if (remainingSeconds <= 0) {
                        stopCountdown();
                        if (onCountdownComplete != null) {
                            onCountdownComplete.run();
                        }
                    }
                })
            );
            countdownTimer.setCycleCount(Timeline.INDEFINITE);
            countdownTimer.play();
        } else {
            // Timer already at zero - immediately call callback
            if (onCountdownComplete != null) {
                onCountdownComplete.run();
            }
        }
    }

    /**
     * Stops the countdown timer.
     */
    public void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }

    /**
     * Updates the timer display labels.
     */
    private void updateTimerDisplay() {
        int hours = remainingSeconds / 3600;
        int minutes = (remainingSeconds % 3600) / 60;
        int seconds = remainingSeconds % 60;

        if (hoursLabel != null) {
            hoursLabel.setText(padZero(hours));
        }
        if (minutesLabel != null) {
            minutesLabel.setText(padZero(minutes));
        }
        if (secondsLabel != null) {
            secondsLabel.setText(padZero(seconds));
        }
    }

    /**
     * Pads a number with a leading zero if less than 10.
     * GWT-compatible alternative to String.format("%02d", n).
     */
    private static String padZero(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    // ===============================
    // PROGRESS BAR METHODS
    // ===============================

    /**
     * Updates the progress display.
     *
     * @param processed Number of processed requests
     * @param total Total number of requests in queue
     */
    public void updateProgress(int processed, int total) {
        int percent = total > 0 ? (processed * 100) / total : 0;

        if (progressPercentLabel != null) {
            progressPercentLabel.setText(percent + "%");
        }

        if (progressBarFill != null && progressBarFill.getParent() instanceof StackPane parent) {
            // Calculate width as percentage of parent
            double parentWidth = parent.getWidth();
            if (parentWidth > 0) {
                progressBarFill.setMaxWidth(parentWidth * percent / 100.0);
            }
        }

        if (positionLabel != null) {
            positionLabel.setText(String.valueOf(processed));
        }

        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(total));
        }
    }

    /**
     * Starts the status message rotation timer.
     */
    public void startStatusMessageRotation() {
        stopStatusMessageRotation();
        currentStatusIndex = 0;
        updateStatusMessage();

        statusMessageTimer = new Timeline(
            new KeyFrame(Duration.seconds(3), event -> {
                currentStatusIndex = (currentStatusIndex + 1) % STATUS_MESSAGE_KEYS.size();
                updateStatusMessage();
            })
        );
        statusMessageTimer.setCycleCount(Timeline.INDEFINITE);
        statusMessageTimer.play();
    }

    /**
     * Stops the status message rotation timer.
     */
    public void stopStatusMessageRotation() {
        if (statusMessageTimer != null) {
            statusMessageTimer.stop();
            statusMessageTimer = null;
        }
    }

    /**
     * Updates the status message label with the current message.
     */
    private void updateStatusMessage() {
        if (statusMessageLabel != null && !STATUS_MESSAGE_KEYS.isEmpty()) {
            Object key = STATUS_MESSAGE_KEYS.get(currentStatusIndex);
            statusMessageLabel.setText(I18n.getI18nText(key));
        }
    }

    // ===============================
    // SETTERS
    // ===============================

    public void setOnCountdownComplete(Runnable callback) {
        this.onCountdownComplete = callback;
    }

    public void setOnLeaveQueueAndEdit(Runnable callback) {
        this.onLeaveQueueAndEdit = callback;
    }

    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    public void setWorkingBookingProperties(WorkingBookingProperties props) {
        this.workingBookingProperties = props;
        // Populate booking summary using UnifiedPriceDisplay (same as DefaultSummarySection)
        if (props != null && summaryContainer != null) {
            summaryContainer.getChildren().clear();

            WorkingBooking workingBooking = props.getWorkingBooking();
            if (workingBooking != null) {
                this.event = workingBooking.getEvent();

                // Initialize unified price display with event for currency detection (same as DefaultSummarySection)
                this.unifiedPriceDisplay = new UnifiedPriceDisplay(event);

                // Get price calculator for accurate pricing (same approach as OrderDetails)
                PriceCalculator priceCalculator = workingBooking.getLatestBookingPriceCalculator();

                // Get attendances to extract dates per item (user requested using getAttendancesAdded)
                java.util.List<Attendance> attendancesAdded = workingBooking.getAttendancesAdded(true);

                // Group attendances by item to get dates per item
                Map<Item, java.util.List<LocalDate>> datesPerItem = attendancesAdded.stream()
                    .collect(Collectors.groupingBy(
                        Attendances::getItem,
                        LinkedHashMap::new,
                        Collectors.mapping(Attendances::getDate, Collectors.toList())));

                // Get document lines and display using UnifiedPriceDisplay (same pattern as DefaultSummarySection)
                java.util.List<DocumentLine> documentLines = workingBooking.getLastestDocumentAggregate().getDocumentLines();

                // Track total price for the total row
                int totalPrice = 0;

                // Group by item family and create line items
                Map<one.modality.base.shared.entities.ItemFamily, java.util.List<DocumentLine>> groupedByFamily = documentLines.stream()
                    .collect(Collectors.groupingBy(
                        dl -> dl.getItem().getFamily(),
                        LinkedHashMap::new,
                        Collectors.toList()));

                for (Map.Entry<one.modality.base.shared.entities.ItemFamily, java.util.List<DocumentLine>> entry : groupedByFamily.entrySet()) {
                    one.modality.base.shared.entities.ItemFamily itemFamily = entry.getKey();
                    java.util.List<DocumentLine> linesInFamily = entry.getValue();

                    // Family header (like DefaultSummarySection section headers)
                    Label familyLabel = new Label(itemFamily.getName());
                    familyLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-muted");
                    VBox.setMargin(familyLabel, new Insets(12, 0, 4, 0));
                    summaryContainer.getChildren().add(familyLabel);

                    // Each line item using UnifiedPriceDisplay.createLineItem() (same as DefaultSummarySection.createPriceLineRow)
                    for (DocumentLine dl : linesInFamily) {
                        Item item = dl.getItem();
                        String itemName = item.getName();

                        // Get dates from attendances for this item
                        java.util.List<LocalDate> itemDates = datesPerItem.get(item);
                        String dates = null;
                        if (itemDates != null && !itemDates.isEmpty()) {
                            // Sort dates and format as interval
                            java.util.List<LocalDate> sortedDates = itemDates.stream().sorted().collect(Collectors.toList());
                            LocalDate minDate = sortedDates.get(0);
                            LocalDate maxDate = sortedDates.get(sortedDates.size() - 1);
                            dates = ModalityDates.formatDateInterval(minDate, maxDate);
                        }

                        // Use price calculator if available (same approach as OrderDetails)
                        int price;
                        if (priceCalculator != null) {
                            price = priceCalculator.calculateDocumentLinesPrice(Stream.of(dl));
                        } else {
                            price = dl.getPriceNet() != null ? dl.getPriceNet() : 0;
                        }

                        // Accumulate total price
                        totalPrice += price;

                        // Use UnifiedPriceDisplay.createLineItem() - same method as DefaultSummarySection
                        HBox lineItem = unifiedPriceDisplay.createLineItem(null, itemName, dates, price);
                        summaryContainer.getChildren().add(lineItem);
                    }
                }

                // Divider before total (same as DefaultSummarySection)
                Region divider = new Region();
                divider.setMinHeight(2);
                divider.setMaxHeight(2);
                divider.getStyleClass().add("bookingpage-bg-gray");
                VBox.setMargin(divider, new Insets(12, 0, 0, 0));
                summaryContainer.getChildren().add(divider);

                // Total row (same as DefaultSummarySection)
                HBox totalRow = new HBox();
                totalRow.setAlignment(Pos.CENTER_LEFT);
                totalRow.setPadding(new Insets(16, 0, 0, 0));

                Label totalTextLabel = I18nControls.newLabel(BookingPageI18nKeys.TotalCost);
                totalTextLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label totalAmountLabel = new Label(unifiedPriceDisplay.formatPrice(totalPrice));
                totalAmountLabel.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-font-bold", "bookingpage-text-primary");

                totalRow.getChildren().addAll(totalTextLabel, spacer, totalAmountLabel);
                summaryContainer.getChildren().add(totalRow);
            }
        }
    }

    // ===============================
    // BOOKINGFORMSECTION INTERFACE
    // ===============================

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.ProcessingRegistration;
    }
}
