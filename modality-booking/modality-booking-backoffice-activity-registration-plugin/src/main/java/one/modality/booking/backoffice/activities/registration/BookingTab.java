package one.modality.booking.backoffice.activities.registration;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
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
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.History;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ItemFamily;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Site;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.crm.shared.services.authn.fx.FXUserName;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.policy.service.PolicyAggregate;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    // WorkingBooking manages the booking state using event sourcing
    private WorkingBooking workingBooking;

    // Observable property to track WorkingBooking changes (bound when workingBooking is loaded)
    private final BooleanProperty hasChangesProperty = new SimpleBooleanProperty(false);

    // Document lines from WorkingBooking (kept in sync for UI binding)
    private final ObservableList<DocumentLine> loadedLines = FXCollections.observableArrayList();

    // Attendance dates grouped by DocumentLine ID for quick lookup (built from WorkingBooking)
    private Map<Object, Set<LocalDate>> attendanceDatesByLineId = new HashMap<>();

    // UI Components
    private BookingTimelineCanvas timelineCanvas;
    private AddOptionPanel addOptionPanel;
    private VBox lineItemsList;

    // Price summary labels (for reactive updates)
    private Label subtotalAmountLabel;
    private Label paidAmountLabel;
    private Label balanceAmountLabel;
    private VBox balanceBox;

    // Booking dates (may differ from document dates if user is editing)
    private final ObjectProperty<LocalDate> arrivalDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> departureDateProperty = new SimpleObjectProperty<>();

    // Loading state
    private boolean isLoading = false;

    public BookingTab(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Document document, UpdateStore updateStore) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;
        this.updateStore = updateStore;

        // Initialize dates from document event
        Event event = document.getEvent();
        if (event != null) {
            arrivalDateProperty.set(event.getStartDate());
            departureDateProperty.set(event.getEndDate());
        }
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
            // Update from WorkingBooking when a new option is added
            // This reloads document lines and attendance from WorkingBooking's aggregate
            updateFromWorkingBooking();
            // Also update hasChangesProperty since we added something
            hasChangesProperty.set(true);
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
     * The canvas expands to show all booking options with gantt bars.
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

        // Handle edit/cancel/delete/restore action button clicks
        timelineCanvas.setOnEditClicked(this::handleEditClicked);
        timelineCanvas.setOnCancelClicked(this::handleCancelClicked);
        timelineCanvas.setOnDeleteClicked(this::handleDeleteClicked);
        timelineCanvas.setOnRestoreClicked(this::handleRestoreClicked);

        // Add canvas directly - it computes its own size based on event dates and lines
        section.getChildren().add(timelineCanvas);

        return section;
    }

    /**
     * Handles when a day is toggled on/off in the timeline.
     * Uses WorkingBooking's event-sourcing pattern for attendance changes.
     */
    private void handleDayToggled(DocumentLine line, LocalDate date) {
        if (workingBooking == null) {
            System.err.println("Cannot toggle attendance: WorkingBooking not loaded");
            return;
        }

        // Use Entities.getPrimaryKey consistently everywhere for key comparisons
        Object lineId = Entities.getPrimaryKey(line);
        if (lineId == null) {
            System.err.println("Cannot toggle attendance: DocumentLine has no ID");
            return;
        }

        // Check current attendance state for this date using the map keys
        Set<LocalDate> currentAttendanceDates = null;
        for (Map.Entry<Object, Set<LocalDate>> entry : attendanceDatesByLineId.entrySet()) {
            if (Entities.samePrimaryKey(entry.getKey(), lineId)) {
                currentAttendanceDates = entry.getValue();
                break;
            }
        }
        if (currentAttendanceDates == null) {
            currentAttendanceDates = new HashSet<>();
        }
        boolean hasAttendance = currentAttendanceDates.contains(date);

        System.out.println("=== DEBUG: handleDayToggled ===");
        System.out.println("  Line ID: " + lineId + " (type: " + (lineId != null ? lineId.getClass().getSimpleName() : "null") + ")");
        System.out.println("  Date: " + date);
        System.out.println("  Map keys: " + attendanceDatesByLineId.keySet());
        System.out.println("  Currently has attendance: " + hasAttendance);

        if (hasAttendance) {
            // Remove the attendance
            removeAttendance(line, date);
        } else {
            // Add attendance
            addAttendance(line, date);
        }

        // Track the change for history
        String itemName = line.getItem() != null ? line.getItem().getName() : "Option";
        String siteName = line.getSite() != null ? line.getSite().getName() : null;
        String optionDescription = siteName != null ? itemName + " (" + siteName + ")" : itemName;
        String formattedDate = date.getDayOfMonth() + " " + date.getMonth().toString().substring(0, 3) + " " + date.getYear();
        trackAttendanceChange((hasAttendance ? "Removed" : "Added") + " date " + formattedDate + " for " + optionDescription);
    }

    /**
     * Adds an attendance record for the specified line and date using WorkingBooking.bookScheduledItems().
     */
    private void addAttendance(DocumentLine line, LocalDate date) {
        if (workingBooking == null) return;

        Object lineId = Entities.getPrimaryKey(line);

        // Find the ScheduledItem matching this date and item from PolicyAggregate
        ScheduledItem scheduledItem = findScheduledItemForDateAndItem(date, line);
        if (scheduledItem == null) {
            System.err.println("  Cannot add attendance: No ScheduledItem found for date " + date + " and item " + line.getItem());
            return;
        }

        // Use WorkingBooking.bookScheduledItems with addOnly=true to add attendance
        workingBooking.bookScheduledItems(java.util.Collections.singletonList(scheduledItem), true);

        // Manually update hasChangesProperty at UX level for immediate UI feedback
        // (the WorkingBooking's deferred property may not update immediately)
        hasChangesProperty.unbind();
        hasChangesProperty.set(true);

        System.out.println("  Added attendance via WorkingBooking.bookScheduledItems for date: " + date + " with ScheduledItem: " + scheduledItem.getId());

        // Update local map for immediate UI feedback - find matching key using Entities.samePrimaryKey
        if (lineId != null) {
            Object matchingKey = null;
            for (Object key : attendanceDatesByLineId.keySet()) {
                if (Entities.samePrimaryKey(key, lineId)) {
                    matchingKey = key;
                    break;
                }
            }
            if (matchingKey != null) {
                attendanceDatesByLineId.get(matchingKey).add(date);
            } else {
                attendanceDatesByLineId.computeIfAbsent(lineId, k -> new HashSet<>()).add(date);
            }
        }

        // Refresh the canvas and recalculate prices
        if (timelineCanvas != null) {
            timelineCanvas.setAttendanceDates(attendanceDatesByLineId);
        }
        recalculateLinePrices();
    }

    /**
     * Removes an attendance record for the specified line and date using WorkingBooking.unbookScheduledItems().
     */
    private void removeAttendance(DocumentLine line, LocalDate date) {
        if (workingBooking == null) return;

        Object lineId = Entities.getPrimaryKey(line);

        // Find the ScheduledItem matching this date and item from PolicyAggregate
        ScheduledItem scheduledItem = findScheduledItemForDateAndItem(date, line);
        if (scheduledItem == null) {
            System.err.println("  Cannot remove attendance: No ScheduledItem found for date " + date + " and item " + line.getItem());
            return;
        }

        // Use WorkingBooking.unbookScheduledItems to remove the attendance
        workingBooking.unbookScheduledItems(java.util.Collections.singletonList(scheduledItem));

        // Manually update hasChangesProperty at UX level for immediate UI feedback
        // (the WorkingBooking's deferred property may not update immediately)
        hasChangesProperty.unbind();
        hasChangesProperty.set(true);

        System.out.println("  Removed attendance via WorkingBooking.unbookScheduledItems for date: " + date);

        // Update local map for immediate UI feedback - find matching key using Entities.samePrimaryKey
        if (lineId != null) {
            for (Map.Entry<Object, Set<LocalDate>> entry : attendanceDatesByLineId.entrySet()) {
                if (Entities.samePrimaryKey(entry.getKey(), lineId)) {
                    entry.getValue().remove(date);
                    break;
                }
            }
        }

        // Refresh the canvas and recalculate prices
        if (timelineCanvas != null) {
            timelineCanvas.setAttendanceDates(attendanceDatesByLineId);
        }
        recalculateLinePrices();
    }

    /**
     * Finds a ScheduledItem from PolicyAggregate matching the given date and item.
     * Delegates to WorkingBooking which has access to PolicyAggregate.
     */
    private ScheduledItem findScheduledItemForDateAndItem(LocalDate date, DocumentLine line) {
        if (workingBooking == null) {
            return null;
        }
        return workingBooking.findScheduledItem(date, line.getItem(), line.getSite());
    }

    /**
     * Recalculates line prices based on current attendance state.
     * Updates the canvas with computed prices after attendance changes.
     * Sums the daily rate for each attendance date from PolicyAggregate.
     */
    private void recalculateLinePrices() {
        if (workingBooking == null || timelineCanvas == null) {
            return;
        }

        // Get PolicyAggregate for rate lookup
        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();
        if (policyAggregate == null) {
            return;
        }

        // Calculate prices for each loaded line based on current attendance dates
        Map<Object, Integer> computedPrices = new HashMap<>();
        for (DocumentLine line : loadedLines) {
            Object lineId = Entities.getPrimaryKey(line);
            if (lineId != null) {
                // Get current attendance dates for this line from UI state
                Set<LocalDate> dates = getAttendanceDatesForLine(lineId);

                // Sum the daily rate for each date
                int totalPrice = 0;
                for (LocalDate date : dates) {
                    int dailyPrice = getDailyRateForLineAndDate(policyAggregate, line, date);
                    totalPrice += dailyPrice;
                }
                computedPrices.put(lineId, totalPrice);
            }
        }

        // Update canvas with computed prices
        timelineCanvas.setComputedPrices(computedPrices);

        // Also refresh the line items list UI if needed
        refreshLineItemsList();

        // Refresh the price summary to show updated totals
        refreshPriceSummary();
    }

    /**
     * Gets attendance dates for a line from the UI state map.
     */
    private Set<LocalDate> getAttendanceDatesForLine(Object lineId) {
        for (Map.Entry<Object, Set<LocalDate>> entry : attendanceDatesByLineId.entrySet()) {
            if (Entities.samePrimaryKey(entry.getKey(), lineId)) {
                return entry.getValue();
            }
        }
        return Collections.emptySet();
    }

    /**
     * Gets the daily rate price for a line and date from PolicyAggregate.
     * Uses the item/site to find the applicable rate.
     */
    private int getDailyRateForLineAndDate(PolicyAggregate policyAggregate, DocumentLine line, LocalDate date) {
        Site site = line.getSite();
        Item item = line.getItem();

        // Get daily rates for this site/item combination
        return policyAggregate.filterDailyRatesStreamOfSiteAndItem(site, item)
            .filter(rate -> isRateApplicableForDate(rate, date))
            .mapToInt(rate -> rate.getPrice() != null ? rate.getPrice() : 0)
            .min()
            .orElse(0);
    }

    /**
     * Checks if a rate is applicable for a specific date.
     * Considers startDate, endDate, and onDate fields.
     */
    private boolean isRateApplicableForDate(Rate rate, LocalDate date) {
        LocalDate onDate = rate.getOnDate();
        if (onDate != null) {
            // If onDate is set, rate only applies to that specific date
            return date.equals(onDate);
        }

        // Otherwise check if date is within startDate-endDate range
        LocalDate startDate = rate.getStartDate();
        LocalDate endDate = rate.getEndDate();

        boolean afterStart = startDate == null || !date.isBefore(startDate);
        boolean beforeEnd = endDate == null || !date.isAfter(endDate);

        return afterStart && beforeEnd;
    }

    /**
     * Refreshes the line items list UI with current prices.
     */
    private void refreshLineItemsList() {
        // The line items list observes loadedLines - trigger a refresh
        if (lineItemsList != null) {
            // Clear and rebuild the non-temporal items section
            buildNonTemporalItemsSection();
        }
    }

    /**
     * Rebuilds the non-temporal items section with current prices.
     */
    private void buildNonTemporalItemsSection() {
        // This will be called to refresh prices in the ONE-TIME OPTIONS section
        // The actual rebuild happens in refreshLineItems() which is called when loadedLines changes
    }

    // Track attendance changes for History record
    private final List<String> pendingAttendanceChanges = new ArrayList<>();

    /**
     * Tracks an attendance change comment for the History record.
     */
    private void trackAttendanceChange(String changeDescription) {
        pendingAttendanceChanges.add(changeDescription);
    }

    /**
     * Gets the pending attendance changes description for the History record.
     */
    public String getPendingAttendanceChangesDescription() {
        if (pendingAttendanceChanges.isEmpty()) {
            return null;
        }
        return String.join("; ", pendingAttendanceChanges);
    }

    /**
     * Clears the pending attendance changes after saving.
     * Also resets the hasChangesProperty based on WorkingBooking's actual state.
     */
    public void clearPendingAttendanceChanges() {
        pendingAttendanceChanges.clear();
        // Also clear any pending cancel/delete statuses on the canvas
        if (timelineCanvas != null) {
            timelineCanvas.clearPendingStatuses();
            // After save, current attendance dates become the new "original" dates
            // This ensures gaps (removed dates) show as empty, not with X
            timelineCanvas.setOriginalAttendanceDates(attendanceDatesByLineId);
        }
        // Reset hasChangesProperty based on actual WorkingBooking state
        // (after save, WorkingBooking should have no pending changes)
        if (workingBooking != null) {
            hasChangesProperty.set(workingBooking.hasChanges());
        } else {
            hasChangesProperty.set(false);
        }
    }

    // ===== Edit/Cancel/Delete/Restore Handlers for Timeline Canvas =====

    /**
     * Handles when the edit button is clicked on a line in the timeline.
     * Opens the EditLineModal to edit the document line using WorkingBooking API.
     */
    private void handleEditClicked(DocumentLine line) {
        if (workingBooking == null) {
            System.err.println("Cannot edit line: WorkingBooking not loaded");
            return;
        }

        // Get the event for currency formatting
        Event event = document.getEvent();

        // Create the EditLineModal with WorkingBooking support
        EditLineModal modal = new EditLineModal(
            line,
            workingBooking,
            event,
            () -> {
                // On save - refresh UI from WorkingBooking
                updateFromWorkingBooking();
                hasChangesProperty.unbind();
                hasChangesProperty.set(true);
            },
            () -> {
                // On cancel - no action needed
            }
        );
        modal.show();
    }

    /**
     * Handles when the cancel button is clicked on a line in the timeline.
     * Cancels the line via WorkingBooking API and shows visual indicator.
     */
    private void handleCancelClicked(DocumentLine line) {
        // Show confirmation modal before cancelling (same as non-temporal options)
        ConfirmActionModal.showCancelConfirmation(line, comment -> {
            // Call WorkingBooking API to cancel the line
            if (workingBooking != null) {
                workingBooking.cancelDocumentLine(line);
            }

            // Set visual pending status on canvas
            if (timelineCanvas != null) {
                timelineCanvas.setPendingStatus(line, "cancelled");
            }

            // Update changes property
            hasChangesProperty.unbind();
            hasChangesProperty.set(true);

            // Track the change for history
            String itemName = line.getItem() != null ? line.getItem().getName() : "Option";
            String historyText = "Cancelled: " + itemName;
            if (comment != null && !comment.isEmpty()) {
                historyText += " - " + comment;
            }
            trackAttendanceChange(historyText);
        });
    }

    /**
     * Handles when the delete button is clicked on a line in the timeline.
     * Removes the line via WorkingBooking API and shows visual indicator.
     * Note: Deletion cannot be undone within the same session.
     */
    private void handleDeleteClicked(DocumentLine line) {
        // Show confirmation modal before deleting (same as non-temporal options)
        ConfirmActionModal.showDeleteConfirmation(line, comment -> {
            // Call WorkingBooking API to remove the line (hard delete)
            if (workingBooking != null) {
                workingBooking.removeDocumentLine(line);
            }

            // Set visual pending status on canvas
            if (timelineCanvas != null) {
                timelineCanvas.setPendingStatus(line, "deleted");
            }

            // Update changes property
            hasChangesProperty.unbind();
            hasChangesProperty.set(true);

            // Track the change for history
            String itemName = line.getItem() != null ? line.getItem().getName() : "Option";
            String historyText = "Removed: " + itemName;
            if (comment != null && !comment.isEmpty()) {
                historyText += " - " + comment;
            }
            trackAttendanceChange(historyText);
        });
    }

    /**
     * Handles when the restore button is clicked on a line with pending status.
     * For cancelled lines: uncancels via WorkingBooking API.
     * For deleted lines: cannot be restored (deletion is permanent in current session).
     */
    private void handleRestoreClicked(DocumentLine line) {
        if (timelineCanvas == null) return;

        // Get the current pending status
        String pendingStatus = timelineCanvas.getPendingStatus(line);
        String itemName = line.getItem() != null ? line.getItem().getName() : "Option";

        if ("cancelled".equals(pendingStatus)) {
            // Show confirmation modal before restoring (same as non-temporal options)
            ConfirmActionModal.showRestoreConfirmation(line, comment -> {
                // Uncancel via WorkingBooking API
                if (workingBooking != null) {
                    workingBooking.uncancelDocumentLine(line);
                }
                timelineCanvas.setPendingStatus(line, null);

                String historyText = "Restored from cancellation: " + itemName;
                if (comment != null && !comment.isEmpty()) {
                    historyText += " - " + comment;
                }
                trackAttendanceChange(historyText);

                // Check if there are still changes
                boolean stillHasChanges = !timelineCanvas.getPendingStatuses().isEmpty() ||
                    (workingBooking != null && workingBooking.hasChanges()) ||
                    !pendingAttendanceChanges.isEmpty();
                hasChangesProperty.set(stillHasChanges);
            });
        } else if ("deleted".equals(pendingStatus)) {
            // Deletion cannot be undone - show message to user
            System.out.println("Cannot restore deleted line: " + itemName + " - deletion is permanent");
            // Keep the pending status since we can't undo the delete
            // The user will need to close without saving to undo the delete
        }
    }

    /**
     * Gets the WorkingBooking instance for this tab.
     * Used by the modal for submitting changes.
     */
    public WorkingBooking getWorkingBooking() {
        return workingBooking;
    }

    /**
     * Checks if there are any unsaved changes in the WorkingBooking or pending cancel/delete statuses.
     */
    public boolean hasChanges() {
        // Check WorkingBooking changes
        if (workingBooking != null && workingBooking.hasChanges()) {
            return true;
        }
        // Check pending cancel/delete statuses on the canvas
        if (timelineCanvas != null && !timelineCanvas.getPendingStatuses().isEmpty()) {
            return true;
        }
        // Check pending attendance changes
        if (!pendingAttendanceChanges.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Returns an observable property indicating if there are unsaved changes.
     * This property is bound to WorkingBooking's hasChangesProperty when it's loaded.
     */
    public javafx.beans.value.ObservableValue<Boolean> hasChangesProperty() {
        return hasChangesProperty;
    }

    /**
     * Applies pending cancel/delete statuses to the given UpdateStore.
     * This should be called before saving to persist the visual pending states.
     * @param updateStore the UpdateStore to apply changes to
     * @return true if any changes were applied
     */
    public boolean applyPendingStatusesToUpdateStore(dev.webfx.stack.orm.entity.UpdateStore updateStore) {
        if (timelineCanvas == null) {
            return false;
        }

        Map<Object, String> pendingStatuses = timelineCanvas.getPendingStatuses();
        if (pendingStatuses.isEmpty()) {
            return false;
        }

        boolean changesApplied = false;

        // Find the DocumentLines and apply the pending statuses
        for (DocumentLine line : loadedLines) {
            Object lineId = dev.webfx.stack.orm.entity.Entities.getPrimaryKey(line);
            if (lineId == null) continue;

            String status = pendingStatuses.get(lineId);
            if (status == null) continue;

            // Get an editable version of the line from the UpdateStore
            DocumentLine editableLine = updateStore.updateEntity(line);

            if ("cancelled".equals(status)) {
                // Mark the line as cancelled
                editableLine.setCancelled(true);
                changesApplied = true;
                System.out.println("Applied cancellation to DocumentLine: " + lineId);
            } else if ("deleted".equals(status)) {
                // Mark the line as removed (soft delete)
                editableLine.setFieldValue("removed", true);
                changesApplied = true;
                System.out.println("Applied removal to DocumentLine: " + lineId);
            }
        }

        return changesApplied;
    }

    /**
     * Checks if there are pending cancel/delete statuses.
     */
    public boolean hasPendingStatuses() {
        return timelineCanvas != null && !timelineCanvas.getPendingStatuses().isEmpty();
    }

    /**
     * Calculates excluded days for a line (days in range but not in attendance).
     */
    private Set<LocalDate> calculateExcludedDays(DocumentLine line, Set<LocalDate> attendanceDates) {
        Set<LocalDate> excludedDays = new HashSet<>();
        LocalDate lineStart = line.getStartDate();
        LocalDate lineEnd = line.getEndDate();

        if (lineStart == null) lineStart = arrivalDateProperty.get();
        if (lineEnd == null) lineEnd = departureDateProperty.get();
        if (lineStart == null || lineEnd == null || attendanceDates.isEmpty()) {
            return excludedDays;
        }

        // Any day in the line's range that's NOT in attendance is excluded
        LocalDate current = lineStart;
        while (!current.isAfter(lineEnd)) {
            if (!attendanceDates.contains(current)) {
                excludedDays.add(current);
            }
            current = current.plusDays(1);
        }
        return excludedDays;
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

            // Recalculate price based on attendance (excluded days = days in range but not in attendance)
            Object lineId = line.getId() != null ? line.getId().getPrimaryKey() : null;
            Set<LocalDate> attendanceDates = lineId != null ? attendanceDatesByLineId.getOrDefault(lineId, Collections.emptySet()) : Collections.emptySet();
            Set<LocalDate> excludedDays = calculateExcludedDays(editableLine, attendanceDates);
            recalculateLinePrice(editableLine, excludedDays);
        }

        // Update timeline canvas and line items display
        if (timelineCanvas != null) {
            timelineCanvas.getDocumentLines().setAll(loadedLines);
        }
        refreshLineItems();
    }

    /**
     * Checks if a category is temporal (date-based, shown in canvas).
     * Temporal items: accommodation, meals, diet, program, and unknown items with dates.
     * Non-temporal items are shown in the list section below the canvas.
     */
    private boolean isTemporalCategory(String category) {
        return "accommodation".equals(category) ||
               "meals".equals(category) ||
               "diet".equals(category) ||
               "program".equals(category) ||
               "other_temporal".equals(category);
    }

    /**
     * Creates the line items section for non-temporal items only.
     * Temporal items (accommodation, meals, diet, program) are shown in the canvas above.
     * Non-temporal items (course fees, transport, services) are shown here in a compact list.
     */
    private Node createLineItemsSection() {
        VBox section = new VBox(8);

        // Divider before non-temporal section (styled like JSX borderTop)
        Region divider = new Region();
        divider.setMinHeight(1);
        divider.setMaxHeight(1);
        divider.setBackground(new Background(new BackgroundFill(BORDER, null, null)));
        VBox.setMargin(divider, new Insets(4, 0, 12, 0));

        // Header - styled like JSX: fontSize 10px, uppercase, muted color
        Label titleLabel = new Label("ONE-TIME OPTIONS");
        titleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        titleLabel.setTextFill(TEXT_MUTED);
        VBox.setMargin(titleLabel, new Insets(0, 0, 8, 0));

        // Line items list - compact styling with 4px gap
        lineItemsList = new VBox(4);

        section.getChildren().addAll(divider, titleLabel, lineItemsList);

        // Hide section when empty (will be shown when non-temporal items are added)
        section.managedProperty().bind(section.visibleProperty());
        ObservableLists.runNowAndOnListChange(change -> {
            boolean hasNonTemporalItems = loadedLines.stream()
                .filter(line -> !Boolean.TRUE.equals(line.getFieldValue("removed")))
                .anyMatch(line -> !isTemporalCategory(getCategoryFromLine(line)));
            section.setVisible(hasNonTemporalItems);
        }, loadedLines);

        return section;
    }

    /**
     * Refreshes the line items list from loaded data.
     * Note: Temporal items (accommodation, meals, diet, program) are shown in the canvas only,
     * not repeated in the line items list below.
     */
    private void refreshLineItems() {
        if (lineItemsList == null) return;

        // DEBUG: Log loaded lines
        System.out.println("=== DEBUG: refreshLineItems() called ===");
        System.out.println("Total loadedLines count: " + loadedLines.size());
        for (DocumentLine line : loadedLines) {
            String itemName = line.getItem() != null ? line.getItem().getName() : "null";
            String familyName = (line.getItem() != null && line.getItem().getFamily() != null)
                ? line.getItem().getFamily().getName() : "null";
            String category = getCategoryFromLine(line);
            System.out.println("  - Line: " + itemName +
                " | Family: " + familyName +
                " | Category: " + category +
                " | StartDate: " + line.getStartDate() +
                " | EndDate: " + line.getEndDate() +
                " | Cancelled: " + line.isCancelled() +
                " | Removed: " + line.getFieldValue("removed"));
        }
        System.out.println("========================================");

        lineItemsList.getChildren().clear();

        for (DocumentLine line : loadedLines) {
            // Skip removed lines
            if (Boolean.TRUE.equals(line.getFieldValue("removed"))) continue;

            // Skip temporal items - they're shown in the canvas, not the list below
            String category = getCategoryFromLine(line);
            if (isTemporalCategory(category)) continue;

            Node card = createLineItemCard(line);
            lineItemsList.getChildren().add(card);
        }

        // Update timeline canvas with ONLY temporal lines (non-temporal items shown in list below)
        if (timelineCanvas != null) {
            List<DocumentLine> temporalLines = new ArrayList<>();
            for (DocumentLine line : loadedLines) {
                String cat = getCategoryFromLine(line);
                if (isTemporalCategory(cat)) {
                    temporalLines.add(line);
                }
            }
            timelineCanvas.getDocumentLines().setAll(temporalLines);
        }

        // Update add option panel with existing lines
        if (addOptionPanel != null) {
            addOptionPanel.setExistingLines(new ArrayList<>(loadedLines));
        }
    }

    /**
     * Creates a compact line item row for non-temporal options.
     * Styled like JSX: icon | name | price (right-aligned) | action buttons
     */
    private Node createLineItemCard(DocumentLine line) {
        boolean isCancelled = Boolean.TRUE.equals(line.isCancelled());
        boolean isDeleted = Boolean.TRUE.equals(line.getFieldValue("removed"));
        boolean isInactive = isCancelled || isDeleted;

        // Container with relative positioning for status badge
        StackPane container = new StackPane();
        container.setAlignment(Pos.TOP_LEFT);

        // Main row - compact padding like JSX (6px 8px)
        HBox row = new HBox(8);
        row.setPadding(new Insets(6, 8, 6, 8));
        row.setBackground(createBackground(BG, BORDER_RADIUS_SMALL));
        row.setAlignment(Pos.CENTER_LEFT);

        // Apply inactive styling
        if (isInactive) {
            row.setOpacity(0.6);
            StackPane.setMargin(row, new Insets(6, 0, 0, 0)); // Space for status badge
        }

        // Get category from item family
        String category = getCategoryFromLine(line);
        Color categoryColor = getCategoryColor(category);

        // Category icon - small 20x20 with category background
        StackPane icon = new StackPane();
        icon.setMinSize(20, 20);
        icon.setMaxSize(20, 20);
        icon.setBackground(createBackground(categoryColor.deriveColor(0, 0.3, 1.2, 0.3), 4));

        Label iconLabel = new Label(getCategoryIconChar(category));
        iconLabel.setFont(Font.font("System", 10));
        iconLabel.setTextFill(categoryColor);
        icon.getChildren().add(iconLabel);

        // Item name - flex 1
        String itemName = line.getItem() != null ? line.getItem().getName() : "Unknown Item";
        Label nameLabel = new Label(itemName);
        nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 11));
        nameLabel.setTextFill(TEXT);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Apply strikethrough for inactive items
        if (isInactive) {
            nameLabel.setStyle("-fx-strikethrough: true;");
        }

        // Price - right-aligned, bold
        Integer price = line.getPriceNet();
        Label priceLabel = new Label(formatPrice(price != null ? price : 0));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        priceLabel.setTextFill(isInactive ? TEXT_MUTED : WARM_BROWN);
        priceLabel.setMinWidth(60);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);

        // Apply strikethrough to price for inactive items
        if (isInactive) {
            priceLabel.setStyle("-fx-strikethrough: true;");
        }

        // Action buttons - compact
        HBox actionButtons = new HBox(2);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        if (isInactive) {
            // Restore button for inactive items
            Button restoreBtn = createCompactActionButton("\u21A9", SUCCESS, () -> handleRestoreLine(line)); // ↩
            restoreBtn.setOpacity(0.6);
            actionButtons.getChildren().add(restoreBtn);
        } else {
            // Cancel and Delete buttons for active items
            Button cancelBtn = createCompactActionButton("\u2298", WARNING, () -> handleCancelLine(line)); // ⊘
            Button deleteBtn = createCompactActionButton("\u2715", DANGER, () -> handleDeleteLine(line)); // ✕
            cancelBtn.setOpacity(0.5);
            deleteBtn.setOpacity(0.5);
            actionButtons.getChildren().addAll(cancelBtn, deleteBtn);
        }

        row.getChildren().addAll(icon, nameLabel, priceLabel, actionButtons);
        container.getChildren().add(row);

        // Status badge for inactive items (positioned at top-left)
        if (isInactive) {
            Label statusBadge = new Label(isCancelled ? "CANCELLED" : "REMOVED");
            statusBadge.setFont(Font.font("System", FontWeight.BOLD, 7));
            statusBadge.setTextFill(isCancelled ? DANGER : TEXT_MUTED);
            statusBadge.setBackground(createBackground(isCancelled ? RED_LIGHT : SAND, 3));
            statusBadge.setBorder(createBorder(isCancelled ? DANGER.deriveColor(0, 1, 1, 0.3) : BORDER, 3));
            statusBadge.setPadding(new Insets(1, 5, 1, 5));
            StackPane.setAlignment(statusBadge, Pos.TOP_LEFT);
            StackPane.setMargin(statusBadge, new Insets(-3, 0, 0, 0));
            container.getChildren().add(statusBadge);
        }

        return container;
    }

    /**
     * Creates a compact action button for line items.
     */
    private Button createCompactActionButton(String symbol, Color color, Runnable action) {
        Button btn = new Button(symbol);
        btn.setFont(Font.font("System", 12));
        btn.setTextFill(color);
        btn.setBackground(Background.EMPTY);
        btn.setBorder(Border.EMPTY);
        btn.setCursor(Cursor.HAND);
        btn.setPadding(new Insets(3));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    /**
     * Gets the color for a category.
     */
    private Color getCategoryColor(String category) {
        switch (category) {
            // Temporal categories
            case "accommodation": return Color.web("#059669"); // Green
            case "meals": return Color.web("#d97706"); // Orange
            case "diet": return Color.web("#7c3aed"); // Purple
            case "program": return Color.web("#be185d"); // Pink
            // Non-temporal categories
            case "transport": return Color.web("#0284c7"); // Blue
            case "parking": return Color.web("#64748b"); // Slate
            case "tax": return Color.web("#dc2626"); // Red
            case "services": return Color.web("#0891b2"); // Cyan
            case "recording": return Color.web("#4f46e5"); // Indigo
            case "course": return Color.web("#16a34a"); // Green
            default: return Color.web("#6b7280"); // Gray
        }
    }

    /**
     * Gets a simple icon character for a category.
     */
    private String getCategoryIconChar(String category) {
        switch (category) {
            // Temporal categories
            case "accommodation": return "\u2302"; // ⌂ House
            case "meals": return "\u2615"; // ☕ Hot beverage
            case "diet": return "\u2618"; // ☘ Shamrock (plant-based)
            case "program": return "\u2605"; // ★ Star
            // Non-temporal categories
            case "transport": return "\u2708"; // ✈ Airplane
            case "parking": return "\u24C5"; // Ⓟ Parking
            case "tax": return "\u00A3"; // £ Pound sign
            case "services": return "\u2606"; // ☆ White star
            case "recording": return "\u266A"; // ♪ Music note
            case "course": return "\u2709"; // ✉ Envelope (for course materials)
            default: return "\u25CF"; // ● Circle
        }
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

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Subtotal
        VBox subtotalBox = new VBox(2);
        subtotalBox.setAlignment(Pos.CENTER);
        subtotalBox.setMinWidth(72);
        subtotalBox.setBackground(createBackground(CREAM, BORDER_RADIUS_SMALL));
        subtotalBox.setPadding(new Insets(6, 14, 6, 14));

        Label subtotalLabel = new Label("SUBTOTAL");
        subtotalLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 9));
        subtotalLabel.setTextFill(WARM_BROWN.deriveColor(0, 1, 0.7, 1));

        subtotalAmountLabel = new Label();
        subtotalAmountLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        subtotalAmountLabel.setTextFill(WARM_BROWN);

        subtotalBox.getChildren().addAll(subtotalLabel, subtotalAmountLabel);

        // Paid
        VBox paidBox = new VBox(2);
        paidBox.setAlignment(Pos.CENTER);
        paidBox.setMinWidth(72);
        paidBox.setBackground(createBackground(SUCCESS_BG, BORDER_RADIUS_SMALL));
        paidBox.setPadding(new Insets(6, 14, 6, 14));

        Label paidLabel = new Label("PAID");
        paidLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 9));
        paidLabel.setTextFill(SUCCESS.deriveColor(0, 1, 0.7, 1));

        paidAmountLabel = new Label();
        paidAmountLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        paidAmountLabel.setTextFill(SUCCESS);

        paidBox.getChildren().addAll(paidLabel, paidAmountLabel);

        // Balance
        balanceBox = new VBox(2);
        balanceBox.setAlignment(Pos.CENTER);
        balanceBox.setMinWidth(72);
        balanceBox.setPadding(new Insets(6, 14, 6, 14));

        Label balanceLabel = new Label("BALANCE");
        balanceLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 9));

        balanceAmountLabel = new Label();
        balanceAmountLabel.setFont(Font.font("System", FontWeight.BOLD, 15));

        balanceBox.getChildren().addAll(balanceLabel, balanceAmountLabel);

        section.getChildren().addAll(spacer, subtotalBox, paidBox, balanceBox);

        // Initialize prices
        refreshPriceSummary();

        return section;
    }

    /**
     * Refreshes the price summary labels with current values.
     * Called after options are added/removed or prices change.
     */
    private void refreshPriceSummary() {
        if (subtotalAmountLabel == null) return;

        int subtotal;
        int paid;

        // Use WorkingBooking to calculate current prices if available
        if (workingBooking != null) {
            subtotal = workingBooking.calculateTotal();
            paid = workingBooking.calculatePreviousBalance() > 0 ? 0 : -workingBooking.calculatePreviousBalance();
            // Fallback to document paid amount if available
            Integer priceDeposit = document.getPriceDeposit();
            if (priceDeposit != null && priceDeposit > 0) {
                paid = priceDeposit;
            }
        } else {
            // Fallback to document values
            Integer priceNet = document.getPriceNet();
            Integer priceDeposit = document.getPriceDeposit();
            subtotal = priceNet != null ? priceNet : 0;
            paid = priceDeposit != null ? priceDeposit : 0;
        }

        int balance = subtotal - paid;

        subtotalAmountLabel.setText(formatPrice(subtotal));
        paidAmountLabel.setText(formatPrice(paid));
        balanceAmountLabel.setText(formatPrice(balance));

        // Update balance box colors
        Color balanceColor = balance > 0 ? DANGER : SUCCESS;
        balanceAmountLabel.setTextFill(balanceColor);
        balanceBox.setBackground(createBackground(balance > 0 ? RED_LIGHT : SUCCESS_BG, BORDER_RADIUS_SMALL));

        // Update balance label color
        if (balanceBox.getChildren().size() > 0 && balanceBox.getChildren().get(0) instanceof Label) {
            ((Label) balanceBox.getChildren().get(0)).setTextFill(balanceColor.deriveColor(0, 1, 0.7, 1));
        }
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

            // Create History record
            String optionName = line.getSite() != null ? line.getSite().getName() : "Option";
            History history = updateStore.insertEntity(History.class);
            history.setDocument(document);
            history.setUsername(FXUserName.getUserName());
            history.setComment("Option cancelled: " + optionName + (comment != null && !comment.isEmpty() ? " - " + comment : ""));

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

            // Create History record
            String optionName = line.getSite() != null ? line.getSite().getName() : "Option";
            History history = updateStore.insertEntity(History.class);
            history.setDocument(document);
            history.setUsername(FXUserName.getUserName());
            history.setComment("Option deleted: " + optionName + (comment != null && !comment.isEmpty() ? " - " + comment : ""));

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

            // Create History record
            String optionName = line.getSite() != null ? line.getSite().getName() : "Option";
            History history = updateStore.insertEntity(History.class);
            history.setDocument(document);
            history.setUsername(FXUserName.getUserName());
            history.setComment("Option restored: " + optionName + (comment != null && !comment.isEmpty() ? " - " + comment : ""));

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

    /**
     * Gets the category string for a DocumentLine based on its Item's KnownItemFamily.
     * Uses the proper KnownItemFamily enum instead of string matching.
     * Unknown items are always displayed with a generic "other" category (non-temporal)
     * to ensure all items are visible in the list below the canvas.
     */
    private String getCategoryFromLine(DocumentLine line) {
        if (line.getItem() == null) {
            // No item - always show in non-temporal list to ensure visibility
            return "other";
        }

        ItemFamily family = line.getItem().getFamily();
        if (family == null) {
            // No family - always show in non-temporal list to ensure visibility
            return "other";
        }

        // Use KnownItemFamily enum for proper categorization
        KnownItemFamily knownFamily = family.getItemFamilyType();
        if (knownFamily == null) {
            knownFamily = KnownItemFamily.UNKNOWN;
        }

        switch (knownFamily) {
            // Temporal categories (date-based, shown in canvas)
            case ACCOMMODATION:
                return "accommodation";
            case MEALS:
                return "meals";
            case DIET:
                return "diet";
            case TEACHING:
            case TRANSLATION:
            case VIDEO:
                return "program";

            // Non-temporal categories (one-time, shown in list below)
            case TRANSPORT:
                return "transport";
            case PARKING:
                return "parking";
            case TAX:
                return "tax";
            case AUDIO_RECORDING:
                return "recording";

            // Unknown family - default to non-temporal (shown in list below canvas)
            // This ensures all unknown items are always displayed and not hidden
            case UNKNOWN:
            default:
                return "other";
        }
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
        Event event = document.getEvent();
        String currencySymbol = EventPriceFormatter.getEventCurrencySymbol(event);
        // Use PriceFormatter with show00cents=true to always show 2 decimal places
        return PriceFormatter.formatWithCurrency(amount, currencySymbol, true);
    }

    // Note: Timeline canvas updates automatically via property binding
    // (timelineCanvas.bookingStartProperty().bind(arrivalDateProperty))
    // No need for manual updateTimelineCanvas() method

    /**
     * Loads the booking data using WorkingBooking (event-sourcing pattern).
     * This properly loads document lines and attendance from the database.
     */
    public void loadWorkingBooking() {
        if (isLoading || workingBooking != null) {
            return;
        }

        isLoading = true;
        System.out.println("=== Loading WorkingBooking for document: " + document.getPrimaryKey() + " ===");

        WorkingBooking.loadWorkingBooking(document)
            .onSuccess(wb -> {
                this.workingBooking = wb;
                isLoading = false;

                // Initialize hasChangesProperty to false (no changes yet)
                // Note: We manage this property manually at the UX level instead of binding
                // because WorkingBooking uses a deferred property that may not update immediately
                hasChangesProperty.set(false);

                // Pass WorkingBooking to AddOptionPanel for loading items from PolicyAggregate
                if (addOptionPanel != null) {
                    addOptionPanel.setWorkingBooking(wb);
                }

                // Update UI on FX thread
                javafx.application.Platform.runLater(() -> {
                    updateFromWorkingBooking();
                });
            })
            .onFailure(error -> {
                isLoading = false;
                System.err.println("Failed to load WorkingBooking: " + error.getMessage());
                error.printStackTrace();
            });
    }

    /**
     * Updates the UI from the loaded WorkingBooking data.
     * Also loads any additional document lines that may have been filtered out by the server
     * (e.g., rounding lines with site=null).
     */
    private void updateFromWorkingBooking() {
        if (workingBooking == null) return;

        // Set up the cancellation checker to use WorkingBooking's proper cancellation detection
        // This ensures cancelled lines are properly identified using the event-based API
        if (timelineCanvas != null) {
            timelineCanvas.setCancellationChecker(workingBooking::isDocumentLineCancelled);
        }

        DocumentAggregate aggregate = workingBooking.getLastestDocumentAggregate();

        // Get document lines from WorkingBooking aggregate
        List<DocumentLine> lines = new ArrayList<>(aggregate.getDocumentLines());

        System.out.println("=== DEBUG: updateFromWorkingBooking() ===");
        System.out.println("Loaded " + lines.size() + " document lines from WorkingBooking");

        // Load additional document lines that may have been filtered out (e.g., rounding with site=null)
        // The server-side query filters site!=null, but we need all lines for the backoffice view
        loadAdditionalDocumentLines(lines);
    }

    /**
     * Loads document lines that may have been filtered out by the server-side query.
     * Specifically loads lines with site=null (like rounding) and merges them with existing lines.
     */
    private void loadAdditionalDocumentLines(List<DocumentLine> existingLines) {
        if (document == null) {
            // No additional lines to load, just use existing
            loadedLines.setAll(existingLines);
            updateAttendanceDatesMap();
            refreshLineItems();
            recalculateLinePrices();
            return;
        }

        // Collect existing line IDs to avoid duplicates
        Set<Object> existingLineIds = new HashSet<>();
        for (DocumentLine line : existingLines) {
            Object pk = Entities.getPrimaryKey(line);
            if (pk != null) {
                existingLineIds.add(pk);
            }
        }

        // Query for document lines with site=null (filtered out by server query)
        // These include rounding and other system-generated lines
        Object docPk = document.getPrimaryKey();
        EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
            .<DocumentLine>executeQuery(
                "select document,item,item.family,price_net,price_minDeposit,price_custom,price_discount " +
                "from DocumentLine where document=? and site=null order by id",
                docPk)
            .onSuccess(additionalLines -> {
                javafx.application.Platform.runLater(() -> {
                    // Merge additional lines with existing ones (avoid duplicates)
                    List<DocumentLine> mergedLines = new ArrayList<>(existingLines);
                    int addedCount = 0;
                    for (DocumentLine line : additionalLines) {
                        Object pk = Entities.getPrimaryKey(line);
                        if (pk != null && !existingLineIds.contains(pk)) {
                            mergedLines.add(line);
                            existingLineIds.add(pk);
                            addedCount++;
                            System.out.println("  Added additional line: " +
                                (line.getItem() != null ? line.getItem().getName() : "unknown") +
                                " (site=null, pk=" + pk + ")");
                        }
                    }
                    System.out.println("Added " + addedCount + " additional document lines (site=null)");

                    loadedLines.setAll(mergedLines);
                    updateAttendanceDatesMap();
                    refreshLineItems();
                    recalculateLinePrices();
                });
            })
            .onFailure(error -> {
                System.err.println("Failed to load additional document lines: " + error.getMessage());
                // Still update with existing lines on failure
                javafx.application.Platform.runLater(() -> {
                    loadedLines.setAll(existingLines);
                    updateAttendanceDatesMap();
                    refreshLineItems();
                    recalculateLinePrices();
                });
            });
    }

    /**
     * Updates the attendance dates map from WorkingBooking data.
     * Groups attendance dates by DocumentLine ID for quick lookup.
     * Only displays what is actually loaded from database - no fallback/pre-population.
     */
    private void updateAttendanceDatesMap() {
        attendanceDatesByLineId.clear();

        if (workingBooking == null) {
            System.out.println("=== DEBUG: updateAttendanceDatesMap() - workingBooking is null ===");
            return;
        }

        // Get booked attendances from WorkingBooking (properly loaded from database)
        List<Attendance> bookedAttendances = workingBooking.getBookedAttendances();

        System.out.println("=== DEBUG: updateAttendanceDatesMap() ===");
        System.out.println("Booked attendances count: " + bookedAttendances.size());

        // Print raw data for first 3 attendances to diagnose issues
        int debugCount = 0;
        for (Attendance att : bookedAttendances) {
            if (debugCount++ < 3) {
                System.out.println("  RAW[" + debugCount + "]: att.getId()=" + att.getId()
                    + ", att.getDocumentLine()=" + att.getDocumentLine()
                    + ", att.getScheduledItem()=" + att.getScheduledItem()
                    + ", scheduledItem.getDate()=" + (att.getScheduledItem() != null ? att.getScheduledItem().getDate() : null));
                if (att.getDocumentLine() != null) {
                    DocumentLine dl = att.getDocumentLine();
                    System.out.println("    DocumentLine: dl.getId()=" + dl.getId()
                        + ", getPrimaryKey=" + Entities.getPrimaryKey(dl)
                        + ", dl.getItem()=" + (dl.getItem() != null ? dl.getItem().getName() : "null"));
                }
            }
        }

        // Group attendance by line ID - only what's loaded from database
        int skippedNoLine = 0;
        int skippedNoDate = 0;
        int added = 0;
        for (Attendance att : bookedAttendances) {
            DocumentLine docLine = att.getDocumentLine();
            LocalDate date = att.getScheduledItem() != null ? att.getScheduledItem().getDate() : null;

            if (docLine == null) {
                skippedNoLine++;
                System.out.println("  SKIP: Attendance " + att.getId() + " has no DocumentLine");
                continue;
            }

            Object lineId = Entities.getPrimaryKey(docLine);
            if (lineId == null) {
                System.out.println("  SKIP: Attendance " + att.getId() + " - DocumentLine has no primary key (docLine.getId()=" + docLine.getId() + ")");
                skippedNoLine++;
                continue;
            }

            if (date == null) {
                skippedNoDate++;
                System.out.println("  SKIP: Attendance " + att.getId() + " has no date");
                continue;
            }

            attendanceDatesByLineId
                .computeIfAbsent(lineId, k -> new HashSet<>())
                .add(date);
            added++;
        }
        System.out.println("Summary: added=" + added + ", skippedNoLine=" + skippedNoLine + ", skippedNoDate=" + skippedNoDate);

        for (Map.Entry<Object, Set<LocalDate>> entry : attendanceDatesByLineId.entrySet()) {
            System.out.println("  LineId " + entry.getKey() + ": " + entry.getValue().size() + " dates");
        }
        System.out.println("=========================================");

        // Update canvas with attendance dates
        if (timelineCanvas != null) {
            // Set original attendance dates first (from database) - used to distinguish gaps from pending removals
            // A cross (X) is only shown when a date was originally included but is now being removed
            timelineCanvas.setOriginalAttendanceDates(attendanceDatesByLineId);
            // Then set current attendance dates (same as original on initial load)
            timelineCanvas.setAttendanceDates(attendanceDatesByLineId);
        }
    }

    /**
     * Sets the active state of the tab.
     */
    public void setActive(boolean active) {
        activeProperty.set(active);
        if (active) {
            loadWorkingBooking();
        }
    }

    /**
     * Refreshes the booking data (reloads from WorkingBooking).
     */
    public void refresh() {
        // Force reload by clearing and reloading
        workingBooking = null;
        loadWorkingBooking();
    }

    /**
     * Gets the active property.
     */
    public BooleanProperty activeProperty() {
        return activeProperty;
    }

    // Popup menu state
    private VBox currentPopupMenu = null;
    private StackPane popupOverlay = null;

    /**
     * Creates a styled button for use in popup menus.
     * GWT-compatible replacement for MenuItem.
     */
    private Button createMenuItemButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setBackground(Background.EMPTY);
        btn.setBorder(Border.EMPTY);
        btn.setCursor(Cursor.HAND);
        btn.setFont(Font.font("System", 12));
        btn.setPadding(new Insets(6, 12, 6, 12));

        // Hover effect
        btn.setOnMouseEntered(e -> btn.setBackground(createBackground(BG, BORDER_RADIUS_SMALL)));
        btn.setOnMouseExited(e -> btn.setBackground(Background.EMPTY));

        btn.setOnAction(e -> {
            hidePopupMenu();
            action.run();
        });

        return btn;
    }

    /**
     * Shows a popup menu near the specified button.
     * GWT-compatible replacement for MenuButton.
     */
    private void showPopupMenu(VBox menuContent, Button anchor) {
        hidePopupMenu(); // Hide any existing popup

        // Create an overlay that covers the whole scene and closes menu when clicked
        popupOverlay = new StackPane();
        popupOverlay.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        popupOverlay.setOnMouseClicked(e -> hidePopupMenu());

        // Position the menu content
        menuContent.setMaxWidth(150);

        // Add menu to overlay
        popupOverlay.getChildren().add(menuContent);
        StackPane.setAlignment(menuContent, Pos.TOP_LEFT);

        // Add overlay to the scene
        if (anchor.getScene() != null && anchor.getScene().getRoot() instanceof Pane) {
            Pane root = (Pane) anchor.getScene().getRoot();

            // Calculate position relative to anchor button (GWT-compatible: use Point2D not Bounds)
            javafx.geometry.Point2D pos = anchor.localToScene(0, 0);
            menuContent.setTranslateX(pos.getX());
            menuContent.setTranslateY(pos.getY() + anchor.getHeight() + 4);

            root.getChildren().add(popupOverlay);
            currentPopupMenu = menuContent;
        }
    }

    /**
     * Hides the current popup menu.
     */
    private void hidePopupMenu() {
        if (popupOverlay != null && popupOverlay.getParent() instanceof Pane) {
            ((Pane) popupOverlay.getParent()).getChildren().remove(popupOverlay);
        }
        popupOverlay = null;
        currentPopupMenu = null;
    }
}
