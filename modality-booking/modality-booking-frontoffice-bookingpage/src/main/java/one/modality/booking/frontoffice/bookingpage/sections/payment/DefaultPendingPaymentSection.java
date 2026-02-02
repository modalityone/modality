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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;

import static one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder.*;

/**
 * Default implementation of the Pending Payment section.
 * Design based on JSX mockup PaymentPending.jsx.
 *
 * Features calm blue styling for reassurance - "we've got this, you're all set".
 * Shows payment verification status, booking details, what happens next,
 * and provides navigation options.
 *
 * @author Claude
 */
public class DefaultPendingPaymentSection implements HasPendingPaymentSection {

    // === SVG ICON PATHS ===
    private static final String ICON_CLOCK = "M12 2a10 10 0 100 20 10 10 0 000-20z M12 6v6l4 2";
    private static final String ICON_USER = "M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2 M12 3a4 4 0 100 8 4 4 0 000-8z";
    private static final String ICON_CHECK_SQUARE = "M9 11l3 3L22 4 M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11";
    private static final String ICON_FILE = "M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z M14 2v6h6 M16 13H8 M16 17H8 M10 9H8";
    private static final String ICON_CALENDAR = "M3 4h18a2 2 0 012 2v14a2 2 0 01-2 2H3a2 2 0 01-2-2V6a2 2 0 012-2z M16 2v4 M8 2v4 M3 10h18";
    private static final String ICON_SEARCH = "M11 3a8 8 0 100 16 8 8 0 000-16z M21 21l-4.35-4.35";
    private static final String ICON_MAIL = "M4 4h16a2 2 0 012 2v12a2 2 0 01-2 2H4a2 2 0 01-2-2V6a2 2 0 012-2z M22 6l-10 7L2 6";
    private static final String ICON_QUESTION = "M12 2a10 10 0 100 20 10 10 0 000-20z M9.09 9a3 3 0 015.83 1c0 2-3 3-3 3 M12 17h.01";
    private static final String ICON_ARROW_RIGHT = "M5 12h14 M12 5l7 7-7 7";

