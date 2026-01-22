package one.modality.booking.frontoffice.bookingpage.pages.countdown;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.util.Booleans;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import one.modality.base.shared.entities.Event;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormButton;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A page that displays a countdown timer before registration opens.
 *
 * <p>Shows:
 * <ul>
 *   <li>Event name and location</li>
 *   <li>Clock icon in themed circle</li>
 *   <li>"Registration Opens Soon" title</li>
 *   <li>Formatted opening date/time</li>
 *   <li>Countdown timer (days, hours, minutes, seconds)</li>
 *   <li>Optional "No Need to Rush" info box (if processing window exists)</li>
 * </ul>
 *
 * <p>When the countdown reaches zero, the {@code onCountdownComplete} callback is invoked
 * to proceed to the normal booking flow.
 *
 * @author Claude
 */
public class RegistrationCountdownPage implements BookingFormPage {

    // SVG paths for clock icon (24x24 viewBox)
    private static final String ICON_CLOCK_CIRCLE = "M12 3a9 9 0 100 18 9 9 0 000-18z";
    private static final String ICON_CLOCK_HANDS = "M12 6v6l4 4";

    private final PolicyAggregate policyAggregate;
    private final BookingFormColorScheme colorScheme;
    private final Runnable onCountdownComplete;

    private WorkingBookingProperties workingBookingProperties;
    private Timeline countdownTimeline;
    private VBox view;

    // Countdown labels
    private Label daysValueLabel;
    private Label hoursValueLabel;
    private Label minutesValueLabel;
    private Label secondsValueLabel;
    private HBox daysBox; // To hide when days = 0

    // Remaining seconds (updated by timer)
    private double remainingSeconds;
    private BookingFormButton[] buttons;

