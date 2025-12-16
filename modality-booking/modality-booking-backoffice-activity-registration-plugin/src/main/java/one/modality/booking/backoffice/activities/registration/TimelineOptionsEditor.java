package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Timeline-based booking options editor.
 * <p>
 * Features:
 * - Visual timeline grid with date columns
 * - Line items shown as colored bars across dates
 * - Toggle individual days on/off for items
 * - Adjust arrival/departure dates with steppers
 * - Add new options picker by category
 * - Edit/cancel/restore line items
 * <p>
 * Based on RegistrationDashboardFull.jsx TimelineOptionsEditor (lines 3788-6583).
 *
 * @author Claude Code
 */
public class TimelineOptionsEditor {

    private static final int BUFFER_DAYS = 2;
    private static final double DAY_COLUMN_WIDTH = 36;
    private static final double LABEL_COLUMN_WIDTH = 160;
    private static final double PRICE_COLUMN_WIDTH = 80;

    private final ViewDomainActivityBase activity;
    private final Document document;
    private final UpdateStore updateStore;

    // Date properties
    private final ObjectProperty<LocalDate> arrivalDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> departureDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> eventStartProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> eventEndProperty = new SimpleObjectProperty<>();

    // Line items
    private final ListProperty<LineItemData> lineItemsProperty = new SimpleListProperty<>();
    private final ObjectProperty<VisualResult> linesVisualResultProperty = new SimpleObjectProperty<>();

    // UI state
    private final BooleanProperty showAddPanelProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty showDeletedProperty = new SimpleBooleanProperty(false);
    private final IntegerProperty hoveredColumnProperty = new SimpleIntegerProperty(-1);

    // UI components
    private VBox timelineContainer;
    private HBox dateHeaderRow;
    private VBox lineItemsContainer;
    private ReactiveVisualMapper<DocumentLine> linesMapper;

    // Callback for room allocation
    private Runnable onShowRoomAllocation;

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEE");
    private static final DateTimeFormatter DAY_NUM_FORMAT = DateTimeFormatter.ofPattern("d");

    /**
     * Data class representing a line item in the timeline.
     */
    public static class LineItemData {
        public Object lineId;
        public Object itemId;
        public String itemName;
        public String category;
        public LocalDate startDate;
        public LocalDate endDate;
        public Set<LocalDate> excludedDays = new HashSet<>();
        public int quantity;
        public int pricePerUnit;
        public int priceNet;
        public String status; // active, cancelled, deleted
        public String unit; // night, day
        public boolean temporal; // true for dated items, false for one-time

        public boolean isActive() {
            return !"cancelled".equals(status) && !"deleted".equals(status);
        }

        public boolean isCancelled() {
            return "cancelled".equals(status);
        }

        public boolean isDeleted() {
            return "deleted".equals(status);
        }
    }

    public TimelineOptionsEditor(ViewDomainActivityBase activity, Document document, UpdateStore updateStore) {
        this.activity = activity;
        this.document = document;
        this.updateStore = updateStore;
    }

    /**
     * Builds the timeline editor UI.
     */
    public Node buildUi() {
        VBox container = new VBox(0);
        container.setBackground(createBackground(BG_CARD, BORDER_RADIUS_MEDIUM));
        container.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        // Header with accommodation info, dates, and total
        Node header = createHeader();

        // Date timeline header
        Node dateHeader = createDateHeader();

        // Line items section
        Node lineItemsSection = createLineItemsSection();
        VBox.setVgrow(lineItemsSection, Priority.ALWAYS);

        // Add option panel (toggleable)
        Node addPanel = createAddOptionPanel();

        container.getChildren().addAll(header, dateHeader, lineItemsSection, addPanel);

        // Initialize dates from document
        initializeDates();

        return container;
    }

    /**
     * Initializes dates from the document.
     */
    private void initializeDates() {
        // TODO: Get actual dates from document
        LocalDate today = LocalDate.now();
        arrivalDateProperty.set(today);
        departureDateProperty.set(today.plusDays(3));
        eventStartProperty.set(today);
        eventEndProperty.set(today.plusDays(5));
    }

