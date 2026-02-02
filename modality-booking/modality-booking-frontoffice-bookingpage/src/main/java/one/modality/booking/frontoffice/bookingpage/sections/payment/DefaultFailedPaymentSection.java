package one.modality.booking.frontoffice.bookingpage.sections.payment;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.windowlocation.WindowLocation;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;

/**
 * Default implementation of the Failed Payment section.
 * Design based on JSX mockup PaymentError.jsx.
 *
 * Features warm amber styling for attention without alarm,
 * displays error details with suggestions, and provides
 * retry/cancel actions.
 *
 * @author Claude
 */
public class DefaultFailedPaymentSection implements HasFailedPaymentSection {

    // === SVG ICON PATHS ===
    private static final String ICON_INFO_CIRCLE = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 15c-.55 0-1-.45-1-1v-4c0-.55.45-1 1-1s1 .45 1 1v4c0 .55-.45 1-1 1zm1-8h-2V7h2v2z";
    private static final String ICON_QUESTION_CIRCLE = "M12 2a10 10 0 100 20 10 10 0 000-20z M9.09 9a3 3 0 015.83 1c0 2-3 3-3 3 M12 17h.01";
    private static final String ICON_CREDIT_CARD = "M2 5h20a2 2 0 012 2v10a2 2 0 01-2 2H2a2 2 0 01-2-2V7a2 2 0 012-2z M2 10h20";
    private static final String ICON_X_CIRCLE = "M12 2a10 10 0 100 20 10 10 0 000-20z M15 9l-6 6 M9 9l6 6";
    private static final String ICON_ARROW_RIGHT = "M5 12h14 M12 5l7 7-7 7";
    private static final String ICON_CALENDAR = "M4 4h16a2 2 0 012 2v14a2 2 0 01-2 2H4a2 2 0 01-2-2V6a2 2 0 012-2z M16 2v4 M8 2v4 M2 10h20";
    private static final String ICON_USER = "M12 4a4 4 0 100 8 4 4 0 000-8z M4 20c0-4 4-6 8-6s8 2 8 6";
    private static final String ICON_TAG = "M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82zM7 7h.01";
    private static final String ICON_MAIL = "M2 4h20a2 2 0 012 2v12a2 2 0 01-2 2H2a2 2 0 01-2-2V6a2 2 0 012-2z M22 6l-10 7L2 6";

