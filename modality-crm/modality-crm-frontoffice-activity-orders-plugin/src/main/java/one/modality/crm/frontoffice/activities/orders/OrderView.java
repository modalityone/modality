package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.i18n.spi.impl.I18nSubKey;
import dev.webfx.extras.operation.OperationUtil;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.activities.account.BookingStatus;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;

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

    private ObservableList<DocumentLine> bookedOptions = FXCollections.observableArrayList(); // Reactive list of booking options
    private final ObjectProperty<Document> bookingProperty = new SimpleObjectProperty<>(); // The booking (order) to display
    private final OrdersActivity ordersActivity; // Reference to parent activity
    private final VBox containerPane = new VBox(); // Main container for the UI
    private final MonoPane toogleOnOffMonopane = new MonoPane(); // Clickable pane to toggle details

    // Icons used to toggle visibility of details
    private final SVGPath chevronUp = SvgIcons.createChevronUp();
    private final SVGPath chevronDown = SvgIcons.createChevronDown();

    private GridPane mainGrid; // Main grid layout for summary and detail sections
    private final List<Node> detailNodes = new ArrayList<>(); // List of all detail nodes, used for toggling
    private boolean detailsVisible = false; // Track whether details are currently shown
    private Label cancelLabel;
    private Label addOptionLabel;
    private EntityStore entityStore;

    public OrderView(Document booking, OrdersActivity ordersActivity) {
        this.bookingProperty.set(booking);
        this.ordersActivity = ordersActivity;
        entityStore = EntityStore.create(ordersActivity.getDataSourceModel());
        // Starts reactive loading of booking options
        buildGridLayout();    // Builds the main layout UI
    }

    /**
     * Builds the main grid layout for the summary view and detail section.
     */
    private void buildGridLayout() {
        Event event = bookingProperty.get().getEvent();

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
        final BookingStatus[] bookingStatus = {BookingStatus.ofBooking(bookingProperty.get())};
        final Label[] statusLabel = {I18nControls.newLabel(bookingStatus[0].getI18nKey())};
        FXProperties.runOnPropertyChange(()-> {
            if(bookingProperty.get()!=null) {
                bookingStatus[0] = BookingStatus.ofBooking(bookingProperty.get());
                I18nControls.bindI18nProperties(statusLabel[0],bookingStatus[0].getI18nKey());
            }},bookingProperty);

        statusLabel[0].setPadding(new Insets(5, 15, 5, 15));
        statusLabel[0].setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(7, 0, 7, 0, false), new Insets(0))));
        Label orderNumberLabel = Bootstrap.textSecondary(I18nControls.newLabel(OrdersI18nKeys.OrderNb,"#" + event.getPrimaryKey() + "-" + bookingProperty.get().getRef()));

        mainGrid.add(Controls.setupTextWrapping(statusLabel[0],true,true), 0, 0);
        mainGrid.add(orderNumberLabel, 1, 0);

        // Row 1: Person, event, dates, price, toggle arrow
        Label personLabel = Bootstrap.strong(new Label(bookingProperty.get().getFullName()));
        Label eventNameLabel = Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", event)));

        DateTimeFormatter dateFormatter = LocalizedTime.dateFormatter("dd/MM/yyyy");
        String dates = event.getStartDate().format(dateFormatter) + " - " + event.getEndDate().format(dateFormatter);
        Label datesLabel = Bootstrap.textSecondary(new Label(dates));
        Label totalPriceLabel = Bootstrap.strong(new Label(EventPriceFormatter.formatWithCurrency(bookingProperty.get().getPriceNet(), event)));
        FXProperties.runOnPropertyChange(()-> {
            if(bookingProperty.get()!=null) {
                totalPriceLabel.setText(EventPriceFormatter.formatWithCurrency(bookingProperty.get().getPriceNet(), event));
            }
        }, bookingProperty);

        // Setup clickable toggle icon
        toogleOnOffMonopane.setOnMouseClicked(e -> toggleDetails());
        toogleOnOffMonopane.setCursor(Cursor.HAND);
        toogleOnOffMonopane.setContent(chevronDown);
        toogleOnOffMonopane.setMinSize(20,20);

        mainGrid.add(Controls.setupTextWrapping(personLabel,true,true), 0, 1);
        mainGrid.add(Controls.setupTextWrapping(eventNameLabel,true,true), 1, 1);
        mainGrid.add(Controls.setupTextWrapping(datesLabel,true,true), 2, 1);
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
            Platform.runLater(() -> {
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
                        String formattedTotal = EventPriceFormatter.formatWithCurrency(totalPrice, bookingProperty.get().getEvent());

                        // Show item category (if changed from previous)
                        if (!Objects.equals(previousItemFamilyName[0], item.getFamily().getName())) {
                            Label categoryLabel = Bootstrap.textPrimary(new Label(item.getFamily().getName()));
                            if (item.getFamily().getLabel() != null) {
                                categoryLabel = Bootstrap.textPrimary(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", item.getFamily().getLabel())));
                            }
                            mainGrid.add(Controls.setupTextWrapping(categoryLabel, true, true), 1, currentRow[0]);
                            detailNodes.add(categoryLabel);
                        }
                        // Check if any DocumentLine in the group is canceled
                        boolean anyCanceled = itemBooked.stream().anyMatch(DocumentLine::isCancelled);
                        // Show item name and total price
                        Label itemNameLabel = new Label(item.getName());
                        if (item.getLabel() != null) {
                            itemNameLabel = I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", item.getLabel()));
                        }
                        if (anyCanceled) {
                            itemNameLabel.getStyleClass().add("strikethrough");
                        }
                        Label totalPriceLabel = new Label(formattedTotal);
                        mainGrid.add(Controls.setupTextWrapping(itemNameLabel, true, true), 2, currentRow[0]);
                        mainGrid.add(totalPriceLabel, 3, currentRow[0]);
                        detailNodes.add(itemNameLabel);
                        detailNodes.add(totalPriceLabel);

                        currentRow[0]++;
                        previousItemFamilyName[0] = item.getFamily().getName();
                    });

                currentRow[0]++; // Space before edit section

                // Add/edit options label with icon
                addOptionLabel = Bootstrap.strong(Bootstrap.textPrimary(I18nControls.newLabel(OrdersI18nKeys.AddOrEditOption)));
                SVGPath addIcon = SvgIcons.setSVGPathFill(SvgIcons.createPlusPath(), Color.web("#0096D6"));
                addOptionLabel.setGraphic(addIcon);
                addOptionLabel.setGraphicTextGap(15);
                addOptionLabel.setCursor(Cursor.HAND);
                mainGrid.add(addOptionLabel, 1, currentRow[0]);
                detailNodes.add(addOptionLabel);
                currentRow[0]++;

                // Total section (with background)
                HBox totalsAndButtonHBox = new HBox();
                totalsAndButtonHBox.setAlignment(Pos.CENTER);
                HBox totalsSection = new HBox(15);
                totalsSection.setAlignment(Pos.CENTER_LEFT);
                totalsSection.setPadding(new Insets(8, 30, 8, 30));
                totalsSection.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, null, null)));

                // Show total, paid, and remaining amounts
                Label totalLabel = Controls.setupTextWrapping(Bootstrap.textSecondary(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Total))), true, true);
                Label totalValue = Controls.setupTextWrapping(Bootstrap.strong(I18nControls.newLabel(EventPriceFormatter.formatWithCurrency(bookingProperty.get().getPriceNet(), bookingProperty.get().getEvent()))), true, true);
                Label paidLabel = Controls.setupTextWrapping(Bootstrap.textSecondary(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.Paid))), true, true);
                Label paidValue = Controls.setupTextWrapping(Bootstrap.strong(I18nControls.newLabel(EventPriceFormatter.formatWithCurrency(bookingProperty.get().getPriceDeposit(), bookingProperty.get().getEvent()))), true, true);
                Label remainingLabel = Controls.setupTextWrapping(Bootstrap.textSecondary(I18nControls.newLabel(I18nKeys.appendColons(EcommerceI18nKeys.RemainingAmount))), true, true);
                Label remainingValue = Controls.setupTextWrapping(Bootstrap.strong(I18nControls.newLabel(EventPriceFormatter.formatWithCurrency(bookingProperty.get().getPriceNet() - bookingProperty.get().getPriceDeposit(), bookingProperty.get().getEvent()))), true, true);

                // Conditional "Make Payment" button
                Button paymentButton = ModalityStyle.blackButton(I18nControls.newButton(OrdersI18nKeys.MakePayment));
                paymentButton.visibleProperty().bind(new BooleanBinding() {
                    @Override
                    protected boolean computeValue() {
                        return bookingProperty.get().getPriceNet() - bookingProperty.get().getPriceDeposit() > 0;
                    }
                });

                totalsSection.getChildren().addAll(totalLabel, totalValue, paidLabel, paidValue, remainingLabel, remainingValue);
                // Cancel booking button
                cancelLabel = Controls.setupTextWrapping(Bootstrap.textDanger(I18nControls.newLabel(OrdersI18nKeys.CancelBooking)), true, true);
                cancelLabel.setCursor(Cursor.HAND);
                computeCancelAndAddLabelVisibility();
                cancelLabel.setOnMouseClicked(event -> {
                    BorderPane errorDialog = new BorderPane();
                    errorDialog.setMinWidth(500);
                    ScalePane errorContainer = new ScalePane(errorDialog);

                    errorDialog.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(16), Insets.EMPTY)));
                    errorDialog.setPadding(new Insets(30));

                    Label title = Bootstrap.strong(Bootstrap.textPrimary(Bootstrap.h3(I18nControls.newLabel(OrdersI18nKeys.CancelBookingTitle))));
                    title.setPadding(new Insets(20, 0, 0, 0));
                    title.setWrapText(true);
                    errorDialog.setTop(title);
                    BorderPane.setAlignment(title, Pos.CENTER);
                    Label areYouSureLabel = Bootstrap.strong(Bootstrap.textSecondary(Bootstrap.h5(I18nControls.newLabel(OrdersI18nKeys.CancelBookingAreYouSure))));
                    areYouSureLabel.setWrapText(true);

                    Label refundInfoLabel = Bootstrap.textSecondary((I18nControls.newLabel(OrdersI18nKeys.CancelBookingRefund)));
                    refundInfoLabel.setWrapText(true);

                    VBox content = new VBox(30, areYouSureLabel, refundInfoLabel);
                    content.setAlignment(Pos.CENTER);
                    BorderPane.setAlignment(content, Pos.CENTER);
                    BorderPane.setMargin(content, new Insets(30, 0, 30, 0));
                    errorDialog.setCenter(content);

                    Label cancelLabel = Bootstrap.strong(Bootstrap.textSecondary(I18nControls.newLabel(BaseI18nKeys.Cancel)));
                    cancelLabel.setCursor(Cursor.HAND);
                    DialogCallback errorMessageCallback = DialogUtil.showModalNodeInGoldLayout(errorContainer, FXMainFrameDialogArea.getDialogArea());
                    cancelLabel.setOnMouseClicked(m -> errorMessageCallback.closeDialog());

                    Button confirmButton = Bootstrap.largeDangerButton(I18nControls.newButton(BaseI18nKeys.Confirm));
                    confirmButton.setOnAction(m -> {
                        Future<?> operationFuture = DocumentService.loadDocumentWithPolicy(bookingProperty.get())
                            .onFailure(Console::log)
                            .compose(policyAndDocumentAggregates -> {
                                PolicyAggregate policyAggregate = policyAndDocumentAggregates.getPolicyAggregate(); // never null
                                DocumentAggregate existingBooking = policyAndDocumentAggregates.getDocumentAggregate(); // might be null
                                WorkingBooking workingBooking = new WorkingBooking(policyAggregate, existingBooking);
                                workingBooking.cancelBooking();
                                return workingBooking.submitChanges("Booking canceled online by user");
                            });

                        OperationUtil.turnOnButtonsWaitModeDuringExecution(operationFuture, confirmButton, cancelLabel);

                        // Close dialog only after operation completes (success or failure)
                        operationFuture.onComplete(x -> {
                            errorMessageCallback.closeDialog();
                            loadFromDatabase();
                        });
                    });

                    HBox buttonsHBox = new HBox(70, cancelLabel, confirmButton);
                    buttonsHBox.setPadding(new Insets(30, 20, 20, 20));
                    buttonsHBox.setAlignment(Pos.CENTER);
                    errorDialog.setBottom(buttonsHBox);
                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                totalsAndButtonHBox.getChildren().addAll(totalsSection, paymentButton, spacer, cancelLabel);

                GridPane.setColumnSpan(totalsAndButtonHBox, 4);
                mainGrid.add(totalsAndButtonHBox, 1, currentRow[0]);
                detailNodes.add(totalsAndButtonHBox);

                // Apply current visibility state
                setDetailsVisibility(detailsVisible);
            });
        });
    }

    private void computeCancelAndAddLabelVisibility() {
        if(cancelLabel!= null)
            cancelLabel.setVisible(LocalDate.now().isBefore(bookingProperty.get().getEvent().getStartDate())&&!bookingProperty.get().isCancelled());
        if(addOptionLabel!= null) {
            addOptionLabel.setVisible(LocalDate.now().isBefore(bookingProperty.get().getEvent().getStartDate()) && !bookingProperty.get().isCancelled());
            addOptionLabel.setManaged(LocalDate.now().isBefore(bookingProperty.get().getEvent().getStartDate()) && !bookingProperty.get().isCancelled());
        }
    }

    /**
     * Toggles the visibility of the booking detail section.
     */
    private void toggleDetails() {
        detailsVisible = !detailsVisible;
        setDetailsVisibility(detailsVisible);
        if(bookedOptions.isEmpty()) {
            loadFromDatabase();
        }
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
        computeCancelAndAddLabelVisibility();
    }

    private void loadFromDatabase() {
            bookedOptions.clear();
            entityStore.executeQueryBatch(
                // Index 0: the  scheduledItem
                new EntityStoreQuery("select " + OrderView.DOCUMENT_LINE_REQUIRED_FIELDS +" from DocumentLine dl " +
                    " where dl.document.event = ? and dl.document.person = ? " +
                    " order by item.family ",  new Object[]{bookingProperty.get().getEvent(), bookingProperty.get().getPerson()}),
                //Index 1: the video Item (we should have exactly 1)
                new EntityStoreQuery("select " + OrderView.BOOKING_REQUIRED_FIELDS + " from Document d " +
                    " where d.id = ?",  new Object[]{bookingProperty.get().getId()}))
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                    bookedOptions.addAll(entityLists[0]);
                    this.bookingProperty.set(null);
                    this.bookingProperty.set((Document) entityLists[1].get(0));
            }));
    }


    /**
     * Returns the root node to be inserted into the UI.
     */
    public Node getView() {
        return containerPane;
    }

    public Document getBooking() {
        return bookingProperty.get();
    }
}
