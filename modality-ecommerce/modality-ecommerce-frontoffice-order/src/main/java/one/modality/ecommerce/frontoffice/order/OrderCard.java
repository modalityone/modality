package one.modality.ecommerce.frontoffice.order;

import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.i18n.spi.impl.I18nSubKey;
import dev.webfx.extras.operation.OperationUtil;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.EventState;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.event.client.lifecycle.EventLifeCycle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
public final class OrderCard {

    // Required fields for retrieving order and its details
    private static final String ORDER_EVENT_REQUIRED_FIELDS = "event.(name,label,state,image.url,live,startDate,endDate,kbs3,venue.(name,label,country),organization.country)";
    private static final String ORDER_PERSON_REQUIRED_FIELDS = "ref,person,person_firstName,person_lastName,person_email,cart.uuid";
    private static final String ORDER_STATUS_REQUIRED_FIELDS = OrderStatus.BOOKING_REQUIRED_FIELDS;
    public static final String ORDER_REQUIRED_FIELDS = ORDER_EVENT_REQUIRED_FIELDS + "," + ORDER_PERSON_REQUIRED_FIELDS + "," + ORDER_STATUS_REQUIRED_FIELDS;
    private static final String DOCUMENT_LINE_REQUIRED_FIELDS = "item.(name,label,family.(name,label)),quantity,price_net,dates,cancelled";

    private final Document orderDocument; // The order document to display
    private final ObservableList<DocumentLine> orderDocumentLines = ObservableLists.newObservableList(this::updateUi); // Reactive list of order options
    private final MonoPane containerPane = new MonoPane(); // Main container for the UI

