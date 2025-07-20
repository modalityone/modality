package one.modality.crm.frontoffice.order;

import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.i18n.spi.impl.I18nSubKey;
import dev.webfx.extras.operation.OperationUtil;
import dev.webfx.extras.panes.CollapsePane;
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
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.windowhistory.WindowHistory;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;
import one.modality.event.client.lifecycle.EventLifeCycle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author David Hello
 */
public final class OrderView {

    // Required fields for retrieving order and its details
    private static final String ORDER_EVENT_REQUIRED_FIELDS = "event.(name,label,image.url,live,startDate,endDate,kbs3, venue.(name,label,country),organization.country)";
    private static final String ORDER_PERSON_REQUIRED_FIELDS = "ref, person, person_firstName,person_lastName";
    private static final String ORDER_STATUS_REQUIRED_FIELDS = OrderStatus.BOOKING_REQUIRED_FIELDS;
    public static final String ORDER_REQUIRED_FIELDS = ORDER_EVENT_REQUIRED_FIELDS + "," + ORDER_PERSON_REQUIRED_FIELDS + "," + ORDER_STATUS_REQUIRED_FIELDS;
    public static final String DOCUMENT_LINE_REQUIRED_FIELDS = "item.name,item.label,item.family.name,item.family.label,quantity,price_net,dates,cancelled";

    private final ObjectProperty<Document> orderDocumentProperty = new SimpleObjectProperty<>(); // The order document to display
    private final ObservableList<DocumentLine> orderDocumentLines = FXCollections.observableArrayList(); // Reactive list of order options
    private final VBox containerPane = new VBox(); // Main container for the UI
    private final ObservableValue<Object> selectedOrderIdProperty;

    private Label cancelOrderLabel;
    private Button modifyOrderButton;
    private final EntityStore entityStore;
    private PriceCalculator priceCalculator = null;
    private final IntegerProperty remainingAmountProperty = new SimpleIntegerProperty();
    private boolean orderDetailsLoaded = false;
    private final UpdateStore updateStore;

    public OrderView(Document order, ObservableValue<Object> selectedOrderIdProperty) {
        this.selectedOrderIdProperty = selectedOrderIdProperty;
        orderDocumentProperty.set(order);
        entityStore = order.getStore();
        //this constructor is called in the OrderView, and we want the header in that case.
        buildUi();
        updateStore = UpdateStore.createAbove(entityStore);
    }

    public OrderView(WorkingBooking workingBooking) {
        selectedOrderIdProperty = null;
        orderDocumentProperty.set(workingBooking.getDocument());
        entityStore = workingBooking.getDocument().getStore();
        containerPane.getChildren().add(createOrderDetails());
        if (workingBooking.getLastestDocumentAggregate() != null) {
            priceCalculator = new PriceCalculator(workingBooking.getLastestDocumentAggregate());
        }
        ObservableLists.runNowAndOnListChange(change ->
                UiScheduler.runInUiThread(() -> orderDocumentLines.setAll(workingBooking.getLastestDocumentAggregate().getDocumentLines()))
            , workingBooking.getDocumentChanges());
        updateStore = UpdateStore.createAbove(entityStore);
    }

    /**
     * Returns the root node to be inserted into the UI.
     */
    public Node getView() {
        return containerPane;
    }

    public Document getOrderDocument() {
        return orderDocumentProperty.get();
    }

    /**
     * Builds the main UI layout for the summary view and detail section.
     */
    private void buildUi() {
        containerPane.getStyleClass().add("container-pane");
        containerPane.getChildren().add(createOrderCard());
    }

    private Node createOrderCard() {
        // Main order card container
        VBox orderCard = new VBox();
        orderCard.getStyleClass().add("order-card");
        orderCard.getChildren().add(createOrderHeader());
        return orderCard;
    }

