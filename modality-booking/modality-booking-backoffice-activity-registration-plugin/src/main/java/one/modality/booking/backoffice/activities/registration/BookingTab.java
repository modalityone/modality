package one.modality.booking.backoffice.activities.registration;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Set;

import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Booking tab for the Registration Edit Modal.
 * <p>
 * New layout based on RegistrationDashboardFull.jsx TimelineOptionsEditor (lines 3873-7248):
 * - Header with accommodation status, date steppers, notes button, total price
 * - Timeline canvas showing booking options over event period
 * - Line item rows with category colors
 * - Add Option panel with availability
 * - Price summary
 *
 * @author Claude Code
 */
public class BookingTab {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Document document;
    private final UpdateStore updateStore;

    private final BooleanProperty activeProperty = new SimpleBooleanProperty(false);

    // Document lines loaded from database
    private final ObservableList<DocumentLine> loadedLines = FXCollections.observableArrayList();

    // UI Components
    private BookingTimelineCanvas timelineCanvas;
    private AddOptionPanel addOptionPanel;
    private VBox lineItemsList;
    private ReactiveEntitiesMapper<DocumentLine> linesMapper;

    // Booking dates (may differ from document dates if user is editing)
    private final ObjectProperty<LocalDate> arrivalDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> departureDateProperty = new SimpleObjectProperty<>();

    // Property to hold the document ID (primary key) for reactive binding
    // Note: Store just the ID value, not the entity, to avoid EntityIdImpl serialization issues
    private final ObjectProperty<Object> documentIdProperty = new SimpleObjectProperty<>();

    public BookingTab(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Document document, UpdateStore updateStore) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;
        this.updateStore = updateStore;
        // Store just the primary key value for the reactive query
        if (document.getId() != null) {
            this.documentIdProperty.set(document.getId().getPrimaryKey());
        }