    // === PROPERTIES ===
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    private final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);

    // === BOOKING DATA ===
    private final StringProperty bookingReferenceProperty = new SimpleStringProperty("");
    private final StringProperty eventNameProperty = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> eventStartDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> eventEndDateProperty = new SimpleObjectProperty<>();
    private final StringProperty guestNameProperty = new SimpleStringProperty("");
    private final StringProperty guestEmailProperty = new SimpleStringProperty("");
    private final IntegerProperty totalAmountProperty = new SimpleIntegerProperty(0);
    private final ObjectProperty<PendingPaymentType> pendingTypeProperty = new SimpleObjectProperty<>(PendingPaymentType.BANK_TRANSFER);
    private final StringProperty estimatedTimeProperty = new SimpleStringProperty("");
    private final BooleanProperty hasAccountProperty = new SimpleBooleanProperty(false);
    private final StringProperty contactEmailProperty = new SimpleStringProperty("info@manjushri.org");

    // === CALLBACKS ===
    private Runnable onViewOrders;
    private Runnable onReturnToEvent;
    private Runnable onBrowseEvents;

    // === UI COMPONENTS ===
    private final VBox container = new VBox();

    // === DATA ===
    private WorkingBookingProperties workingBookingProperties;

    public DefaultPendingPaymentSection() {
        buildUI();
    }

    private void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-pending-payment-section");

        // Calm blue header
        VBox header = buildHeader();

        // Booking reference box
        VBox referenceBox = buildBookingReferenceBox();

        // Booking summary
        VBox summarySection = buildBookingSummary();

        // Payment info section
        VBox paymentInfoSection = buildPaymentInfoSection();

        // What happens next
        VBox nextStepsSection = buildNextStepsSection();

        // Account info box (conditional)
        VBox accountBox = buildAccountInfoBox();

        // Action buttons
        VBox actionButtons = buildActionButtons();

        // Contact section
        VBox contactSection = buildContactSection();

        // Footer
        HBox footer = buildFooter();

        container.getChildren().addAll(
                header, referenceBox, summarySection, paymentInfoSection,
                nextStepsSection, accountBox, actionButtons, contactSection, footer
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
        header.getStyleClass().add("bookingpage-pending-payment-header");

        // Clock icon circle with pulse effect (via CSS)
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(72, 72);
        iconCircle.setMaxSize(72, 72);
        iconCircle.getStyleClass().add("bookingpage-pending-payment-icon-circle");

        SVGPath icon = new SVGPath();
        icon.setContent(ICON_CLOCK);
        icon.setStroke(Color.web("#3B82F6"));
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);
        iconCircle.getChildren().add(icon);
        VBox.setMargin(iconCircle, new Insets(0, 0, 8, 0));

        // Title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.PaymentBeingVerified);
        titleLabel.getStyleClass().add("bookingpage-pending-payment-title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 4, 0));

        // Subtitle
        Label subtitleLabel = I18nControls.newLabel(BookingPageI18nKeys.PaymentReceivedProcessing);
        subtitleLabel.getStyleClass().add("bookingpage-pending-payment-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setMaxWidth(500);

        header.getChildren().addAll(iconCircle, titleLabel, subtitleLabel);
        return header;
    }

    private VBox buildBookingReferenceBox() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 24, 20, 24));
        box.getStyleClass().add("bookingpage-pending-payment-ref-box");
        VBox.setMargin(box, new Insets(0, 24, 24, 24));

        // Label
        Label refLabel = I18nControls.newLabel(BookingPageI18nKeys.BookingReference);
        refLabel.getStyleClass().add("bookingpage-pending-payment-ref-label");

        // Value
        Label refValue = new Label();
        refValue.textProperty().bind(bookingReferenceProperty);
        refValue.getStyleClass().add("bookingpage-pending-payment-ref-value");

        // Status badge
        HBox statusBadge = new HBox(6);
        statusBadge.setAlignment(Pos.CENTER);
        statusBadge.getStyleClass().add("bookingpage-pending-payment-status-badge");
        statusBadge.setPadding(new Insets(6, 14, 6, 14));

        SVGPath clockIcon = new SVGPath();
        clockIcon.setContent(ICON_CLOCK);
        clockIcon.setStroke(Color.WHITE);
        clockIcon.setStrokeWidth(2);
        clockIcon.setFill(Color.TRANSPARENT);
        clockIcon.setScaleX(0.5);
        clockIcon.setScaleY(0.5);

        Label badgeLabel = I18nControls.newLabel(BookingPageI18nKeys.AwaitingConfirmation);
        badgeLabel.getStyleClass().add("bookingpage-pending-payment-badge-text");

        statusBadge.getChildren().addAll(clockIcon, badgeLabel);
        VBox.setMargin(statusBadge, new Insets(8, 0, 0, 0));

        box.getChildren().addAll(refLabel, refValue, statusBadge);
        return box;
    }

    private VBox buildBookingSummary() {
        VBox section = new VBox(0);
        section.setPadding(new Insets(0, 24, 24, 24));

        // Event row
        HBox eventRow = createSummaryRow(BookingPageI18nKeys.Event, eventNameProperty);

        // Date row
        HBox dateRow = createSummaryRow(BookingPageI18nKeys.Dates, createDateBinding());

        // Guest row
        HBox guestRow = createSummaryRow(BookingPageI18nKeys.Guest, guestNameProperty);

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

    private HBox createSummaryRow(Object labelKey, StringProperty valueProperty) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 0, 12, 0));
        row.getStyleClass().add("bookingpage-pending-payment-summary-row");

        // Label
        Label label = I18nControls.newLabel(labelKey);
        label.getStyleClass().add("bookingpage-pending-payment-summary-label");
        HBox.setHgrow(label, Priority.ALWAYS);

        // Value
        Label value = new Label();
        value.textProperty().bind(valueProperty);
        value.getStyleClass().add("bookingpage-pending-payment-summary-value");

        row.getChildren().addAll(label, value);
        return row;
    }

    private HBox createAmountRow() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 0, 12, 0));
        row.getStyleClass().add("bookingpage-pending-payment-summary-row-last");

        // Label
        Label label = I18nControls.newLabel(BookingPageI18nKeys.TotalAmount);
        label.getStyleClass().add("bookingpage-pending-payment-summary-label");
        HBox.setHgrow(label, Priority.ALWAYS);

        // Value
        Label value = new Label();
        value.getStyleClass().add("bookingpage-pending-payment-summary-total");

        // Update value when amount changes
        Runnable updateAmount = () -> {
            int amount = totalAmountProperty.get();
            Event event = workingBookingProperties != null ? workingBookingProperties.getEvent() : null;
            String formatted = EventPriceFormatter.formatWithCurrency(amount, event);
            value.setText(formatted);
        };
        totalAmountProperty.addListener((obs, old, val) -> updateAmount.run());
        updateAmount.run();

        row.getChildren().addAll(label, value);
        return row;
    }

    private VBox buildPaymentInfoSection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(20));
        section.getStyleClass().add("bookingpage-pending-payment-info-section");
        VBox.setMargin(section, new Insets(0, 24, 24, 24));

        // Header with icon
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Payment method icon
        Label iconLabel = new Label(getPaymentMethodIcon());
        iconLabel.getStyleClass().add("bookingpage-pending-payment-info-icon");

        VBox titleBox = new VBox(2);
        Label titleLabel = I18nControls.newLabel(getPaymentMethodTitleKey());
        titleLabel.getStyleClass().add("bookingpage-pending-payment-info-title");

        Label subtitleLabel = I18nControls.newLabel(BookingPageI18nKeys.PaymentReceived);
        subtitleLabel.getStyleClass().add("bookingpage-pending-payment-info-subtitle");

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        headerRow.getChildren().addAll(iconLabel, titleBox);

        // Description
        Label descLabel = I18nControls.newLabel(getPaymentMethodDescKey());
        descLabel.getStyleClass().add("bookingpage-pending-payment-info-description");
        descLabel.setWrapText(true);
        VBox.setMargin(descLabel, new Insets(8, 0, 12, 0));

        // Timeline indicator
        HBox timeline = new HBox(8);
        timeline.setAlignment(Pos.CENTER_LEFT);
        timeline.setPadding(new Insets(12));
        timeline.getStyleClass().add("bookingpage-pending-payment-timeline");

        SVGPath timeIcon = new SVGPath();
        timeIcon.setContent(ICON_CLOCK);
        timeIcon.setStroke(Color.web("#0284C7"));
        timeIcon.setStrokeWidth(2);
        timeIcon.setFill(Color.TRANSPARENT);
        timeIcon.setScaleX(0.6);
        timeIcon.setScaleY(0.6);

        Label timeLabel = I18nControls.newLabel(getPaymentMethodTimelineKey());
        timeLabel.getStyleClass().add("bookingpage-pending-payment-timeline-text");

        timeline.getChildren().addAll(timeIcon, timeLabel);

        section.getChildren().addAll(headerRow, descLabel, timeline);
        return section;
    }

    private String getPaymentMethodIcon() {
        PendingPaymentType type = pendingTypeProperty.get();
        return switch (type) {
            case PENDING_AUTHORIZATION -> "\uD83D\uDCB3"; // Credit card
            case CHECK_PAYMENT -> "\uD83D\uDCDD"; // Memo
            case INVOICE -> "\uD83D\uDCC4"; // Page
            default -> "\uD83C\uDFE6"; // Bank
        };
    }

    private Object getPaymentMethodTitleKey() {
        PendingPaymentType type = pendingTypeProperty.get();
        return switch (type) {
            case PENDING_AUTHORIZATION -> BookingPageI18nKeys.PaymentAuthorizing;
            case CHECK_PAYMENT -> BookingPageI18nKeys.CheckPayment;
            case INVOICE -> BookingPageI18nKeys.InvoicePayment;
            default -> BookingPageI18nKeys.BankTransfer;
        };
    }

    private Object getPaymentMethodDescKey() {
        PendingPaymentType type = pendingTypeProperty.get();
        return switch (type) {
            case PENDING_AUTHORIZATION -> BookingPageI18nKeys.PendingAuthorizationDesc;
            case CHECK_PAYMENT -> BookingPageI18nKeys.CheckPaymentDesc;
            case INVOICE -> BookingPageI18nKeys.InvoicePaymentDesc;
            default -> BookingPageI18nKeys.BankTransferDesc;
        };
    }

    private Object getPaymentMethodTimelineKey() {
        PendingPaymentType type = pendingTypeProperty.get();
        return switch (type) {
            case PENDING_AUTHORIZATION -> BookingPageI18nKeys.PendingAuthorizationTimeline;
            case CHECK_PAYMENT -> BookingPageI18nKeys.CheckPaymentTimeline;
            case INVOICE -> BookingPageI18nKeys.InvoicePaymentTimeline;
            default -> BookingPageI18nKeys.BankTransferTimeline;
        };
    }

    private VBox buildNextStepsSection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(0, 24, 24, 24));

        // Title
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        SVGPath checkIcon = new SVGPath();
        checkIcon.setContent(ICON_CHECK_SQUARE);
        checkIcon.setStroke(colorScheme.get().getPrimary());
        checkIcon.setStrokeWidth(2);
        checkIcon.setFill(Color.TRANSPARENT);
        checkIcon.setScaleX(0.6);
        checkIcon.setScaleY(0.6);

        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.WhatHappensNext);
        titleLabel.getStyleClass().add("bookingpage-pending-payment-next-title");

        titleRow.getChildren().addAll(checkIcon, titleLabel);

        // Steps list
        VBox stepsList = new VBox(8);

        // Step 1
        HBox step1 = createStepItem("1", BookingPageI18nKeys.WeVerifyPayment, BookingPageI18nKeys.WeVerifyPaymentDesc);
        // Step 2
        HBox step2 = createStepItem("2", BookingPageI18nKeys.YouReceiveConfirmation, BookingPageI18nKeys.YouReceiveConfirmationDesc);
        // Step 3
        HBox step3 = createStepItem("3", BookingPageI18nKeys.YouAreAllSet, BookingPageI18nKeys.YouAreAllSetDesc);

        stepsList.getChildren().addAll(step1, step2, step3);

        section.getChildren().addAll(titleRow, stepsList);
        return section;
    }

    private HBox createStepItem(String number, Object titleKey, Object descKey) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.TOP_LEFT);
        item.setPadding(new Insets(12));
        item.getStyleClass().add("bookingpage-pending-payment-step-item");

        // Number circle
        StackPane numberCircle = new StackPane();
        numberCircle.setMinSize(24, 24);
        numberCircle.setMaxSize(24, 24);
        numberCircle.getStyleClass().add("bookingpage-pending-payment-step-number");

        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("bookingpage-pending-payment-step-number-text");
        numberCircle.getChildren().add(numberLabel);

        // Content
        VBox content = new VBox(4);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label titleLabel = I18nControls.newLabel(titleKey);
        titleLabel.getStyleClass().add("bookingpage-pending-payment-step-title");

        Label descLabel = I18nControls.newLabel(descKey);
        descLabel.getStyleClass().add("bookingpage-pending-payment-step-desc");
        descLabel.setWrapText(true);

        content.getChildren().addAll(titleLabel, descLabel);

        item.getChildren().addAll(numberCircle, content);
        return item;
    }

    private VBox buildAccountInfoBox() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.getStyleClass().add("bookingpage-pending-payment-account-box");
        VBox.setMargin(box, new Insets(0, 24, 24, 24));

        // Only show if user has account
        box.visibleProperty().bind(hasAccountProperty);
        box.managedProperty().bind(hasAccountProperty);

        // Title
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        SVGPath userIcon = new SVGPath();
        userIcon.setContent(ICON_USER);
        userIcon.setStroke(colorScheme.get().getPrimary());
        userIcon.setStrokeWidth(2);
        userIcon.setFill(Color.TRANSPARENT);
        userIcon.setScaleX(0.6);
        userIcon.setScaleY(0.6);

        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.TrackYourBooking);
        titleLabel.getStyleClass().add("bookingpage-pending-payment-account-title");

        titleRow.getChildren().addAll(userIcon, titleLabel);

        // Message
        Label messageLabel = I18nControls.newLabel(BookingPageI18nKeys.TrackBookingMessage);
        messageLabel.getStyleClass().add("bookingpage-pending-payment-account-text");
        messageLabel.setWrapText(true);

        // Go to My Orders link
        HBox linkRow = new HBox(6);
        linkRow.setAlignment(Pos.CENTER_LEFT);
        linkRow.setCursor(Cursor.HAND);
        linkRow.setOnMouseClicked(e -> {
            if (onViewOrders != null) {
                onViewOrders.run();
            }
        });

        Label linkLabel = I18nControls.newLabel(BookingPageI18nKeys.GoToMyOrders);
        linkLabel.getStyleClass().add("bookingpage-pending-payment-account-link");

        SVGPath arrowIcon = new SVGPath();
        arrowIcon.setContent(ICON_ARROW_RIGHT);
        arrowIcon.setStroke(colorScheme.get().getPrimary());
        arrowIcon.setStrokeWidth(2);
        arrowIcon.setFill(Color.TRANSPARENT);
        arrowIcon.setScaleX(0.5);
        arrowIcon.setScaleY(0.5);

        linkRow.getChildren().addAll(linkLabel, arrowIcon);
        VBox.setMargin(linkRow, new Insets(4, 0, 0, 0));

        box.getChildren().addAll(titleRow, messageLabel, linkRow);
        return box;
    }

    private VBox buildActionButtons() {
        VBox buttons = new VBox(12);
        buttons.setPadding(new Insets(0, 24, 24, 24));

        // Primary button - View My Orders (if has account) or Return to Event
        Button primaryButton = hasAccountProperty.get()
            ? BookingPageUIBuilder.createPrimaryButton(BookingPageI18nKeys.ViewMyOrders)
            : BookingPageUIBuilder.createPrimaryButton(BookingPageI18nKeys.ReturnToEventPage);

        primaryButton.setMaxWidth(Double.MAX_VALUE);
        primaryButton.setOnAction(e -> {
            if (hasAccountProperty.get() && onViewOrders != null) {
                onViewOrders.run();
            } else if (onReturnToEvent != null) {
                onReturnToEvent.run();
            }
        });

        // Update primary button when hasAccount changes
        hasAccountProperty.addListener((obs, old, hasAcc) -> {
            I18nControls.bindI18nProperties(primaryButton,
                hasAcc ? BookingPageI18nKeys.ViewMyOrders : BookingPageI18nKeys.ReturnToEventPage);
        });

        // Secondary button - Browse More Events
        Button secondaryButton = I18nControls.newButton(BookingPageI18nKeys.BrowseMoreEvents);
        secondaryButton.getStyleClass().add("bookingpage-btn-secondary-outline");
        secondaryButton.setPadding(new Insets(14, 24, 14, 24));
        secondaryButton.setMaxWidth(Double.MAX_VALUE);
        secondaryButton.setCursor(Cursor.HAND);
        secondaryButton.setOnAction(e -> {
            if (onBrowseEvents != null) {
                onBrowseEvents.run();
            }
        });

        buttons.getChildren().addAll(primaryButton, secondaryButton);
        return buttons;
    }

    private VBox buildContactSection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(16));
        section.getStyleClass().add("bookingpage-pending-payment-contact-section");
        VBox.setMargin(section, new Insets(0, 24, 24, 24));

        // Title
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        SVGPath questionIcon = new SVGPath();
        questionIcon.setContent(ICON_QUESTION);
        questionIcon.setStroke(Color.web("#6B7280"));
        questionIcon.setStrokeWidth(2);
        questionIcon.setFill(Color.TRANSPARENT);
        questionIcon.setScaleX(0.6);
        questionIcon.setScaleY(0.6);

        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.HaveQuestions);
        titleLabel.getStyleClass().add("bookingpage-pending-payment-contact-title");

        titleRow.getChildren().addAll(questionIcon, titleLabel);

        // Message
        Label messageLabel = I18nControls.newLabel(BookingPageI18nKeys.ContactTeamMessage);
        messageLabel.getStyleClass().add("bookingpage-pending-payment-contact-text");
        messageLabel.setWrapText(true);

        // Email row
        HBox emailRow = new HBox(8);
        emailRow.setAlignment(Pos.CENTER_LEFT);
        emailRow.setPadding(new Insets(12));
        emailRow.getStyleClass().add("bookingpage-pending-payment-contact-email-box");

        SVGPath mailIcon = new SVGPath();
        mailIcon.setContent(ICON_MAIL);
        mailIcon.setStroke(Color.web("#9CA3AF"));
        mailIcon.setStrokeWidth(2);
        mailIcon.setFill(Color.TRANSPARENT);
        mailIcon.setScaleX(0.6);
        mailIcon.setScaleY(0.6);

        Label emailLabel = new Label();
        emailLabel.textProperty().bind(contactEmailProperty);
        emailLabel.getStyleClass().add("bookingpage-pending-payment-contact-email");
        HBox.setHgrow(emailLabel, Priority.ALWAYS);

        emailRow.getChildren().addAll(mailIcon, emailLabel);

        section.getChildren().addAll(titleRow, messageLabel, emailRow);
        return section;
    }

    private HBox buildFooter() {
        HBox footer = new HBox(4);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(0, 24, 32, 24));

        Label helpLabel = I18nControls.newLabel(BookingPageI18nKeys.NeedHelpContact);
        helpLabel.getStyleClass().add("bookingpage-contact-footer");

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

    // === HasPendingPaymentSection INTERFACE ===

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
    public void setGuestEmail(String email) {
        guestEmailProperty.set(email);
    }

    @Override
    public void setTotalAmount(int amount) {
        totalAmountProperty.set(amount);
    }

    @Override
    public void setPendingPaymentDetails(PendingPaymentType type, String estimatedTime) {
        pendingTypeProperty.set(type);
        estimatedTimeProperty.set(estimatedTime != null ? estimatedTime : "");
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
    public void setOnViewOrders(Runnable callback) {
        this.onViewOrders = callback;
    }

    @Override
    public void setOnReturnToEvent(Runnable callback) {
        this.onReturnToEvent = callback;
    }

    @Override
    public void setOnBrowseEvents(Runnable callback) {
        this.onBrowseEvents = callback;
    }
}