    private Node createOrderHeader() {
        Document document = getOrderDocument();
        Event event = document.getEvent();
        VBox orderHeader = new VBox(15);
        orderHeader.setPadding(new Insets(16, 16, 16, 16));
        orderHeader.getStyleClass().add("order-header");

        // Order Status
        Label[] statusLabel = new Label[1];
        statusLabel[0] = Bootstrap.badge(new Label());
        // Bind status style class
        FXProperties.runNowAndOnPropertyChange(orderDocument -> {
            if (orderDocument != null) {
                statusLabel[0].getStyleClass().clear();
                switch (OrderStatus.ofDocument(orderDocument)) {
                    case INCOMPLETE:
                        statusLabel[0].getStyleClass().add(Bootstrap.DANGER);
                        break;
                    case CANCELLED:
                        statusLabel[0].getStyleClass().add(Bootstrap.WARNING);
                        break;
                    case IN_PROGRESS:
                        statusLabel[0].getStyleClass().add(Bootstrap.PRIMARY);
                        break;
                    case COMPLETE, CONFIRMED:
                        statusLabel[0].getStyleClass().add(Bootstrap.SUCCESS);
                        break;
                    default:
                        //statusLabel[0].getStyleClass().add(Bootstrap.SECONDARY);
                        break;
                }
                statusLabel[0].getStyleClass().add(Bootstrap.BADGE);
                statusLabel[0].getStyleClass().add(Bootstrap.STRONG);
                I18nControls.bindI18nProperties(statusLabel[0], I18nKeys.upperCase(OrderStatus.ofDocument(orderDocument).getI18nKey()));
                computeCancelAndAddLabelVisibility();
            }
        }, orderDocumentProperty);

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

        Label orderPriceLabel = new Label(EventPriceFormatter.formatWithCurrency(document.getPriceNet(), event));
        orderPriceLabel.getStyleClass().add("order-price");

        FXProperties.runOnPropertyChange(b -> {
            if (b != null) {
                orderPriceLabel.setText(EventPriceFormatter.formatWithCurrency(b.getPriceNet(), event));
            }
        }, orderDocumentProperty);

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
        orderDetailsAndSummary.getChildren().addAll(createOrderDetails(),
            createPaymentSummary(),
            contactUsLabel,
            createOrderActions());
        CollapsePane detailsCollapsePane = new CollapsePane(orderDetailsAndSummary);
        detailsCollapsePane.setCollapsed(true);
        Layouts.bindManagedAndVisiblePropertiesTo(detailsCollapsePane.collapsedProperty().not(), hideDetailsLabel);
        Layouts.bindManagedAndVisiblePropertiesTo(detailsCollapsePane.collapsedProperty(), viewDetailsLabel);

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

        orderHeader.getChildren().addAll(statusLabel[0], orderTitleLabel, orderMeta, dateHBox, viewDetailsHBox, detailsCollapsePane);
        return orderHeader;
    }

    private Node createOrderDetails() {
        VBox orderDetails = new VBox();

        orderDocumentLines.addListener((InvalidationListener) observable -> {
            orderDetails.getChildren().clear(); // Clear existing details
            // Group and display each item (e.g., meals, rooms) in the booking
            Document document = orderDocumentProperty.get();
            orderDocumentLines.stream()
                .collect(Collectors.groupingBy(dl -> dl.getItem().getFamily(), LinkedHashMap::new, Collectors.toList()))
                .forEach((itemFamily, documentLinesInFamily) -> {
                    // Create expandable detail item for each family
                    VBox detailItem = new VBox();
                    detailItem.getStyleClass().add("detail-item");
                    detailItem.setPadding(new Insets(15, 0, 0, 0));
                    HBox detailHeader = new HBox();
                    detailHeader.getStyleClass().add("detail-header");
                    detailHeader.getStyleClass().add("expandable");

                    // Show item family category
                    Label categoryLabel = Bootstrap.textPrimary(new Label(itemFamily.getName()));
                    if (itemFamily.getLabel() != null) {
                        categoryLabel = Bootstrap.textPrimary(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", itemFamily.getLabel())));
                    }
                    categoryLabel.getStyleClass().add("detail-label");

                    // Calculate the total price for this family
                    int familyTotalPrice = documentLinesInFamily.stream()
                        .mapToInt(dl -> {
                            int price = dl.getPriceNet() != null ? dl.getPriceNet() : 0;
                            if (priceCalculator != null) {
                                price = priceCalculator.calculateDocumentLinesPrice(Stream.of(dl));
                            }
                            return price;
                        })
                        .sum();
                    Label familyPriceLabel = new Label(EventPriceFormatter.formatWithCurrency(familyTotalPrice, document.getEvent()));
                    familyPriceLabel.getStyleClass().add("detail-value");

                    detailHeader.getChildren().addAll(categoryLabel, new Region(), familyPriceLabel);
                    HBox.setHgrow(detailHeader.getChildren().get(1), Priority.ALWAYS); // Spacer

                    VBox detailContentVBox = new VBox();
                    detailContentVBox.setPadding(new Insets(10, 0, 15, 20));

                    documentLinesInFamily.forEach(documentLine -> {
                        int price = documentLine.getPriceNet() != null ? documentLine.getPriceNet() : 0;
                        if (priceCalculator != null) {
                            price = priceCalculator.calculateDocumentLinesPrice(Stream.of(documentLine));
                        }
                        String formattedPrice = EventPriceFormatter.formatWithCurrency(price, document.getEvent());
                        boolean isCancelled = Booleans.booleanValue(documentLine.isCancelled());

                        HBox subItem = new HBox();
                        subItem.getStyleClass().add("detail-subitem");

                        // Show item family category
                        Label itemNameLabel = new Label(documentLine.getItem().getName());
                        if (itemFamily.getLabel() != null) {
                            itemNameLabel = I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", documentLine.getItem().getLabel()));
                        }
                        itemNameLabel.getStyleClass().add("detail-subitem-label");
                        itemNameLabel.setWrapText(true);
                        if (isCancelled) {
                            itemNameLabel.getStyleClass().add("strikethrough");
                        }

                        Label datesLabel = Bootstrap.textSecondary(new Label(documentLine.getDates()));

                        datesLabel.setPadding(new Insets(0, 40, 0, 0));
                        if (isCancelled) {
                            datesLabel.getStyleClass().add("strikethrough");
                        }
                        Label priceLabel = Bootstrap.textSecondary(new Label(formattedPrice));
                        priceLabel.getStyleClass().add("detail-subitem-value");

                        Region subSpacer = new Region();
                        HBox.setHgrow(subSpacer, Priority.ALWAYS);
                        subItem.getChildren().addAll(itemNameLabel, subSpacer, datesLabel, priceLabel);
                        detailContentVBox.getChildren().add(subItem);
                    });
                    detailItem.getChildren().addAll(detailHeader, detailContentVBox);
                    orderDetails.getChildren().add(detailItem);
                });
        });
        return orderDetails;
    }

    private Node createPaymentSummary() {
        VBox paymentSummary = new VBox(15);
        paymentSummary.setPadding(new Insets(15, 0, 25, 0));

        // Total row
        HBox totalRow = new HBox();
        totalRow.getStyleClass().add("payment-row");
        Label totalLabel = Controls.setupTextWrapping(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Total)), true, true);
        Label totalValue = Controls.setupTextWrapping(new Label(), true, true);