    /**
     * Creates the header section with accommodation, dates, and total.
     */
    private Node createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(new Background(new BackgroundFill(
            Color.web("#f7f5ef"), new CornerRadii(BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, 0, 0, false), null
        )));
        header.setBorder(new Border(new BorderStroke(
            BORDER, BorderStrokeStyle.SOLID,
            new CornerRadii(BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, 0, 0, false),
            new BorderWidths(0, 0, 1, 0)
        )));

        // Left: Accommodation status
        Node accommodationStatus = createAccommodationStatus();

        // Center: Date steppers
        Node dateStepper = createDateSteppers();
        HBox.setHgrow(dateStepper, Priority.ALWAYS);

        // Right: Total price
        Node totalSection = createTotalSection();

        header.getChildren().addAll(accommodationStatus, dateStepper, totalSection);
        return header;
    }

    /**
     * Creates the accommodation status indicator.
     */
    private Node createAccommodationStatus() {
        VBox container = new VBox(4);
        container.setPadding(new Insets(6, 12, 6, 12));
        container.setBackground(createBackground(WARNING_BG, BORDER_RADIUS_SMALL));
        container.setBorder(new Border(new BorderStroke(
            WARNING, BorderStrokeStyle.SOLID, new CornerRadii(BORDER_RADIUS_SMALL), new BorderWidths(2)
        )));
        container.setMinWidth(200);

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Warning icon
        Label iconLabel = new Label("⚠");
        iconLabel.setFont(FONT_SUBTITLE);
        iconLabel.setTextFill(WARNING);

        // Accommodation type
        Label typeLabel = new Label("Accommodation"); // TODO: Get from document
        typeLabel.setFont(FONT_BODY);
        typeLabel.setTextFill(Color.web("#92400e"));

        topRow.getChildren().addAll(iconLabel, typeLabel);

        // Status label
        Label statusLabel = new Label("NOT ALLOCATED");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setTextFill(WARNING);

        // Allocate button
        Button allocateButton = new Button("Allocate Room");
        applyWarningButtonStyle(allocateButton);
        allocateButton.setOnAction(e -> {
            if (onShowRoomAllocation != null) {
                onShowRoomAllocation.run();
            }
        });

        HBox bottomRow = new HBox(8);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        bottomRow.getChildren().addAll(statusLabel, allocateButton);

        container.getChildren().addAll(topRow, bottomRow);
        return container;
    }

    /**
     * Creates the date stepper controls.
     */
    private Node createDateSteppers() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);

        // Arrival stepper
        VBox arrivalBox = createDateStepper("Arrival", SUCCESS, arrivalDateProperty, true);

        // Duration display
        VBox durationBox = new VBox(2);
        durationBox.setAlignment(Pos.CENTER);

        Label durationLabel = new Label("Duration");
        durationLabel.setFont(FONT_SMALL);
        durationLabel.setTextFill(TEXT_MUTED);

        Label durationValue = new Label();
        durationValue.setFont(FONT_SUBTITLE);
        durationValue.setTextFill(Color.WHITE);
        durationValue.setPadding(new Insets(4, 12, 4, 12));
        durationValue.setBackground(createBackground(TEXT_MUTED, 14));

        // Update duration when dates change
        FXProperties.runNowAndOnPropertiesChange(() -> {
            LocalDate arrival = arrivalDateProperty.get();
            LocalDate departure = departureDateProperty.get();
            if (arrival != null && departure != null) {
                long days = ChronoUnit.DAYS.between(arrival, departure);
                durationValue.setText(days + " day" + (days != 1 ? "s" : ""));
            }
        }, arrivalDateProperty, departureDateProperty);

        durationBox.getChildren().addAll(durationLabel, durationValue);

        // Departure stepper
        VBox departureBox = createDateStepper("Departure", DANGER, departureDateProperty, false);

        container.getChildren().addAll(arrivalBox, durationBox, departureBox);
        return container;
    }

    /**
     * Creates a date stepper control.
     */
    private VBox createDateStepper(String label, Color accentColor, ObjectProperty<LocalDate> dateProperty, boolean isArrival) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(label.toUpperCase());
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setTextFill(accentColor);

        HBox stepperBox = new HBox(0);
        stepperBox.setAlignment(Pos.CENTER);
        stepperBox.setBorder(createBorder(accentColor.deriveColor(0, 1, 1, 0.3), BORDER_RADIUS_SMALL));

        // Decrement button
        Button decrementBtn = new Button("<");
        decrementBtn.setMinWidth(28);
        decrementBtn.setMinHeight(28);
        decrementBtn.setBackground(createBackground(accentColor.deriveColor(0, 1, 1.2, 0.3), 6, 0, 0, 6));
        decrementBtn.setTextFill(accentColor.darker());
        decrementBtn.setBorder(Border.EMPTY);
        decrementBtn.setOnAction(e -> adjustDate(dateProperty, -1, isArrival));

        // Date display
        Label dateLabel = new Label();
        dateLabel.setMinWidth(90);
        dateLabel.setAlignment(Pos.CENTER);
        dateLabel.setFont(FONT_BODY);
        dateLabel.setTextFill(accentColor.darker());
        dateLabel.setPadding(new Insets(4, 10, 4, 10));

        FXProperties.runNowAndOnPropertiesChange(() -> {
            LocalDate date = dateProperty.get();
            if (date != null) {
                dateLabel.setText(date.format(DateTimeFormatter.ofPattern("EEE, d MMM")));
            }
        }, dateProperty);

        // Increment button
        Button incrementBtn = new Button(">");
        incrementBtn.setMinWidth(28);
        incrementBtn.setMinHeight(28);
        incrementBtn.setBackground(createBackground(accentColor.deriveColor(0, 1, 1.2, 0.3), 0, 6, 6, 0));
        incrementBtn.setTextFill(accentColor.darker());
        incrementBtn.setBorder(Border.EMPTY);
        incrementBtn.setOnAction(e -> adjustDate(dateProperty, 1, isArrival));

        stepperBox.getChildren().addAll(decrementBtn, dateLabel, incrementBtn);
        box.getChildren().addAll(titleLabel, stepperBox);
        return box;
    }

    /**
     * Adjusts a date by delta days and updates all temporal line items.
     */
    private void adjustDate(ObjectProperty<LocalDate> dateProperty, int delta, boolean isArrival) {
        LocalDate currentDate = dateProperty.get();
        if (currentDate == null) return;

        LocalDate newDate = currentDate.plusDays(delta);

        // Validation
        if (isArrival) {
            // Arrival can't go past departure
            LocalDate departure = departureDateProperty.get();
            if (departure != null && !newDate.isBefore(departure)) return;
        } else {
            // Departure can't go before arrival
            LocalDate arrival = arrivalDateProperty.get();
            if (arrival != null && !newDate.isAfter(arrival)) return;
        }

        dateProperty.set(newDate);

        // Update all temporal line items
        updateAllLineDates();
    }

    /**
     * Updates all temporal line item dates to match arrival/departure.
     */
    private void updateAllLineDates() {
        // TODO: Update line items in updateStore
        // For each temporal line:
        // - Update startDate to arrivalDate
        // - Update endDate to departureDate (or +1 for day-based items)
        // - Recalculate quantity and price
    }

    /**
     * Creates the total price section.
     */
    private Node createTotalSection() {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setMinWidth(100);

        Label totalLabel = new Label("TOTAL");
        totalLabel.setFont(FONT_SMALL);
        totalLabel.setTextFill(TEXT_MUTED);

        Label totalValue = new Label("£0"); // TODO: Calculate from lines
        totalValue.setFont(FONT_TITLE);
        totalValue.setTextFill(SUCCESS);

        box.getChildren().addAll(totalLabel, totalValue);
        return box;
    }

    /**
     * Creates the date header row showing day columns.
     */
    private Node createDateHeader() {
        VBox headerSection = new VBox(6);
        headerSection.setPadding(new Insets(10, 16, 10, 16));
        headerSection.setBackground(createBackground(Color.web("#faf9f5"), 0));
        headerSection.setBorder(new Border(new BorderStroke(
            BORDER, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0)
        )));

        dateHeaderRow = new HBox(1);
        dateHeaderRow.setAlignment(Pos.CENTER_LEFT);

        // Rebuild header when dates change
        FXProperties.runNowAndOnPropertiesChange(() -> {
            rebuildDateHeader();
        }, arrivalDateProperty, departureDateProperty, eventStartProperty, eventEndProperty);

        headerSection.getChildren().add(dateHeaderRow);
        return headerSection;
    }

    /**
     * Rebuilds the date header row based on current dates.
     */
    private void rebuildDateHeader() {
        dateHeaderRow.getChildren().clear();

        LocalDate arrival = arrivalDateProperty.get();
        LocalDate departure = departureDateProperty.get();
        LocalDate eventStart = eventStartProperty.get();
        LocalDate eventEnd = eventEndProperty.get();

        if (arrival == null || departure == null) return;

        // Calculate extended range with buffer
        LocalDate earliest = arrival.isBefore(eventStart != null ? eventStart : arrival) ? arrival : eventStart;
        LocalDate latest = departure.isAfter(eventEnd != null ? eventEnd : departure) ? departure : eventEnd;

        LocalDate rangeStart = earliest.minusDays(BUFFER_DAYS);
        LocalDate rangeEnd = latest.plusDays(BUFFER_DAYS);

        // Label column
        Label labelHeader = new Label("Dates");
        labelHeader.setMinWidth(LABEL_COLUMN_WIDTH);
        labelHeader.setFont(FONT_SMALL);
        labelHeader.setTextFill(TEXT_MUTED);
        dateHeaderRow.getChildren().add(labelHeader);

        // Day columns container
        HBox daysContainer = new HBox(1);
        daysContainer.setBackground(createBackground(Color.web("#e2e8f0"), BORDER_RADIUS_SMALL));
        daysContainer.setPadding(new Insets(1));
        HBox.setHgrow(daysContainer, Priority.ALWAYS);

        LocalDate current = rangeStart;
        int index = 0;
        while (!current.isAfter(rangeEnd)) {
            final int colIndex = index;
            final LocalDate date = current;

            final boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            final boolean inEvent = eventStart != null && eventEnd != null &&
                             !date.isBefore(eventStart) && date.isBefore(eventEnd);
            final boolean inBooking = !date.isBefore(arrival) && date.isBefore(departure);
            final boolean isBuffer = !inEvent && !inBooking;
            final boolean isArrival = date.equals(arrival);
            final boolean isDeparture = date.equals(departure);

            // Determine background color
            final Color bgColor;
            if (inEvent && inBooking) bgColor = Color.web("#dcfce7"); // both
            else if (inEvent) bgColor = Color.web("#ede9fe"); // event only
            else if (inBooking) bgColor = Color.web("#d1fae5"); // booking outside event
            else bgColor = Color.web("#f8fafc"); // buffer

            VBox dayColumn = new VBox(2);
            dayColumn.setAlignment(Pos.CENTER);
            dayColumn.setMinWidth(DAY_COLUMN_WIDTH);
            dayColumn.setPadding(new Insets(4, 2, 4, 2));
            dayColumn.setBackground(createBackground(bgColor, 0));
            if (isBuffer) dayColumn.setOpacity(0.5);

            // Day of week
            Label dayLabel = new Label(date.format(DAY_FORMAT).toUpperCase().substring(0, 3));
            dayLabel.setFont(FONT_SMALL);
            dayLabel.setTextFill(isWeekend ? Color.web("#7c3aed") : TEXT_MUTED);

            // Day number
            Label dayNum = new Label(date.format(DAY_NUM_FORMAT));
            dayNum.setFont(FONT_BODY);
            dayNum.setTextFill(isWeekend ? Color.web("#5b21b6") : TEXT);

            dayColumn.getChildren().addAll(dayLabel, dayNum);

            // Add arrival/departure border indicators
            if (isArrival) {
                dayColumn.setBorder(new Border(new BorderStroke(
                    SUCCESS, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0, 2)
                )));
            } else if (isDeparture) {
                dayColumn.setBorder(new Border(new BorderStroke(
                    DANGER, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 2, 0, 0)
                )));
            }

            // Hover effect
            dayColumn.setOnMouseEntered(e -> {
                hoveredColumnProperty.set(colIndex);
                dayColumn.setBackground(createBackground(Color.web("#1e3a5f"), 0));
                dayLabel.setTextFill(Color.web("#93c5fd"));
                dayNum.setTextFill(Color.WHITE);
            });
            dayColumn.setOnMouseExited(e -> {
                hoveredColumnProperty.set(-1);
                dayColumn.setBackground(createBackground(bgColor, 0));
                dayLabel.setTextFill(isWeekend ? Color.web("#7c3aed") : TEXT_MUTED);
                dayNum.setTextFill(isWeekend ? Color.web("#5b21b6") : TEXT);
            });

            daysContainer.getChildren().add(dayColumn);

            current = current.plusDays(1);
            index++;
        }

        // Price column spacer
        Region priceSpacer = new Region();
        priceSpacer.setMinWidth(PRICE_COLUMN_WIDTH);

        dateHeaderRow.getChildren().addAll(daysContainer, priceSpacer);
    }

    /**
     * Creates the line items section.
     */
    private Node createLineItemsSection() {
        lineItemsContainer = new VBox(6);
        lineItemsContainer.setPadding(new Insets(12, 16, 12, 16));

        // Placeholder for line items
        Label placeholder = new Label("No booking items");
        placeholder.setFont(FONT_BODY);
        placeholder.setTextFill(TEXT_MUTED);
        placeholder.setPadding(new Insets(20));

        lineItemsContainer.getChildren().add(placeholder);

        ScrollPane scrollPane = new ScrollPane(lineItemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setMinHeight(150);

        return scrollPane;
    }

    /**
     * Creates the add option panel.
     */
    private Node createAddOptionPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(12, 16, 16, 16));
        panel.setBackground(createBackground(BG, 0));
        panel.setBorder(new Border(new BorderStroke(
            BORDER, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 0, 0, 0)
        )));

        // Visibility binding
        panel.managedProperty().bind(showAddPanelProperty);
        panel.visibleProperty().bind(showAddPanelProperty);

        // Category groups
        Node accommodationSection = createCategorySection("Accommodation", ACCOMMODATION_FG, List.of(
            "Single Room", "Shared Room", "Dormitory", "Family Room"
        ));

        Node mealsSection = createCategorySection("Meals", MEALS_FG, List.of(
            "Full Board", "Breakfast Only", "Lunch Only", "Dinner Only"
        ));

        Node dietSection = createCategorySection("Diet", DIET_FG, List.of(
            "Vegetarian", "Vegan", "Gluten Free"
        ));

        Node programSection = createCategorySection("Program", PROGRAM_FG, List.of(
            "Course Fee", "Workshop", "Meditation Sessions"
        ));

        Node transportSection = createCategorySection("Transport", TRANSPORT_FG, List.of(
            "Airport Transfer", "Local Transport"
        ));

        // Close button
        Button closeButton = new Button("Close");
        applySecondaryButtonStyle(closeButton);
        closeButton.setOnAction(e -> showAddPanelProperty.set(false));

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getChildren().add(closeButton);

        panel.getChildren().addAll(accommodationSection, mealsSection, dietSection, programSection, transportSection, footer);

        // Add button (shown when panel is hidden)
        Button addButton = new Button("+ Add Option");
        applyPrimaryButtonStyle(addButton);
        addButton.setOnAction(e -> showAddPanelProperty.set(true));

        HBox addButtonContainer = new HBox(addButton);
        addButtonContainer.setPadding(new Insets(12, 16, 12, 16));
        addButtonContainer.setAlignment(Pos.CENTER);
        addButtonContainer.managedProperty().bind(showAddPanelProperty.not());
        addButtonContainer.visibleProperty().bind(showAddPanelProperty.not());

        VBox wrapper = new VBox(0);
        wrapper.getChildren().addAll(addButtonContainer, panel);
        return wrapper;
    }

    /**
     * Creates a category section with option chips.
     */
    private Node createCategorySection(String categoryName, Color accentColor, List<String> items) {
        VBox section = new VBox(8);

        Label titleLabel = new Label(categoryName);
        titleLabel.setFont(FONT_BODY);
        titleLabel.setTextFill(accentColor);

        FlowPane chipsPane = new FlowPane(8, 8);

        for (String itemName : items) {
            Button chip = createOptionChip(itemName, accentColor);
            chip.setOnAction(e -> addOption(itemName, categoryName));
            chipsPane.getChildren().add(chip);
        }

        section.getChildren().addAll(titleLabel, chipsPane);
        return section;
    }

    /**
     * Creates an option chip button.
     */
    private Button createOptionChip(String name, Color accentColor) {
        Button chip = new Button(name);
        chip.setFont(FONT_SMALL);
        chip.setTextFill(accentColor.darker());
        chip.setPadding(new Insets(6, 12, 6, 12));
        chip.setBackground(createBackground(accentColor.deriveColor(0, 0.3, 1.3, 0.3), 16));
        chip.setBorder(createBorder(accentColor.deriveColor(0, 1, 1, 0.3), 16));
        chip.setCursor(javafx.scene.Cursor.HAND);

        chip.setOnMouseEntered(e -> {
            chip.setBackground(createBackground(accentColor.deriveColor(0, 0.5, 1.2, 0.5), 16));
        });
        chip.setOnMouseExited(e -> {
            chip.setBackground(createBackground(accentColor.deriveColor(0, 0.3, 1.3, 0.3), 16));
        });

        return chip;
    }

    /**
     * Adds an option to the booking.
     */
    private void addOption(String itemName, String category) {
        // TODO: Create new DocumentLine in updateStore
        // - Find matching Item entity
        // - Create DocumentLine with proper dates
        // - Add to document

        System.out.println("Adding option: " + itemName + " (" + category + ")");
        showAddPanelProperty.set(false);
    }

    /**
     * Applies warning button style.
     */
    private void applyWarningButtonStyle(Button button) {
        button.setFont(FONT_SMALL);
        button.setTextFill(Color.WHITE);
        button.setPadding(new Insets(5, 12, 5, 12));
        button.setBackground(createBackground(WARNING, BORDER_RADIUS_SMALL));
        button.setBorder(Border.EMPTY);
        button.setCursor(javafx.scene.Cursor.HAND);
    }

    /**
     * Sets the callback for showing room allocation modal.
     */
    public void setOnShowRoomAllocation(Runnable callback) {
        this.onShowRoomAllocation = callback;
    }

    /**
     * Gets the add panel visibility property.
     */
    public BooleanProperty showAddPanelProperty() {
        return showAddPanelProperty;
    }

    /**
     * Sets up the reactive mapper for line items.
     */
    public void setupLinesMapper() {
        if (linesMapper == null && document.getId() != null) {
            linesMapper = ReactiveVisualMapper.<DocumentLine>createMasterPushReactiveChain(activity, linesVisualResultProperty)
                .always("{class: 'DocumentLine', alias: 'dl', columns: ['item.name', 'item.family.name', 'item.family.code', 'price_net', 'quantity', 'cancelled'], where: 'document=" + document.getId().getPrimaryKey() + " and !removed', orderBy: 'item.family.ord,item.ord'}")
                .start();
        }
    }
}
