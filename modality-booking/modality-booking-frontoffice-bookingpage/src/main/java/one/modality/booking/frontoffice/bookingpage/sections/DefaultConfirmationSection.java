package one.modality.booking.frontoffice.bookingpage.sections;

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
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.PriceFormatter;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
    protected int paidAmount = 0;

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox bookingReferencesContent;

    protected Runnable onMakeAnotherBooking;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

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

        // What's Next section
        VBox whatsNextSection = buildWhatsNextSection();

        // Note: Action buttons are managed via composite API (ButtonNavigation)
        // not created inside this section

        container.getChildren().addAll(
                successHeader, bookingReferencesSection, detailsSection,
                paymentSummarySection, whatsNextSection
        );
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
        checkCircle.getStyleClass().add("bookingpage-confirmation-check-circle");
        SVGPath checkmark = createThemedIcon("M20 6L9 17l-5-5", 1.2);
        checkmark.setStrokeWidth(3);
        checkCircle.getChildren().add(checkmark);
        VBox.setMargin(checkCircle, new Insets(0, 0, 24, 0));

        // Title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.PaymentConfirmed);
        titleLabel.getStyleClass().addAll("bookingpage-text-3xl", "bookingpage-font-bold", "bookingpage-text-primary");
        VBox.setMargin(titleLabel, new Insets(0, 0, 12, 0));

        // Subtitle
        String email = confirmedBookings.isEmpty() ? "" : confirmedBookings.get(0).getEmail();
        String formattedAmount = PriceFormatter.formatPriceWithCurrencyNoDecimals(paidAmount);
        Label subtitleLabel = new Label();
        I18nControls.bindI18nProperties(subtitleLabel, BookingPageI18nKeys.PaymentConfirmedMessage, formattedAmount, email);
        subtitleLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-text-muted");
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
        bookingReferencesContent.getStyleClass().add("bookingpage-card");

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
            row.getStyleClass().add("bookingpage-divider-thin-bottom");
            VBox.setMargin(row, new Insets(0, 0, 12, 0));
        }

        VBox nameBox = new VBox(0);
        Label nameLabel = new Label(booking.getName());
        nameLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label emailLabel = new Label(booking.getEmail());
        emailLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        nameBox.getChildren().addAll(nameLabel, emailLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        Label refLabel = new Label(booking.getReference());
        refLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark", "bookingpage-font-mono");

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
        content.getStyleClass().add("bookingpage-card");

        // Event
        VBox eventBox = new VBox(4);
        Label eventTitleLabel = I18nControls.newLabel(BookingPageI18nKeys.Event);
        eventTitleLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        Label eventValueLabel = new Label();
        eventValueLabel.textProperty().bind(eventNameProperty);
        eventValueLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        eventBox.getChildren().addAll(eventTitleLabel, eventValueLabel);

        // Grid for Dates and Attendees
        HBox gridRow = new HBox(12);

        // Dates
        VBox datesBox = new VBox(4);
        Label datesTitleLabel = I18nControls.newLabel(BookingPageI18nKeys.Dates);
        datesTitleLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        Label datesValueLabel = new Label(formatDateRange());
        datesValueLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        datesBox.getChildren().addAll(datesTitleLabel, datesValueLabel);
        HBox.setHgrow(datesBox, Priority.ALWAYS);

        // Attendees
        VBox attendeesBox = new VBox(4);
        Label attendeesTitleLabel = I18nControls.newLabel(BookingPageI18nKeys.Attendees);
        attendeesTitleLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        int count = confirmedBookings.size();
        Object personKey = count == 1 ? BookingPageI18nKeys.Person : BookingPageI18nKeys.People;
        String personText = I18n.getI18nText(personKey);
        Label attendeesValueLabel = new Label(count + " " + personText);
        attendeesValueLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

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

        DateTimeFormatter dayMonthFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

        return start.format(dayMonthFormatter) + " - " + end.format(fullFormatter);
    }

    protected VBox buildPaymentSummarySection() {
        VBox section = new VBox(0);

        // Section header
        HBox header = new StyledSectionHeader(BookingPageI18nKeys.PaymentSummary, StyledSectionHeader.ICON_CREDIT_CARD);

        // Content
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("bookingpage-card");

        // Total Amount row
        String formattedTotal = PriceFormatter.formatPriceWithCurrencyNoDecimals(totalAmount);
        HBox totalRow = createPaymentRow(BookingPageI18nKeys.TotalAmount, formattedTotal, false);

        // Paid Today row
        String formattedPaid = PriceFormatter.formatPriceWithCurrencyNoDecimals(paidAmount);
        HBox paidRow = createPaymentRow(BookingPageI18nKeys.PaidToday, formattedPaid, false);

        content.getChildren().addAll(totalRow, paidRow);

        int balanceDue = totalAmount - paidAmount;
        if (balanceDue > 0) {
            // Divider
            Region divider = new Region();
            divider.setMinHeight(1);
            divider.setMaxHeight(1);
            divider.getStyleClass().add("bookingpage-bg-light");

            // Balance row
            String formattedBalance = PriceFormatter.formatPriceWithCurrencyNoDecimals(balanceDue);
            HBox balanceRow = createPaymentRow(BookingPageI18nKeys.BalanceRemaining, formattedBalance, true);

            // Info note
            VBox infoNote = new VBox();
            infoNote.setPadding(new Insets(12, 16, 12, 16));
            infoNote.getStyleClass().addAll("bookingpage-bg-light", "bookingpage-rounded");
            VBox.setMargin(infoNote, new Insets(12, 0, 0, 0));

            String formattedBalanceNote = PriceFormatter.formatPriceWithCurrencyNoDecimals(balanceDue);
            Label noteLabel = new Label();
            I18nControls.bindI18nProperties(noteLabel, BookingPageI18nKeys.BalanceRemainingNote, formattedBalanceNote);
            noteLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-secondary");
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
            labelNode.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-secondary");
        } else {
            labelNode.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-secondary");
        }

        // Add spacer to push value to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueNode = new Label(value);
        if (isTotal) {
            valueNode.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");
        } else {
            valueNode.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-dark");
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
        content.getStyleClass().add("bookingpage-card");

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
            String formattedBalanceStep = PriceFormatter.formatPriceWithCurrencyNoDecimals(balanceDue);
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
        titleLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label descLabel = new Label();
        if (param != null) {
            I18nControls.bindI18nProperties(descLabel, descKey, param);
        } else {
            I18nControls.bindI18nProperties(descLabel, descKey);
        }
        descLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
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
    public void setPaymentAmounts(int total, int paid) {
        this.totalAmount = total;
        this.paidAmount = paid;
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
}
