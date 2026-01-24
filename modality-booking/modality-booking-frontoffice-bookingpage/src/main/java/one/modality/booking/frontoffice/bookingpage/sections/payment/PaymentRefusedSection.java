package one.modality.booking.frontoffice.bookingpage.sections.payment;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.PriceFormatter;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.payment.PaymentFailureReason;

import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Section displayed when an embedded payment is refused/declined.
 * Based on JSX mockup EventRegistrationFlow.jsx Step8PaymentRefused (lines 8856-9050).
 *
 * Features:
 * - Amber warning header with credit card X icon
 * - "Payment Declined" title and subtitle
 * - "Decline Reason" amber card showing the specific failure reason
 * - "Try Again" primary button
 * - "Pay Later" info box
 *
 * @author Claude
 */
public class PaymentRefusedSection implements BookingFormSection {

    // === SVG ICON PATHS ===
    private static final String ICON_CREDIT_CARD_X = "M2 5h20a2 2 0 012 2v10a2 2 0 01-2 2H2a2 2 0 01-2-2V7a2 2 0 012-2z M2 10h20 M15 14l-6 0";
    private static final String ICON_WARNING = "M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z";
    private static final String ICON_RETRY = "M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15";
    private static final String ICON_CLOCK = "M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z";

    // === PROPERTIES ===
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    private final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);
    private final IntegerProperty amountProperty = new SimpleIntegerProperty(0);
    private final ObjectProperty<PaymentFailureReason> failureReasonProperty = new SimpleObjectProperty<>(PaymentFailureReason.UNKNOWN_REASON);
    private final StringProperty eventNameProperty = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> eventStartDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> eventEndDateProperty = new SimpleObjectProperty<>();

    // === CALLBACKS ===
    private Supplier<Future<Void>> onRetryPayment;
    private Runnable onCancelBooking;

    // === UI COMPONENTS ===
    private final VBox container = new VBox();
    private Label failureReasonLabel;

    public PaymentRefusedSection() {
        buildUI();
    }

    private void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.setPadding(new Insets(0, 24, 32, 24));
        container.getStyleClass().add("bookingpage-payment-refused-section");

        // 1. Amber warning header
        VBox header = buildHeader();

        // 2. Decline Reason amber card
        VBox declineReasonCard = buildDeclineReasonCard();

        // 3. Try Again button
        VBox tryAgainSection = buildTryAgainButton();

        // 4. Pay Later info box
        VBox payLaterBox = buildPayLaterInfoBox();

        container.getChildren().addAll(header, declineReasonCard, tryAgainSection, payLaterBox);
    }

    private VBox buildHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 32, 0));

        // Amber icon circle (80px with 4px border)
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(80, 80);
        iconCircle.setMaxSize(80, 80);
        iconCircle.getStyleClass().add("bookingpage-warning-icon-circle");

        // Credit card with X icon
        SVGPath icon = new SVGPath();
        icon.setContent(ICON_CREDIT_CARD_X);
        icon.setStroke(Color.web("#F59E0B")); // Amber
        icon.setStrokeWidth(2.5);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(1.4);
        icon.setScaleY(1.4);
        iconCircle.getChildren().add(icon);
        VBox.setMargin(iconCircle, new Insets(0, 0, 24, 0));

        // Title: "Payment Declined"
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.PaymentDeclinedTitle);
        titleLabel.getStyleClass().add("bookingpage-warning-title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 8, 0));

        // Subtitle
        Label subtitleLabel = I18nControls.newLabel(BookingPageI18nKeys.PaymentDeclinedSubtitle);
        subtitleLabel.getStyleClass().add("bookingpage-warning-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setMaxWidth(500);

        header.getChildren().addAll(iconCircle, titleLabel, subtitleLabel);
        return header;
    }

    private VBox buildDeclineReasonCard() {
        // Amber card with 4px left border accent
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("bookingpage-decline-reason-card");
        VBox.setMargin(card, new Insets(0, 0, 24, 0));

        HBox content = new HBox(12);
        content.setAlignment(Pos.TOP_LEFT);

        // Warning icon in circle
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(32, 32);
        iconCircle.setMaxSize(32, 32);
        iconCircle.getStyleClass().add("bookingpage-warning-icon-circle-sm");

        SVGPath warningIcon = new SVGPath();
        warningIcon.setContent(ICON_WARNING);
        warningIcon.setStroke(Color.web("#D97706"));
        warningIcon.setStrokeWidth(2);
        warningIcon.setFill(Color.TRANSPARENT);
        warningIcon.setScaleX(0.6);
        warningIcon.setScaleY(0.6);
        iconCircle.getChildren().add(warningIcon);

        // Text content
        VBox textContent = new VBox(4);
        textContent.setAlignment(Pos.TOP_LEFT);

        // "DECLINE REASON" header
        Label headerLabel = I18nControls.newLabel(BookingPageI18nKeys.DeclineReason);
        headerLabel.getStyleClass().add("bookingpage-decline-reason-header");

        // Failure reason message (dynamically updated)
        failureReasonLabel = new Label();
        failureReasonLabel.getStyleClass().add("bookingpage-decline-reason-message");
        failureReasonLabel.setWrapText(true);
        updateFailureReasonLabel();

        // Listen for failure reason changes
        failureReasonProperty.addListener((obs, old, newVal) -> updateFailureReasonLabel());

        textContent.getChildren().addAll(headerLabel, failureReasonLabel);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        content.getChildren().addAll(iconCircle, textContent);
        card.getChildren().add(content);

        return card;
    }

    private void updateFailureReasonLabel() {
        PaymentFailureReason reason = failureReasonProperty.get();
        if (reason == null) reason = PaymentFailureReason.UNKNOWN_REASON;

        // Get i18n key for the failure reason
        Object i18nKey = getI18nKeyForFailureReason(reason);
        I18nControls.bindI18nProperties(failureReasonLabel, i18nKey);
    }

    private Object getI18nKeyForFailureReason(PaymentFailureReason reason) {
        return switch (reason) {
            case INVALID_CARD_NUMBER -> BookingPageI18nKeys.PaymentFailureInvalidCardNumber;
            case EXPIRED_CARD -> BookingPageI18nKeys.PaymentFailureExpiredCard;
            case INVALID_CVV -> BookingPageI18nKeys.PaymentFailureInvalidCvv;
            case INVALID_EXPIRY_DATE -> BookingPageI18nKeys.PaymentFailureInvalidExpiryDate;
            case INSUFFICIENT_FUNDS -> BookingPageI18nKeys.PaymentFailureInsufficientFunds;
            case DECLINED_BY_BANK -> BookingPageI18nKeys.PaymentFailureDeclinedByBank;
            case GATEWAY_ERROR -> BookingPageI18nKeys.PaymentFailureGatewayError;
            default -> BookingPageI18nKeys.PaymentFailureUnknownReason;
        };
    }

    private VBox buildTryAgainButton() {
        VBox section = new VBox();
        section.setAlignment(Pos.CENTER);
        VBox.setMargin(section, new Insets(0, 0, 24, 0));

        Button tryAgainButton = I18nControls.newButton(BookingPageI18nKeys.TryAgain);
        tryAgainButton.getStyleClass().addAll("booking-form-primary-btn", "booking-form-primary-btn-text");
        tryAgainButton.setPadding(new Insets(16, 24, 16, 24));
        tryAgainButton.setCursor(Cursor.HAND);
        tryAgainButton.setMaxWidth(Double.MAX_VALUE);

        // Add retry icon
        SVGPath retryIcon = new SVGPath();
        retryIcon.setContent(ICON_RETRY);
        retryIcon.setStroke(Color.WHITE);
        retryIcon.setStrokeWidth(2);
        retryIcon.setFill(Color.TRANSPARENT);
        retryIcon.setScaleX(0.7);
        retryIcon.setScaleY(0.7);

        HBox buttonContent = new HBox(10);
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.getChildren().addAll(retryIcon, tryAgainButton.getGraphic() != null ? tryAgainButton.getGraphic() : new Label());

        tryAgainButton.setGraphic(retryIcon);
        tryAgainButton.setOnAction(e -> {
            if (onRetryPayment != null) {
                AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    onRetryPayment.get(),
                    tryAgainButton
                );
            }
        });

        section.getChildren().add(tryAgainButton);
        return section;
    }

    private VBox buildPayLaterInfoBox() {
        VBox box = new VBox();
        box.setPadding(new Insets(20));
        box.getStyleClass().add("bookingpage-info-box-neutral-rounded");

        HBox content = new HBox(12);
        content.setAlignment(Pos.TOP_LEFT);

        // Clock icon in themed circle
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(36, 36);
        iconCircle.setMaxSize(36, 36);

        // Bind background color to color scheme
        colorScheme.addListener((obs, old, scheme) -> {
            if (scheme != null) {
                iconCircle.setStyle("-fx-background-color: " + toHexString(scheme.getSelectedBg()) + "; -fx-background-radius: 18;");
            }
        });
        BookingFormColorScheme scheme = colorScheme.get();
        if (scheme != null) {
            iconCircle.setStyle("-fx-background-color: " + toHexString(scheme.getSelectedBg()) + "; -fx-background-radius: 18;");
        }

        SVGPath clockIcon = new SVGPath();
        clockIcon.setContent(ICON_CLOCK);
        clockIcon.setStrokeWidth(2);
        clockIcon.setFill(Color.TRANSPARENT);
        clockIcon.setScaleX(0.7);
        clockIcon.setScaleY(0.7);

        // Bind stroke color to color scheme
        colorScheme.addListener((obs, old, newScheme) -> {
            if (newScheme != null) {
                clockIcon.setStroke(newScheme.getPrimary());
            }
        });
        if (scheme != null) {
            clockIcon.setStroke(scheme.getPrimary());
        }

        iconCircle.getChildren().add(clockIcon);

        // Text content
        VBox textContent = new VBox(6);

        // Title: "Pay Later"
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.PayLater);
        titleLabel.getStyleClass().add("bookingpage-paylater-title");

        // Description with Orders link using HtmlText for WebFX compatibility
        HtmlText descHtml = new HtmlText();
        descHtml.getStyleClass().add("bookingpage-paylater-desc-text");
        FXProperties.runNowAndOnPropertiesChange(() -> {
            String template = I18n.getI18nText(BookingPageI18nKeys.PayLaterDescription, "###LINK###").toString();
            String linkText = I18n.getI18nText(BookingPageI18nKeys.Orders);
            String html = template.replace("###LINK###",
                "<a href=\"/orders\" class=\"bookingpage-orders-link\">" + linkText + "</a>");
            descHtml.setText(html);
        }, I18n.dictionaryProperty());

        textContent.getChildren().addAll(titleLabel, descHtml);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        content.getChildren().addAll(iconCircle, textContent);
        box.getChildren().add(content);

        return box;
    }

    /**
     * Converts a Color to a hex string (e.g., "#FF5500").
     * GWT-compatible alternative to String.format("#%02X%02X%02X", ...).
     */
    private String toHexString(Color color) {
        return "#" + toHex((int) (color.getRed() * 255))
                   + toHex((int) (color.getGreen() * 255))
                   + toHex((int) (color.getBlue() * 255));
    }

    private static String toHex(int value) {
        String hex = Integer.toHexString(value).toUpperCase();
        return hex.length() == 1 ? "0" + hex : hex;
    }

    // === BookingFormSection INTERFACE ===

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.Payment;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties props) {
        // Not needed for this section
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // === PUBLIC SETTERS ===

    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    public void setAmount(int amount) {
        this.amountProperty.set(amount);
    }

    public void setFailureReason(PaymentFailureReason reason) {
        this.failureReasonProperty.set(reason);
    }

    public void setEventName(String name) {
        this.eventNameProperty.set(name);
    }

    public void setEventDates(LocalDate start, LocalDate end) {
        this.eventStartDateProperty.set(start);
        this.eventEndDateProperty.set(end);
    }

    public void setOnRetryPayment(Supplier<Future<Void>> callback) {
        this.onRetryPayment = callback;
    }

    public void setOnCancelBooking(Runnable callback) {
        this.onCancelBooking = callback;
    }
}
