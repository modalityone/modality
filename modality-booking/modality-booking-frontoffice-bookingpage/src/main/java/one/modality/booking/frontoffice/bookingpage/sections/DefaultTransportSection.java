package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.platform.console.Console;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of the transport section combining Parking and Shuttle options.
 * Displays both parking choices and airport shuttle options in a single cohesive UI.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Section header with transport icon</li>
 *   <li>Parking options displayed as checkbox cards</li>
 *   <li>Shuttle options in a grouped container with outbound/return trips</li>
 *   <li>Shuttle trips enabled/disabled based on arrival/departure date matching</li>
 *   <li>Updates total prices when selections change</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-transport-section} - section container</li>
 *   <li>{@code .bookingpage-checkbox-card} - parking option card</li>
 *   <li>{@code .bookingpage-shuttle-container} - grouped shuttle container</li>
 *   <li>{@code .bookingpage-shuttle-trip} - individual shuttle trip card</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasTransportSection
 */
public class DefaultTransportSection implements HasTransportSection {

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === DATE PROPERTIES (for shuttle availability) ===
    protected final ObjectProperty<LocalDate> arrivalDateProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDate> departureDateProperty = new SimpleObjectProperty<>();

    // === OPTIONS ===
    protected final List<ParkingOption> parkingOptions = new ArrayList<>();
    protected final List<ShuttleOption> shuttleOptions = new ArrayList<>();

    // === CONFIGURATION ===
    protected int daysCount = 1;

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox parkingContainer;
    protected VBox shuttleGroupContainer;
    protected VBox shuttleTripsContainer;
    protected Label shuttleTotalPriceLabel;
    protected HBox shuttleHeaderPriceBox;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;
    protected PolicyAggregate policyAggregate;

    // === CALLBACKS ===
    protected Runnable onSelectionChangedCallback;

    public DefaultTransportSection() {
        // Set up date change listeners to update shuttle availability
        arrivalDateProperty.addListener((obs, old, newDate) -> updateShuttleAvailability());
        departureDateProperty.addListener((obs, old, newDate) -> updateShuttleAvailability());

        buildUI();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(16);
        container.getStyleClass().add("bookingpage-transport-section");

        // Section header
        HBox sectionHeader = new StyledSectionHeader(BookingPageI18nKeys.Transport, StyledSectionHeader.ICON_CAR);

        // Parking container
        parkingContainer = new VBox(12);
        parkingContainer.setAlignment(Pos.TOP_LEFT);
        buildParkingCards();

        // Shuttle grouped container
        shuttleGroupContainer = buildShuttleGroupContainer();

        container.getChildren().addAll(sectionHeader, parkingContainer, shuttleGroupContainer);
        VBox.setMargin(sectionHeader, new Insets(0, 0, 8, 0));

        // Update visibility based on available options
        updateVisibility();
    }

    // ========================================
    // PARKING UI
    // ========================================

    /**
     * Builds parking option cards.
     */
    protected void buildParkingCards() {
        parkingContainer.getChildren().clear();

        if (parkingOptions.isEmpty()) {
            return;
        }

        for (ParkingOption option : parkingOptions) {
            VBox card = createParkingCard(option);
            parkingContainer.getChildren().add(card);
        }
    }

    /**
     * Creates a card for a single parking option.
     */
    protected VBox createParkingCard(ParkingOption option) {
        VBox card = new VBox(0);
        card.getStyleClass().add("bookingpage-checkbox-card");

        HBox mainRow = new HBox(12);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(16));
        mainRow.setCursor(Cursor.HAND);

        // Checkbox indicator
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(option.selectedProperty(), colorScheme);