        // Initialize dates from document
        // TODO: Get actual arrival/departure from document when fields are available
    }

    /**
     * Builds the Booking tab UI.
     */
    public Node buildUi() {
        VBox container = new VBox(SPACING_LARGE);
        container.setPadding(PADDING_LARGE);

        // 1. Header section with accommodation status, date steppers, etc.
        Node headerSection = createHeaderSection();

        // 2. Timeline section with canvas
        Node timelineSection = createTimelineSection();

        // 3. Line items list
        Node lineItemsSection = createLineItemsSection();
        VBox.setVgrow(lineItemsSection, Priority.ALWAYS);

        // 4. Add Option panel
        addOptionPanel = new AddOptionPanel(activity, pm, document, updateStore);
        addOptionPanel.setOnItemAdded(() -> {
            // Refresh line items when a new option is added
            refreshLineItems();
        });
        Node addOptionNode = addOptionPanel.buildUi();

        // 5. Price summary
        Node priceSummary = createPriceSummary();

        container.getChildren().addAll(headerSection, timelineSection, lineItemsSection, addOptionNode, priceSummary);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }

    /**
     * Creates the header section with accommodation status, date steppers, etc.
     * Based on JSX lines 3930-4120.
     */
    private Node createHeaderSection() {
        HBox header = new HBox(12);
        header.setPadding(PADDING_MEDIUM);
        header.setAlignment(Pos.CENTER_LEFT);

        // Accommodation status (shown if there's an accommodation line)
        Node accommodationStatus = createAccommodationStatus();

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date stepper group
        Node dateSteppers = createDateStepperGroup();

        // Notes button
        Node notesButton = createNotesButton();

        // Total price display
        Node totalPrice = createTotalPriceDisplay();

        header.getChildren().addAll(accommodationStatus, spacer, dateSteppers, notesButton, totalPrice);
        return header;
    }

    /**
     * Creates the accommodation status display.
     */
    private Node createAccommodationStatus() {
        HBox status = new HBox(10);
        status.setPadding(new Insets(10, 14, 10, 14));
        status.setBackground(createBackground(CREAM, BORDER_RADIUS_MEDIUM));
        status.setBorder(createBorder(CREAM_BORDER, BORDER_RADIUS_MEDIUM));
        status.setAlignment(Pos.CENTER_LEFT);

        // Category icon (accommodation)
        StackPane icon = createCategoryIcon("accommodation", 20);

        // Room info
        VBox roomInfo = new VBox(2);
        Label roomLabel = new Label("Room TBA");
        roomLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        roomLabel.setTextFill(TEXT);

        Label typeLabel = new Label("Accommodation");
        typeLabel.setFont(Font.font("System", 11));
        typeLabel.setTextFill(TEXT_MUTED);

        roomInfo.getChildren().addAll(roomLabel, typeLabel);

        // Status badge
        Label statusBadge = new Label("Unallocated");
        statusBadge.setFont(Font.font("System", FontWeight.MEDIUM, 11));
        statusBadge.setBackground(createBackground(WARNING_BG, BORDER_RADIUS_SMALL));
        statusBadge.setTextFill(WARNING);
        statusBadge.setPadding(new Insets(4, 8, 4, 8));

        status.getChildren().addAll(icon, roomInfo, statusBadge);

        // Update with actual data when lines are loaded
        ObservableLists.runNowAndOnListChange(change -> {
            DocumentLine accommodationLine = findAccommodationLine();
            if (accommodationLine != null) {
                status.setVisible(true);
                status.setManaged(true);
                // Update room info
                if (accommodationLine.getResourceConfiguration() != null) {
                    roomLabel.setText(accommodationLine.getResourceConfiguration().getName());
                    statusBadge.setText("Allocated");
                    statusBadge.setBackground(createBackground(SUCCESS_BG, BORDER_RADIUS_SMALL));
                    statusBadge.setTextFill(SUCCESS);
                } else {
                    roomLabel.setText("Room TBA");
                    statusBadge.setText("Unallocated");
                    statusBadge.setBackground(createBackground(WARNING_BG, BORDER_RADIUS_SMALL));
                    statusBadge.setTextFill(WARNING);
                }
            } else {
                status.setVisible(false);
                status.setManaged(false);
            }
        }, loadedLines);

        return status;
    }

    /**
     * Creates the date stepper group with arrival, duration, and departure.
     */
    private Node createDateStepperGroup() {
        HBox group = new HBox(4);
        group.setAlignment(Pos.CENTER);

        // Arrival stepper (green theme)
        Node arrivalStepper = createDateStepper("Arrival", arrivalDateProperty, ARRIVAL_BG, ARRIVAL_TEXT, ARRIVAL_BORDER, true);

        // Duration badge
        Label durationBadge = new Label("- nights");
        durationBadge.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        durationBadge.setTextFill(WARM_BROWN);
        durationBadge.setBackground(createBackground(SAND, BORDER_RADIUS_SMALL));
        durationBadge.setPadding(new Insets(6, 10, 6, 10));

        // Update duration when dates change
        FXProperties.runNowAndOnPropertiesChange(() -> {
            LocalDate arrival = arrivalDateProperty.get();
            LocalDate departure = departureDateProperty.get();
            if (arrival != null && departure != null && !departure.isBefore(arrival)) {
                long nights = ChronoUnit.DAYS.between(arrival, departure);
                durationBadge.setText(nights + " night" + (nights != 1 ? "s" : ""));
            } else {
                durationBadge.setText("- nights");
            }
        }, arrivalDateProperty, departureDateProperty);

        // Departure stepper (red theme)
        Node departureStepper = createDateStepper("Departure", departureDateProperty, DEPARTURE_BG, DEPARTURE_TEXT, DEPARTURE_BORDER, false);

        group.getChildren().addAll(arrivalStepper, durationBadge, departureStepper);
        return group;
    }

    /**
     * Creates a date stepper control.
     */
    private Node createDateStepper(String label, ObjectProperty<LocalDate> dateProperty, Color bg, Color text, Color border, boolean isArrival) {
        HBox stepper = new HBox(2);
        stepper.setAlignment(Pos.CENTER);
        stepper.setPadding(new Insets(4, 8, 4, 8));
        stepper.setBackground(createBackground(bg, BORDER_RADIUS_SMALL));
        stepper.setBorder(createBorder(border, BORDER_RADIUS_SMALL));

        // Minus button
        Button minusBtn = new Button("-");
        minusBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        minusBtn.setTextFill(text);
        minusBtn.setBackground(Background.EMPTY);
        minusBtn.setBorder(Border.EMPTY);
        minusBtn.setCursor(Cursor.HAND);
        minusBtn.setOnAction(e -> {
            LocalDate current = dateProperty.get();
            if (current != null) {
                dateProperty.set(current.minusDays(1));
                // Adjust all temporal line dates
                adjustAllLineDates(isArrival ? -1 : 0, isArrival ? 0 : -1);
            }
        });

        // Date display
        VBox dateDisplay = new VBox(0);
        dateDisplay.setAlignment(Pos.CENTER);
        dateDisplay.setMinWidth(60);

        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", 9));
        labelText.setTextFill(text.deriveColor(0, 1, 1, 0.7));

        Label dateText = new Label("-");
        dateText.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        dateText.setTextFill(text);

        dateDisplay.getChildren().addAll(labelText, dateText);

        // Update date display
        FXProperties.runNowAndOnPropertiesChange(() -> {
            LocalDate date = dateProperty.get();
            if (date != null) {
                dateText.setText(date.getDayOfMonth() + " " + date.getMonth().toString().substring(0, 3));
            } else {
                dateText.setText("-");
            }
        }, dateProperty);

        // Plus button
        Button plusBtn = new Button("+");
        plusBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        plusBtn.setTextFill(text);
        plusBtn.setBackground(Background.EMPTY);
        plusBtn.setBorder(Border.EMPTY);
        plusBtn.setCursor(Cursor.HAND);
        plusBtn.setOnAction(e -> {
            LocalDate current = dateProperty.get();
            if (current != null) {
                dateProperty.set(current.plusDays(1));
                // Adjust all temporal line dates
                adjustAllLineDates(isArrival ? 1 : 0, isArrival ? 0 : 1);
            }
        });

        stepper.getChildren().addAll(minusBtn, dateDisplay, plusBtn);
        return stepper;
    }

    /**
     * Creates the notes button with badge count.
     */
    private Node createNotesButton() {
        Button notesBtn = new Button();
        HBox content = new HBox(4);
        content.setAlignment(Pos.CENTER);

        Label icon = new Label("\uD83D\uDCAC"); // Speech bubble emoji
        Label text = new Label("Notes");
        text.setFont(Font.font("System", 12));

        content.getChildren().addAll(icon, text);

        notesBtn.setGraphic(content);
        notesBtn.setBackground(createBackground(WARM_BROWN_LIGHT, BORDER_RADIUS_SMALL));
        notesBtn.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
        notesBtn.setPadding(new Insets(6, 12, 6, 12));
        notesBtn.setCursor(Cursor.HAND);

        // TODO: Add badge count for notes if present
        // TODO: Handle notes button click

        return notesBtn;
    }

    /**
     * Creates the total price display.
     */
    private Node createTotalPriceDisplay() {
        VBox priceBox = new VBox(0);
        priceBox.setAlignment(Pos.CENTER_RIGHT);

        Label labelText = new Label("Total");
        labelText.setFont(Font.font("System", 9));
        labelText.setTextFill(TEXT_MUTED);

        Label priceText = new Label(formatPrice(document.getPriceNet() != null ? document.getPriceNet() : 0));
        priceText.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceText.setTextFill(WARM_BROWN);

        priceBox.getChildren().addAll(labelText, priceText);
        return priceBox;
    }

    /**
     * Creates the timeline section with canvas.
     */
    private Node createTimelineSection() {
        VBox section = new VBox(8);
        section.setPadding(PADDING_MEDIUM);
        section.setBackground(createBackground(BG_CARD, BORDER_RADIUS_MEDIUM));
        section.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        // Create timeline canvas
        timelineCanvas = new BookingTimelineCanvas();

        // Set event data
        Event event = document.getEvent();
        if (event != null) {
            timelineCanvas.setEvent(event);
            // Initialize booking dates from event if not set
            if (arrivalDateProperty.get() == null) {
                arrivalDateProperty.set(event.getStartDate());
            }
            if (departureDateProperty.get() == null) {
                departureDateProperty.set(event.getEndDate());
            }
        }

        // Bind booking dates
        timelineCanvas.bookingStartProperty().bind(arrivalDateProperty);
        timelineCanvas.bookingEndProperty().bind(departureDateProperty);

        // Handle day toggle events
        timelineCanvas.setOnDayToggled(this::handleDayToggled);

        // Set min height
        timelineCanvas.setMinHeight(120);

        section.getChildren().add(timelineCanvas);
        VBox.setVgrow(timelineCanvas, Priority.ALWAYS);

        return section;
    }

    /**
     * Handles when a day is toggled on/off in the timeline.
     */
    private void handleDayToggled(DocumentLine line, LocalDate date) {
        // Get the excluded days set from the canvas
        Set<LocalDate> excludedDays = timelineCanvas.getExcludedDays(line);

        // Get or create an editable copy of the line
        DocumentLine editableLine = updateStore.updateEntity(line);

        // Mark the line as having attendance gaps if there are excluded days
        editableLine.setHasAttendanceGap(!excludedDays.isEmpty());

        // Recalculate price based on included days
        recalculateLinePrice(editableLine, excludedDays);

        // Refresh the line items display to show updated price
        refreshLineItems();
    }

    /**
     * Recalculates the price for a line based on excluded days.
     */
    private void recalculateLinePrice(DocumentLine line, Set<LocalDate> excludedDays) {
        LocalDate lineStart = line.getStartDate();
        LocalDate lineEnd = line.getEndDate();

        if (lineStart == null) lineStart = arrivalDateProperty.get();
        if (lineEnd == null) lineEnd = departureDateProperty.get();
        if (lineStart == null || lineEnd == null) return;

        // Count included days
        int includedDays = 0;
        LocalDate current = lineStart;
        while (!current.isAfter(lineEnd)) {
            if (!excludedDays.contains(current)) {
                includedDays++;
            }
            current = current.plusDays(1);
        }

        // Get original price and calculate price per day
        Integer originalPrice = line.getPriceNet();
        int totalDays = (int) ChronoUnit.DAYS.between(lineStart, lineEnd) + 1;

        if (originalPrice != null && totalDays > 0) {
            // Calculate price per day based on original total
            int pricePerDay = originalPrice / totalDays;
            // Set new price based on included days
            line.setPriceNet(pricePerDay * includedDays);
        }
    }

    /**
     * Adjusts all temporal line dates when arrival/departure changes.
     * @param arrivalDelta Days to add to arrival (-1 for earlier, +1 for later)
     * @param departureDelta Days to add to departure (-1 for earlier, +1 for later)
     */
    private void adjustAllLineDates(int arrivalDelta, int departureDelta) {
        for (DocumentLine line : loadedLines) {
            // Skip removed/cancelled lines
            if (Boolean.TRUE.equals(line.getFieldValue("removed"))) continue;
            if (Boolean.TRUE.equals(line.isCancelled())) continue;

            // Check if this is a temporal option
            String category = getCategoryFromLine(line);
            if (!isTemporalCategory(category)) continue;

            DocumentLine editableLine = updateStore.updateEntity(line);

            // Adjust dates
            LocalDate currentStart = editableLine.getStartDate();
            LocalDate currentEnd = editableLine.getEndDate();

            if (currentStart != null && arrivalDelta != 0) {
                editableLine.setStartDate(currentStart.plusDays(arrivalDelta));
            }
            if (currentEnd != null && departureDelta != 0) {
                editableLine.setEndDate(currentEnd.plusDays(departureDelta));
            }

            // Recalculate price with any excluded days
            Set<LocalDate> excludedDays = timelineCanvas.getExcludedDays(line);
            recalculateLinePrice(editableLine, excludedDays);
        }

        // Update timeline canvas and line items display
        if (timelineCanvas != null) {
            timelineCanvas.getDocumentLines().setAll(loadedLines);
        }
        refreshLineItems();
    }

    /**
     * Checks if a category is temporal (date-based).
     */
    private boolean isTemporalCategory(String category) {
        return "accommodation".equals(category) ||
               "meals".equals(category) ||
               "diet".equals(category) ||
               "program".equals(category);
    }

    /**
     * Creates the line items section.
     */
    private Node createLineItemsSection() {
        VBox section = new VBox(8);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Booking Options");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(TEXT);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        header.getChildren().add(titleLabel);

        // Line items list
        lineItemsList = new VBox(6);

        section.getChildren().addAll(header, lineItemsList);
        return section;
    }

    /**
     * Refreshes the line items list from loaded data.
     */
    private void refreshLineItems() {
        if (lineItemsList == null) return;

        lineItemsList.getChildren().clear();

        for (DocumentLine line : loadedLines) {
            // Skip removed lines
            if (Boolean.TRUE.equals(line.getFieldValue("removed"))) continue;

            Node card = createLineItemCard(line);
            lineItemsList.getChildren().add(card);
        }

        // Update timeline canvas
        if (timelineCanvas != null) {
            timelineCanvas.getDocumentLines().setAll(loadedLines);
        }

        // Update add option panel with existing lines
        if (addOptionPanel != null) {
            addOptionPanel.setExistingLines(new ArrayList<>(loadedLines));
        }
    }

    /**
     * Creates a line item card.
     */
    private Node createLineItemCard(DocumentLine line) {
        HBox card = new HBox(12);
        card.setPadding(PADDING_MEDIUM);
        card.setBackground(createBackground(BG_CARD, BORDER_RADIUS_SMALL));
        card.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
        card.setAlignment(Pos.CENTER_LEFT);

        // Get category from item family
        String category = getCategoryFromLine(line);

        // Category icon
        StackPane icon = createCategoryIcon(category, 20);

        // Item info
        VBox itemInfo = new VBox(2);
        HBox.setHgrow(itemInfo, Priority.ALWAYS);

        String itemName = line.getItem() != null ? line.getItem().getName() : "Unknown Item";
        Label nameLabel = new Label(itemName);
        nameLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        nameLabel.setTextFill(TEXT);

        String dateRange = formatDateRange(line.getStartDate(), line.getEndDate());
        Label datesLabel = new Label(dateRange);
        datesLabel.setFont(Font.font("System", 11));
        datesLabel.setTextFill(TEXT_MUTED);

        itemInfo.getChildren().addAll(nameLabel, datesLabel);

        // Price
        Integer price = line.getPriceNet();
        Label priceLabel = new Label(formatPrice(price != null ? price : 0));
        priceLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        priceLabel.setTextFill(WARM_BROWN);
        priceLabel.setMinWidth(60);

        // Actions menu
        MenuButton actionsMenu = new MenuButton("\u22EE"); // Vertical ellipsis
        actionsMenu.setFont(Font.font("System", 14));
        actionsMenu.setBackground(Background.EMPTY);
        actionsMenu.setBorder(Border.EMPTY);

        boolean isCancelled = Boolean.TRUE.equals(line.isCancelled());

        if (isCancelled) {
            // For cancelled lines, show Restore and Delete options
            MenuItem restoreItem = new MenuItem("↩️ Restore");
            restoreItem.setOnAction(e -> handleRestoreLine(line));

            MenuItem deleteItem = new MenuItem("🗑️ Delete");
            deleteItem.setOnAction(e -> handleDeleteLine(line));

            actionsMenu.getItems().addAll(restoreItem, deleteItem);
        } else {
            // For active lines, show Edit, Cancel, Delete options
            MenuItem editItem = new MenuItem("✏️ Edit");
            editItem.setOnAction(e -> handleEditLine(line));

            MenuItem cancelItem = new MenuItem("🚫 Cancel");
            cancelItem.setOnAction(e -> handleCancelLine(line));

            MenuItem deleteItem = new MenuItem("🗑️ Delete");
            deleteItem.setOnAction(e -> handleDeleteLine(line));

            actionsMenu.getItems().addAll(editItem, cancelItem, deleteItem);
        }

        card.getChildren().addAll(icon, itemInfo, priceLabel, actionsMenu);

        // Add cancelled styling if cancelled
        if (Boolean.TRUE.equals(line.isCancelled())) {
            card.setOpacity(0.5);
            nameLabel.setStyle("-fx-strikethrough: true;");
        }

        return card;
    }

    /**
     * Creates the price summary section.
     */
    private Node createPriceSummary() {
        HBox section = new HBox(SPACING_LARGE);
        section.setPadding(PADDING_LARGE);
        section.setBackground(createBackground(BG, BORDER_RADIUS_MEDIUM));
        section.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));
        section.setAlignment(Pos.CENTER_RIGHT);

        // Get prices
        Integer priceNet = document.getPriceNet();
        Integer priceDeposit = document.getPriceDeposit();
        int subtotal = priceNet != null ? priceNet : 0;
        int paid = priceDeposit != null ? priceDeposit : 0;
        int balance = subtotal - paid;

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Subtotal
        VBox subtotalBox = createPriceSummaryItem("SUBTOTAL", subtotal, WARM_BROWN);
        subtotalBox.setBackground(createBackground(CREAM, BORDER_RADIUS_SMALL));
        subtotalBox.setPadding(new Insets(6, 14, 6, 14));

        // Paid
        VBox paidBox = createPriceSummaryItem("PAID", paid, SUCCESS);
        paidBox.setBackground(createBackground(SUCCESS_BG, BORDER_RADIUS_SMALL));
        paidBox.setPadding(new Insets(6, 14, 6, 14));

        // Balance
        Color balanceColor = balance > 0 ? DANGER : SUCCESS;
        VBox balanceBox = createPriceSummaryItem("BALANCE", balance, balanceColor);
        balanceBox.setBackground(createBackground(balance > 0 ? RED_LIGHT : SUCCESS_BG, BORDER_RADIUS_SMALL));
        balanceBox.setPadding(new Insets(6, 14, 6, 14));

        section.getChildren().addAll(spacer, subtotalBox, paidBox, balanceBox);
        return section;
    }

    /**
     * Creates a price summary item box.
     */
    private VBox createPriceSummaryItem(String label, int amount, Color textColor) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(72);

        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", FontWeight.SEMI_BOLD, 9));
        labelText.setTextFill(textColor.deriveColor(0, 1, 0.7, 1));

        Label amountText = new Label(formatPrice(amount));
        amountText.setFont(Font.font("System", FontWeight.BOLD, 15));
        amountText.setTextFill(textColor);

        box.getChildren().addAll(labelText, amountText);
        return box;
    }

    // Event handlers

    private void handleEditLine(DocumentLine line) {
        // Get editable line from UpdateStore
        DocumentLine editableLine = updateStore.updateEntity(line);

        EditLineModal modal = new EditLineModal(
            editableLine,
            updateStore,
            () -> {
                // On save - refresh line items display
                refreshLineItems();
            },
            () -> {
                // On cancel - no action needed
            }
        );
        modal.show();
    }

    private void handleCancelLine(DocumentLine line) {
        ConfirmActionModal.showCancelConfirmation(line, comment -> {
            // Get editable line and set cancelled
            DocumentLine editableLine = updateStore.updateEntity(line);
            editableLine.setCancelled(true);
            // TODO: Set cancel reason when field is available
            // editableLine.setCancelReason(comment);

            // Submit changes
            updateStore.submitChanges()
                .onSuccess(result -> refreshLineItems())
                .onFailure(error -> System.err.println("Failed to cancel line: " + error.getMessage()));
        });
    }

    private void handleDeleteLine(DocumentLine line) {
        ConfirmActionModal.showDeleteConfirmation(line, comment -> {
            // Get editable line and set removed (soft delete)
            DocumentLine editableLine = updateStore.updateEntity(line);
            editableLine.setFieldValue("removed", true);
            // TODO: Set delete reason when field is available
            // editableLine.setDeleteReason(comment);

            // Submit changes
            updateStore.submitChanges()
                .onSuccess(result -> refreshLineItems())
                .onFailure(error -> System.err.println("Failed to delete line: " + error.getMessage()));
        });
    }

    private void handleRestoreLine(DocumentLine line) {
        ConfirmActionModal.showRestoreConfirmation(line, comment -> {
            // Get editable line and restore it
            DocumentLine editableLine = updateStore.updateEntity(line);
            editableLine.setCancelled(false);
            editableLine.setFieldValue("removed", false);

            // Submit changes
            updateStore.submitChanges()
                .onSuccess(result -> refreshLineItems())
                .onFailure(error -> System.err.println("Failed to restore line: " + error.getMessage()));
        });
    }

    // Helper methods

    private DocumentLine findAccommodationLine() {
        for (DocumentLine line : loadedLines) {
            String category = getCategoryFromLine(line);
            if ("accommodation".equals(category) && !Boolean.TRUE.equals(line.isCancelled())) {
                return line;
            }
        }
        return null;
    }

    private String getCategoryFromLine(DocumentLine line) {
        if (line.getItem() == null) return "program";

        one.modality.base.shared.entities.ItemFamily family = line.getItem().getFamily();
        if (family == null) return "program";

        String familyName = family.getName();
        String familyCode = family.getCode();
        String search = (familyName != null ? familyName : familyCode);
        if (search == null) return "program";

        search = search.toLowerCase();

        if (search.contains("accommodation") || search.contains("room") || search.contains("bed")) {
            return "accommodation";
        } else if (search.contains("meal") || search.contains("breakfast") || search.contains("lunch") || search.contains("dinner")) {
            return "meals";
        } else if (search.contains("diet") || search.contains("vegetarian") || search.contains("vegan")) {
            return "diet";
        } else if (search.contains("transport") || search.contains("shuttle") || search.contains("bus")) {
            return "transport";
        } else if (search.contains("parking") || search.contains("car")) {
            return "parking";
        }

        return "program";
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null && end == null) return "No dates";
        if (start == null) return "Until " + formatDate(end);
        if (end == null) return "From " + formatDate(start);
        return formatDate(start) + " - " + formatDate(end);
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return date.getDayOfMonth() + " " + date.getMonth().toString().substring(0, 3);
    }

    private String formatPrice(int amount) {
        return "\u00A3" + String.format("%,d", amount); // £ symbol
    }

    // Note: Timeline canvas updates automatically via property binding
    // (timelineCanvas.bookingStartProperty().bind(arrivalDateProperty))
    // No need for manual updateTimelineCanvas() method

    /**
     * Sets up the reactive lines mapper.
     */
    public void setupLinesMapper() {
        if (linesMapper == null && documentIdProperty.get() != null) {
            // Create reactive mapper that stores entities directly into loadedLines
            // Uses proper parameter binding via .always(property, fn) pattern (like HouseholdGanttDataLoader)
            linesMapper = ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(activity)
                // DocumentLine fields - use fields: with parentheses for nested entities (like HouseholdGanttDataLoader)
                .always("{class: 'DocumentLine', alias: 'dl', fields: 'site,item.(name,ord,family.(name,code,ord)),startDate,endDate,resourceConfiguration.name,price_net,cancelled', orderBy: 'item.family.ord,item.ord'}")
                // Filter by document using the primary key value (not entity) to avoid serialization issues
                .always(documentIdProperty, docId -> where("document=?", docId))
                .storeEntitiesInto(loadedLines)
                .start();

            // Update UI when loadedLines changes
            ObservableLists.runNowAndOnListChange(change -> refreshLineItems(), loadedLines);
        }
    }

    /**
     * Sets the active state of the tab.
     */
    public void setActive(boolean active) {
        activeProperty.set(active);
        if (active) {
            setupLinesMapper();
        }
        // Note: Don't call linesMapper.getReactiveDqlQuery().setActive() manually
        // because when using createPushReactiveChain(activity), the active state
        // is bound to the activity's active property automatically
    }

    /**
     * Refreshes the lines mapper (triggers a new query).
     */
    public void refresh() {
        if (linesMapper != null) {
            linesMapper.refreshWhenActive();
        }
    }

    /**
     * Gets the active property.
     */
    public BooleanProperty activeProperty() {
        return activeProperty;
    }
}
