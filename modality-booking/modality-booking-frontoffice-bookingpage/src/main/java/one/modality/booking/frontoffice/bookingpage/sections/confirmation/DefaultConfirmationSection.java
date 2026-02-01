package one.modality.booking.frontoffice.bookingpage.sections.confirmation;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Event;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.components.price.UnifiedPriceDisplay;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;
import static one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder.*;

/**
 * Default implementation of the Confirmation section.
 * Design based on JSX mockup Step8Confirmation.
 *
 * @author Bruno Salmon
 */
public class DefaultConfirmationSection implements HasConfirmationSection {

    // === PROPERTIES ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);

    // === BOOKING DATA ===
    protected final ObservableList<ConfirmedBooking> confirmedBookings = FXCollections.observableArrayList();
    protected final StringProperty eventNameProperty = new SimpleStringProperty("");
    protected final ObjectProperty<LocalDate> eventStartDateProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDate> eventEndDateProperty = new SimpleObjectProperty<>();
    protected int totalAmount = 0;
    protected int previouslyPaidAmount = 0; // Amount paid before this transaction
    protected int paidAmount = 0; // Amount paid in this transaction
    protected boolean isPaymentOnly = false; // True for PAY_BOOKING entry point

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox bookingReferencesContent;

    protected Runnable onMakeAnotherBooking;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;
    protected Event event;
    protected UnifiedPriceDisplay unifiedPriceDisplay;

    public DefaultConfirmationSection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-confirmation-section");

        // Success header
        VBox successHeader = buildSuccessHeader();

        // Booking References section
        VBox bookingReferencesSection = buildBookingReferencesSection();

        // Details section
        VBox detailsSection = buildDetailsSection();

        // Payment Summary section
        VBox paymentSummarySection = buildPaymentSummarySection();

        // Note: Action buttons are managed via composite API (ButtonNavigation)
        // not created inside this section

        container.getChildren().addAll(
                successHeader, bookingReferencesSection, detailsSection,
                paymentSummarySection
        );

