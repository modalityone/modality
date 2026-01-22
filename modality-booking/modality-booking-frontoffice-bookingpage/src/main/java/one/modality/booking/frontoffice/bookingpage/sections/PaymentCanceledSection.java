package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.windowhistory.WindowHistory;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.function.Supplier;

/**
 * Section displayed when user cancels an embedded payment.
 * Based on JSX mockup EventRegistrationFlow.jsx Step8PaymentCanceled (lines 9053-).
 *
 * Features:
 * - Amber warning header with back arrow icon
 * - "Payment Canceled" title with subtitle explaining booking is saved
 * - "Try Again" primary button
 *
 * @author Claude
 */
public class PaymentCanceledSection implements BookingFormSection {

    // === SVG ICON PATHS ===
    private static final String ICON_ARROW_LEFT = "M19 12H5m7-7l-7 7 7 7";
    private static final String ICON_RETRY = "M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15";

    // === PROPERTIES ===
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    private final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);
    private final IntegerProperty amountProperty = new SimpleIntegerProperty(0);

    // === CALLBACKS ===
    private Supplier<Future<Void>> onRetryPayment;

    // === UI COMPONENTS ===
    private final VBox container = new VBox();

    public PaymentCanceledSection() {
        buildUI();
    }

    private void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.setPadding(new Insets(0, 24, 32, 24));
        container.getStyleClass().add("bookingpage-payment-canceled-section");

        // 1. Amber warning header
        VBox header = buildHeader();

        // 2. Try Again button
        VBox tryAgainSection = buildTryAgainButton();

        container.getChildren().addAll(header, tryAgainSection);
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

        // Back arrow icon (indicating user went back/canceled)
        SVGPath icon = new SVGPath();
        icon.setContent(ICON_ARROW_LEFT);
        icon.setStroke(Color.web("#F59E0B")); // Amber
        icon.setStrokeWidth(3);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(1.4);
        icon.setScaleY(1.4);
        iconCircle.getChildren().add(icon);
        VBox.setMargin(iconCircle, new Insets(0, 0, 24, 0));

        // Title: "Payment Canceled"
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.PaymentCanceledTitle);
        titleLabel.getStyleClass().add("bookingpage-warning-title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 8, 0));

        // Subtitle with Orders link: "Your booking is saved. Complete your payment anytime from the {0} menu."
        // Use TextFlow for proper text wrapping across multiple lines
        String subtitleTemplate = I18n.getI18nText(BookingPageI18nKeys.PaymentCanceledSubtitle, "###LINK###").toString();
        String[] parts = subtitleTemplate.split("###LINK###");

        Text subtitlePart1 = new Text(parts.length > 0 ? parts[0] : "");
        subtitlePart1.getStyleClass().add("bookingpage-warning-subtitle-text");

        Hyperlink ordersLink = I18nControls.newHyperlink(BookingPageI18nKeys.Orders);
        ordersLink.getStyleClass().addAll("bookingpage-warning-subtitle", "bookingpage-orders-link");
        ordersLink.setCursor(Cursor.HAND);
        ordersLink.setOnAction(e -> WindowHistory.getProvider().push("/orders"));

        Text subtitlePart2 = new Text(parts.length > 1 ? parts[1] : "");
        subtitlePart2.getStyleClass().add("bookingpage-warning-subtitle-text");

        TextFlow subtitleFlow = new TextFlow(subtitlePart1, ordersLink, subtitlePart2);
        subtitleFlow.setTextAlignment(TextAlignment.CENTER);
        subtitleFlow.setMaxWidth(500);

        header.getChildren().addAll(iconCircle, titleLabel, subtitleFlow);
        return header;
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

    public void setOnRetryPayment(Supplier<Future<Void>> callback) {
        this.onRetryPayment = callback;
    }
}