    // === PROPERTIES ===
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    private final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);

    // === BOOKING DATA ===
    private final StringProperty bookingReferenceProperty = new SimpleStringProperty("");
    private final StringProperty eventNameProperty = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> eventStartDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> eventEndDateProperty = new SimpleObjectProperty<>();
    private final StringProperty guestNameProperty = new SimpleStringProperty("");
    private final IntegerProperty amountDueProperty = new SimpleIntegerProperty(0);
    private final ObjectProperty<PaymentErrorType> errorTypeProperty = new SimpleObjectProperty<>(PaymentErrorType.CARD_DECLINED);
    private final StringProperty bankErrorMessageProperty = new SimpleStringProperty("");
    private final StringProperty errorCodeProperty = new SimpleStringProperty("");
    private final BooleanProperty hasAccountProperty = new SimpleBooleanProperty(false);
    private final StringProperty contactEmailProperty = new SimpleStringProperty("info@manjushri.org");

    // === CALLBACKS ===
    private Runnable onRetryPayment;
    private Runnable onCancelBooking;
    private Runnable onGoToOrders;

    // === UI COMPONENTS ===
    private final VBox container = new VBox();

    // === DATA ===
    private WorkingBookingProperties workingBookingProperties;

    public DefaultFailedPaymentSection() {
        buildUI();
    }

    private void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-failed-payment-section");

        // Amber header
        VBox header = buildHeader();

        // Booking reference box
        VBox referenceBox = buildBookingReferenceBox();

        // Booking summary
        VBox summarySection = buildBookingSummary();

        // Error details section
        VBox errorSection = buildErrorDetailsSection();

        // Action buttons
        HBox actionButtons = buildActionButtons();

        // Account info box (conditional)
        VBox accountBox = buildAccountInfoBox();

        // Footer with contact
        HBox footer = buildFooter();

        container.getChildren().addAll(
                header, referenceBox, summarySection, errorSection,
                actionButtons, accountBox, footer
        );
    }

    private void rebuildUI() {
        container.getChildren().clear();
        buildUI();
    }

    private VBox buildHeader() {
        VBox header = new VBox(12);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(40, 24, 32, 24));
        header.getStyleClass().add("bookingpage-failed-payment-header");

        // Amber icon circle with info icon
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(64, 64);
        iconCircle.setMaxSize(64, 64);
        iconCircle.getStyleClass().add("bookingpage-failed-payment-icon-circle");

        SVGPath icon = new SVGPath();
        icon.setContent(ICON_INFO_CIRCLE);
        icon.setStroke(Color.web("#E5A545"));
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.9);
        icon.setScaleY(0.9);
        iconCircle.getChildren().add(icon);
        VBox.setMargin(iconCircle, new Insets(0, 0, 8, 0));

        // Title - dynamic based on error type
        Label titleLabel = I18nControls.newLabel(getTitleI18nKeyForError());
        titleLabel.getStyleClass().add("bookingpage-failed-payment-title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 4, 0));

        // Subtitle
        Label subtitleLabel = I18nControls.newLabel(BookingPageI18nKeys.PaymentNotCompleted);
        subtitleLabel.getStyleClass().add("bookingpage-failed-payment-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setMaxWidth(500);

        header.getChildren().addAll(iconCircle, titleLabel, subtitleLabel);
        return header;
    }

    private Object getTitleI18nKeyForError() {
        PaymentErrorType type = errorTypeProperty.get();
        return switch (type) {
            case INSUFFICIENT_FUNDS -> BookingPageI18nKeys.InsufficientFundsTitle;
            case PROCESSING_ERROR -> BookingPageI18nKeys.ProcessingErrorTitle;
            case TIMEOUT -> BookingPageI18nKeys.TimeoutTitle;
            case EXPIRED_CARD -> BookingPageI18nKeys.ExpiredCardTitle;
            default -> BookingPageI18nKeys.CardDeclinedTitle;
        };
    }

    private VBox buildBookingReferenceBox() {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16, 24, 16, 24));
        box.getStyleClass().add("bookingpage-failed-payment-ref-box");
        VBox.setMargin(box, new Insets(0, 24, 24, 24));

        // Label
        Label refLabel = I18nControls.newLabel(BookingPageI18nKeys.BookingReference);
        refLabel.getStyleClass().add("bookingpage-failed-payment-ref-label");

        // Value
        Label refValue = new Label();
        refValue.textProperty().bind(bookingReferenceProperty);
        refValue.getStyleClass().add("bookingpage-failed-payment-ref-value");

        box.getChildren().addAll(refLabel, refValue);
        return box;
    }

    private VBox buildBookingSummary() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(0, 24, 24, 24));

        // Event row
        HBox eventRow = createInfoRow(ICON_CALENDAR, BookingPageI18nKeys.Event, eventNameProperty);

        // Date row
        HBox dateRow = createInfoRow(ICON_CALENDAR, BookingPageI18nKeys.Dates, createDateBinding());

        // Guest row
        HBox guestRow = createInfoRow(ICON_USER, BookingPageI18nKeys.Guest, guestNameProperty);

        // Amount row
        HBox amountRow = createAmountRow();

        section.getChildren().addAll(eventRow, dateRow, guestRow, amountRow);
        return section;
    }

    private StringProperty createDateBinding() {
        StringProperty dateString = new SimpleStringProperty();
        Runnable updateDate = () -> {
            LocalDate start = eventStartDateProperty.get();
            LocalDate end = eventEndDateProperty.get();
            if (start != null && end != null) {
                dateString.set(BookingPageUIBuilder.formatDateRangeFull(start, end));
            } else {
                dateString.set("");
            }
        };
        eventStartDateProperty.addListener((obs, old, val) -> updateDate.run());
        eventEndDateProperty.addListener((obs, old, val) -> updateDate.run());
        updateDate.run();
        return dateString;
    }

    private HBox createInfoRow(String iconPath, Object labelKey, StringProperty valueProperty) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        // Icon
        SVGPath icon = BookingPageUIBuilder.createMutedIcon(iconPath, 0.6);

        // Label
        Label label = I18nControls.newLabel(labelKey);
        label.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_muted);
        label.setMinWidth(80);

        // Value
        Label value = new Label();
        value.textProperty().bind(valueProperty);
        value.getStyleClass().addAll(bookingpage_text_sm, bookingpage_font_semibold, bookingpage_text_dark);

        row.getChildren().addAll(icon, label, value);
        return row;
    }

    private HBox createAmountRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        // Icon
        SVGPath icon = BookingPageUIBuilder.createMutedIcon(ICON_TAG, 0.6);

        // Label
        Label label = I18nControls.newLabel(BookingPageI18nKeys.AmountDue);
        label.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_muted);
        label.setMinWidth(80);

        // Value
        Label value = new Label();
        value.getStyleClass().addAll(bookingpage_text_sm, bookingpage_font_semibold, bookingpage_text_dark);

        // Update value when amount changes
        Runnable updateAmount = () -> {
            int amount = amountDueProperty.get();
            Event event = workingBookingProperties != null ? workingBookingProperties.getEvent() : null;
            String formatted = EventPriceFormatter.formatWithCurrency(amount, event);
            value.setText(formatted);
        };
        amountDueProperty.addListener((obs, old, val) -> updateAmount.run());
        updateAmount.run();

        row.getChildren().addAll(icon, label, value);
        return row;
    }

    private VBox buildErrorDetailsSection() {
        VBox section = new VBox(16);
        section.setPadding(new Insets(20));
        section.getStyleClass().add("bookingpage-error-details-section");
        VBox.setMargin(section, new Insets(0, 24, 24, 24));

        // Header with question icon
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        SVGPath questionIcon = new SVGPath();
        questionIcon.setContent(ICON_QUESTION_CIRCLE);
        questionIcon.setStroke(Color.web("#5D4E37"));
        questionIcon.setStrokeWidth(2);
        questionIcon.setFill(Color.TRANSPARENT);
        questionIcon.setScaleX(0.7);
        questionIcon.setScaleY(0.7);

        Label headerLabel = I18nControls.newLabel(BookingPageI18nKeys.WhatHappened);
        headerLabel.getStyleClass().add("bookingpage-error-details-title");

        headerRow.getChildren().addAll(questionIcon, headerLabel);

        // Description
        Label descLabel = I18nControls.newLabel(getDescriptionI18nKeyForError());
        descLabel.getStyleClass().add("bookingpage-error-description");
        descLabel.setWrapText(true);
        VBox.setMargin(descLabel, new Insets(8, 0, 12, 0));

        // Suggestions list
        VBox suggestionsList = buildSuggestionsList();

        // Bank error box (if there's a bank message)
        VBox bankErrorBox = buildBankErrorBox();

        section.getChildren().addAll(headerRow, descLabel, suggestionsList);
        if (!bankErrorMessageProperty.get().isEmpty()) {
            section.getChildren().add(bankErrorBox);
        }

        // Listen for bank error changes
        bankErrorMessageProperty.addListener((obs, old, newVal) -> {
            section.getChildren().remove(bankErrorBox);
            if (!newVal.isEmpty()) {
                section.getChildren().add(buildBankErrorBox());
            }
        });

        return section;
    }

    private Object getDescriptionI18nKeyForError() {
        PaymentErrorType type = errorTypeProperty.get();
        return switch (type) {
            case INSUFFICIENT_FUNDS -> BookingPageI18nKeys.InsufficientFundsDesc;
            case PROCESSING_ERROR -> BookingPageI18nKeys.ProcessingErrorDesc;
            case TIMEOUT -> BookingPageI18nKeys.TimeoutDesc;
            case EXPIRED_CARD -> BookingPageI18nKeys.ExpiredCardDesc;
            default -> BookingPageI18nKeys.CardDeclinedDesc;
        };
    }

    private VBox buildSuggestionsList() {
        VBox list = new VBox(8);
        list.setPadding(new Insets(0, 0, 0, 8));

        PaymentErrorType type = errorTypeProperty.get();

        Object[] suggestionKeys = getSuggestionKeysForError(type);
        for (Object key : suggestionKeys) {
            HBox item = createSuggestionItem(key);
            list.getChildren().add(item);
        }

        return list;
    }

    private Object[] getSuggestionKeysForError(PaymentErrorType type) {
        return switch (type) {
            case INSUFFICIENT_FUNDS -> new Object[]{
                    BookingPageI18nKeys.InsufficientFundsSuggestion1,
                    BookingPageI18nKeys.InsufficientFundsSuggestion2,
                    BookingPageI18nKeys.InsufficientFundsSuggestion3
            };
            case PROCESSING_ERROR -> new Object[]{
                    BookingPageI18nKeys.ProcessingErrorSuggestion1,
                    BookingPageI18nKeys.ProcessingErrorSuggestion2
            };
            case TIMEOUT -> new Object[]{
                    BookingPageI18nKeys.TimeoutSuggestion1,
                    BookingPageI18nKeys.TimeoutSuggestion2
            };
            case EXPIRED_CARD -> new Object[]{
                    BookingPageI18nKeys.ExpiredCardSuggestion1,
                    BookingPageI18nKeys.ExpiredCardSuggestion2
            };
            default -> new Object[]{ // CARD_DECLINED and UNKNOWN
                    BookingPageI18nKeys.CardDeclinedSuggestion1,
                    BookingPageI18nKeys.CardDeclinedSuggestion2,
                    BookingPageI18nKeys.CardDeclinedSuggestion3,
                    BookingPageI18nKeys.CardDeclinedSuggestion4
            };
        };
    }

    private HBox createSuggestionItem(Object i18nKey) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.TOP_LEFT);

        Label bullet = new Label("•");
        bullet.getStyleClass().addAll("bookingpage-suggestion-item", bookingpage_text_muted);

        Label text = I18nControls.newLabel(i18nKey);
        text.getStyleClass().add("bookingpage-suggestion-item");
        text.setWrapText(true);

        item.getChildren().addAll(bullet, text);
        return item;
    }

    private VBox buildBankErrorBox() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16, 0, 0, 0));
        box.getStyleClass().add("bookingpage-bank-error-box");
        VBox.setMargin(box, new Insets(12, 0, 0, 0));

        // Label
        Label label = I18nControls.newLabel(BookingPageI18nKeys.BankResponse);
        label.getStyleClass().add("bookingpage-bank-error-label");

        // Message box
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(8, 12, 8, 12));
        messageBox.getStyleClass().add("bookingpage-bank-error-message");

        Label messageLabel = new Label();
        messageLabel.textProperty().bind(bankErrorMessageProperty);
        messageBox.getChildren().add(messageLabel);

        box.getChildren().addAll(label, messageBox);

        // Error code if present
        if (!errorCodeProperty.get().isEmpty()) {
            Label codeLabel = new Label();
            I18nControls.bindI18nProperties(codeLabel, BookingPageI18nKeys.ErrorCode);
            codeLabel.setText(codeLabel.getText() + ": " + errorCodeProperty.get());
            codeLabel.getStyleClass().add("bookingpage-bank-error-meta");
            box.getChildren().add(codeLabel);
        }

        return box;
    }

    private HBox buildActionButtons() {
        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(0, 24, 0, 24));
        VBox.setMargin(buttons, new Insets(0, 0, 24, 0));

        // Retry Payment button (primary)
        Button retryButton = BookingPageUIBuilder.createPrimaryButton(BookingPageI18nKeys.RetryPayment);
        retryButton.setOnAction(e -> {
            if (onRetryPayment != null) {
                onRetryPayment.run();
            }
        });
        HBox.setHgrow(retryButton, Priority.ALWAYS);
        retryButton.setMaxWidth(Double.MAX_VALUE);

        // Cancel Booking button (danger outline)
        Button cancelButton = I18nControls.newButton(BookingPageI18nKeys.CancelBooking);
        cancelButton.getStyleClass().add("bookingpage-btn-danger-outline");
        cancelButton.setPadding(new Insets(14, 32, 14, 32));
        cancelButton.setCursor(Cursor.HAND);
        cancelButton.setOnAction(e -> {
            if (onCancelBooking != null) {
                onCancelBooking.run();
            }
        });
        HBox.setHgrow(cancelButton, Priority.ALWAYS);
        cancelButton.setMaxWidth(Double.MAX_VALUE);

        buttons.getChildren().addAll(retryButton, cancelButton);
        return buttons;
    }

    private VBox buildAccountInfoBox() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.getStyleClass().add("bookingpage-account-info-box");
        VBox.setMargin(box, new Insets(0, 24, 24, 24));

        // Only show if user has account
        box.visibleProperty().bind(hasAccountProperty);
        box.managedProperty().bind(hasAccountProperty);

        // Title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.YouHaveAnAccount);
        titleLabel.getStyleClass().add("bookingpage-account-info-title");

        // Message
        Label messageLabel = I18nControls.newLabel(BookingPageI18nKeys.AccountInfoMessage);
        messageLabel.getStyleClass().add("bookingpage-account-info-text");
        messageLabel.setWrapText(true);

        // Go to My Orders link
        HBox linkRow = new HBox(6);
        linkRow.setAlignment(Pos.CENTER_LEFT);
        linkRow.setCursor(Cursor.HAND);
        linkRow.setOnMouseClicked(e -> {
            if (onGoToOrders != null) {
                onGoToOrders.run();
            }
        });

        Label linkLabel = I18nControls.newLabel(BookingPageI18nKeys.GoToMyOrders);
        linkLabel.getStyleClass().add("bookingpage-account-info-link");

        SVGPath arrowIcon = new SVGPath();
        arrowIcon.setContent(ICON_ARROW_RIGHT);
        arrowIcon.setStroke(colorScheme.get().getPrimary());
        arrowIcon.setStrokeWidth(2);
        arrowIcon.setFill(Color.TRANSPARENT);
        arrowIcon.setScaleX(0.5);
        arrowIcon.setScaleY(0.5);

        linkRow.getChildren().addAll(linkLabel, arrowIcon);
        VBox.setMargin(linkRow, new Insets(4, 0, 0, 0));

        box.getChildren().addAll(titleLabel, messageLabel, linkRow);
        return box;
    }

    private HBox buildFooter() {
        HBox footer = new HBox(4);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(0, 24, 32, 24));

        // Help text
        Label helpLabel = I18nControls.newLabel(BookingPageI18nKeys.NeedHelpContact);
        helpLabel.getStyleClass().add("bookingpage-contact-footer");

        // Email link
        Hyperlink emailLink = new Hyperlink();
        emailLink.textProperty().bind(contactEmailProperty);
        emailLink.getStyleClass().add("bookingpage-contact-link");
        emailLink.setOnAction(e -> {
            String email = contactEmailProperty.get();
            if (email != null && !email.isEmpty()) {
                WindowLocation.assignHref("mailto:" + email);
            }
        });

        footer.getChildren().addAll(helpLabel, emailLink);
        return footer;
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
        this.workingBookingProperties = props;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // === HasFailedPaymentSection INTERFACE ===

    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public void setBookingReference(String reference) {
        bookingReferenceProperty.set(reference);
    }

    @Override
    public void setEventName(String name) {
        eventNameProperty.set(name);
    }

    @Override
    public void setEventDates(LocalDate start, LocalDate end) {
        eventStartDateProperty.set(start);
        eventEndDateProperty.set(end);
    }

    @Override
    public void setGuestName(String name) {
        guestNameProperty.set(name);
    }

    @Override
    public void setAmountDue(int amount) {
        amountDueProperty.set(amount);
    }

    @Override
    public void setErrorDetails(PaymentErrorType errorType, String bankErrorMessage, String errorCode) {
        errorTypeProperty.set(errorType);
        bankErrorMessageProperty.set(bankErrorMessage != null ? bankErrorMessage : "");
        errorCodeProperty.set(errorCode != null ? errorCode : "");
        rebuildUI();
    }

    @Override
    public void setHasAccount(boolean hasAccount) {
        hasAccountProperty.set(hasAccount);
    }

    @Override
    public void setContactEmail(String email) {
        contactEmailProperty.set(email);
    }

    @Override
    public void setOnRetryPayment(Runnable callback) {
        this.onRetryPayment = callback;
    }

    @Override
    public void setOnCancelBooking(Runnable callback) {
        this.onCancelBooking = callback;
    }

    @Override
    public void setOnGoToOrders(Runnable callback) {
        this.onGoToOrders = callback;
    }
}
