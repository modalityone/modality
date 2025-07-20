package one.modality.crm.frontoffice.order;

import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.i18n.spi.impl.I18nSubKey;
import dev.webfx.extras.operation.OperationUtil;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowhistory.WindowHistory;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.client.lifecycle.EventLifeCycle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author David Hello
 */
public final class OrderCardView {

    // Required fields for retrieving order and its details
    private static final String ORDER_EVENT_REQUIRED_FIELDS = "event.(name,label,image.url,live,startDate,endDate,kbs3, venue.(name,label,country),organization.country)";
    private static final String ORDER_PERSON_REQUIRED_FIELDS = "ref, person, person_firstName,person_lastName";
    private static final String ORDER_STATUS_REQUIRED_FIELDS = OrderStatus.BOOKING_REQUIRED_FIELDS;
    public static final String ORDER_REQUIRED_FIELDS = ORDER_EVENT_REQUIRED_FIELDS + "," + ORDER_PERSON_REQUIRED_FIELDS + "," + ORDER_STATUS_REQUIRED_FIELDS;
    private static final String DOCUMENT_LINE_REQUIRED_FIELDS = "item.name,item.label,item.family.name,item.family.label,quantity,price_net,dates,cancelled";

    private final Document orderDocument; // The order document to display
    private final ObservableList<DocumentLine> orderDocumentLines = ObservableLists.newObservableList(this::updateUi); // Reactive list of order options
    private final VBox containerPane = new VBox(); // Main container for the UI
    private final ObservableValue<Object> selectedOrderIdProperty;
    private final EntityStore entityStore;
    private final UpdateStore updateStore;