        // Icon
        SVGPath icon = new SVGPath();
        icon.setContent(BookingPageUIBuilder.ICON_CAR);
        icon.setStroke(Color.web("#64748b"));
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.85);
        icon.setScaleY(0.85);

        // Text content
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = new Label(option.getName());
        title.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-dark");

        if (option.getDescription() != null && !option.getDescription().isEmpty()) {
            Label subtitle = new Label(option.getDescription());
            subtitle.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
            textContent.getChildren().addAll(title, subtitle);
        } else {
            textContent.getChildren().add(title);
        }

        // Price label
        Label priceLabel = new Label(formatParkingPrice(option));
        priceLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        mainRow.getChildren().addAll(checkbox, icon, textContent, priceLabel);
        card.getChildren().add(mainRow);

        // Initial selection state
        if (option.isSelected()) {
            card.getStyleClass().add("selected");
        }

        // Selection handling
        option.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                if (!card.getStyleClass().contains("selected")) {
                    card.getStyleClass().add("selected");
                }
            } else {
                card.getStyleClass().remove("selected");
            }
            if (onSelectionChangedCallback != null) {
                onSelectionChangedCallback.run();
            }
        });

        mainRow.setOnMouseClicked(e -> option.setSelected(!option.isSelected()));

        return card;
    }

    /**
     * Formats parking price for display.
     */
    protected String formatParkingPrice(ParkingOption option) {
        if (option.getPrice() == 0) {
            return "Free";
        }
        String priceStr = "$" + (option.getPrice() / 100);
        if (option.isPerDay()) {
            priceStr += "/day";
        }
        return priceStr;
    }

    // ========================================
    // SHUTTLE UI
    // ========================================

    /**
     * Builds the grouped shuttle container with header and trip cards.
     */
    protected VBox buildShuttleGroupContainer() {
        VBox groupContainer = new VBox(0);
        groupContainer.getStyleClass().add("bookingpage-shuttle-container");
        groupContainer.setPadding(new Insets(0));

        // Header with icon, title, description, and price
        HBox header = createShuttleHeader();

        // Trips container
        shuttleTripsContainer = new VBox(10);
        shuttleTripsContainer.setPadding(new Insets(0, 16, 16, 16));
        shuttleTripsContainer.setAlignment(Pos.TOP_LEFT);

        // Build trip cards from options
        buildShuttleTripCards();

        groupContainer.getChildren().addAll(header, shuttleTripsContainer);

        return groupContainer;
    }

    /**
     * Creates the shuttle header section.
     */
    protected HBox createShuttleHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16));

        // Icon container with themed background
        StackPane iconContainer = createShuttleIconContainer();

        // Text content (title + description)
        VBox textContent = new VBox(4);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = new Label();
        title.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.AirportShuttle));
        title.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label description = new Label();
        description.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.SelectYourJourney, getFormattedSingleTripPrice()));
        description.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        textContent.getChildren().addAll(title, description);

        // Price label (only visible when any shuttle selected)
        shuttleTotalPriceLabel = new Label();
        shuttleTotalPriceLabel.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-text-primary");
        updateShuttleTotalPriceLabel();

        shuttleHeaderPriceBox = new HBox(shuttleTotalPriceLabel);
        shuttleHeaderPriceBox.setAlignment(Pos.CENTER_RIGHT);
        shuttleHeaderPriceBox.setVisible(false);
        shuttleHeaderPriceBox.setManaged(false);

        header.getChildren().addAll(iconContainer, textContent, shuttleHeaderPriceBox);

        return header;
    }

    /**
     * Creates the shuttle icon container with plane icon.
     */
    protected StackPane createShuttleIconContainer() {
        StackPane iconContainer = new StackPane();
        iconContainer.setMinSize(36, 36);
        iconContainer.setMaxSize(36, 36);
        iconContainer.getStyleClass().add("bookingpage-shuttle-icon-container");

        SVGPath icon = new SVGPath();
        icon.setContent(BookingPageUIBuilder.ICON_PLANE);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.getStyleClass().add("bookingpage-shuttle-icon");

        iconContainer.getChildren().add(icon);
        iconContainer.setAlignment(Pos.CENTER);

        return iconContainer;
    }

    /**
     * Builds shuttle trip cards from the current options list.
     * Cards are sorted chronologically by scheduled date.
     */
    protected void buildShuttleTripCards() {
        shuttleTripsContainer.getChildren().clear();

        if (shuttleOptions.isEmpty()) {
            return;
        }

        // Sort options by date chronologically (earliest first)
        List<ShuttleOption> sortedOptions = shuttleOptions.stream()
            .sorted((a, b) -> {
                LocalDate dateA = a.getScheduledDate();
                LocalDate dateB = b.getScheduledDate();
                if (dateA == null && dateB == null) return 0;
                if (dateA == null) return 1;  // null dates go to end
                if (dateB == null) return -1;
                return dateA.compareTo(dateB);
            })
            .collect(Collectors.toList());

        for (ShuttleOption option : sortedOptions) {
            HBox tripCard = createShuttleTripCard(option);
            shuttleTripsContainer.getChildren().add(tripCard);
        }
    }

    /**
     * Creates a trip card for a single shuttle option.
     */
    protected HBox createShuttleTripCard(ShuttleOption option) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add("bookingpage-shuttle-trip");

        // Checkbox indicator
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(option.selectedProperty(), colorScheme);

        // Text content (trip name + date/time)
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = new Label(option.getName());
        title.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium");

        Label dateTime = new Label(formatTripDateTime(option));
        dateTime.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        textContent.getChildren().addAll(title, dateTime);

        // Price label
        Label priceLabel = new Label(formatShuttlePrice(option.getPrice()));
        priceLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold");

        card.getChildren().addAll(checkbox, textContent, priceLabel);

        // Selection handling
        option.selectedProperty().addListener((obs, old, newVal) -> {
            updateShuttleTripCardStyle(card, option);
            updateShuttleContainerStyle();
            updateShuttleTotalPriceLabel();
            if (onSelectionChangedCallback != null) {
                onSelectionChangedCallback.run();
            }
        });

        // Availability handling
        option.availableProperty().addListener((obs, old, newVal) -> {
            updateShuttleTripCardAvailability(card, option);
        });

        // Initial state
        updateShuttleTripCardStyle(card, option);
        updateShuttleTripCardAvailability(card, option);

        // Click to toggle (only if available)
        card.setOnMouseClicked(e -> {
            if (option.isAvailable()) {
                option.setSelected(!option.isSelected());
            }
        });

        return card;
    }

    /**
     * Updates shuttle trip card style based on selection state.
     */
    protected void updateShuttleTripCardStyle(HBox card, ShuttleOption option) {
        if (option.isSelected()) {
            if (!card.getStyleClass().contains("selected")) {
                card.getStyleClass().add("selected");
            }
        } else {
            card.getStyleClass().remove("selected");
        }
    }

    /**
     * Updates shuttle trip card availability based on date matching.
     */
    protected void updateShuttleTripCardAvailability(HBox card, ShuttleOption option) {
        if (option.isAvailable()) {
            card.getStyleClass().remove("disabled");
            card.setCursor(Cursor.HAND);
            card.setOpacity(1.0);
        } else {
            if (!card.getStyleClass().contains("disabled")) {
                card.getStyleClass().add("disabled");
            }
            card.setCursor(Cursor.DEFAULT);
            card.setOpacity(0.5);
            // Deselect if was selected but now unavailable
            if (option.isSelected()) {
                option.setSelected(false);
            }
        }
    }

    /**
     * Updates the shuttle grouped container style based on whether any shuttle is selected.
     */
    protected void updateShuttleContainerStyle() {
        boolean hasSelection = shuttleOptions.stream().anyMatch(ShuttleOption::isSelected);
        if (hasSelection) {
            if (!shuttleGroupContainer.getStyleClass().contains("has-selection")) {
                shuttleGroupContainer.getStyleClass().add("has-selection");
            }
            shuttleHeaderPriceBox.setVisible(true);
            shuttleHeaderPriceBox.setManaged(true);
        } else {
            shuttleGroupContainer.getStyleClass().remove("has-selection");
            shuttleHeaderPriceBox.setVisible(false);
            shuttleHeaderPriceBox.setManaged(false);
        }
    }

    /**
     * Updates the shuttle total price label in the header.
     */
    protected void updateShuttleTotalPriceLabel() {
        int total = getTotalShuttleCost();
        shuttleTotalPriceLabel.setText(formatShuttlePrice(total));
    }

    /**
     * Updates shuttle availability based on current arrival/departure dates.
     */
    protected void updateShuttleAvailability() {
        LocalDate arrivalDate = arrivalDateProperty.get();
        LocalDate departureDate = departureDateProperty.get();

        Console.log("DefaultTransportSection: Updating shuttle availability - arrival=" + arrivalDate + ", departure=" + departureDate);

        for (ShuttleOption option : shuttleOptions) {
            boolean available = false;

            if (option.isOutbound() && arrivalDate != null) {
                available = arrivalDate.equals(option.getScheduledDate());
                Console.log("DefaultTransportSection: Outbound '" + option.getName() +
                    "' scheduled=" + option.getScheduledDate() + " available=" + available);
            } else if (option.isReturn() && departureDate != null) {
                available = departureDate.equals(option.getScheduledDate());
                Console.log("DefaultTransportSection: Return '" + option.getName() +
                    "' scheduled=" + option.getScheduledDate() + " available=" + available);
            }

            option.setAvailable(available);
        }

        // Rebuild trip cards to reflect new availability
        buildShuttleTripCards();
        updateShuttleContainerStyle();
    }

    /**
     * Formats trip date and time for display.
     */
    protected String formatTripDateTime(ShuttleOption option) {
        LocalDate date = option.getScheduledDate();
        if (date == null) {
            return "";
        }

        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        int dayOfMonth = date.getDayOfMonth();
        String month = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

        String dateStr = dayOfWeek + " " + dayOfMonth + " " + month;

        String departureTime = option.getDescription();
        if (departureTime != null && !departureTime.isEmpty()) {
            return dateStr + ", " + departureTime;
        }

        return dateStr;
    }

    /**
     * Formats a shuttle price for display.
     */
    protected String formatShuttlePrice(int priceInCents) {
        if (priceInCents == 0) {
            return "Free";
        }
        return "$" + (priceInCents / 100);
    }

    /**
     * Gets the formatted price for a single trip.
     */
    protected String getFormattedSingleTripPrice() {
        if (shuttleOptions.isEmpty()) {
            return "$0";
        }
        int price = shuttleOptions.stream()
            .mapToInt(ShuttleOption::getPrice)
            .findFirst()
            .orElse(0);
        return formatShuttlePrice(price);
    }

    // ========================================
    // VISIBILITY
    // ========================================

    /**
     * Updates visibility of sub-containers based on available options.
     */
    protected void updateVisibility() {
        Layouts.setManagedAndVisibleProperties(parkingContainer, !parkingOptions.isEmpty());
        Layouts.setManagedAndVisibleProperties(shuttleGroupContainer, !shuttleOptions.isEmpty());
    }

    /**
     * Sets the visibility of the entire section.
     */
    public void setVisible(boolean visible) {
        Layouts.setManagedAndVisibleProperties(container, visible);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.Transport;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        // Always valid - transport options are optional
        return new SimpleBooleanProperty(true);
    }

    // ========================================
    // HasTransportSection INTERFACE
    // ========================================

    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public ObjectProperty<LocalDate> arrivalDateProperty() {
        return arrivalDateProperty;
    }

    @Override
    public ObjectProperty<LocalDate> departureDateProperty() {
        return departureDateProperty;
    }

    @Override
    public void setDaysCount(int days) {
        this.daysCount = days;
    }

    @Override
    public int getDaysCount() {
        return daysCount;
    }

    @Override
    public List<ParkingOption> getParkingOptions() {
        return parkingOptions;
    }

    @Override
    public void clearParkingOptions() {
        parkingOptions.clear();
        if (parkingContainer != null) {
            parkingContainer.getChildren().clear();
        }
    }

    @Override
    public void addParkingOption(ParkingOption option) {
        parkingOptions.add(option);
    }

    @Override
    public List<ShuttleOption> getShuttleOptions() {
        return shuttleOptions;
    }

    @Override
    public void clearShuttleOptions() {
        shuttleOptions.clear();
        if (shuttleTripsContainer != null) {
            shuttleTripsContainer.getChildren().clear();
        }
    }

    @Override
    public void addShuttleOption(ShuttleOption option) {
        shuttleOptions.add(option);
    }

    @Override
    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChangedCallback = callback;
    }

    @Override
    public void reset() {
        // Deselect all parking options
        for (ParkingOption option : parkingOptions) {
            option.setSelected(false);
        }
        // Deselect all shuttle options
        for (ShuttleOption option : shuttleOptions) {
            option.setSelected(false);
        }
        updateShuttleContainerStyle();
        updateShuttleTotalPriceLabel();
    }

    // ========================================
    // DATA POPULATION FROM POLICY AGGREGATE
    // ========================================

    @Override
    public void populateFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) {
            Console.log("DefaultTransportSection: PolicyAggregate is null");
            return;
        }

        this.policyAggregate = policyAggregate;

        // Clear existing options
        clearAllOptions();

        // Load parking options
        loadParkingOptions(policyAggregate);

        // Load shuttle options
        loadShuttleOptions(policyAggregate);

        Console.log("DefaultTransportSection: Loaded " + parkingOptions.size() + " parking options and " +
            shuttleOptions.size() + " shuttle options");

        // Update shuttle availability based on current dates
        updateShuttleAvailability();

        // Rebuild UI
        rebuildUI();
    }

    /**
     * Loads parking options from PolicyAggregate.
     */
    protected void loadParkingOptions(PolicyAggregate policyAggregate) {
        List<ScheduledItem> parkingItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.PARKING);
        if (parkingItems == null || parkingItems.isEmpty()) {
            Console.log("DefaultTransportSection: No parking scheduled items found");
            return;
        }

        Console.log("DefaultTransportSection: Found " + parkingItems.size() + " parking scheduled items");

        // Group by Item
        Map<Item, List<ScheduledItem>> itemMap = parkingItems.stream()
            .filter(si -> si.getItem() != null)
            .collect(Collectors.groupingBy(ScheduledItem::getItem));

        for (Map.Entry<Item, List<ScheduledItem>> entry : itemMap.entrySet()) {
            Item item = entry.getKey();
            List<ScheduledItem> scheduledItems = entry.getValue();

            // Get price from rate using the first scheduled item
            int price = 0;
            if (!scheduledItems.isEmpty()) {
                Rate rate = policyAggregate.getScheduledItemDailyRate(scheduledItems.get(0));
                if (rate != null) {
                    price = rate.getPrice();
                }
            }

            String name = item.getName() != null ? item.getName() : "Parking";
            String description = "";
            if (item.getLabel() != null && item.getLabel().getEn() != null) {
                description = item.getLabel().getEn();
            }

            ParkingOption option = new ParkingOption(
                item.getPrimaryKey(),
                item,
                name,
                description,
                price,
                true, // Parking is typically per-day
                scheduledItems
            );

            addParkingOption(option);
            Console.log("DefaultTransportSection: Added parking option '" + name + "' price=" + price);
        }
    }

    /**
     * Loads shuttle/transport options from PolicyAggregate.
     */
    protected void loadShuttleOptions(PolicyAggregate policyAggregate) {
        List<ScheduledItem> transportItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.TRANSPORT);
        if (transportItems == null || transportItems.isEmpty()) {
            Console.log("DefaultTransportSection: No transport scheduled items found");
            return;
        }

        Console.log("DefaultTransportSection: Found " + transportItems.size() + " transport scheduled items");

        // Group by Item
        Map<Item, List<ScheduledItem>> itemMap = transportItems.stream()
            .filter(si -> si.getItem() != null)
            .collect(Collectors.groupingBy(ScheduledItem::getItem));

        for (Map.Entry<Item, List<ScheduledItem>> entry : itemMap.entrySet()) {
            Item item = entry.getKey();
            List<ScheduledItem> scheduledItems = entry.getValue();

            // Get price from rate using the first scheduled item
            int price = 0;
            if (!scheduledItems.isEmpty()) {
                Rate rate = policyAggregate.getScheduledItemDailyRate(scheduledItems.get(0));
                if (rate != null) {
                    price = rate.getPrice();
                }
            }

            // Determine direction from item name
            ShuttleDirection direction = determineShuttleDirection(item);

            String name = item.getName() != null ? item.getName() : "Shuttle";

            // Get scheduled date and departure time from first scheduled item
            LocalDate scheduledDate = null;
            String departureTimeStr = "";

            for (ScheduledItem si : scheduledItems) {
                if (si.getDate() != null) {
                    scheduledDate = si.getDate();
                    // Get departure time from scheduled item
                    java.time.LocalTime startTime = si.getStartTime();
                    if (startTime != null) {
                        // Format time like "2:00 PM"
                        departureTimeStr = formatDepartureTime(startTime);
                    }
                    break;
                }
            }

            ShuttleOption option = new ShuttleOption(
                item.getPrimaryKey(),
                item,
                name,
                departureTimeStr,  // Use formatted time as description
                price,
                scheduledDate,
                direction,
                scheduledItems
            );

            addShuttleOption(option);
            Console.log("DefaultTransportSection: Added shuttle option '" + name +
                "' direction=" + direction + " date=" + scheduledDate +
                " time=" + departureTimeStr + " price=" + price);
        }
    }

    /**
     * Formats a LocalTime as a user-friendly departure time string.
     * @param time the departure time
     * @return formatted time like "2:00 PM" or "14:00"
     */
    protected String formatDepartureTime(java.time.LocalTime time) {
        if (time == null) {
            return "";
        }
        // Format as "h:mm a" (e.g., "2:00 PM")
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        return "departs " + time.format(formatter);
    }

    /**
     * Determines the shuttle direction based on item name.
     * Supports patterns like:
     * - "outbound", "arrival", "to venue" → OUTBOUND (to venue)
     * - "return", "departure", "from venue" → RETURN (from venue)
     * - "Airport => Venue" → OUTBOUND (arrow points to venue)
     * - "Venue => Airport" → RETURN (arrow points away from venue)
     */
    protected ShuttleDirection determineShuttleDirection(Item item) {
        String name = item.getName() != null ? item.getName().toLowerCase() : "";

        // Check explicit direction keywords first
        if (name.contains("outbound") || name.contains("arrival")) {
            return ShuttleDirection.OUTBOUND;
        } else if (name.contains("return") || name.contains("departure")) {
            return ShuttleDirection.RETURN;
        }

        // Check for arrow patterns like "Airport => Venue" or "Venue => Airport"
        // If the name contains "=>" or "→", check destination
        // Common airport keywords that suggest going TO airport (RETURN trip)
        String[] airportKeywords = {"airport", "jfk", "lga", "ewr", "newark", "laguardia", "heathrow", "gatwick"};

        if (name.contains("=>") || name.contains("→") || name.contains("->")) {
            // Split on arrow and check destination (right side)
            String[] parts = name.split("=>|→|->");
            if (parts.length >= 2) {
                String destination = parts[1].trim().toLowerCase();
                // If destination contains an airport keyword, it's RETURN (going to airport)
                for (String airport : airportKeywords) {
                    if (destination.contains(airport)) {
                        return ShuttleDirection.RETURN;
                    }
                }
                // Otherwise it's likely going to the venue (OUTBOUND)
                return ShuttleDirection.OUTBOUND;
            }
        }

        // Check for "to " or "from " patterns
        if (name.contains("to ") || name.contains("→")) {
            return ShuttleDirection.OUTBOUND;
        } else if (name.contains("from ") || name.contains("←")) {
            return ShuttleDirection.RETURN;
        }

        Console.log("DefaultTransportSection: Could not determine direction for '" + name + "', defaulting to OUTBOUND");
        return ShuttleDirection.OUTBOUND;
    }

    /**
     * Rebuilds the UI to reflect updated options.
     */
    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
    }
}
