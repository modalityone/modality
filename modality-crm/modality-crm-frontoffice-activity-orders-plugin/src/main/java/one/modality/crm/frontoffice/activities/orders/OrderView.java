package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.i18n.spi.impl.I18nSubKey;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.activities.account.BookingStatus;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A view class that displays a booking (Document) summary and its details in a responsive grid layout.
 * It includes toggling functionality to show/hide booking options, totals, and payment actions.
 * Author: David Hello
 */
public final class OrderView {

    // Required fields for retrieving booking and its details
    private static final String BOOKING_EVENT_REQUIRED_FIELDS = "event.(name,label,image.url,live,startDate,endDate,venue.(name,label,country),organization.country)";
    private static final String BOOKING_PERSON_REQUIRED_FIELDS = "ref, person, person_firstName,person_lastName";
    private static final String BOOKING_STATUS_REQUIRED_FIELDS = BookingStatus.BOOKING_REQUIRED_FIELDS;
    public  static final String BOOKING_REQUIRED_FIELDS = BOOKING_EVENT_REQUIRED_FIELDS + "," + BOOKING_PERSON_REQUIRED_FIELDS + "," + BOOKING_STATUS_REQUIRED_FIELDS;
    public  static final String DOCUMENT_LINE_REQUIRED_FIELDS = "item.name,item.label,item.family.name,item.family.label,quantity,price_net,dates,cancelled";

    private final ObservableList<DocumentLine> bookedOptions = FXCollections.observableArrayList(); // Reactive list of booking options
    private final Document booking; // The booking (order) to display
    private final OrdersActivity ordersActivity; // Reference to parent activity
    private final VBox containerPane = new VBox(); // Main container for the UI
    private final MonoPane toogleOnOffMonopane = new MonoPane(); // Clickable pane to toggle details

    // Icons used to toggle visibility of details
    private final SVGPath chevronUp = SvgIcons.createChevronUp();
    private final SVGPath chevronDown = SvgIcons.createChevronDown();

    private GridPane mainGrid; // Main grid layout for summary and detail sections
    private final List<Node> detailNodes = new ArrayList<>(); // List of all detail nodes, used for toggling
    private boolean detailsVisible = false; // Track whether details are currently shown
    private Label cancelButton;

    public OrderView(Document booking, OrdersActivity ordersActivity) {
        this.booking = booking;
        this.ordersActivity = ordersActivity;
        startLogic();         // Starts reactive loading of booking options
        buildGridLayout();    // Builds the main layout UI
    }

    /**
     * Builds the main grid layout for the summary view and detail section.
     */
    private void buildGridLayout() {
        Event event = booking.getEvent();

        // Initialize and configure the main grid layout
        mainGrid = new GridPane();
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(10));

        // Configure column widths (percent-based for responsiveness)
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(18); // Status
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(45); // Event name
        ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(25); // Dates
        ColumnConstraints col4 = new ColumnConstraints(); col4.setPercentWidth(6);  // Price
        ColumnConstraints col5 = new ColumnConstraints(); col5.setPercentWidth(6);  // Toggle icon

        mainGrid.getColumnConstraints().addAll(col1, col2, col3, col4, col5);
        mainGrid.setVgap(20);

