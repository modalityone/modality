package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.util.layout.Layouts;
import javafx.beans.property.BooleanProperty;
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
import one.modality.base.client.time.ModalityDates;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of the shuttle options section with grouped UI.
 * Displays airport shuttle options (outbound/return trips) in a single
 * grouped container with a header showing the total price.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Grouped container with icon, title, description, and total price in header</li>
 *   <li>Individual checkbox cards for outbound and return trips</li>
 *   <li>Trips are enabled/disabled based on arrival/departure date matching</li>
 *   <li>Shows date and departure time for each trip</li>
 *   <li>Updates total price in header when selections change</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-shuttle-container} - grouped container</li>
 *   <li>{@code .bookingpage-shuttle-container.has-selection} - when any shuttle selected</li>
 *   <li>{@code .bookingpage-shuttle-trip} - individual trip card</li>
 *   <li>{@code .bookingpage-shuttle-trip.selected} - selected trip</li>
 *   <li>{@code .bookingpage-shuttle-trip.disabled} - trip not available for selected dates</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasShuttleOptionsSection
 */
public class DefaultShuttleOptionsSection implements HasShuttleOptionsSection {

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === DATE PROPERTIES ===
    protected final ObjectProperty<LocalDate> arrivalDateProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDate> departureDateProperty = new SimpleObjectProperty<>();

    // === SELECTION PROPERTIES ===
    protected final BooleanProperty shuttleOutboundSelected = new SimpleBooleanProperty(false);
    protected final BooleanProperty shuttleReturnSelected = new SimpleBooleanProperty(false);

    // === OPTIONS ===
    protected final List<ShuttleOption> options = new ArrayList<>();

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox groupedContainer;
    protected VBox tripsContainer;
    protected Label totalPriceLabel;
    protected HBox headerPriceBox;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;
    protected PolicyAggregate policyAggregate;

    // === CALLBACKS ===
    protected Runnable onSelectionChangedCallback;

    public DefaultShuttleOptionsSection() {
        // Set up date change listeners to update availability
        arrivalDateProperty.addListener((obs, old, newDate) -> updateAvailability());
        departureDateProperty.addListener((obs, old, newDate) -> updateAvailability());

        buildUI();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-shuttle-section");

        // Main grouped container with border
        groupedContainer = new VBox(0);
        groupedContainer.getStyleClass().add("bookingpage-shuttle-container");
        groupedContainer.setPadding(new Insets(0)); // Padding handled by inner elements

        // Header with icon, title, description, and price
        HBox header = createHeader();

        // Trips container
        tripsContainer = new VBox(10);
        tripsContainer.setPadding(new Insets(0, 16, 16, 16));
        tripsContainer.setAlignment(Pos.TOP_LEFT);

        // Build trip cards from options
        buildTripCards();

        groupedContainer.getChildren().addAll(header, tripsContainer);
        container.getChildren().add(groupedContainer);
    }

