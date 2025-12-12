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
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.PriceFormatter;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.function.Consumer;

import static one.modality.booking.frontoffice.bookingpage.theme.BookingFormStyles.*;

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

        // Update styles when color scheme changes
        colorScheme.addListener((obs, old, newScheme) -> rebuildUI());
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

        BookingFormColorScheme colors = colorScheme.get();

        // Title with checkmark icon
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER);

        // Checkmark circle
        StackPane checkCircle = new StackPane();
        checkCircle.setMinSize(28, 28);
        checkCircle.setMaxSize(28, 28);
        checkCircle.setBackground(bg(colors.getPrimary(), RADII_14));

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
        BookingFormColorScheme colors = colorScheme.get();

        HBox box = new HBox(14);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(20, 24, 20, 24));
        box.setBackground(bg(colors.getSelectedBg(), RADII_8));
        box.setBorder(borderLeft(colors.getPrimary(), 4, RADII_8));

        // Clipboard icon
        SVGPath icon = new SVGPath();
        icon.setContent("M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2 " +
                "M8 2h8v4H8V2 M9 12l2 2 4-4");
        icon.setStroke(colors.getPrimary());
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);

        VBox content = new VBox(6);

        // Title - per JSX: fontWeight: 600, fontSize: 15px
        Label titleLabel = new Label("Your Registered Attendees");
        titleLabel.setFont(fontSemiBold(15));
        titleLabel.setTextFill(colors.getDarkText());

        // Description - per JSX: fontSize: 13px, color: muted
        Label descLabel = new Label("Review the details below and proceed to payment when ready, or add more attendees.");
        descLabel.setFont(font(13));
        descLabel.setTextFill(TEXT_MUTED);
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
        BookingFormColorScheme colors = colorScheme.get();

        VBox card = new VBox(0);
        card.getStyleClass().addAll("bookingpage-bg-white", "bookingpage-rounded-lg");
        card.setEffect(SHADOW_CARD);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.getStyleClass().add("bookingpage-bg-lighter");
        header.setBackground(bg(BG_LIGHTER, new CornerRadii(12, 12, 0, 0, false)));
        header.setBorder(borderBottom(BORDER_GRAY, 2));

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
        String headerText;
        if (booking.getBookingReference() != null && !booking.getBookingReference().isEmpty()) {
            headerText = "Ref: " + booking.getBookingReference();
        } else {
            headerText = "Booking #" + bookingNumber;
        }
        Label bookingNumLabel = new Label(headerText);
        bookingNumLabel.setFont(fontSemiBold(14));
        bookingNumLabel.setTextFill(colors.getPrimary());
        rightBox.getChildren().add(bookingNumLabel);

        // Paid status badge
        if (booking.isPaid()) {
            Label paidBadge = new Label("✓ Paid");
            paidBadge.setFont(fontSemiBold(12));
            paidBadge.setTextFill(Color.WHITE);
            paidBadge.setBackground(bg(Color.web("#28a745"), new CornerRadii(4)));
            paidBadge.setPadding(new Insets(2, 8, 2, 8));
            rightBox.getChildren().add(paidBadge);
        } else if (booking.getPaidAmount() > 0) {
            // Partially paid
            Label partialBadge = new Label("Deposit Paid");
            partialBadge.setFont(fontSemiBold(12));
            partialBadge.setTextFill(Color.WHITE);
            partialBadge.setBackground(bg(Color.web("#ffc107"), new CornerRadii(4)));
            partialBadge.setPadding(new Insets(2, 8, 2, 8));
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

        String eventDetails = booking.getEventDetails();
        if (eventDetails != null && !eventDetails.isEmpty()) {
            Label eventDetailsLabel = new Label(eventDetails);
            eventDetailsLabel.getStyleClass().add("bookingpage-label-caption");
            eventDetailsLabel.setWrapText(true);
            eventInfoBox.getChildren().addAll(eventNameLabel, eventDetailsLabel);
        } else {
            eventInfoBox.getChildren().add(eventNameLabel);
        }
        VBox.setMargin(eventInfoBox, new Insets(0, 0, 16, 0));

        // Line items
        VBox lineItems = new VBox(0);
        for (BookingLineItem item : booking.getLineItems()) {
            HBox row = createLineItemRow(item, colors);
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

        Label totalTextLabel = new Label("Booking Total");
        totalTextLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label totalPriceLabel = new Label(currencySymbol + PriceFormatter.formatPriceNoCurrencyWithDecimals(booking.getTotalAmount()));
        totalPriceLabel.setFont(fontBold(20));
        totalPriceLabel.setTextFill(colors.getPrimary());

        totalRow.getChildren().addAll(totalTextLabel, spacer, totalPriceLabel);
        totalSection.getChildren().add(totalRow);

        // Show paid amount and balance if partially paid
        if (booking.getPaidAmount() > 0 && !booking.isPaid()) {
            // Paid row
            HBox paidRow = new HBox();
            paidRow.setAlignment(Pos.CENTER_LEFT);

            Label paidTextLabel = new Label("Already Paid");
            paidTextLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-muted");
            Region paidSpacer = new Region();
            HBox.setHgrow(paidSpacer, Priority.ALWAYS);

            Label paidAmountLabel = new Label("-" + currencySymbol + PriceFormatter.formatPriceNoCurrencyWithDecimals((int) booking.getPaidAmount()));
            paidAmountLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-success");

            paidRow.getChildren().addAll(paidTextLabel, paidSpacer, paidAmountLabel);
            totalSection.getChildren().add(paidRow);

            // Balance row
            HBox balanceRow = new HBox();
            balanceRow.setAlignment(Pos.CENTER_LEFT);

            Label balanceTextLabel = new Label("Balance Due");
            balanceTextLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");
            Region balanceSpacer = new Region();
            HBox.setHgrow(balanceSpacer, Priority.ALWAYS);

            Label balanceAmountLabel = new Label(currencySymbol + PriceFormatter.formatPriceNoCurrencyWithDecimals((int) booking.getBalance()));
            balanceAmountLabel.setFont(fontBold(18));
            balanceAmountLabel.setTextFill(colors.getPrimary());

            balanceRow.getChildren().addAll(balanceTextLabel, balanceSpacer, balanceAmountLabel);
            totalSection.getChildren().add(balanceRow);
        }

        contentBox.getChildren().addAll(eventInfoBox, lineItems, totalSection);
        card.getChildren().addAll(header, contentBox);

        return card;
    }

    protected HBox createLineItemRow(BookingLineItem item, BookingFormColorScheme colors) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium", "bookingpage-text-muted");
        nameLabel.setWrapText(true);

        // Add spacer to push price to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String priceText;
        boolean isIncluded = item.getAmount() == 0 && item.isIncluded();
        if (isIncluded) {
            priceText = "Included";
        } else {
            priceText = currencySymbol + PriceFormatter.formatPriceNoCurrencyNoDecimals(item.getAmount());
        }

        Label priceLabel = new Label(priceText);
        priceLabel.setFont(isIncluded ? fontSemiBold(13) : fontBold(13));
        priceLabel.setTextFill(isIncluded ? TEXT_MUTED : colors.getPrimary());
        priceLabel.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(nameLabel, spacer, priceLabel);
        return row;
    }

    protected VBox buildPaymentSummaryBox() {
        BookingFormColorScheme colors = colorScheme.get();

        VBox box = new VBox(0);
        box.setPadding(new Insets(32));
        box.getStyleClass().addAll("bookingpage-bg-white", "bookingpage-rounded-lg");
        box.setEffect(SHADOW_CARD);

        // Title
        Label titleLabel = new Label("Payment Summary");
        titleLabel.getStyleClass().addAll("bookingpage-font-bold", "bookingpage-text-dark");
        titleLabel.setFont(fontBold(20));
        VBox.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // Total bookings row
        HBox bookingsRow = new HBox();
        bookingsRow.setAlignment(Pos.CENTER_LEFT);
        bookingsRow.setPadding(new Insets(12, 0, 12, 0));
        bookingsRow.setBorder(borderBottom(BG_LIGHTER, 1));

        Label bookingsTextLabel = new Label("Total Bookings");
        bookingsTextLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-medium", "bookingpage-text-muted");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        totalBookingsLabel = new Label("0 attendees");
        totalBookingsLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        bookingsRow.getChildren().addAll(bookingsTextLabel, spacer1, totalBookingsLabel);

        // Total cost row
        HBox costRow = new HBox();
        costRow.setAlignment(Pos.CENTER_LEFT);
        costRow.setPadding(new Insets(12, 0, 12, 0));
        costRow.setBorder(borderBottom(BG_LIGHTER, 1));

        Label costTextLabel = new Label("Total Cost");
        costTextLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-medium", "bookingpage-text-muted");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        totalCostLabel = new Label(currencySymbol + "0");
        totalCostLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        costRow.getChildren().addAll(costTextLabel, spacer2, totalCostLabel);

        // Total amount row (prominent)
        HBox amountRow = new HBox();
        amountRow.setAlignment(Pos.CENTER_LEFT);
        amountRow.setPadding(new Insets(16, 0, 0, 0));
        amountRow.setBorder(borderTop(colors.getPrimary(), 2));
        VBox.setMargin(amountRow, new Insets(16, 0, 0, 0));

        Label amountTextLabel = new Label("Total Amount");
        amountTextLabel.getStyleClass().addAll("bookingpage-font-bold", "bookingpage-text-dark");
        amountTextLabel.setFont(fontBold(22));
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        totalAmountLabel = new Label(currencySymbol + "0");
        totalAmountLabel.setFont(fontBold(28));
        totalAmountLabel.setTextFill(colors.getPrimary());

        amountRow.getChildren().addAll(amountTextLabel, spacer3, totalAmountLabel);

        box.getChildren().addAll(titleLabel, bookingsRow, costRow, amountRow);
        return box;
    }

    protected void updatePaymentSummary() {
        int count = bookings.size();
        double total = bookings.stream().mapToDouble(BookingItem::getTotalAmount).sum();

        if (totalBookingsLabel != null) {
            totalBookingsLabel.setText(count + (count == 1 ? " attendee" : " attendees"));
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
    public void setCurrencySymbol(String symbol) {
        this.currencySymbol = symbol;
        rebuildBookingCards();
        updatePaymentSummary();
    }

    @Override
    public void addBooking(BookingItem booking) {
        bookings.add(booking);
    }

    @Override
    public void removeBooking(BookingItem booking) {
        bookings.remove(booking);
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
    public int getBookingCount() {
        return bookings.size();
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

    @Override
    public void setOnRemoveBooking(Consumer<BookingItem> callback) {
        this.onRemoveBooking = callback;
    }
}