    /**
     * Creates a new countdown page.
     *
     * @param policyAggregate      The policy aggregate containing opening date info
     * @param colorScheme          The color scheme for theming
     * @param onCountdownComplete  Callback when countdown reaches zero
     */
    public RegistrationCountdownPage(PolicyAggregate policyAggregate,
                                     BookingFormColorScheme colorScheme,
                                     Runnable onCountdownComplete) {
        this.policyAggregate = policyAggregate;
        this.colorScheme = colorScheme != null ? colorScheme : BookingFormColorScheme.DEFAULT;
        this.onCountdownComplete = onCountdownComplete;

        Double seconds = policyAggregate.getSecondsToOpeningDate();
        this.remainingSeconds = seconds != null ? seconds : 0;
    }

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.RegistrationOpensSoon;
    }

    @Override
    public Node getView() {
        if (view == null) {
            view = buildView();
            startCountdown();
        }
        return view;
    }

    @Override
    public boolean isHeaderVisible() {
        return false; // Hide step navigation header
    }

    @Override
    public boolean isPriceBarRelevantToShow() {
        return false; // No price bar on countdown page
    }

    @Override
    public boolean isStep() {
        return false; // Not a regular step
    }

    @Override
    public ObservableBooleanValue canGoForwardProperty() {
        return new SimpleBooleanProperty(false); // Cannot manually proceed
    }

    @Override
    public ObservableBooleanValue canGoBackProperty() {
        return new SimpleBooleanProperty(false); // Cannot go back
    }

    @Override
    public BookingFormButton[] getButtons() {
        return buttons;
    }

    public RegistrationCountdownPage setButtons(BookingFormButton... buttons) {
        this.buttons = buttons;
        return this;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        // This page is shown via direct navigation, not automatic applicability
        return false;
    }

    private VBox buildView() {
        Event event = policyAggregate.getEvent();

        VBox container = new VBox(0);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(40, 20, 40, 20));
        container.getStyleClass().add("registration-countdown-page");

        // Event header (name + location)
        VBox eventHeader = buildEventHeader(event);
        VBox.setMargin(eventHeader, new Insets(0, 0, 32, 0));

        // Clock icon in themed circle
        StackPane clockIcon = buildClockIcon();
        VBox.setMargin(clockIcon, new Insets(0, 0, 24, 0));

        // "Registration Opens Soon" title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.RegistrationOpensSoon);
        titleLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-dark");
        VBox.setMargin(titleLabel, new Insets(0, 0, 8, 0));

        // Formatted opening date/time
        Label dateLabel = new Label(formatOpeningDate());
        dateLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");
        VBox.setMargin(dateLabel, new Insets(0, 0, 32, 0));

        // Countdown timer boxes
        HBox countdownTimer = buildCountdownTimer();
        VBox.setMargin(countdownTimer, new Insets(0, 0, 40, 0));

        container.getChildren().addAll(eventHeader, clockIcon, titleLabel, dateLabel, countdownTimer);

        // "No Need to Rush" info box (only if processing window exists)
        Double secondsToProcessStart = policyAggregate.getSecondsToBookingProcessStart();
        Double secondsToOpening = policyAggregate.getSecondsToOpeningDate();

        if (secondsToProcessStart != null && secondsToOpening != null
                && secondsToProcessStart > secondsToOpening) {
            int windowMinutes = (int) Math.round((secondsToProcessStart - secondsToOpening) / 60.0);
            VBox infoBox = buildNoNeedToRushInfoBox(windowMinutes);
            VBox.setMargin(infoBox, new Insets(0, 0, 32, 0));
            container.getChildren().add(infoBox);
        }

        return container;
    }

    private VBox buildEventHeader(Event event) {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        // Event name with Bootstrap h3 styling
        String eventName = event != null ? event.getName() : "";
        Label nameLabel = Bootstrap.h3(new Label(eventName));
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(600);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        header.getChildren().add(nameLabel);

        // Organization name (center)
        if (event != null && event.getOrganization() != null) {
            Label orgLabel = new Label(event.getOrganization().getName());
            orgLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");
            orgLabel.setWrapText(true);
            orgLabel.setMaxWidth(600);
            orgLabel.setAlignment(Pos.CENTER);
            orgLabel.setTextAlignment(TextAlignment.CENTER);
            header.getChildren().add(orgLabel);
        }

        return header;
    }

    private StackPane buildClockIcon() {
        double size = 100;
        Color primaryColor = colorScheme.getPrimary();
        Color bgColor = colorScheme.getSelectedBg();

        // Circle background
        Circle circle = new Circle(size / 2);
        circle.setFill(bgColor);
        circle.setStroke(primaryColor);
        circle.setStrokeWidth(3);

        // Clock circle path
        SVGPath clockCircle = new SVGPath();
        clockCircle.setContent(ICON_CLOCK_CIRCLE);
        clockCircle.setStroke(primaryColor);
        clockCircle.setStrokeWidth(2);
        clockCircle.setFill(Color.TRANSPARENT);
        clockCircle.setScaleX(2.0);
        clockCircle.setScaleY(2.0);

        // Clock hands path
        SVGPath clockHands = new SVGPath();
        clockHands.setContent(ICON_CLOCK_HANDS);
        clockHands.setStroke(primaryColor);
        clockHands.setStrokeWidth(2);
        clockHands.setFill(Color.TRANSPARENT);
        clockHands.setScaleX(2.0);
        clockHands.setScaleY(2.0);

        StackPane iconPane = new StackPane(clockCircle, clockHands);
        iconPane.setAlignment(Pos.CENTER);

        StackPane container = new StackPane(circle, iconPane);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);

        return container;
    }

    private HBox buildCountdownTimer() {
        HBox timer = new HBox(16);
        timer.setAlignment(Pos.CENTER);

        // Days (hidden if 0)
        daysBox = buildTimeUnit("0", BookingPageI18nKeys.Days);
        // Structure: HBox -> VBox(unitContainer) -> VBox(valueBox) -> Label
        daysValueLabel = (Label) ((VBox) ((VBox) daysBox.getChildren().get(0)).getChildren().get(0)).getChildren().get(0);

        // Hours
        HBox hoursBox = buildTimeUnit("0", BookingPageI18nKeys.Hours);
        hoursValueLabel = (Label) ((VBox) ((VBox) hoursBox.getChildren().get(0)).getChildren().get(0)).getChildren().get(0);

        // Minutes
        HBox minutesBox = buildTimeUnit("0", BookingPageI18nKeys.Minutes);
        minutesValueLabel = (Label) ((VBox) ((VBox) minutesBox.getChildren().get(0)).getChildren().get(0)).getChildren().get(0);

        // Seconds
        HBox secondsBox = buildTimeUnit("0", BookingPageI18nKeys.Seconds);
        secondsValueLabel = (Label) ((VBox) ((VBox) secondsBox.getChildren().get(0)).getChildren().get(0)).getChildren().get(0);

        timer.getChildren().addAll(daysBox, hoursBox, minutesBox, secondsBox);

        // Update initial values
        updateCountdownDisplay();

        return timer;
    }

    private HBox buildTimeUnit(String value, Object labelI18nKey) {
        // Value label - uses CSS class for themed text color
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("registration-countdown-timer-value");
        valueLabel.setMinWidth(60);
        valueLabel.setAlignment(Pos.CENTER);

        // Value box with themed background and border via CSS class
        VBox valueBox = new VBox(valueLabel);
        valueBox.setAlignment(Pos.CENTER);
        valueBox.setPadding(new Insets(16, 24, 16, 24));
        valueBox.setMinWidth(80);
        valueBox.getStyleClass().add("registration-countdown-timer-box");

        // Label below
        Label label = I18nControls.newLabel(labelI18nKey);
        label.getStyleClass().add("registration-countdown-timer-label");

        VBox unitContainer = new VBox(8);
        unitContainer.setAlignment(Pos.CENTER);
        unitContainer.getChildren().addAll(valueBox, label);

        HBox wrapper = new HBox(unitContainer);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    private VBox buildNoNeedToRushInfoBox(int windowMinutes) {
        VBox infoBox = new VBox(20);
        infoBox.setAlignment(Pos.TOP_LEFT);
        infoBox.setPadding(new Insets(28));
        infoBox.setMaxWidth(600);
        infoBox.getStyleClass().addAll("bookingpage-info-card", "registration-countdown-info-card");

        // Header with info icon
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane infoIcon = BookingPageUIBuilder.createThemedIconCircle(40);
        SVGPath infoPath = BookingPageUIBuilder.createThemedIcon(BookingPageUIBuilder.ICON_INFO_CIRCLE + " " + BookingPageUIBuilder.ICON_INFO_I, 0.83);
        infoIcon.getChildren().add(infoPath);

        Label headerLabel = I18nControls.newLabel(BookingPageI18nKeys.NoNeedToRush);
        headerLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        header.getChildren().addAll(infoIcon, headerLabel);

        // Explanation text
        Label explanation = I18nControls.newLabel(BookingPageI18nKeys.FairAccessExplanation);
        explanation.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        explanation.setWrapText(true);

        // Steps
        VBox steps = new VBox(12);
        steps.getChildren().addAll(
            buildStep(1, BookingPageI18nKeys.RegistrationWindowTitle, BookingPageI18nKeys.RegistrationWindowDesc, windowMinutes),
            buildStep(2, BookingPageI18nKeys.RandomAllocationTitle, BookingPageI18nKeys.RandomAllocationDesc, windowMinutes),
            buildStep(3, BookingPageI18nKeys.EqualChancesTitle, BookingPageI18nKeys.EqualChancesDesc, windowMinutes)
        );

        // Reassurance note
        HBox reassurance = new HBox(10);
        reassurance.setAlignment(Pos.CENTER_LEFT);
        reassurance.setPadding(new Insets(12, 16, 12, 16));
        reassurance.getStyleClass().add("registration-countdown-reassurance-box");

        SVGPath checkIcon = BookingPageUIBuilder.createThemedIcon("M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z", 0.75);
        Label reassuranceLabel = I18nControls.newLabel(BookingPageI18nKeys.TakeYourTime);
        reassuranceLabel.getStyleClass().addAll("bookingpage-text-sm", "registration-countdown-reassurance-text");
        reassuranceLabel.setWrapText(true);

        reassurance.getChildren().addAll(checkIcon, reassuranceLabel);

        infoBox.getChildren().addAll(header, explanation, steps, reassurance);
        return infoBox;
    }

    private HBox buildStep(int number, Object titleI18nKey, Object descI18nKey, int windowMinutes) {
        HBox step = new HBox(12);
        step.setAlignment(Pos.TOP_LEFT);

        // Number circle - uses CSS for themed background
        StackPane numberCircle = new StackPane();
        numberCircle.setMinSize(28, 28);
        numberCircle.setMaxSize(28, 28);
        numberCircle.getStyleClass().add("registration-countdown-step-number");
        Label numberLabel = new Label(String.valueOf(number));
        numberLabel.getStyleClass().add("registration-countdown-step-number-text");
        numberCircle.getChildren().add(numberLabel);

        // Text content with {0} placeholder replaced by windowMinutes
        VBox textContent = new VBox(4);
        String titleText = dev.webfx.extras.i18n.I18n.getI18nText(titleI18nKey, windowMinutes);
        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-dark");

        String descText = dev.webfx.extras.i18n.I18n.getI18nText(descI18nKey, windowMinutes);
        Label descLabel = new Label(descText);
        descLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        descLabel.setWrapText(true);

        textContent.getChildren().addAll(titleLabel, descLabel);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        step.getChildren().addAll(numberCircle, textContent);
        return step;
    }

    private String formatOpeningDate() {
        Double secondsToOpening = policyAggregate.getSecondsToOpeningDate();
        if (secondsToOpening == null) return "";

        // Calculate opening date from now + seconds
        Instant openingInstant = Instant.now().plusSeconds(secondsToOpening.longValue());
        ZoneId zoneId = ZoneId.systemDefault();

        // Try to get event timezone if available, fallback to system default
        Event event = policyAggregate.getEvent();
        if (event != null) {
            try {
                ZoneId eventZone = event.getEventZoneId();
                if (eventZone != null) {
                    zoneId = eventZone;
                }
            } catch (Exception ignored) {
                // Use system default
            }
        }

        LocalDateTime openingDateTime = LocalDateTime.ofInstant(openingInstant, zoneId);

        // Format: "Saturday, 15 March 2026 at 09:00"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "EEEE, d MMMM yyyy 'at' HH:mm", Locale.ENGLISH
        ).withZone(zoneId);

        return openingDateTime.atZone(zoneId).format(formatter);
    }

    private void startCountdown() {
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            if (remainingSeconds <= 0) {
                stopCountdown();
                if (onCountdownComplete != null) {
                    onCountdownComplete.run();
                }
            } else {
                updateCountdownDisplay();
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    private void updateCountdownDisplay() {
        long totalSeconds = (long) Math.max(0, remainingSeconds);

        long days = totalSeconds / (24 * 60 * 60);
        long hours = (totalSeconds % (24 * 60 * 60)) / (60 * 60);
        long minutes = (totalSeconds % (60 * 60)) / 60;
        long seconds = totalSeconds % 60;

        // Update labels with zero-padding
        if (daysValueLabel != null) daysValueLabel.setText(padZero(days));
        if (hoursValueLabel != null) hoursValueLabel.setText(padZero(hours));
        if (minutesValueLabel != null) minutesValueLabel.setText(padZero(minutes));
        if (secondsValueLabel != null) secondsValueLabel.setText(padZero(seconds));

        // Hide days box if days = 0
        if (daysBox != null) {
            daysBox.setVisible(days > 0);
            daysBox.setManaged(days > 0);
        }
    }

    /**
     * Pads a number to 2 digits with leading zero if needed.
     * GWT-compatible alternative to String.format("%02d", value).
     */
    private static String padZero(long value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }
}