    private final Label statusBadge = Bootstrap.badge(new Label());
    private final Label orderPriceLabel = new Label();
    private final Label totalLabel = I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Total));
    private final Label totalValue = BookingElements.createPriceLabel();
    private final Label paidLabel = I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Paid));
    private final Label paidValue = BookingElements.createPriceLabel();
    private final Label remainingLabel = I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.RemainingAmount));
    private final Label remainingValue = new Label();
    private final Label contactUsLabel;
    private final Button modifyOrderButton;
    private final Button makePaymentButton;
    private final Button askRefundButton;
    private final Button legacyCartButton;
    private final Label cancelOrderLabel;
    private final Label viewHideDetailsLabel = Bootstrap.h4(Bootstrap.textPrimary(new Label()));
    private final CollapsePane detailsCollapsePane = new CollapsePane();

    private boolean orderDetailsLoaded = false;

    // Public API

    // This constructor is called by OrdersView and will be part of a list (ex: upcoming orders or past orders)
    public OrderCard(Document orderDocument) {
        this.orderDocument = orderDocument;
        contactUsLabel = OrderActions.newContactUsLabel(orderDocument);
        modifyOrderButton = OrderActions.newModifyOrderButton(orderDocument);
        makePaymentButton = OrderActions.newMakePaymentButton(orderDocument);
        askRefundButton = OrderActions.newAskRefundButton(orderDocument);
        legacyCartButton = OrderActions.newLegacyCartButton(orderDocument);
        cancelOrderLabel = OrderActions.newCancelOrderLabel(orderDocument, this::loadFromDatabase);
        // Building the UI
        buildUi();
        // First update UI (later update will occur when orderDocumentLines are loaded)
        updateUi();
    }

    /**
     * Returns the root node to be inserted into the UI.
     */
    public Node getView() {
        return containerPane;
    }

    public boolean autoScrollToExpandedDetailsIfOrderId(Object orderId) {
        boolean autoScrollAndExpand = Entities.samePrimaryKey(orderId, orderDocument);
        detailsCollapsePane.setAnimate(false);
        if (autoScrollAndExpand) {
            loadAndExpandOrderDetails(true);
        } else {
            detailsCollapsePane.collapse();
            detailsCollapsePane.setAnimate(true);
        }
        return autoScrollAndExpand;
    }

    // Private implementation

    /**
     * Builds the main UI layout for the card view and detail section.
     */
    private void buildUi() {
        Node orderDetails = new OrderDetails(orderDocument, orderDocumentLines, null, false).getView();
        // embedded in a card with a header
        containerPane.setContent(createOrderCard(orderDetails));
        containerPane.getStyleClass().addAll("container-pane");
        BookingElements.styleBookingElementsContainer(containerPane, false);
    }

    private void updateUi() { // Note: orderDetails updates itself when orderDocumentLines changes, so no need to update it
        // Updating status badge
        OrderStatus orderStatus = OrderStatus.ofDocument(orderDocument);
        I18nControls.bindI18nProperties(statusBadge, I18nKeys.upperCase(orderStatus.getI18nKey()));
        String badgeStyleClass = switch (orderStatus) {
            case INCOMPLETE -> Bootstrap.DANGER;
            case CANCELLED -> Bootstrap.WARNING;
            case IN_PROGRESS -> Bootstrap.PRIMARY;
            case COMPLETE, CONFIRMED -> Bootstrap.SUCCESS;
        };
        statusBadge.getStyleClass().setAll(Bootstrap.BADGE, Bootstrap.STRONG, badgeStyleClass);

        // Updating prices
        int totalPriceNet = orderDocument.getPriceNet();
        int deposit = orderDocument.getPriceDeposit();
        int remainingAmount = totalPriceNet - deposit;
        Event event = orderDocument.getEvent();
        orderPriceLabel.setText(EventPriceFormatter.formatWithCurrency(totalPriceNet, event));
        totalValue.setText(EventPriceFormatter.formatWithCurrency(totalPriceNet, event));
        paidValue.setText(EventPriceFormatter.formatWithCurrency(deposit, event));
        remainingValue.setText(EventPriceFormatter.formatWithCurrency(remainingAmount, event));

        // Updating buttons visibility
        Boolean cancelled = orderDocument.isCancelled();
        boolean isNotCancelled = cancelled != null && !cancelled;
        boolean isKBS3 = EventLifeCycle.isKbs3Event(event);
        boolean notStarted = LocalDate.now().isBefore(event.getStartDate());
        boolean isClosed = EventLifeCycle.isClosed(event, true);
        boolean isOpen = !isClosed;
        Layouts.setManagedAndVisibleProperties(modifyOrderButton, isKBS3 && isOpen && isNotCancelled);
        Layouts.setManagedAndVisibleProperties(makePaymentButton, isKBS3 && isOpen && remainingAmount > 0);
        Layouts.setManagedAndVisibleProperties(legacyCartButton, !isKBS3 && isOpen);
        Layouts.setManagedAndVisibleProperties(askRefundButton, isOpen && remainingAmount < 0);
        Layouts.setManagedAndVisibleProperties(cancelOrderLabel, isNotCancelled && notStarted);
    }

    private Node createOrderCard(Node orderDetails) {
        Event event = orderDocument.getEvent();

        // Order Title
        Label orderTitleLabel = Bootstrap.h4(Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", event))));
        orderTitleLabel.setWrapText(true);

        // Order Meta (Booker Name, Order ID, Price)
        Label bookerNameLabel = Bootstrap.textPrimary(new Label(orderDocument.getFullName()));
        bookerNameLabel.getStyleClass().add("booker-name");
        Label orderIdLabel = I18nControls.newLabel(OrderI18nKeys.OrderNb, "#" + event.getPrimaryKey() + "-" + orderDocument.getRef());
        orderIdLabel.getStyleClass().add("order-id");
        VBox orderInfo = new VBox(8,
            bookerNameLabel,
            orderIdLabel
        );
        HBox orderMeta = new HBox(orderInfo, Layouts.createHGrowable(), orderPriceLabel);

        orderPriceLabel.getStyleClass().add("order-price");

        // Order Dates
        DateTimeFormatter dayMonthFormatter = LocalizedTime.dateFormatter("MMM d");
        DateTimeFormatter dayMonthYearFormatter = LocalizedTime.dateFormatter("MMM d, yyyy");
        String eventDates;
        LocalDate eventStartDate = event.getStartDate();
        if (eventStartDate.getYear() == event.getEndDate().getYear()) {
            eventDates = eventStartDate.format(dayMonthFormatter) + " - " + event.getEndDate().format(dayMonthYearFormatter);
        } else {
            eventDates = eventStartDate.format(dayMonthYearFormatter) + " - " + event.getEndDate().format(dayMonthYearFormatter);
        }
        Label orderDatesLabel = new Label(eventDates);
        orderDatesLabel.getStyleClass().add("order-dates");
        HBox dateHBox = new HBox(orderDatesLabel);
        dateHBox.setAlignment(Pos.CENTER);

        viewHideDetailsLabel.getStyleClass().add("semi-bold");
        viewHideDetailsLabel.setPadding(new Insets(30, 0, 0, 0));
        // We embed the label in a stretchable mono pane so it appears horizontally centered
        MonoPane viewHideDetailsPane = new MonoPane(viewHideDetailsLabel);
        viewHideDetailsPane.setMaxWidth(Double.MAX_VALUE);

        VBox orderDetailsAndSummary = new VBox(
            orderDetails,
            createPaymentSummary(),
            contactUsLabel,
            createOrderActionsBar());
        detailsCollapsePane.setContent(orderDetailsAndSummary);
        detailsCollapsePane.setCollapsed(true);

        // Show / hide details management:
        // We initially bind the label with i18n ViewDetails, which also has a chevron icon which we display on the right
        I18nControls.bindI18nProperties(viewHideDetailsLabel, OrderI18nKeys.ViewDetails);
        viewHideDetailsLabel.setContentDisplay(ContentDisplay.RIGHT);
        viewHideDetailsLabel.setGraphicTextGap(15); // adding some extra space
        // We capture that chevron once set and ask the collapse pane to animate it in dependence of the CollapsePane state
        FXProperties.onPropertySet(viewHideDetailsLabel.graphicProperty(), graphic ->
            CollapsePane.animateChevron(graphic, detailsCollapsePane));
        // We also alternate the label text in dependence of the CollapsePane state
        FXProperties.runOnPropertyChange(collapsed -> {
            I18nControls.bindI18nTextProperty(viewHideDetailsLabel, collapsed ? OrderI18nKeys.ViewDetails : OrderI18nKeys.HideDetails);
        }, detailsCollapsePane.collapsedProperty());
        // We set the mouse click handler on the label
        viewHideDetailsLabel.setOnMouseClicked(e -> {
            if (orderDetailsLoaded) // if already loaded, we just toggle the CollapsePane state
                detailsCollapsePane.toggleCollapse();
            else // otherwise (first time the user clicks on it), we load the details from the database
                loadAndExpandOrderDetails(false); // CollapsePane will be expanded once loaded (see below)
        });
        viewHideDetailsLabel.setCursor(Cursor.HAND);

        boolean showTestModeBadge = event.getState() == EventState.TESTING;
        VBox orderHeader = new VBox(15,
            showTestModeBadge ? new HBox(10, statusBadge, BookingElements.createTestModeBadge()) : statusBadge,
            orderTitleLabel,
            orderMeta,
            dateHBox,
            viewHideDetailsPane,
            detailsCollapsePane
        );
        orderHeader.setPadding(new Insets(16));
        orderHeader.getStyleClass().add("order-header");

        MonoPane orderCard = new MonoPane(orderHeader);
        orderCard.getStyleClass().add("order-card");
        return orderCard;
    }

    private void loadAndExpandOrderDetails(boolean autoscroll) {
        OperationUtil.turnOnButtonsWaitModeDuringExecution(
            loadFromDatabase()
                .onComplete(ar ->
                    // Calling in UI thread, but after another animation frame to ensure the layout pass is
                    // finished, and the height is stabilized for the CollapsePane animation
                    UiScheduler.scheduleInAnimationFrame(() -> {
                        detailsCollapsePane.expand();
                        detailsCollapsePane.setAnimate(true); // autoScrollToExpandedDetailsIfOrderId() might have set it to false
                        if (autoscroll) {
                            Controls.setVerticalScrollNodeWishedPosition(containerPane, VPos.CENTER);
                            SceneUtil.scrollNodeToBeVerticallyVisibleOnScene(containerPane, false, true);
                        }
                    }, 2))
            , viewHideDetailsLabel);
    }

    private Future<?> loadFromDatabase() {
        orderDetailsLoaded = true;
        return orderDocument.getStore().executeQueryBatch(
                new EntityStoreQuery("select " + DOCUMENT_LINE_REQUIRED_FIELDS + " from DocumentLine dl " +
                                     " where dl.document.id = ? and !item.family.summaryHidden " +
                                     " order by item.family ", orderDocument),
                // Note: this will update the fields of the already present orderDocument because we use the same entity store
                new EntityStoreQuery("select " + ORDER_REQUIRED_FIELDS + " from Document d " +
                                     " where d = ?", orderDocument))
            .onFailure(Console::log)
            // We update orderDocumentLines, which will consequently update both the card and details UI (so we do that in the UI thread)
            .inUiThread()
            .onSuccess(entityLists -> orderDocumentLines.setAll(entityLists[0]));
    }

    private Node createPaymentSummary() {
        // Total row
        HBox totalRow = new HBox(totalLabel, Layouts.createHGrowable(), totalValue);
        totalRow.getStyleClass().add("payment-row");

        // Paid row
        HBox paidRow = new HBox(paidLabel, Layouts.createHGrowable(), paidValue);
        paidRow.getStyleClass().add("payment-row");

        // Remaining row
        HBox remainingRow = new HBox(remainingLabel, Layouts.createHGrowable(), remainingValue);
        remainingRow.setPadding(new Insets(15, 0, 0, 0));
        remainingRow.getStyleClass().add("payment-remaining");

        VBox paymentSummary = new VBox(15,
            totalRow,
            paidRow,
            remainingRow
        );
        paymentSummary.setPadding(new Insets(15, 0, 25, 0));

        return paymentSummary;
    }

    private Node createOrderActionsBar() {
        HBox orderActionsBar = new HBox(12, // Gap of 12 px
            modifyOrderButton, makePaymentButton, legacyCartButton, askRefundButton, Layouts.createHGrowable(), cancelOrderLabel);
        orderActionsBar.setPadding(new Insets(20, 0, 0, 0));
        orderActionsBar.setAlignment(Pos.CENTER);
        Layouts.bindManagedAndVisiblePropertiesTo(
            modifyOrderButton.managedProperty().or(makePaymentButton.managedProperty()).or(askRefundButton.managedProperty()).or(legacyCartButton.managedProperty()).or(cancelOrderLabel.managedProperty()),
            orderActionsBar
        );
        return orderActionsBar;
    }

}