        // What's Next section - only show for new bookings, not for PAY_BOOKING (payment-only)
        if (!isPaymentOnly) {
            VBox whatsNextSection = buildWhatsNextSection();
            container.getChildren().add(whatsNextSection);
        }
    }

    protected void setupBindings() {
        // Note: Color scheme listener removed - CSS handles theme changes via CSS variables
    }

    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
        rebuildBookingReferences();
    }

    protected VBox buildSuccessHeader() {
        VBox header = new VBox(0);
        header.setAlignment(Pos.CENTER);
        VBox.setMargin(header, new Insets(0, 0, 40, 0));

        // Checkmark circle - uses CSS for themed background and border
        StackPane checkCircle = BookingPageUIBuilder.createThemedIconCircle(80);
        checkCircle.getStyleClass().add(bookingpage_confirmation_check_circle);
        SVGPath checkmark = createThemedIcon("M20 6L9 17l-5-5", 1.2);
        checkmark.setStrokeWidth(3);
        checkCircle.getChildren().add(checkmark);
        VBox.setMargin(checkCircle, new Insets(0, 0, 24, 0));

        // Title - dynamic based on payment status
        Label titleLabel;
        if (paidAmount <= 0) {
            // No payment - show "Booking Submitted"
            titleLabel = I18nControls.newLabel(BookingPageI18nKeys.BookingSubmitted);
        } else {
            // Payment made - show "Payment Confirmed"
            titleLabel = I18nControls.newLabel(BookingPageI18nKeys.PaymentConfirmed);
        }
        titleLabel.getStyleClass().addAll(bookingpage_text_3xl, bookingpage_font_bold, bookingpage_text_primary);
        VBox.setMargin(titleLabel, new Insets(0, 0, 12, 0));

        // Subtitle - dynamic based on payment status and entry point
        Label subtitleLabel = new Label();
        if (paidAmount <= 0) {
            // No payment - show generic booking submitted message
            I18nControls.bindI18nProperties(subtitleLabel, BookingPageI18nKeys.BookingSubmittedMessage);
        } else if (isPaymentOnly) {
            // PAY_BOOKING entry point - simplified message without email receipt mention
            String formattedAmount = unifiedPriceDisplay != null
                    ? unifiedPriceDisplay.formatPrice(paidAmount)
                    : one.modality.base.shared.entities.formatters.EventPriceFormatter.formatWithCurrency(paidAmount, event);
            I18nControls.bindI18nProperties(subtitleLabel, BookingPageI18nKeys.PaymentOnlyConfirmedMessage, formattedAmount);
        } else {
            // New booking with payment - show full message with amount and email
            String email = confirmedBookings.isEmpty() ? "" : confirmedBookings.get(0).getEmail();
            String formattedAmount = unifiedPriceDisplay != null
                    ? unifiedPriceDisplay.formatPrice(paidAmount)
                    : one.modality.base.shared.entities.formatters.EventPriceFormatter.formatWithCurrency(paidAmount, event);
            I18nControls.bindI18nProperties(subtitleLabel, BookingPageI18nKeys.PaymentConfirmedMessage, formattedAmount, email);
        }
        subtitleLabel.getStyleClass().addAll(bookingpage_text_md, bookingpage_text_muted);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setMaxWidth(600);

        header.getChildren().addAll(checkCircle, titleLabel, subtitleLabel);
        return header;
    }

    protected VBox buildBookingReferencesSection() {
        VBox section = new VBox(0);

        // Section header
        Object headerKey = confirmedBookings.size() > 1
            ? BookingPageI18nKeys.BookingReferences
            : BookingPageI18nKeys.BookingReference;
        HBox header = new StyledSectionHeader(headerKey, StyledSectionHeader.ICON_TICKET);

        // Content
        bookingReferencesContent = new VBox(0);
        bookingReferencesContent.setPadding(new Insets(16));
        bookingReferencesContent.getStyleClass().add(bookingpage_card_static);

        section.getChildren().addAll(header, bookingReferencesContent);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));
        VBox.setMargin(section, new Insets(0, 0, 24, 0));

        rebuildBookingReferences();

        return section;
    }

    protected void rebuildBookingReferences() {
        if (bookingReferencesContent == null) return;

        bookingReferencesContent.getChildren().clear();

        for (int i = 0; i < confirmedBookings.size(); i++) {
            ConfirmedBooking booking = confirmedBookings.get(i);
            HBox row = createBookingReferenceRow(booking, i < confirmedBookings.size() - 1);
            bookingReferencesContent.getChildren().add(row);
        }
    }

    protected HBox createBookingReferenceRow(ConfirmedBooking booking, boolean showBorder) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        if (showBorder) {
            row.setPadding(new Insets(0, 0, 12, 0));
            row.getStyleClass().add(bookingpage_divider_thin_bottom);
            VBox.setMargin(row, new Insets(0, 0, 12, 0));
        }

        VBox nameBox = new VBox(0);
        Label nameLabel = new Label(booking.getName());
        nameLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_font_semibold, bookingpage_text_dark);

        Label emailLabel = new Label(booking.getEmail());
        emailLabel.getStyleClass().addAll(bookingpage_text_xs, bookingpage_text_muted);

        nameBox.getChildren().addAll(nameLabel, emailLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        Label refLabel = new Label(booking.getReference());
        refLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_font_semibold, bookingpage_text_dark, bookingpage_font_mono);

        row.getChildren().addAll(nameBox, refLabel);
        return row;
    }

    protected VBox buildDetailsSection() {
        VBox section = new VBox(0);

        // Section header
        HBox header = new StyledSectionHeader(BookingPageI18nKeys.Details, StyledSectionHeader.ICON_CALENDAR);

        // Content
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.getStyleClass().add(bookingpage_card_static);

        // Event
        VBox eventBox = new VBox(4);
        Label eventTitleLabel = I18nControls.newLabel(BookingPageI18nKeys.Event);
        eventTitleLabel.getStyleClass().addAll(bookingpage_text_xs, bookingpage_text_muted);

        Label eventValueLabel = new Label();
        eventValueLabel.textProperty().bind(eventNameProperty);
        eventValueLabel.getStyleClass().addAll(bookingpage_text_md, bookingpage_font_semibold, bookingpage_text_dark);

        eventBox.getChildren().addAll(eventTitleLabel, eventValueLabel);

        // Grid for Dates and Attendees
        HBox gridRow = new HBox(12);

        // Dates
        VBox datesBox = new VBox(4);
        Label datesTitleLabel = I18nControls.newLabel(BookingPageI18nKeys.Dates);
        datesTitleLabel.getStyleClass().addAll(bookingpage_text_xs, bookingpage_text_muted);

        Label datesValueLabel = new Label(formatDateRange());
        datesValueLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_font_semibold, bookingpage_text_dark);

        datesBox.getChildren().addAll(datesTitleLabel, datesValueLabel);
        HBox.setHgrow(datesBox, Priority.ALWAYS);

        // Attendees
        VBox attendeesBox = new VBox(4);
        Label attendeesTitleLabel = I18nControls.newLabel(BookingPageI18nKeys.Attendees);
        attendeesTitleLabel.getStyleClass().addAll(bookingpage_text_xs, bookingpage_text_muted);

        int count = confirmedBookings.size();
        Object countKey = count == 1 ? BookingPageI18nKeys.PersonCount : BookingPageI18nKeys.PeopleCount;
        Label attendeesValueLabel = new Label(I18n.getI18nText(countKey, count));
        attendeesValueLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_font_semibold, bookingpage_text_dark);

        attendeesBox.getChildren().addAll(attendeesTitleLabel, attendeesValueLabel);
        HBox.setHgrow(attendeesBox, Priority.ALWAYS);

        gridRow.getChildren().addAll(datesBox, attendeesBox);

        content.getChildren().addAll(eventBox, gridRow);

        section.getChildren().addAll(header, content);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));
        VBox.setMargin(section, new Insets(0, 0, 24, 0));

        return section;
    }

    protected String formatDateRange() {
        LocalDate start = eventStartDateProperty.get();
        LocalDate end = eventEndDateProperty.get();

        if (start == null || end == null) {
            return "";
        }

        // Use UnifiedPriceDisplay for consistent date formatting via ModalityDates
        if (unifiedPriceDisplay != null) {
            return unifiedPriceDisplay.formatDateRange(start, end);
        }
        // Fallback
        return one.modality.base.client.time.ModalityDates.formatDateInterval(start, end);
    }

    protected VBox buildPaymentSummarySection() {
        VBox section = new VBox(0);

        // Section header
        HBox header = new StyledSectionHeader(BookingPageI18nKeys.PaymentSummary, StyledSectionHeader.ICON_CREDIT_CARD);

        // Content
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.getStyleClass().add(bookingpage_card_static);

        // Total Amount row
        String formattedTotal = unifiedPriceDisplay != null
                ? unifiedPriceDisplay.formatPrice(totalAmount)
                : one.modality.base.shared.entities.formatters.EventPriceFormatter.formatWithCurrency(totalAmount, event);
        HBox totalRow = createPaymentRow(BookingPageI18nKeys.TotalAmount, formattedTotal, false);

        content.getChildren().add(totalRow);

        // Already Paid row (only show if there were previous payments)
        if (previouslyPaidAmount > 0) {
            String formattedPreviouslyPaid = unifiedPriceDisplay != null
                    ? unifiedPriceDisplay.formatPrice(previouslyPaidAmount)
                    : one.modality.base.shared.entities.formatters.EventPriceFormatter.formatWithCurrency(previouslyPaidAmount, event);
            HBox alreadyPaidRow = createPaymentRow(BookingPageI18nKeys.AlreadyPaid, formattedPreviouslyPaid, false);
            content.getChildren().add(alreadyPaidRow);
        }

        // Paid Today row
        String formattedPaid = unifiedPriceDisplay != null
                ? unifiedPriceDisplay.formatPrice(paidAmount)
                : one.modality.base.shared.entities.formatters.EventPriceFormatter.formatWithCurrency(paidAmount, event);
        HBox paidRow = createPaymentRow(BookingPageI18nKeys.PaidToday, formattedPaid, false);

        content.getChildren().add(paidRow);

        int balanceDue = totalAmount - previouslyPaidAmount - paidAmount;
        if (balanceDue > 0) {
            // Divider
            Region divider = new Region();
            divider.setMinHeight(1);
            divider.setMaxHeight(1);
            divider.getStyleClass().add(bookingpage_bg_light);

            // Balance row
            String formattedBalance = unifiedPriceDisplay != null
                    ? unifiedPriceDisplay.formatPrice(balanceDue)
                    : one.modality.base.shared.entities.formatters.EventPriceFormatter.formatWithCurrency(balanceDue, event);
            HBox balanceRow = createPaymentRow(BookingPageI18nKeys.BalanceRemaining, formattedBalance, true);

            // Info note
            VBox infoNote = new VBox();
            infoNote.setPadding(new Insets(12, 16, 12, 16));
            infoNote.getStyleClass().addAll(bookingpage_bg_light, bookingpage_rounded);
            VBox.setMargin(infoNote, new Insets(12, 0, 0, 0));

            String formattedBalanceNote = unifiedPriceDisplay != null
                    ? unifiedPriceDisplay.formatPrice(balanceDue)
                    : one.modality.base.shared.entities.formatters.EventPriceFormatter.formatWithCurrency(balanceDue, event);
            Label noteLabel = new Label();
            I18nControls.bindI18nProperties(noteLabel, BookingPageI18nKeys.BalanceRemainingNote, formattedBalanceNote);
            noteLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
            noteLabel.setWrapText(true);

            infoNote.getChildren().add(noteLabel);
            content.getChildren().addAll(divider, balanceRow, infoNote);
        }

        section.getChildren().addAll(header, content);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));
        VBox.setMargin(section, new Insets(0, 0, 24, 0));

        return section;
    }

    protected HBox createPaymentRow(Object labelKey, String value, boolean isTotal) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = I18nControls.newLabel(labelKey);
        if (isTotal) {
            labelNode.getStyleClass().addAll(bookingpage_text_lg, bookingpage_font_semibold, bookingpage_text_secondary);
        } else {
            labelNode.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_secondary);
        }

        // Add spacer to push value to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueNode = new Label(value);
        if (isTotal) {
            valueNode.getStyleClass().addAll(bookingpage_text_lg, bookingpage_font_bold, bookingpage_text_dark);
        } else {
            valueNode.getStyleClass().addAll(bookingpage_text_sm, bookingpage_font_semibold, bookingpage_text_dark);
        }

        row.getChildren().addAll(labelNode, spacer, valueNode);
        return row;
    }

    protected VBox buildWhatsNextSection() {
        VBox section = new VBox(0);

        // Section header
        HBox header = new StyledSectionHeader(BookingPageI18nKeys.WhatsNext, StyledSectionHeader.ICON_CHECKLIST);

        // Content
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.getStyleClass().add(bookingpage_card_static);

        // Check Your Email
        String email = confirmedBookings.isEmpty() ? "" : confirmedBookings.get(0).getEmail();
        HBox emailStep = createWhatsNextStep(
                createThemedIcon(ICON_ENVELOPE, 0.6),
                BookingPageI18nKeys.CheckYourEmailTitle,
                BookingPageI18nKeys.CheckYourEmailDesc,
                email
        );

        // Confirmation Letter
        HBox confirmStep = createWhatsNextStep(
                createThemedIcon(ICON_CHECK, 0.6),
                BookingPageI18nKeys.ConfirmationLetterTitle,
                BookingPageI18nKeys.ConfirmationLetterDesc,
                null
        );

        content.getChildren().addAll(emailStep, confirmStep);

        // Balance step (if applicable)
        int balanceDue = totalAmount - paidAmount;
        if (balanceDue > 0) {
            String formattedBalanceStep = unifiedPriceDisplay != null
                    ? unifiedPriceDisplay.formatPrice(balanceDue)
                    : one.modality.base.shared.entities.formatters.EventPriceFormatter.formatWithCurrency(balanceDue, event);
            HBox balanceStep = createWhatsNextStep(
                    createThemedIcon(ICON_CREDIT_CARD, 0.6),
                    BookingPageI18nKeys.PayBalanceTitle,
                    BookingPageI18nKeys.PayBalanceDesc,
                    formattedBalanceStep
            );
            content.getChildren().add(balanceStep);
        }

        section.getChildren().addAll(header, content);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));
        VBox.setMargin(section, new Insets(0, 0, 40, 0));

        return section;
    }

    protected HBox createWhatsNextStep(Node icon, Object titleKey, Object descKey, String param) {
        HBox step = new HBox(12);
        step.setAlignment(Pos.TOP_LEFT);

        // Icon circle - uses CSS for themed background
        StackPane iconCircle = BookingPageUIBuilder.createThemedIconCircle(36);
        iconCircle.getChildren().add(icon);

        // Text content
        VBox textBox = new VBox(4);

        Label titleLabel = I18nControls.newLabel(titleKey);
        titleLabel.getStyleClass().addAll(bookingpage_text_md, bookingpage_font_semibold, bookingpage_text_dark);

        Label descLabel = new Label();
        if (param != null) {
            I18nControls.bindI18nProperties(descLabel, descKey, param);
        } else {
            I18nControls.bindI18nProperties(descLabel, descKey);
        }
        descLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_muted);
        descLabel.setWrapText(true);

        textBox.getChildren().addAll(titleLabel, descLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        step.getChildren().addAll(iconCircle, textBox);
        return step;
    }

    // === BookingFormSection INTERFACE ===

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.Confirmation;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties props) {
        this.workingBookingProperties = props;
        if (props != null) {
            this.event = props.getEvent();
            this.unifiedPriceDisplay = new UnifiedPriceDisplay(event);
        }
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // === HasConfirmationSection INTERFACE ===

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     * Use theme classes like "theme-wisdom-blue" on a parent element instead.
     * This property is kept for dynamic element coloring which requires Java.
     */
    @Deprecated
    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    /**
     * @deprecated Use CSS theme classes instead.
     */
    @Deprecated
    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    /**
     * Sets the event for currency formatting.
     * This should be called before setPaymentAmounts() to ensure correct currency display.
     *
     * @param event the event entity (used to determine currency symbol)
     */
    public void setEvent(Event event) {
        this.event = event;
        this.unifiedPriceDisplay = new UnifiedPriceDisplay(event);
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
    public void setPaymentAmounts(int total, int previouslyPaid, int paidToday) {
        this.totalAmount = total;
        this.previouslyPaidAmount = previouslyPaid;
        this.paidAmount = paidToday;
        rebuildUI();
    }

    @Override
    public void addConfirmedBooking(ConfirmedBooking booking) {
        confirmedBookings.add(booking);
        rebuildUI();
    }

    @Override
    public void clearConfirmedBookings() {
        confirmedBookings.clear();
        rebuildUI();
    }

    @Override
    public void setOnMakeAnotherBooking(Runnable callback) {
        this.onMakeAnotherBooking = callback;
    }

    @Override
    public void setPaymentOnly(boolean paymentOnly) {
        this.isPaymentOnly = paymentOnly;
        // Note: rebuildUI() will be called later when setPaymentAmounts() is invoked
    }
}
