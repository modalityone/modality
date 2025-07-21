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
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowhistory.WindowHistory;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.browser.BrowserUtil;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.frontoffice.bookingelements.BookingElements;
import one.modality.event.client.lifecycle.EventLifeCycle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author David Hello
 */
public final class OrderCard {

    // Required fields for retrieving order and its details
    private static final String ORDER_EVENT_REQUIRED_FIELDS = "event.(name,label,state,image.url,live,startDate,endDate,kbs3, venue.(name,label,country),organization.country)";
    private static final String ORDER_PERSON_REQUIRED_FIELDS = "ref,person,person_firstName,person_lastName,cart.uuid";
    private static final String ORDER_STATUS_REQUIRED_FIELDS = OrderStatus.BOOKING_REQUIRED_FIELDS;
    public static final String ORDER_REQUIRED_FIELDS = ORDER_EVENT_REQUIRED_FIELDS + "," + ORDER_PERSON_REQUIRED_FIELDS + "," + ORDER_STATUS_REQUIRED_FIELDS;
    private static final String DOCUMENT_LINE_REQUIRED_FIELDS = "item.name,item.label,item.family.name,item.family.label,quantity,price_net,dates,cancelled";

    private final Document orderDocument; // The order document to display
    private final ObservableList<DocumentLine> orderDocumentLines = ObservableLists.newObservableList(this::updateUi); // Reactive list of order options
    private final MonoPane containerPane = new MonoPane(); // Main container for the UI