    /**
     * Creates the header section with icon, title, description, and price.
     */
    protected HBox createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16));

        // Icon container with themed background
        StackPane iconContainer = createIconContainer();

        // Text content (title + description)
        VBox textContent = new VBox(4);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = new Label();
        title.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.AirportShuttle));
        title.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label description = new Label();
        // Use formatted text with price placeholder
        description.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.SelectYourJourney, getFormattedSingleTripPrice()));
        description.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        textContent.getChildren().addAll(title, description);

        // Price label (only visible when any shuttle selected)
        totalPriceLabel = new Label();
        totalPriceLabel.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-text-primary");
        updateTotalPriceLabel();

        headerPriceBox = new HBox(totalPriceLabel);
        headerPriceBox.setAlignment(Pos.CENTER_RIGHT);
        headerPriceBox.setVisible(false);
        headerPriceBox.setManaged(false);

        header.getChildren().addAll(iconContainer, textContent, headerPriceBox);

        return header;
    }

    /**
     * Creates the icon container with plane icon.
     */
    protected StackPane createIconContainer() {
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
     * Builds trip cards from the current options list.
     */
    protected void buildTripCards() {
        tripsContainer.getChildren().clear();

        if (options.isEmpty()) {
            return;
        }

        // Sort options: outbound first, then return
        List<ShuttleOption> sortedOptions = options.stream()
            .sorted((a, b) -> {
                if (a.isOutbound() && b.isReturn()) return -1;
                if (a.isReturn() && b.isOutbound()) return 1;
                return 0;
            })
            .collect(Collectors.toList());

        for (ShuttleOption option : sortedOptions) {
            HBox tripCard = createTripCard(option);
            tripsContainer.getChildren().add(tripCard);
        }
    }

    /**
     * Creates a trip card for a single shuttle option.
     */
    protected HBox createTripCard(ShuttleOption option) {
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
        Label priceLabel = new Label(formatPrice(option.getPrice()));
        priceLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold");

        card.getChildren().addAll(checkbox, textContent, priceLabel);

        // Selection handling
        option.selectedProperty().addListener((obs, old, newVal) -> {
            updateCardStyle(card, option);
            updateContainerStyle();
            updateTotalPriceLabel();
            syncLegacyProperties(option);
            if (onSelectionChangedCallback != null) {
                onSelectionChangedCallback.run();
            }
        });

        // Availability handling
        option.availableProperty().addListener((obs, old, newVal) -> {
            updateCardAvailability(card, option);
        });

        // Initial state
        updateCardStyle(card, option);
        updateCardAvailability(card, option);

        // Click to toggle (only if available)
        card.setOnMouseClicked(e -> {
            if (option.isAvailable()) {
                option.setSelected(!option.isSelected());
            }
        });

        return card;
    }

    /**
     * Updates card style based on selection state.
     */
    protected void updateCardStyle(HBox card, ShuttleOption option) {
        if (option.isSelected()) {
            if (!card.getStyleClass().contains("selected")) {
                card.getStyleClass().add("selected");
            }
        } else {
            card.getStyleClass().remove("selected");
        }
    }

    /**
     * Updates card availability based on date matching.
     */
    protected void updateCardAvailability(HBox card, ShuttleOption option) {
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
     * Updates the grouped container style based on whether any shuttle is selected.
     */
    protected void updateContainerStyle() {
        boolean hasSelection = options.stream().anyMatch(ShuttleOption::isSelected);
        if (hasSelection) {
            if (!groupedContainer.getStyleClass().contains("has-selection")) {
                groupedContainer.getStyleClass().add("has-selection");
            }
            headerPriceBox.setVisible(true);
            headerPriceBox.setManaged(true);
        } else {
            groupedContainer.getStyleClass().remove("has-selection");
            headerPriceBox.setVisible(false);
            headerPriceBox.setManaged(false);
        }
    }

    /**
     * Updates the total price label in the header.
     */
    protected void updateTotalPriceLabel() {
        int total = getTotalShuttleCost();
        totalPriceLabel.setText(formatPrice(total));
    }

    /**
     * Syncs legacy properties for backward compatibility.
     */
    protected void syncLegacyProperties(ShuttleOption option) {
        if (option.isOutbound()) {
            shuttleOutboundSelected.set(option.isSelected());
        } else if (option.isReturn()) {
            shuttleReturnSelected.set(option.isSelected());
        }
    }

    /**
     * Updates shuttle availability based on current arrival/departure dates.
     */
    protected void updateAvailability() {
        LocalDate arrivalDate = arrivalDateProperty.get();
        LocalDate departureDate = departureDateProperty.get();

        for (ShuttleOption option : options) {
            boolean available = false;

            if (option.isOutbound() && arrivalDate != null) {
                // Outbound shuttle available if scheduled date matches arrival date
                available = arrivalDate.equals(option.getScheduledDate());
            } else if (option.isReturn() && departureDate != null) {
                // Return shuttle available if scheduled date matches departure date
                available = departureDate.equals(option.getScheduledDate());
            }

            option.setAvailable(available);
        }

        // Rebuild trip cards to reflect new availability
        buildTripCards();
        updateContainerStyle();
    }

    /**
     * Formats trip date and time for display.
     * Format: "Tue 23 Jun, departs 2:00 PM"
     */
    protected String formatTripDateTime(ShuttleOption option) {
        LocalDate date = option.getScheduledDate();
        if (date == null) {
            return "";
        }

        // Format date as "Tue 23 Jun"
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        int dayOfMonth = date.getDayOfMonth();
        String month = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

        String dateStr = dayOfWeek + " " + dayOfMonth + " " + month;

        // Get departure time from item description or label if available
        String departureTime = option.getDescription();
        if (departureTime != null && !departureTime.isEmpty()) {
            return dateStr + ", " + departureTime;
        }

        return dateStr;
    }

    /**
     * Formats a price in cents for display.
     */
    protected String formatPrice(int priceInCents) {
        if (priceInCents == 0) {
            return "Free";
        }
        return "$" + (priceInCents / 100);
    }

    /**
     * Gets the formatted price for a single trip (used in description).
     */
    protected String getFormattedSingleTripPrice() {
        if (options.isEmpty()) {
            return "$0";
        }
        // Get price from first option
        int price = options.stream()
            .mapToInt(ShuttleOption::getPrice)
            .findFirst()
            .orElse(0);
        return formatPrice(price);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.AirportShuttle;
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
        // Always valid - shuttle is optional
        return new SimpleBooleanProperty(true);
    }

    // ========================================
    // HasShuttleOptionsSection INTERFACE
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
    public BooleanProperty shuttleOutboundSelectedProperty() {
        return shuttleOutboundSelected;
    }

    @Override
    public BooleanProperty shuttleReturnSelectedProperty() {
        return shuttleReturnSelected;
    }

    @Override
    public List<ShuttleOption> getOptions() {
        return options;
    }

    @Override
    public void clearOptions() {
        options.clear();
        shuttleOutboundSelected.set(false);
        shuttleReturnSelected.set(false);
        if (tripsContainer != null) {
            tripsContainer.getChildren().clear();
        }
    }

    @Override
    public void addOption(ShuttleOption option) {
        options.add(option);
    }

    @Override
    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChangedCallback = callback;
    }

    @Override
    public void reset() {
        // Deselect all shuttles
        for (ShuttleOption option : options) {
            option.setSelected(false);
        }
        shuttleOutboundSelected.set(false);
        shuttleReturnSelected.set(false);
        updateContainerStyle();
        updateTotalPriceLabel();
    }

    // ========================================
    // DATA POPULATION FROM POLICY AGGREGATE
    // ========================================

    @Override
    public void populateFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) {
            return;
        }

        this.policyAggregate = policyAggregate;

        // Clear existing options
        clearOptions();

        // Get transport scheduled items
        List<ScheduledItem> transportItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.TRANSPORT);
        if (transportItems == null || transportItems.isEmpty()) {
            rebuildUI();
            return;
        }

        // Group by Item
        Map<Item, List<ScheduledItem>> itemMap = transportItems.stream()
            .filter(si -> si.getItem() != null)
            .collect(Collectors.groupingBy(ScheduledItem::getItem));

        // Process each transport Item
        for (Map.Entry<Item, List<ScheduledItem>> entry : itemMap.entrySet()) {
            Item item = entry.getKey();
            List<ScheduledItem> scheduledItems = entry.getValue();

            // Get price from rate
            int price = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, item)
                .findFirst()
                .map(Rate::getPrice)
                .orElse(0);

            // Determine direction from item name
            ShuttleDirection direction = determineDirection(item);

            // Get item name and description
            String name = item.getName() != null ? item.getName() : "Shuttle";
            String description = "";
            if (item.getLabel() != null && item.getLabel().getEn() != null) {
                description = item.getLabel().getEn();
            }

            // Get scheduled date from first scheduled item (assuming all have same date)
            LocalDate scheduledDate = scheduledItems.stream()
                .map(ScheduledItem::getDate)
                .filter(d -> d != null)
                .findFirst()
                .orElse(null);

            ShuttleOption option = new ShuttleOption(
                item.getPrimaryKey(),
                item,
                name,
                description,
                price,
                scheduledDate,
                direction,
                scheduledItems
            );

            addOption(option);
        }

        // Initial availability check
        updateAvailability();

        // Rebuild UI
        rebuildUI();
    }

    /**
     * Determines the shuttle direction based on item name.
     * Uses the existing pattern from DefaultAdditionalOptionsSection.
     */
    protected ShuttleDirection determineDirection(Item item) {
        String name = item.getName() != null ? item.getName().toLowerCase() : "";

        if (name.contains("outbound") || name.contains("arrival") || name.contains("to ") || name.contains("→")) {
            return ShuttleDirection.OUTBOUND;
        } else if (name.contains("return") || name.contains("departure") || name.contains("from ") || name.contains("←")) {
            return ShuttleDirection.RETURN;
        }

        // Default: infer from context or default to outbound
        return ShuttleDirection.OUTBOUND;
    }

    /**
     * Rebuilds the UI to reflect updated options.
     */
    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
    }

    /**
     * Sets the visibility of the section.
     */
    public void setVisible(boolean visible) {
        Layouts.setManagedAndVisibleProperties(container, visible);
    }
}