        // Row 0: Booking status and order number
        BookingStatus bookingStatus = BookingStatus.ofBooking(booking);
        Label statusLabel = I18nControls.newLabel(bookingStatus.getI18nKey());
        statusLabel.setPadding(new Insets(5, 15, 5, 15));
        statusLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(7, 0, 7, 0, false), new Insets(0))));
        Label orderNumberLabel = Bootstrap.textSecondary(I18nControls.newLabel(OrdersI18nKeys.OrderNb,"#" + event.getPrimaryKey() + "-" + booking.getRef()));

        mainGrid.add(statusLabel, 0, 0);
        mainGrid.add(orderNumberLabel, 1, 0);

        // Row 1: Person, event, dates, price, toggle arrow
        Label personLabel = Bootstrap.strong(new Label(booking.getFullName()));
        Label eventNameLabel = Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", event)));

        DateTimeFormatter dateFormatter = LocalizedTime.dateFormatter("dd/MM/yyyy");
        String dates = event.getStartDate().format(dateFormatter) + " - " + event.getEndDate().format(dateFormatter);
        Label datesLabel = Bootstrap.textSecondary(new Label(dates));
        Label totalPriceLabel = Bootstrap.strong(new Label(EventPriceFormatter.formatWithCurrency(booking.getPriceNet(), event)));

        // Setup clickable toggle icon
        toogleOnOffMonopane.setOnMouseClicked(e -> toggleDetails());
        toogleOnOffMonopane.setCursor(Cursor.HAND);
        toogleOnOffMonopane.setContent(chevronDown);
        toogleOnOffMonopane.setMinSize(20,20);

        mainGrid.add(personLabel, 0, 1);
        mainGrid.add(eventNameLabel, 1, 1);
        mainGrid.add(datesLabel, 2, 1);
        mainGrid.add(totalPriceLabel, 3, 1);
        mainGrid.add(toogleOnOffMonopane, 4, 1);

        // Setup detail section (starts hidden)
        setupDetailsGridLayout();

        // Add a visual separator below the grid
        Separator separator = new Separator();
        separator.setPadding(new Insets(15,0,0,0));
        detailNodes.add(separator);

        containerPane.getChildren().addAll(mainGrid, separator);
    }

    /**
     * Sets up the detailed grid layout for booking options (e.g., rooms, meals).
     * Updates automatically when the `bookedOptions` list changes.
     */
    private void setupDetailsGridLayout() {
        final int[] currentRow = {2}; // Row index for details
        bookedOptions.addListener((InvalidationListener) observable -> {
            // Remove old detail nodes before rebuilding
            detailNodes.forEach(node -> mainGrid.getChildren().remove(node));
            detailNodes.clear();
            currentRow[0] = 2;

            final String[] previousItemFamilyName = {""};

            // Group and display each item (e.g., meals, rooms) in the booking
            bookedOptions.stream()
                .collect(Collectors.groupingBy(DocumentLine::getItem, LinkedHashMap::new, Collectors.toList()))
                .forEach((item, itemBooked) -> {
                    int totalPrice = itemBooked.stream().mapToInt(dl -> dl.getPriceNet() != null ? dl.getPriceNet() : 0).sum();
                    String formattedTotal = EventPriceFormatter.formatWithCurrency(totalPrice, booking.getEvent());

                    // Show item category (if changed from previous)
                    if (!Objects.equals(previousItemFamilyName[0], item.getFamily().getName())) {
                        Label categoryLabel = Bootstrap.textPrimary(new Label(item.getFamily().getName()));
                        if (item.getFamily().getLabel() != null) {
                            categoryLabel = Bootstrap.textPrimary(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", item.getFamily().getLabel())));
                        }
                        mainGrid.add(categoryLabel, 1, currentRow[0]);
                        detailNodes.add(categoryLabel);
                    }

                    // Show item name and total price
                    Label itemNameLabel = new Label(item.getName());
                    Label totalPriceLabel = new Label(formattedTotal);
                    mainGrid.add(itemNameLabel, 2, currentRow[0]);
                    mainGrid.add(totalPriceLabel, 3, currentRow[0]);
                    detailNodes.add(itemNameLabel);
                    detailNodes.add(totalPriceLabel);

                    currentRow[0]++;
                    previousItemFamilyName[0] = item.getFamily().getName();
                });

            currentRow[0]++; // Space before edit section

            // Add/edit options label with icon
            Label addEditLabel = Bootstrap.strong(Bootstrap.textPrimary(I18nControls.newLabel(OrdersI18nKeys.AddOrEditOption)));
            SVGPath addIcon = SvgIcons.setSVGPathFill(SvgIcons.createPlusPath(), Color.web("#0096D6"));
            addEditLabel.setGraphic(addIcon);
            addEditLabel.setGraphicTextGap(15);
            addEditLabel.setCursor(Cursor.HAND);
            mainGrid.add(addEditLabel, 1, currentRow[0]);
            detailNodes.add(addEditLabel);
            currentRow[0]++;

            // Total section (with background)
            HBox totalsAndButtonHBox = new HBox();
            HBox totalsSection = new HBox(15);
            totalsSection.setAlignment(Pos.CENTER_LEFT);
            totalsSection.setPadding(new Insets(8,30,8,30));
            totalsSection.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, null, null)));

            // Show total, paid, and remaining amounts
            Label totalLabel = Bootstrap.textSecondary(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Total)));
            Label totalValue = Bootstrap.strong(I18nControls.newLabel(EventPriceFormatter.formatWithCurrency(booking.getPriceNet(), booking.getEvent())));
            Label paidLabel = Bootstrap.textSecondary(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Paid)));
            Label paidValue = Bootstrap.strong(I18nControls.newLabel(EventPriceFormatter.formatWithCurrency(booking.getPriceDeposit(), booking.getEvent())));
            Label remainingLabel = Bootstrap.textSecondary(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.RemainingAmount)));
            Label remainingValue = Bootstrap.strong(I18nControls.newLabel(EventPriceFormatter.formatWithCurrency(booking.getPriceNet() - booking.getPriceDeposit(), booking.getEvent())));

            // Conditional "Make Payment" button
            Button paymentButton = ModalityStyle.blackButton(I18nControls.newButton(OrdersI18nKeys.MakePayment));
            paymentButton.visibleProperty().bind(new BooleanBinding() {
                @Override
                protected boolean computeValue() {
                    return booking.getPriceNet() - booking.getPriceDeposit() > 0;
                }
            });

            totalsSection.getChildren().addAll(totalLabel, totalValue, paidLabel, paidValue, remainingLabel, remainingValue);
            totalsAndButtonHBox.getChildren().addAll(totalsSection, paymentButton);

            GridPane.setColumnSpan(totalsAndButtonHBox, 2);
            mainGrid.add(totalsAndButtonHBox, 1, currentRow[0]);
            detailNodes.add(totalsAndButtonHBox);

            // Cancel booking button
            cancelButton = Bootstrap.textDanger(I18nControls.newLabel(OrdersI18nKeys.CancelBooking));
            cancelButton.setCursor(Cursor.HAND);
            cancelButton.setVisible(LocalDate.now().isBefore(booking.getEvent().getStartDate()));

            GridPane.setColumnSpan(cancelButton, 2);
            mainGrid.add(cancelButton, 3, currentRow[0]);
            detailNodes.add(cancelButton);

            // Apply current visibility state
            setDetailsVisibility(detailsVisible);
        });
    }

    /**
     * Toggles the visibility of the booking detail section.
     */
    private void toggleDetails() {
        detailsVisible = !detailsVisible;
        setDetailsVisibility(detailsVisible);
        toogleOnOffMonopane.setContent(detailsVisible ? chevronUp : chevronDown);
    }

    /**
     * Sets the visibility of all detail nodes in the grid.
     */
    private void setDetailsVisibility(boolean visible) {
        detailNodes.forEach(node -> {
            node.setVisible(visible);
            node.setManaged(visible);
        });
        cancelButton.setVisible(visible && LocalDate.now().isBefore(booking.getEvent().getStartDate()));
    }

    /**
     * Returns the root node to be inserted into the UI.
     */
    public Node getView() {
        return containerPane;
    }

    public Document getBooking() {
        return booking;
    }

    /**
     * Starts the reactive query to load the booking's related DocumentLine options.
     */
    private void startLogic() {
        ReactiveEntitiesMapper.<DocumentLine>createReactiveChain(ordersActivity)
            .always("{class: 'DocumentLine', alias: 'dl', orderBy: 'item.family'}")
            .always(DqlStatement.fields(OrderView.DOCUMENT_LINE_REQUIRED_FIELDS))
            .always(DqlStatement.where("dl.document.event = ?", booking.getEvent()))
            .always(DqlStatement.where("!cancelled"))
            .always(DqlStatement.where("dl.document.person = ?", booking.getPerson()))
            .storeEntitiesInto(bookedOptions)
            .start();
    }
}