    private final Label statusBadge = Bootstrap.badge(new Label());
    private final Label orderPriceLabel = new Label();
    private final Label totalLabel = Controls.setupTextWrapping(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Total)), true, true);
    private final Label totalValue = Controls.setupTextWrapping(new Label(), true, true);
    private final Label paidLabel = Controls.setupTextWrapping(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Paid)), true, true);
    private final Label paidValue = Controls.setupTextWrapping(new Label(), true, true);
    private final Label remainingLabel = Controls.setupTextWrapping(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.RemainingAmount)), true, true);
    private final Label remainingValue = Controls.setupTextWrapping(new Label(), true, true);
    private final Label contactUsLabel = Bootstrap.strong(Bootstrap.textPrimary(I18nControls.newLabel(OrderI18nKeys.ContactUsAboutThisBooking)));
    private final Button modifyOrderButton = Bootstrap.secondaryButton(I18nControls.newButton(OrderI18nKeys.AddOrEditOption));
    private final Button makePaymentButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.MakePayment));
    private final Button askRefundButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.AskARefund));
    private final Button legacyCartButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.LegacyCart));
    private final Label cancelOrderLabel = Bootstrap.textDanger(I18nControls.newLabel(OrderI18nKeys.CancelBooking));
    private final Label viewDetailsLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(OrderI18nKeys.ViewDetails)));
    private final CollapsePane detailsCollapsePane = new CollapsePane();

    private int remainingAmount; // computed in update UI
    private boolean orderDetailsLoaded = false;

    // Public API

    // This constructor is called by OrdersView and will be part of a list (ex: upcoming orders or past orders)
    public OrderCard(Document orderDocument) {
        this.orderDocument = orderDocument;
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
        Node orderDetails = new OrderDetails(orderDocument, orderDocumentLines, null).getView();
        // embedded in a card with a header
        containerPane.setContent(createOrderCard(orderDetails));
        containerPane.getStyleClass().add("container-pane");
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
        remainingAmount = totalPriceNet - deposit;
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
        boolean notEnded = LocalDate.now().isBefore(event.getEndDate().plusDays(30));
        Layouts.setManagedAndVisibleProperties(modifyOrderButton, isKBS3 && notEnded && isNotCancelled);
        Layouts.setManagedAndVisibleProperties(makePaymentButton, isKBS3 && notEnded && remainingAmount > 0);
        Layouts.setManagedAndVisibleProperties(legacyCartButton, !isKBS3);
        Layouts.setManagedAndVisibleProperties(askRefundButton, notEnded && remainingAmount < 0);
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

        setupContactUsLabel();

        VBox orderDetailsAndSummary = new VBox(
            orderDetails,
            createPaymentSummary(),
            contactUsLabel,
            createOrderActionsBar());
        detailsCollapsePane.setContent(orderDetailsAndSummary);
        detailsCollapsePane.setCollapsed(true);
        Layouts.bindManagedAndVisiblePropertiesTo(detailsCollapsePane.collapsedProperty().not(), hideDetailsLabel);
        Layouts.bindManagedAndVisiblePropertiesTo(detailsCollapsePane.collapsedProperty(), viewDetailsLabel);

        viewDetailsHBox.setOnMouseClicked(e -> {
            if (orderDetailsLoaded)
                detailsCollapsePane.toggleCollapse();
            else
                loadAndExpandOrderDetails(false);
        });

        boolean showTestModeBadge = event.getState() == EventState.TESTING;

        VBox orderHeader = new VBox(15,
            showTestModeBadge ? new HBox(10, statusBadge, BookingElements.createTestModeBadge()) : statusBadge,
            orderTitleLabel,
            orderMeta,
            dateHBox,
            viewDetailsHBox,
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
            , viewDetailsLabel);
    }

    private Future<?> loadFromDatabase() {
        orderDetailsLoaded = true;
        return orderDocument.getStore().executeQueryBatch(
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

    private Node createOrderActionsBar() {
        setupModifyOrderButton();
        setupMakePaymentButton();
        setupLegacyCartButton();
        setupAskRefundButton();
        setupCancelOrderButton();

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

    // Button setups

    private void setupContactUsLabel() {
        setupLabeled(contactUsLabel, true, e -> {
            ContactUsDialog contactUsWindow = new ContactUsDialog();
            contactUsWindow.buildUI();
            DialogCallback messageWindowCallback = DialogUtil.showModalNodeInGoldLayout(contactUsWindow.getContainer(), FXMainFrameDialogArea.getDialogArea());
            contactUsWindow.getCancelButton().setOnMouseClicked(m -> messageWindowCallback.closeDialog());

            contactUsWindow.getSendButton().setOnAction(ae -> {
                Document d = orderDocument;
                UpdateStore updateStore = UpdateStore.createAbove(d.getStore());
                Mail email = updateStore.insertEntity(Mail.class);
                email.setFromName(d.getFirstName() + ' ' + d.getLastName());
                email.setFromEmail(d.getEmail());
                email.setSubject("[" + d.getPrimaryKey() + "-" + d.getRef() + "] " + contactUsWindow.getSubject());
                email.setOut(false);
                email.setDocument(d);
                String content = contactUsWindow.getMessage();
                content = content.replaceAll("\r", "<br/>");
                content = content.replaceAll("\n", "<br/>");
                content = "<html>" + content + "</html>";
                email.setContent(content);
                History history = updateStore.insertEntity(History.class);
                history.setUsername("online");
                history.setComment("Sent " + email.getSubject());
                history.setDocument(d);
                history.setMail(email);

                OperationUtil.turnOnButtonsWaitModeDuringExecution(
                    updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onComplete(c -> contactUsWindow.displaySuccessMessage(5000, messageWindowCallback::closeDialog)),
                    contactUsWindow.getSendButton(), contactUsWindow.getCancelButton());
            });
        });
    }

    private void setupModifyOrderButton() {
        setupButton(modifyOrderButton, false, e ->
            WindowHistory.getProvider().push("/modify-order/" + orderDocument.getPrimaryKey()));
    }

    private void setupMakePaymentButton() {
        setupButton(makePaymentButton, false, e ->
            WindowHistory.getProvider().push("/pay-order/" + orderDocument.getPrimaryKey()));
    }

    private void setupLegacyCartButton() {
        legacyCartButton.setGraphicTextGap(10);
        setupButton(legacyCartButton, false,e ->
            BrowserUtil.openExternalBrowser(EventLifeCycle.getKbs2BookingCartUrl(orderDocument)));
    }

    private void setupAskRefundButton() {
        askRefundButton.setOnAction(e -> {
            Event event = orderDocument.getEvent();
            String formattedPrice = EventPriceFormatter.formatWithCurrency(remainingAmount, event);
            RefundDialog refundWindow = new RefundDialog(formattedPrice, String.valueOf(orderDocument.getRef()), event);
            refundWindow.buildUI();
            DialogCallback messageWindowCallback = DialogUtil.showModalNodeInGoldLayout(refundWindow.getContainer(), FXMainFrameDialogArea.getDialogArea());
            refundWindow.getCancelButton().setOnMouseClicked(m -> messageWindowCallback.closeDialog());
            refundWindow.getRefundButton().setOnAction(m -> {
                Document d = orderDocument;
                UpdateStore updateStore = UpdateStore.createAbove(d.getStore());
                Mail email = updateStore.insertEntity(Mail.class);
                email.setFromName(d.getFirstName() + ' ' + d.getLastName());
                email.setFromEmail(d.getEmail());
                email.setSubject("[" + d.getPrimaryKey() + "-" + d.getRef() + "] Refund of " + formattedPrice + " requested");
                email.setOut(false);
                email.setDocument(d);
                String content = "The user has requested a refund for his canceled booking. Amount : " + formattedPrice;
                content = content.replaceAll("\r", "<br/>");
                content = content.replaceAll("\n", "<br/>");
                content = "<html>" + content + "</html>";
                email.setContent(content);
                History history = updateStore.insertEntity(History.class);
                history.setUsername("online");
                history.setComment("Sent " + email.getSubject());
                history.setDocument(d);
                history.setMail(email);

                //TODO: prevent the Refund to display if the refund as already been requested, and display somewhere in the interface that the refund has been requested
                OperationUtil.turnOnButtonsWaitModeDuringExecution(
                    updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onComplete(c -> refundWindow.displayRefundSuccessMessage(8000, messageWindowCallback::closeDialog)),
                    refundWindow.getRefundButton(), refundWindow.getDonateButton(), refundWindow.getCancelButton());

            });
            refundWindow.getDonateButton().setOnAction(ae -> {
                UpdateStore updateStore = UpdateStore.createAbove(orderDocument.getStore());
                //TODO implementation
                OperationUtil.turnOnButtonsWaitModeDuringExecution(
                    updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onComplete(c -> refundWindow.displayDonationSuccessMessage(8000, messageWindowCallback::closeDialog)),
                    refundWindow.getRefundButton(), refundWindow.getDonateButton());

            });
        });
    }

    private void setupCancelOrderButton() {
        cancelOrderLabel.setCursor(Cursor.HAND);
        cancelOrderLabel.setWrapText(true);
        cancelOrderLabel.setOnMouseClicked(e -> {
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
            cancelLabelText.setOnMouseClicked(e2 -> errorMessageCallback.closeDialog());

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
                        .onComplete(ar -> {
                            // Close the dialog only after the operation completes (success or failure)
                            errorMessageCallback.closeDialog();
                        }), confirmButton, cancelLabelText));
            HBox buttonsHBox = new HBox(70, cancelLabelText, confirmButton);
            buttonsHBox.setPadding(new Insets(30, 20, 20, 20));
            buttonsHBox.setAlignment(Pos.CENTER);
            errorDialog.setBottom(buttonsHBox);
        });
    }

    private static void setupLabeled(Labeled button, boolean wrap, EventHandler<MouseEvent> onMouseClicked) {
        button.setCursor(Cursor.HAND);
        if (wrap)
            button.setWrapText(true);
        else
            button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnMouseClicked(onMouseClicked);
    }

    private static void setupButton(ButtonBase button, boolean wrap, EventHandler<ActionEvent> onAction) {
        setupLabeled(button, wrap, null);
        button.setOnAction(onAction);
    }
}
