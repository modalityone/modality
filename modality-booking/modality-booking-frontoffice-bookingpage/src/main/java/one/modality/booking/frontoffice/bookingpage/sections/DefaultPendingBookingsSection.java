package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.PriceFormatter;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;

import java.util.function.Consumer;

import static one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder.BadgeType;

/**
 * Default implementation of the Pending Bookings (basket) section.
 * Shows all bookings in cart before proceeding to payment.
 * <p>
 * Design based on JSX mockup Step5Basket.
 *
 * @author Bruno Salmon
 */
public class DefaultPendingBookingsSection implements HasPendingBookingsSection {

    // === PROPERTIES ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);

    // === BOOKINGS ===
    protected final ObservableList<BookingItem> bookings = FXCollections.observableArrayList();
    protected String currencySymbol = "£";

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox bookingsContainer;
    protected VBox paymentSummaryBox;
    protected Label totalBookingsLabel;
    protected Label totalCostLabel;
    protected Label totalAmountLabel;

    // === CALLBACKS ===
    protected Runnable onRegisterAnotherPerson;
    protected Runnable onProceedToPayment;
    protected Runnable onBackPressed;
    protected Consumer<BookingItem> onRemoveBooking;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultPendingBookingsSection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-pending-bookings-section");

        // Header with checkmark icon
        VBox headerSection = buildHeaderSection();

        // Info box
        HBox infoBox = buildInfoBox();

        // Bookings container
        bookingsContainer = new VBox(24);

        // Payment summary
        paymentSummaryBox = buildPaymentSummaryBox();

        // Note: Action buttons are managed via composite API (ButtonNavigation)
        // not created inside this section

