package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.PriceFormatter;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static one.modality.booking.frontoffice.bookingpage.theme.BookingFormStyles.*;

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

    // === CALLBACKS ===
    protected Runnable onDownloadConfirmation;
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
        // Update on color scheme change
        colorScheme.addListener((obs, old, newScheme) -> rebuildUI());
    }

    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
        rebuildBookingReferences();
    }

    protected VBox buildSuccessHeader() {
        BookingFormColorScheme colors = colorScheme.get();

        VBox header = new VBox(0);
        header.setAlignment(Pos.CENTER);
        VBox.setMargin(header, new Insets(0, 0, 40, 0));

        // Checkmark circle
        StackPane checkCircle = new StackPane();
        checkCircle.setMinSize(80, 80);
        checkCircle.setMaxSize(80, 80);
        CornerRadii circleRadii = new CornerRadii(40);
        checkCircle.setBackground(bg(colors.getSelectedBg(), circleRadii));
        checkCircle.setBorder(border(colors.getPrimary(), 4, circleRadii));

        SVGPath checkmark = new SVGPath();
        checkmark.setContent("M20 6L9 17l-5-5");
        checkmark.setStroke(colors.getPrimary());
        checkmark.setStrokeWidth(3);
        checkmark.setFill(Color.TRANSPARENT);
        checkmark.setScaleX(1.2);
        checkmark.setScaleY(1.2);
        checkCircle.getChildren().add(checkmark);
        VBox.setMargin(checkCircle, new Insets(0, 0, 24, 0));

        // Title
        Label titleLabel = new Label("Payment Confirmed");
        titleLabel.setFont(fontBold(28));
        titleLabel.setTextFill(colors.getPrimary());
        VBox.setMargin(titleLabel, new Insets(0, 0, 12, 0));

        // Subtitle
        String email = confirmedBookings.isEmpty() ? "" : confirmedBookings.get(0).getEmail();
        Label subtitleLabel = new Label("Thank you for your booking. We've received your payment of " +
                                        PriceFormatter.formatPriceWithCurrencyNoDecimals(paidAmount) + " and sent a booking receipt to " + email + ".");
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
        HBox header = buildSectionHeader("Booking Reference" +
                (confirmedBookings.size() > 1 ? "s" : ""), createTicketIcon());

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
        HBox header = buildSectionHeader("Details", createCalendarIcon());

        // Content
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("bookingpage-card");

        // Event
        VBox eventBox = new VBox(4);
        Label eventTitleLabel = new Label("Event");
        eventTitleLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        Label eventValueLabel = new Label();
        eventValueLabel.textProperty().bind(eventNameProperty);
        eventValueLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        eventBox.getChildren().addAll(eventTitleLabel, eventValueLabel);

        // Grid for Dates and Attendees
        HBox gridRow = new HBox(12);

        // Dates
        VBox datesBox = new VBox(4);
        Label datesTitleLabel = new Label("Dates");
        datesTitleLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        Label datesValueLabel = new Label(formatDateRange());
        datesValueLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        datesBox.getChildren().addAll(datesTitleLabel, datesValueLabel);
        HBox.setHgrow(datesBox, Priority.ALWAYS);

        // Attendees
        VBox attendeesBox = new VBox(4);
        Label attendeesTitleLabel = new Label("Attendees");
        attendeesTitleLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        int count = confirmedBookings.size();
        Label attendeesValueLabel = new Label(count + (count == 1 ? " person" : " people"));
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
        HBox header = buildSectionHeader("Payment Summary", createCardIcon());

        // Content
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("bookingpage-card");

        // Total Amount row
        HBox totalRow = createPaymentRow("Total Amount", PriceFormatter.formatPriceWithCurrencyNoDecimals(totalAmount), false);

        // Paid Today row
        HBox paidRow = createPaymentRow("Paid Today", PriceFormatter.formatPriceWithCurrencyNoDecimals(paidAmount), false);

        content.getChildren().addAll(totalRow, paidRow);

        int balanceDue = totalAmount - paidAmount;
        if (balanceDue > 0) {
            // Divider
            Region divider = new Region();
            divider.setMinHeight(1);
            divider.setMaxHeight(1);
            divider.getStyleClass().add("bookingpage-bg-light");

            // Balance row
            HBox balanceRow = createPaymentRow("Balance Remaining", PriceFormatter.formatPriceWithCurrencyNoDecimals((int) balanceDue), true);

            // Info note
            VBox infoNote = new VBox();
            infoNote.setPadding(new Insets(12, 16, 12, 16));
            infoNote.getStyleClass().addAll("bookingpage-bg-light", "bookingpage-rounded");
            VBox.setMargin(infoNote, new Insets(12, 0, 0, 0));

            Label noteLabel = new Label("The remaining balance of " + PriceFormatter.formatPriceWithCurrencyWithDecimals(balanceDue) +
                                        " can be paid online through your account anytime before the event, or at the reception desk when you arrive.");
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

    protected HBox createPaymentRow(String label, String value, boolean isTotal) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setTextFill(TEXT_SECONDARY);
        if (isTotal) {
            labelNode.setFont(fontSemiBold(16));
        } else {
            labelNode.setFont(font(14));
        }

        // Add spacer to push value to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueNode = new Label(value);
        valueNode.setTextFill(TEXT_DARK);
        if (isTotal) {
            valueNode.setFont(fontBold(18));
        } else {
            valueNode.setFont(fontSemiBold(14));
        }

        row.getChildren().addAll(labelNode, spacer, valueNode);
        return row;
    }

    protected VBox buildWhatsNextSection() {
        BookingFormColorScheme colors = colorScheme.get();

        VBox section = new VBox(0);

        // Section header
        HBox header = buildSectionHeader("What's Next?", createChecklistIcon());

        // Content
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("bookingpage-card");

        // Check Your Email
        String email = confirmedBookings.isEmpty() ? "" : confirmedBookings.get(0).getEmail();
        HBox emailStep = createWhatsNextStep(
                createEnvelopeIcon(colors),
                "Check Your Email",
                "Your booking receipt has been sent to " + email + ". If you don't see it within a few minutes, please check your spam folder.",
                colors
        );

        // Confirmation Letter
        HBox confirmStep = createWhatsNextStep(
                createSmallCheckIcon(colors),
                "Confirmation Letter Coming Soon",
                "Your booking will be processed by our registration team. You'll receive your confirmation letter within 48 hours.",
                colors
        );

        content.getChildren().addAll(emailStep, confirmStep);

        // Balance step (if applicable)
        int balanceDue = totalAmount - paidAmount;
        if (balanceDue > 0) {
            HBox balanceStep = createWhatsNextStep(
                    createSmallCardIcon(colors),
                    "Pay the Balance When Ready",
                "The remaining " + PriceFormatter.formatPriceWithCurrencyNoDecimals(balanceDue) +
                " can be paid online through your account, or at reception when you arrive.",
                    colors
            );
            content.getChildren().add(balanceStep);
        }

        section.getChildren().addAll(header, content);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));
        VBox.setMargin(section, new Insets(0, 0, 40, 0));

        return section;
    }

    protected HBox createWhatsNextStep(Node icon, String title, String description, BookingFormColorScheme colors) {
        HBox step = new HBox(12);
        step.setAlignment(Pos.TOP_LEFT);

        // Icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(36, 36);
        iconCircle.setMaxSize(36, 36);
        iconCircle.setBackground(bg(colors.getSelectedBg(), new CornerRadii(18)));
        iconCircle.getChildren().add(icon);

        // Text content
        VBox textBox = new VBox(4);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        descLabel.setWrapText(true);

        textBox.getChildren().addAll(titleLabel, descLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        step.getChildren().addAll(iconCircle, textBox);
        return step;
    }

    protected HBox buildSectionHeader(String title, Node icon) {
        BookingFormColorScheme colors = colorScheme.get();

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setBackground(bg(colors.getSelectedBg(), RADII_8));
        header.setBorder(borderLeft(colors.getPrimary(), 4, RADII_8));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-semibold", "bookingpage-text-dark");

        header.getChildren().addAll(icon, titleLabel);
        return header;
    }

    // ========================================
    // ICON HELPERS
    // ========================================

    protected SVGPath createTicketIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M2 9a3 3 0 0 1 3-3h14a3 3 0 0 1 3 3a3 3 0 0 1-3 3a3 3 0 0 1 3 3a3 3 0 0 1-3 3H5a3 3 0 0 1-3-3a3 3 0 0 1 3-3a3 3 0 0 1-3-3z");
        icon.setStroke(colorScheme.get().getPrimary());
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    protected SVGPath createCalendarIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 4h18a2 2 0 012 2v14a2 2 0 01-2 2H3a2 2 0 01-2-2V6a2 2 0 012-2z M16 2v4 M8 2v4 M3 10h18");
        icon.setStroke(colorScheme.get().getPrimary());
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    protected SVGPath createCardIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M2 5h20a2 2 0 012 2v10a2 2 0 01-2 2H2a2 2 0 01-2-2V7a2 2 0 012-2z M2 10h20");
        icon.setStroke(colorScheme.get().getPrimary());
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    protected SVGPath createChecklistIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2 M9 3h6v4H9V3 M9 14l2 2 4-4");
        icon.setStroke(colorScheme.get().getPrimary());
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    protected SVGPath createEnvelopeIcon(BookingFormColorScheme colors) {
        SVGPath icon = new SVGPath();
        icon.setContent("M2 4h20a2 2 0 012 2v12a2 2 0 01-2 2H2a2 2 0 01-2-2V6a2 2 0 012-2z M22 6l-10 7L2 6");
        icon.setStroke(colors.getPrimary());
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.6);
        icon.setScaleY(0.6);
        return icon;
    }

    protected SVGPath createSmallCheckIcon(BookingFormColorScheme colors) {
        SVGPath icon = new SVGPath();
        icon.setContent("M20 6L9 17l-5-5");
        icon.setStroke(colors.getPrimary());
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.6);
        icon.setScaleY(0.6);
        return icon;
    }

    protected SVGPath createSmallCardIcon(BookingFormColorScheme colors) {
        SVGPath icon = new SVGPath();
        icon.setContent("M2 5h20a2 2 0 012 2v10a2 2 0 01-2 2H2a2 2 0 01-2-2V7a2 2 0 012-2z M2 10h20");
        icon.setStroke(colors.getPrimary());
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.6);
        icon.setScaleY(0.6);
        return icon;
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