    private final Label statusBadge = Bootstrap.badge(new Label());
    private final Label orderPriceLabel = new Label();
    private final Label totalLabel = Controls.setupTextWrapping(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Total)), true, true);
    private final Label totalValue = Controls.setupTextWrapping(new Label(), true, true);
    private final Label paidLabel = Controls.setupTextWrapping(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Paid)), true, true);
    private final Label paidValue = Controls.setupTextWrapping(new Label(), true, true);
    private final Label remainingLabel = Controls.setupTextWrapping(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.RemainingAmount)), true, true);
    private final Label remainingValue = Controls.setupTextWrapping(new Label(), true, true);

    private boolean orderDetailsLoaded = false;
    private Label cancelOrderLabel;
    private Button modifyOrderButton;
    private final IntegerProperty remainingAmountProperty = new SimpleIntegerProperty();

    // This constructor is called by OrdersView and will be part of a list (ex: upcoming orders or past orders)
    public OrderCardView(Document orderDocument, ObservableValue<Object> selectedOrderIdProperty) {
        this.orderDocument = orderDocument;
        this.selectedOrderIdProperty = selectedOrderIdProperty;
        entityStore = orderDocument.getStore();
        updateStore = UpdateStore.createAbove(entityStore);
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

    private Document getOrderDocument() {
        return orderDocument;
    }

    /**
     * Builds the main UI layout for the summary view and detail section.
     */
    private void buildUi() {
        Node orderDetails = new OrderDetailsView(orderDocument, orderDocumentLines, null).getView();
        // embedded in a card with a header
        containerPane.getChildren().add(createOrderCard(orderDetails));
        containerPane.getStyleClass().add("container-pane");
    }

    private void updateUi() {
        // Updating status badge
        OrderStatus orderStatus = OrderStatus.ofDocument(orderDocument);
        I18nControls.bindI18nProperties(statusBadge, I18nKeys.upperCase(orderStatus.getI18nKey()));
        statusBadge.getStyleClass().setAll(Bootstrap.BADGE, Bootstrap.STRONG, switch (orderStatus) {
            case INCOMPLETE -> Bootstrap.DANGER;
            case CANCELLED -> Bootstrap.WARNING;
            case IN_PROGRESS -> Bootstrap.PRIMARY;
            case COMPLETE, CONFIRMED -> Bootstrap.SUCCESS;
        });

        // Updating order price
        int totalPriceNet = orderDocument.getPriceNet();
        int deposit = orderDocument.getPriceDeposit();
        int remainingAmount = totalPriceNet - deposit;
        Event event = orderDocument.getEvent();
        orderPriceLabel.setText(EventPriceFormatter.formatWithCurrency(totalPriceNet, event));
        totalValue.setText(EventPriceFormatter.formatWithCurrency(totalPriceNet, event));
        paidValue.setText(EventPriceFormatter.formatWithCurrency(deposit, event));
        remainingValue.setText(EventPriceFormatter.formatWithCurrency(remainingAmount, event));
        remainingAmountProperty.set(remainingAmount);

        computeCancelAndAddLabelVisibility();
    }

    private Node createOrderCard(Node orderDetails) {
        Document document = getOrderDocument();
        Event event = document.getEvent();

        // Order Title
        Label orderTitleLabel = Bootstrap.h4(Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", event))));
        orderTitleLabel.setWrapText(true);

        // Order Meta (Booker Name, Order ID, Price)
        HBox orderMeta = new HBox();
        VBox orderInfo = new VBox(8);
        Label bookerNameLabel = Bootstrap.textPrimary(new Label(document.getFullName()));
        bookerNameLabel.getStyleClass().add("booker-name");
        Label orderIdLabel = I18nControls.newLabel(OrderI18nKeys.OrderNb, "#" + event.getPrimaryKey() + "-" + document.getRef());
        orderIdLabel.getStyleClass().add("order-id");
        orderInfo.getChildren().addAll(bookerNameLabel, orderIdLabel);

        orderPriceLabel.getStyleClass().add("order-price");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        orderMeta.getChildren().addAll(orderInfo, spacer, orderPriceLabel);

        // Order Dates

        DateTimeFormatter dayMonthFormatter = LocalizedTime.dateFormatter("MMM d");
        DateTimeFormatter dayMonthYearFormatter = LocalizedTime.dateFormatter("MMM d, yyyy");
        String eventDates;
        if (event.getStartDate().getYear() == event.getEndDate().getYear()) {
            eventDates = event.getStartDate().format(dayMonthFormatter) + " - " + event.getEndDate().format(dayMonthYearFormatter);
        } else {
            eventDates = event.getStartDate().format(dayMonthYearFormatter) + " - " + event.getEndDate().format(dayMonthYearFormatter);
        }
        Label orderDatesLabel = new Label(eventDates);
        orderDatesLabel.getStyleClass().add("order-dates");
        HBox dateHBox = new HBox(orderDatesLabel);
        dateHBox.setAlignment(Pos.CENTER);


        Label viewDetailsLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(OrderI18nKeys.ViewDetails)));
        viewDetailsLabel.getStyleClass().add("semi-bold");
        viewDetailsLabel.setContentDisplay(ContentDisplay.RIGHT);
        viewDetailsLabel.setGraphicTextGap(20);
        Label hideDetailsLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(OrderI18nKeys.HideDetails)));
        hideDetailsLabel.getStyleClass().add("semi-bold");
        hideDetailsLabel.setContentDisplay(ContentDisplay.RIGHT);
        hideDetailsLabel.setGraphicTextGap(20);

        HBox viewDetailsHBox = new HBox(5, viewDetailsLabel, hideDetailsLabel);
        viewDetailsHBox.setPadding(new Insets(30, 0, 0, 0));
        viewDetailsHBox.setAlignment(Pos.CENTER);

        Label contactUsLabel = Bootstrap.strong(Bootstrap.textPrimary(I18nControls.newLabel(OrderI18nKeys.ContactUsAboutThisBooking)));
        contactUsLabel.setCursor(Cursor.HAND);
        contactUsLabel.setWrapText(true);
        contactUsLabel.setOnMouseClicked(e -> {
            ContactUsDialog contactUsWindow = new ContactUsDialog();
            contactUsWindow.buildUI();
            DialogCallback messageWindowCallback = DialogUtil.showModalNodeInGoldLayout(contactUsWindow.getContainer(), FXMainFrameDialogArea.getDialogArea());
            contactUsWindow.getCancelButton().setOnMouseClicked(m -> messageWindowCallback.closeDialog());

            contactUsWindow.getSendButton().setOnAction(m -> {
                Mail email = updateStore.insertEntity(Mail.class);
                email.setFromName(document.getFirstName() + ' ' + document.getLastName());
                email.setFromEmail(document.getEmail());
                email.setSubject("[" + document.getPrimaryKey() + "-" + document.getRef() + "] " + contactUsWindow.getSubject());
                email.setOut(false);
                email.setDocument(document);
                String content = contactUsWindow.getMessage();
                content = content.replaceAll("\r", "<br/>");
                content = content.replaceAll("\n", "<br/>");
                content = "<html>" + content + "</html>";
                email.setContent(content);
                History history = updateStore.insertEntity(History.class);
                history.setUsername("online");
                history.setComment("Sent " + email.getSubject());
                history.setDocument(document);
                history.setMail(email);

                OperationUtil.turnOnButtonsWaitModeDuringExecution(
                    updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onComplete(c -> contactUsWindow.displaySuccessMessage(5000, messageWindowCallback::closeDialog)),
                    contactUsWindow.getSendButton(), contactUsWindow.getCancelButton());

            });
        });
        VBox orderDetailsAndSummary = new VBox();
        orderDetailsAndSummary.getChildren().addAll(
            orderDetails,
            createPaymentSummary(),
            contactUsLabel,
            createOrderActions());
        CollapsePane detailsCollapsePane = new CollapsePane(orderDetailsAndSummary);
        detailsCollapsePane.setCollapsed(true);
        Layouts.bindManagedAndVisiblePropertiesTo(detailsCollapsePane.collapsedProperty().not(), hideDetailsLabel);
        Layouts.bindManagedAndVisiblePropertiesTo(detailsCollapsePane.collapsedProperty(), viewDetailsLabel);

        viewDetailsHBox.setOnMouseClicked(m -> {
            if (!orderDetailsLoaded) {
                OperationUtil.turnOnButtonsWaitModeDuringExecution(
                    loadFromDatabase()
                        .onComplete(x ->
                            // Calling in UI thread, but after another animation frame to ensure the layout pass is
                            // finished, and the height is stabilized for the CollapsePane animation
                            UiScheduler.scheduleInAnimationFrame(detailsCollapsePane::toggleCollapse, 2))
                    , viewDetailsLabel);

            } else {
                detailsCollapsePane.toggleCollapse();
            }
        });

        VBox orderHeader = new VBox(15,
            statusBadge, orderTitleLabel, orderMeta, dateHBox, viewDetailsHBox, detailsCollapsePane);
        orderHeader.setPadding(new Insets(16, 16, 16, 16));
        orderHeader.getStyleClass().add("order-header");

        FXProperties.runNowAndOnPropertyChange(id -> {
            boolean weAreLookingThisOrder = Entities.samePrimaryKey(id, document);
            if (weAreLookingThisOrder/* && !orderDetailsLoaded*/) {
                loadFromDatabase()
                    .onComplete(e -> UiScheduler.runInUiThread(() -> {
                        detailsCollapsePane.setCollapsed(false);
                        Controls.setVerticalScrollNodeWishedPosition(orderHeader, VPos.CENTER);
                        SceneUtil.scrollNodeToBeVerticallyVisibleOnScene(orderHeader, false, true);
                    }));
            } else {
                detailsCollapsePane.setCollapsed(true);
            }
        }, selectedOrderIdProperty);

        MonoPane orderCard = new MonoPane(orderHeader);
        orderCard.getStyleClass().add("order-card");
        return orderCard;
    }

    private Node createPaymentSummary() {
        VBox paymentSummary = new VBox(15);
        paymentSummary.setPadding(new Insets(15, 0, 25, 0));

        // Total row
        HBox totalRow = new HBox();
        totalRow.getStyleClass().add("payment-row");

        totalRow.getChildren().addAll(totalLabel, Layouts.createHGrowable(), totalValue);
        paymentSummary.getChildren().add(totalRow);

        // Paid row
        HBox paidRow = new HBox();
        paidRow.getStyleClass().add("payment-row");

        paidRow.getChildren().addAll(paidLabel, Layouts.createHGrowable(), paidValue);
        paymentSummary.getChildren().add(paidRow);

        // Remaining row
        HBox remainingRow = new HBox();
        remainingRow.setPadding(new Insets(15, 0, 0, 0));
        remainingRow.getStyleClass().add("payment-remaining");
        remainingRow.getChildren().addAll(remainingLabel, Layouts.createHGrowable(), remainingValue);
        paymentSummary.getChildren().add(remainingRow);

        return paymentSummary;
    }

    private Node createOrderActions() {
        modifyOrderButton = Bootstrap.secondaryButton(I18nControls.newButton(OrderI18nKeys.AddOrEditOption));
        modifyOrderButton.setMinWidth(Region.USE_PREF_SIZE);
        modifyOrderButton.setOnAction(e -> {
            WindowHistory.getProvider().push("/modify-order/" + orderDocument.getPrimaryKey());
        });

        Button makePaymentButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.MakePayment));
        makePaymentButton.setMinWidth(Region.USE_PREF_SIZE);
        makePaymentButton.visibleProperty().bind(remainingAmountProperty.greaterThan(0));
        makePaymentButton.managedProperty().bind(makePaymentButton.visibleProperty());

        Button askRefundButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.AskARefund));
        askRefundButton.visibleProperty().bind(remainingAmountProperty.lessThan(0));
        askRefundButton.managedProperty().bind(askRefundButton.visibleProperty());
        askRefundButton.setOnAction(e -> {
            Event event = orderDocument.getEvent();
            String formattedPrice = EventPriceFormatter.formatWithCurrency(remainingAmountProperty.get(), event);
            RefundDialog refundWindow = new RefundDialog(formattedPrice, String.valueOf(orderDocument.getRef()), event);
            refundWindow.buildUI();
            DialogCallback messageWindowCallback = DialogUtil.showModalNodeInGoldLayout(refundWindow.getContainer(), FXMainFrameDialogArea.getDialogArea());
            refundWindow.getCancelButton().setOnMouseClicked(m -> messageWindowCallback.closeDialog());
            refundWindow.getRefundButton().setOnAction(m -> {
                Mail email = updateStore.insertEntity(Mail.class);
                email.setFromName(orderDocument.getFirstName() + ' ' + orderDocument.getLastName());
                email.setFromEmail(orderDocument.getEmail());
                email.setSubject("[" + orderDocument.getPrimaryKey() + "-" + orderDocument.getRef() + "] Refund of " + formattedPrice + " requested");
                email.setOut(false);
                email.setDocument(orderDocument);
                String content = "The user has requested a refund for his canceled booking. Amount : " + formattedPrice;
                content = content.replaceAll("\r", "<br/>");
                content = content.replaceAll("\n", "<br/>");
                content = "<html>" + content + "</html>";
                email.setContent(content);
                History history = updateStore.insertEntity(History.class);
                history.setUsername("online");
                history.setComment("Sent " + email.getSubject());
                history.setDocument(orderDocument);
                history.setMail(email);

                //TODO: prevent the Refund to display if the refund as already been requested, and display somewhere in the interface that the refund has been requested
                OperationUtil.turnOnButtonsWaitModeDuringExecution(
                    updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onComplete(c -> refundWindow.displayRefundSuccessMessage(8000, messageWindowCallback::closeDialog)),
                    refundWindow.getRefundButton(), refundWindow.getDonateButton(), refundWindow.getCancelButton());

            });
            refundWindow.getDonateButton().setOnAction(m -> {
                //TODO implementation
                OperationUtil.turnOnButtonsWaitModeDuringExecution(
                    updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onComplete(c -> refundWindow.displayDonationSuccessMessage(8000, messageWindowCallback::closeDialog)),
                    refundWindow.getRefundButton(), refundWindow.getDonateButton());

            });
        });

        cancelOrderLabel = Bootstrap.textDanger(I18nControls.newLabel(OrderI18nKeys.CancelBooking));
        cancelOrderLabel.setCursor(Cursor.HAND);
        cancelOrderLabel.setWrapText(true);
        cancelOrderLabel.setOnMouseClicked(event -> {
            BorderPane errorDialog = new BorderPane();
            errorDialog.setMinWidth(500);
            ScalePane errorContainer = new ScalePane(errorDialog);

            errorDialog.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(16), Insets.EMPTY)));
            errorDialog.setPadding(new Insets(30));

            Label title = Bootstrap.strong(Bootstrap.textPrimary(Bootstrap.h3(I18nControls.newLabel(OrderI18nKeys.CancelBookingTitle))));
            title.setPadding(new Insets(20, 0, 0, 0));
            title.setWrapText(true);
            errorDialog.setTop(title);
            BorderPane.setAlignment(title, Pos.CENTER);
            Label areYouSureLabel = Bootstrap.strong(Bootstrap.textSecondary(Bootstrap.h5(I18nControls.newLabel(OrderI18nKeys.CancelBookingAreYouSure))));
            areYouSureLabel.setWrapText(true);

            VBox content = new VBox(30, areYouSureLabel);
            content.setAlignment(Pos.CENTER);
            BorderPane.setAlignment(content, Pos.CENTER);
            BorderPane.setMargin(content, new Insets(30, 0, 30, 0));
            errorDialog.setCenter(content);

            Label cancelLabelText = Bootstrap.strong(Bootstrap.textSecondary(I18nControls.newLabel(BaseI18nKeys.Cancel)));
            cancelLabelText.setCursor(Cursor.HAND);
            DialogCallback errorMessageCallback = DialogUtil.showModalNodeInGoldLayout(errorContainer, FXMainFrameDialogArea.getDialogArea());
            cancelLabelText.setOnMouseClicked(m -> errorMessageCallback.closeDialog());

            Button confirmButton = Bootstrap.largeDangerButton(I18nControls.newButton(BaseI18nKeys.Confirm));
            confirmButton.setOnAction(ae ->
                OperationUtil.turnOnButtonsWaitModeDuringExecution(
                    DocumentService.loadDocumentWithPolicy(orderDocument)
                        .compose(policyAndDocumentAggregates -> {
                            PolicyAggregate policyAggregate = policyAndDocumentAggregates.getPolicyAggregate(); // never null
                            DocumentAggregate existingBooking = policyAndDocumentAggregates.getDocumentAggregate(); // might be null
                            WorkingBooking workingBooking = new WorkingBooking(policyAggregate, existingBooking);
                            workingBooking.cancelBooking();
                            return workingBooking.submitChanges("Booking canceled online by user")
                                .compose(result -> loadFromDatabase());
                        })
                        .onFailure(Console::log)
                        .onComplete(x -> {
                            // Close the dialog only after the operation completes (success or failure)
                            errorMessageCallback.closeDialog();
                        }), confirmButton, cancelLabelText));
            HBox buttonsHBox = new HBox(70, cancelLabelText, confirmButton);
            buttonsHBox.setPadding(new Insets(30, 20, 20, 20));
            buttonsHBox.setAlignment(Pos.CENTER);
            errorDialog.setBottom(buttonsHBox);
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox orderActions = new HBox(12, // Gap of 12 px
            modifyOrderButton, makePaymentButton, askRefundButton, spacer, cancelOrderLabel);
        orderActions.setPadding(new Insets(20, 0, 0, 0));
        orderActions.setAlignment(Pos.CENTER);
        Layouts.bindManagedAndVisiblePropertiesTo(
            modifyOrderButton.managedProperty().or(makePaymentButton.managedProperty()).or(askRefundButton.managedProperty()).or(cancelOrderLabel.managedProperty()),
            orderActions
        );
        return orderActions;
    }


    private void computeCancelAndAddLabelVisibility() {
        Event event = orderDocument.getEvent();
        if (cancelOrderLabel != null) //We don't display the cancel button on a new booking (when priceCalculator!=null)
            cancelOrderLabel.setVisible(LocalDate.now().isBefore(event.getStartDate()) && !orderDocument.isCancelled());

        if (modifyOrderButton != null) {
            Boolean cancelled = orderDocument.isCancelled();
            boolean isNotCancelled = cancelled != null && !cancelled;
            boolean isKBS3 = EventLifeCycle.isKbs3Event(event);
            boolean notEnded = LocalDate.now().isBefore(event.getEndDate().plusDays(30));
            boolean visible = notEnded && isNotCancelled && isKBS3;
            Layouts.setManagedAndVisibleProperties(modifyOrderButton, visible);
        }
    }

    private Future<?> loadFromDatabase() {
        orderDetailsLoaded = true;
        return entityStore.executeQueryBatch(
                new EntityStoreQuery("select " + DOCUMENT_LINE_REQUIRED_FIELDS + " from DocumentLine dl " +
                                     " where dl.document.id = ? and !item.family.summaryHidden " +
                                     " order by item.family ", new Object[]{orderDocument}),
                // Note: this will update the fields of the already present orderDocument because we use the same entity store
                new EntityStoreQuery("select " + ORDER_REQUIRED_FIELDS + " from Document d " +
                                     " where d = ?", new Object[]{orderDocument}))
            .onFailure(Console::log)
            // We update orderDocumentLines, which will consequently update both the card and details UI (so we do that in the UI thread)
            .onSuccess(entityLists -> UiScheduler.runInUiThread(() -> orderDocumentLines.setAll(entityLists[0])));
    }
}