        Label paidLabel = Controls.setupTextWrapping(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Paid)), true, true);
        Label paidValue = Controls.setupTextWrapping(new Label(), true, true);
        Label remainingLabel = Controls.setupTextWrapping(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.RemainingAmount)), true, true);
        Label remainingValue = Controls.setupTextWrapping(new Label(), true, true);

        FXProperties.runNowAndOnPropertyChange(orderDocument -> {
            if (orderDocument != null) {
                Integer totalPriceNet = orderDocument.getPriceNet();
                Integer deposit = orderDocument.getPriceDeposit();
                if (priceCalculator != null) {
                    totalPriceNet = priceCalculator.calculateTotalPrice();
                    deposit = orderDocument.getPriceDeposit() != null ? orderDocument.getPriceDeposit() : 0;
                }
                totalValue.setText(EventPriceFormatter.formatWithCurrency(totalPriceNet, orderDocument.getEvent()));
                paidValue.setText(EventPriceFormatter.formatWithCurrency(deposit, orderDocument.getEvent()));
                remainingValue.setText(EventPriceFormatter.formatWithCurrency(totalPriceNet - deposit, orderDocument.getEvent()));
                remainingAmountProperty.set(totalPriceNet - deposit);
            }
        }, orderDocumentProperty);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        totalRow.getChildren().addAll(totalLabel, spacer1, totalValue);
        paymentSummary.getChildren().add(totalRow);

        // Paid row
        HBox paidRow = new HBox();
        paidRow.getStyleClass().add("payment-row");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        paidRow.getChildren().addAll(paidLabel, spacer2, paidValue);
        paymentSummary.getChildren().add(paidRow);

        // Remaining row
        HBox remainingRow = new HBox();
        remainingRow.setPadding(new Insets(15, 0, 0, 0));
        remainingRow.getStyleClass().add("payment-remaining");
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        remainingRow.getChildren().addAll(remainingLabel, spacer3, remainingValue);
        paymentSummary.getChildren().add(remainingRow);

        return paymentSummary;
    }

    private Node createOrderActions() {
        HBox orderActions = new HBox(12); // Gap of 12 px
        orderActions.setPadding(new Insets(20, 0, 0, 0));
        orderActions.setAlignment(Pos.CENTER);
        modifyOrderButton = Bootstrap.secondaryButton(I18nControls.newButton(OrderI18nKeys.AddOrEditOption));
        modifyOrderButton.setMinWidth(Region.USE_PREF_SIZE);
        modifyOrderButton.setOnAction(e -> {
            WindowHistory.getProvider().push("/modify-order/" + orderDocumentProperty.get().getPrimaryKey());
        });

        Button makePaymentButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.MakePayment));
        makePaymentButton.setMinWidth(Region.USE_PREF_SIZE);
        makePaymentButton.visibleProperty().bind(remainingAmountProperty.greaterThan(0));
        makePaymentButton.managedProperty().bind(makePaymentButton.visibleProperty());

        Button askRefundButton = Bootstrap.primaryButton(I18nControls.newButton(OrderI18nKeys.AskARefund));
        askRefundButton.visibleProperty().bind(remainingAmountProperty.lessThan(0));
        askRefundButton.managedProperty().bind(askRefundButton.visibleProperty());
        askRefundButton.setOnAction(event -> {
            String formattedPrice = EventPriceFormatter.formatWithCurrency(remainingAmountProperty.get(), orderDocumentProperty.get().getEvent());
            RefundDialog refundWindow = new RefundDialog(formattedPrice, String.valueOf(orderDocumentProperty.get().getRef()), orderDocumentProperty.get().getEvent());
            refundWindow.buildUI();
            DialogCallback messageWindowCallback = DialogUtil.showModalNodeInGoldLayout(refundWindow.getContainer(), FXMainFrameDialogArea.getDialogArea());
            refundWindow.getCancelButton().setOnMouseClicked(m -> messageWindowCallback.closeDialog());
            refundWindow.getRefundButton().setOnAction(m -> {
                Mail email = updateStore.insertEntity(Mail.class);
                email.setFromName(orderDocumentProperty.get().getFirstName() + ' ' + orderDocumentProperty.get().getLastName());
                email.setFromEmail(orderDocumentProperty.get().getEmail());
                email.setSubject("[" + orderDocumentProperty.get().getPrimaryKey() + "-" + orderDocumentProperty.get().getRef() + "] Refund of " + formattedPrice + " requested");
                email.setOut(false);
                email.setDocument(orderDocumentProperty.get());
                String content = "The user has requested a refund for his canceled booking. Amount : " + formattedPrice;
                content = content.replaceAll("\r", "<br/>");
                content = content.replaceAll("\n", "<br/>");
                content = "<html>" + content + "</html>";
                email.setContent(content);
                History history = updateStore.insertEntity(History.class);
                history.setUsername("online");
                history.setComment("Sent " + email.getSubject());
                history.setDocument(orderDocumentProperty.get());
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
                    DocumentService.loadDocumentWithPolicy(orderDocumentProperty.get())
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
        orderActions.getChildren().addAll(modifyOrderButton, makePaymentButton, askRefundButton, spacer, cancelOrderLabel);
        Layouts.bindManagedAndVisiblePropertiesTo(
            modifyOrderButton.managedProperty().or(makePaymentButton.managedProperty().or(askRefundButton.managedProperty().or(cancelOrderLabel.managedProperty()))),
            orderActions
        );
        return orderActions;
    }


    private void computeCancelAndAddLabelVisibility() {
        Document document = orderDocumentProperty.get();
        Event event = document.getEvent();
        if (cancelOrderLabel != null) //We don't display the cancel button on a new booking (when priceCalculator!=null)
            cancelOrderLabel.setVisible(priceCalculator == null && LocalDate.now().isBefore(event.getStartDate()) && !document.isCancelled());

        if (modifyOrderButton != null) {
            Boolean cancelled = document.isCancelled();
            boolean isNotCancelled = cancelled != null && !cancelled;
            boolean isKBS3 = EventLifeCycle.isKbs3Event(event);
            boolean notEnded = LocalDate.now().isBefore(event.getEndDate().plusDays(30));
            boolean visible = notEnded && isNotCancelled && isKBS3;
            modifyOrderButton.setVisible(visible);
            modifyOrderButton.setManaged(visible);
        }
    }

    private Future<?> loadFromDatabase() {
        orderDetailsLoaded = true;
        return entityStore.executeQueryBatch(
                new EntityStoreQuery("select " + DOCUMENT_LINE_REQUIRED_FIELDS + " from DocumentLine dl " +
                                     " where dl.document.id = ? and !item.family.summaryHidden " +
                                     " order by item.family ", new Object[]{getOrderDocument()}),
                new EntityStoreQuery("select " + ORDER_REQUIRED_FIELDS + " from Document d " +
                                     " where d = ?", new Object[]{getOrderDocument()}))
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                orderDocumentLines.setAll(entityLists[0]);
                orderDocumentProperty.set((Document) entityLists[1].get(0));
            }));
    }
}