        container.getChildren().addAll(headerSection, infoBox, bookingsContainer, paymentSummaryBox);
        VBox.setMargin(infoBox, new Insets(0, 0, 32, 0));
        VBox.setMargin(bookingsContainer, new Insets(0, 0, 24, 0));
        VBox.setMargin(paymentSummaryBox, new Insets(0, 0, 32, 0));
    }

    protected void setupBindings() {
        // Rebuild booking cards when list changes
        bookings.addListener((ListChangeListener<BookingItem>) change -> {
            rebuildBookingCards();
            updatePaymentSummary();
            validProperty.set(!bookings.isEmpty());
        });

        // Note: Color scheme listener removed - CSS handles theme changes via CSS variables
    }

    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
        rebuildBookingCards();
        updatePaymentSummary();
    }

    protected VBox buildHeaderSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);
        VBox.setMargin(section, new Insets(0, 0, 24, 0));

        // Title with checkmark icon
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER);

        // Checkmark circle - uses CSS for theming with solid primary background
        StackPane checkCircle = BookingPageUIBuilder.createThemedIconCircle(28);
        checkCircle.getStyleClass().add("bookingpage-icon-circle-primary");
        SVGPath checkmark = new SVGPath();
        checkmark.setContent("M8 12l3 3 5-6");
        checkmark.setStroke(Color.WHITE);
        checkmark.setStrokeWidth(2);
        checkmark.setFill(Color.TRANSPARENT);
        checkmark.setScaleX(0.7);
        checkmark.setScaleY(0.7);
        checkCircle.getChildren().add(checkmark);

        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.RegistrationSubmitted);
        titleLabel.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-bold", "bookingpage-text-dark");

        titleRow.getChildren().addAll(checkCircle, titleLabel);

        section.getChildren().add(titleRow);
        return section;
    }

    protected HBox buildInfoBox() {
        HBox box = new HBox(14);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(20, 24, 20, 24));
        // CSS class handles background and left border with theme colors
        box.getStyleClass().add("bookingpage-info-box-info");

        // Clipboard icon - uses themed primary color
        SVGPath icon = BookingPageUIBuilder.createThemedIcon(
                "M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2 " +
                "M8 2h8v4H8V2 M9 12l2 2 4-4", 0.7);

        VBox content = new VBox(6);

        // Title - per JSX: fontWeight: 600, fontSize: 15px
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.YourRegisteredAttendees);
        titleLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Description - per JSX: fontSize: 13px, color: muted
        Label descLabel = I18nControls.newLabel(BookingPageI18nKeys.ReviewDetailsSubtitle);
        descLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        descLabel.setWrapText(true);

        content.getChildren().addAll(titleLabel, descLabel);
        HBox.setHgrow(content, Priority.ALWAYS);

        box.getChildren().addAll(icon, content);
        return box;
    }

    protected void rebuildBookingCards() {
        bookingsContainer.getChildren().clear();

        int index = 1;
        for (BookingItem booking : bookings) {
            VBox card = createBookingCard(booking, index);
            bookingsContainer.getChildren().add(card);
            index++;
        }
    }

    protected VBox createBookingCard(BookingItem booking, int bookingNumber) {
        VBox card = new VBox(0);
        card.getStyleClass().addAll("bookingpage-bg-white", "bookingpage-rounded-lg");
        card.setEffect(BookingPageUIBuilder.SHADOW_CARD);

        // Header - CSS classes handle background, border-radius, and bottom border
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.getStyleClass().addAll("bookingpage-card-header", "bookingpage-divider-bottom-gray");

        VBox nameBox = new VBox(4);
        Label nameLabel = new Label(booking.getPersonName());
        nameLabel.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-bold", "bookingpage-text-dark");
        Label emailLabel = new Label(booking.getPersonEmail());
        emailLabel.getStyleClass().add("bookingpage-label-caption");
        nameBox.getChildren().addAll(nameLabel, emailLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        // Right side: booking number + reference + paid status
        VBox rightBox = new VBox(4);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        // Booking number/reference
        Label bookingNumLabel = new Label();
        if (booking.getBookingReference() != null && !booking.getBookingReference().isEmpty()) {
            I18nControls.bindI18nProperties(bookingNumLabel, BookingPageI18nKeys.RefPrefix, booking.getBookingReference());
        } else {
            I18nControls.bindI18nProperties(bookingNumLabel, BookingPageI18nKeys.BookingNumberPrefix, bookingNumber);
        }
        bookingNumLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-primary");
        rightBox.getChildren().add(bookingNumLabel);

        // Paid status badge - using CSS-styled badges
        if (booking.isPaid()) {
            Label paidBadge = BookingPageUIBuilder.createStatusBadge(null, BadgeType.SUCCESS);
            I18nControls.bindI18nProperties(paidBadge, BookingPageI18nKeys.PaidStatus);
            rightBox.getChildren().add(paidBadge);
        } else if (booking.getPaidAmount() > 0) {
            // Partially paid
            Label partialBadge = BookingPageUIBuilder.createStatusBadge(null, BadgeType.WARNING);
            I18nControls.bindI18nProperties(partialBadge, BookingPageI18nKeys.DepositPaid);
            rightBox.getChildren().add(partialBadge);
        }

        header.getChildren().addAll(nameBox, rightBox);

        // Content
        VBox contentBox = new VBox(0);
        contentBox.setPadding(new Insets(24));

        // Event info box
        VBox eventInfoBox = new VBox(8);
        eventInfoBox.setPadding(new Insets(16));
        eventInfoBox.getStyleClass().addAll("bookingpage-bg-light", "bookingpage-rounded");

        Label eventNameLabel = new Label(booking.getEventName());
        eventNameLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Event dates - get from document's event if available
        Label eventDatesLabel = new Label();
        Document doc = booking.getDocument();
        if (doc != null && doc.getEvent() != null) {
            Event event = doc.getEvent();
            String dateText = BookingPageUIBuilder.formatDateRangeFull(event.getStartDate(), event.getEndDate());
            eventDatesLabel.setText(dateText);
        } else {
            // Fallback: use eventDetails if no document available
            String eventDetails = booking.getEventDetails();
            eventDatesLabel.setText(eventDetails != null ? eventDetails : "");
        }
        eventDatesLabel.getStyleClass().add("bookingpage-label-caption");
        eventDatesLabel.setWrapText(true);
        eventInfoBox.getChildren().addAll(eventNameLabel, eventDatesLabel);
        VBox.setMargin(eventInfoBox, new Insets(0, 0, 16, 0));

        // Line items
        VBox lineItems = new VBox(0);
        for (BookingLineItem item : booking.getLineItems()) {
            HBox row = createLineItemRow(item);
            lineItems.getChildren().add(row);
        }

        // Booking total section
        VBox totalSection = new VBox(8);
        totalSection.setPadding(new Insets(20, 0, 0, 0));
        totalSection.getStyleClass().add("bookingpage-divider-top");
        VBox.setMargin(totalSection, new Insets(20, 0, 0, 0));

        // Total row
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);

        Label totalTextLabel = I18nControls.newLabel(BookingPageI18nKeys.BookingTotal);
        totalTextLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label totalPriceLabel = new Label(currencySymbol + PriceFormatter.formatPriceNoCurrencyWithDecimals(booking.getTotalAmount()));
        totalPriceLabel.getStyleClass().addAll("bookingpage-price-large", "bookingpage-text-primary");

        totalRow.getChildren().addAll(totalTextLabel, spacer, totalPriceLabel);
        totalSection.getChildren().add(totalRow);

        // Always show paid amount and balance due
        int paidAmount = (int) booking.getPaidAmount();
        int balance = (int) booking.getBalance();

        // Already Paid row
        HBox paidRow = new HBox();
        paidRow.setAlignment(Pos.CENTER_LEFT);

        Label paidTextLabel = I18nControls.newLabel(BookingPageI18nKeys.AlreadyPaid);
        paidTextLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-muted");
        Region paidSpacer = new Region();
        HBox.setHgrow(paidSpacer, Priority.ALWAYS);

        Label paidAmountLabel = new Label(currencySymbol + PriceFormatter.formatPriceNoCurrencyWithDecimals(paidAmount));
        // Green if paid > 0, muted if 0
        if (paidAmount > 0) {
            paidAmountLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-success");
        } else {
            paidAmountLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-muted");
        }

        paidRow.getChildren().addAll(paidTextLabel, paidSpacer, paidAmountLabel);
        totalSection.getChildren().add(paidRow);

        // Balance Due row (remaining to pay)
        HBox balanceRow = new HBox();
        balanceRow.setAlignment(Pos.CENTER_LEFT);

        Label balanceTextLabel = I18nControls.newLabel(BookingPageI18nKeys.BalanceDue);
        balanceTextLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");
        Region balanceSpacer = new Region();
        HBox.setHgrow(balanceSpacer, Priority.ALWAYS);

        Label balanceAmountLabel = new Label(currencySymbol + PriceFormatter.formatPriceNoCurrencyWithDecimals(balance));
        // Always use theme primary color for balance due
        balanceAmountLabel.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-text-primary");

        balanceRow.getChildren().addAll(balanceTextLabel, balanceSpacer, balanceAmountLabel);
        totalSection.getChildren().add(balanceRow);

        contentBox.getChildren().addAll(eventInfoBox, lineItems, totalSection);
        card.getChildren().addAll(header, contentBox);

        return card;
    }

    protected HBox createLineItemRow(BookingLineItem item) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium", "bookingpage-text-muted");
        nameLabel.setWrapText(true);

        // Add spacer to push price to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean isIncluded = item.getAmount() == 0 && item.isIncluded();
        Label priceLabel;
        if (isIncluded) {
            priceLabel = I18nControls.newLabel(BookingPageI18nKeys.Included);
            priceLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-muted");
        } else {
            priceLabel = new Label(currencySymbol + PriceFormatter.formatPriceNoCurrencyNoDecimals(item.getAmount()));
            priceLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-bold", "bookingpage-text-primary");
        }
        priceLabel.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(nameLabel, spacer, priceLabel);
        return row;
    }

    protected VBox buildPaymentSummaryBox() {
        VBox box = new VBox(0);
        box.setPadding(new Insets(32));
        box.getStyleClass().addAll("bookingpage-bg-white", "bookingpage-rounded-lg");
        box.setEffect(BookingPageUIBuilder.SHADOW_CARD);

        // Title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.PaymentSummary);
        titleLabel.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-bold", "bookingpage-text-dark");
        VBox.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // Total bookings row
        HBox bookingsRow = new HBox();
        bookingsRow.setAlignment(Pos.CENTER_LEFT);
        bookingsRow.setPadding(new Insets(12, 0, 12, 0));
        bookingsRow.getStyleClass().add("bookingpage-divider-thin-bottom");

        Label bookingsTextLabel = I18nControls.newLabel(BookingPageI18nKeys.TotalBookings);
        bookingsTextLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-medium", "bookingpage-text-muted");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        totalBookingsLabel = new Label();
        totalBookingsLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        bookingsRow.getChildren().addAll(bookingsTextLabel, spacer1, totalBookingsLabel);

        // Total cost row
        HBox costRow = new HBox();
        costRow.setAlignment(Pos.CENTER_LEFT);
        costRow.setPadding(new Insets(12, 0, 12, 0));
        costRow.getStyleClass().add("bookingpage-divider-thin-bottom");

        Label costTextLabel = I18nControls.newLabel(BookingPageI18nKeys.TotalCost);
        costTextLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-medium", "bookingpage-text-muted");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        totalCostLabel = new Label();
        totalCostLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        costRow.getChildren().addAll(costTextLabel, spacer2, totalCostLabel);

        // Total amount row (prominent) - CSS handles the themed border
        HBox amountRow = new HBox();
        amountRow.setAlignment(Pos.CENTER_LEFT);
        amountRow.setPadding(new Insets(16, 0, 0, 0));
        amountRow.getStyleClass().add("bookingpage-divider-top-primary");
        VBox.setMargin(amountRow, new Insets(16, 0, 0, 0));

        Label amountTextLabel = I18nControls.newLabel(BookingPageI18nKeys.TotalAmount);
        amountTextLabel.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-bold", "bookingpage-text-dark");
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        totalAmountLabel = new Label();
        totalAmountLabel.getStyleClass().addAll("bookingpage-price-large", "bookingpage-text-primary");

        amountRow.getChildren().addAll(amountTextLabel, spacer3, totalAmountLabel);

        box.getChildren().addAll(titleLabel, bookingsRow, costRow, amountRow);
        return box;
    }

    protected void updatePaymentSummary() {
        int count = bookings.size();
        double total = bookings.stream().mapToDouble(BookingItem::getTotalAmount).sum();

        if (totalBookingsLabel != null) {
            // Use plural form based on count
            Object i18nKey = count == 1 ? BookingPageI18nKeys.AttendeeCount : BookingPageI18nKeys.AttendeesCount;
            I18nControls.bindI18nProperties(totalBookingsLabel, i18nKey, count);
        }
        if (totalCostLabel != null) {
            totalCostLabel.setText(currencySymbol + PriceFormatter.formatPriceNoCurrencyNoDecimals((int) total));
        }
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(currencySymbol + PriceFormatter.formatPriceNoCurrencyNoDecimals((int) total));
        }
    }

    // === BookingFormSection INTERFACE ===

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.PendingBookings;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties props) {
        this.workingBookingProperties = props;
        // Update currency symbol from event when working booking is set
        if (props != null && props.getEvent() != null) {
            this.currencySymbol = EventPriceFormatter.getEventCurrencySymbol(props.getEvent());
            rebuildBookingCards();
            updatePaymentSummary();
        }
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // === HasPendingBookingsSection INTERFACE ===

    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public void addBooking(BookingItem booking) {
        bookings.add(booking);
    }

    @Override
    public void clearBookings() {
        bookings.clear();
    }

    @Override
    public ObservableList<BookingItem> getBookings() {
        return bookings;
    }

    @Override
    public int getTotalAmount() {
        return bookings.stream().mapToInt(BookingItem::getTotalAmount).sum();
    }

    @Override
    public void setOnRegisterAnotherPerson(Runnable callback) {
        this.onRegisterAnotherPerson = callback;
    }

    @Override
    public void setOnProceedToPayment(Runnable callback) {
        this.onProceedToPayment = callback;
    }

    @Override
    public void setOnBackPressed(Runnable callback) {
        this.onBackPressed = callback;
    }

}
