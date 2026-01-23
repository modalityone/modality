package one.modality.booking.frontoffice.bookingpage.sections.transport;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;
import one.modality.booking.frontoffice.bookingpage.standard.BookingSelectionState;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Interface for a unified transport section combining Parking and Shuttle options.
 * This section displays both parking choices and airport shuttle options in a single
 * cohesive UI component with a shared header.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Section header with transport/travel icon</li>
 *   <li>Parking options displayed as checkbox cards</li>
 *   <li>Shuttle options grouped with outbound/return trips</li>
 *   <li>Shuttle trips only available when arrival/departure date matches scheduled date</li>
 *   <li>Independent selection for parking and shuttles</li>
 * </ul>
 *
 * <p>Item Families handled:</p>
 * <ul>
 *   <li>{@code KnownItemFamily.PARKING} - Parking options</li>
 *   <li>{@code KnownItemFamily.TRANSPORT} - Shuttle/transport options</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 * @see HasShuttleOptionsSection
 * @see HasAdditionalOptionsSection
 */
public interface HasTransportSection extends BookingFormSection, ResettableSection {

    // ========================================
    // PARKING AVAILABILITY STATUS
    // ========================================

    /**
     * Availability status for a parking option.
     */
    enum ParkingAvailabilityStatus {
        /** Parking is available for booking */
        AVAILABLE,
        /** Limited parking spots remaining */
        LIMITED,
        /** Parking is sold out */
        SOLD_OUT
    }

    // ========================================
    // PARKING OPTION
    // ========================================

    /**
     * Data class representing a parking option (e.g., Standard, Handicap/Disabled).
     * Multiple parking options are displayed within a single unified card with radio buttons.
     * Supports availability tracking and default selection from PolicyAggregate.
     */
    class ParkingOption {
        private final Object itemId;
        private final Item itemEntity;
        private final String name;
        private final String description;
        private final int price;        // Price in cents
        private final boolean perDay;   // If true, price is per day
        private final List<ScheduledItem> scheduledItems;
        private final ParkingAvailabilityStatus availabilityStatus;
        private final boolean isDefault;  // From ItemPolicy.isDefault()
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        /**
         * Creates a parking option with availability and default status.
         *
         * @param itemId             the Item primary key
         * @param itemEntity         the Item entity
         * @param name               display name (from Item.name)
         * @param description        optional description
         * @param price              price in cents
         * @param perDay             whether price is per day
         * @param scheduledItems     the ScheduledItems to book when selected
         * @param availabilityStatus availability status (AVAILABLE, LIMITED, SOLD_OUT)
         * @param isDefault          whether this is the default selection (from ItemPolicy)
         */
        public ParkingOption(Object itemId, Item itemEntity, String name, String description,
                             int price, boolean perDay, List<ScheduledItem> scheduledItems,
                             ParkingAvailabilityStatus availabilityStatus, boolean isDefault) {
            this.itemId = itemId;
            this.itemEntity = itemEntity;
            this.name = name;
            this.description = description;
            this.price = price;
            this.perDay = perDay;
            this.scheduledItems = scheduledItems != null ? scheduledItems : Collections.emptyList();
            this.availabilityStatus = availabilityStatus != null ? availabilityStatus : ParkingAvailabilityStatus.AVAILABLE;
            this.isDefault = isDefault;
        }

        /**
         * Creates a parking option with default availability (AVAILABLE) and not default.
         * For backwards compatibility.
         */
        public ParkingOption(Object itemId, Item itemEntity, String name, String description,
                             int price, boolean perDay, List<ScheduledItem> scheduledItems) {
            this(itemId, itemEntity, name, description, price, perDay, scheduledItems,
                 ParkingAvailabilityStatus.AVAILABLE, false);
        }

        public Object getItemId() { return itemId; }
        public Item getItemEntity() { return itemEntity; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getPrice() { return price; }
        public boolean isPerDay() { return perDay; }
        public List<ScheduledItem> getScheduledItems() { return scheduledItems; }

        public ParkingAvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
        public boolean isDefault() { return isDefault; }
        public boolean isSoldOut() { return availabilityStatus == ParkingAvailabilityStatus.SOLD_OUT; }
        public boolean isAvailable() { return availabilityStatus != ParkingAvailabilityStatus.SOLD_OUT; }
        public boolean isLimited() { return availabilityStatus == ParkingAvailabilityStatus.LIMITED; }

        public BooleanProperty selectedProperty() { return selected; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean value) { selected.set(value); }
    }

    // ========================================
    // SHUTTLE OPTION (reusing from HasShuttleOptionsSection)
    // ========================================

    /**
     * Direction of the shuttle trip.
     */
    enum ShuttleDirection {
        /** Outbound trip: from airport to venue (matches arrival date) */
        OUTBOUND,
        /** Return trip: from venue to airport (matches departure date) */
        RETURN
    }

    /**
     * Data class representing a shuttle trip option.
     * Contains the item entity, scheduled items for the trip, and metadata.
     */
    class ShuttleOption {
        private final Object itemId;
        private final Item itemEntity;
        private final String name;         // Display name from Item (e.g., "JFK → Glen Spey")
        private final String description;  // Optional description
        private final int price;           // Price in cents
        private final LocalDate scheduledDate;  // Date when this shuttle runs
        private final ShuttleDirection direction;
        private final List<ScheduledItem> scheduledItems;  // ScheduledItems to book when selected
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final BooleanProperty available = new SimpleBooleanProperty(false);  // Available based on date matching

        /**
         * Creates a shuttle option for a specific trip.
         *
         * @param itemId         the Item primary key
         * @param itemEntity     the Item entity
         * @param name           display name (from database)
         * @param description    optional description
         * @param price          price in cents
         * @param scheduledDate  the date this shuttle runs
         * @param direction      outbound or return
         * @param scheduledItems the ScheduledItems to book when selected
         */
        public ShuttleOption(Object itemId, Item itemEntity, String name, String description,
                             int price, LocalDate scheduledDate, ShuttleDirection direction,
                             List<ScheduledItem> scheduledItems) {
            this.itemId = itemId;
            this.itemEntity = itemEntity;
            this.name = name;
            this.description = description;
            this.price = price;
            this.scheduledDate = scheduledDate;
            this.direction = direction;
            this.scheduledItems = scheduledItems != null ? scheduledItems : Collections.emptyList();
        }

        public Object getItemId() { return itemId; }
        public Item getItemEntity() { return itemEntity; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getPrice() { return price; }
        public LocalDate getScheduledDate() { return scheduledDate; }
        public ShuttleDirection getDirection() { return direction; }
        public List<ScheduledItem> getScheduledItems() { return scheduledItems; }

        public BooleanProperty selectedProperty() { return selected; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean value) { selected.set(value); }

        public BooleanProperty availableProperty() { return available; }
        public boolean isAvailable() { return available.get(); }
        public void setAvailable(boolean value) { available.set(value); }

        /**
         * Returns true if this is an outbound trip (to venue).
         */
        public boolean isOutbound() {
            return direction == ShuttleDirection.OUTBOUND;
        }

        /**
         * Returns true if this is a return trip (from venue).
         */
        public boolean isReturn() {
            return direction == ShuttleDirection.RETURN;
        }
    }

    // ========================================
    // CONFIGURATION
    // ========================================

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

    // ========================================
    // DATE BINDING (for shuttle availability)
    // ========================================

    /**
     * Returns the arrival date property.
     * Outbound shuttles are available when their scheduled date matches this date.
     */
    ObjectProperty<LocalDate> arrivalDateProperty();

    /**
     * Gets the arrival date.
     */
    default LocalDate getArrivalDate() {
        return arrivalDateProperty().get();
    }

    /**
     * Sets the arrival date. This updates outbound shuttle availability.
     */
    default void setArrivalDate(LocalDate date) {
        arrivalDateProperty().set(date);
    }

    /**
     * Returns the departure date property.
     * Return shuttles are available when their scheduled date matches this date.
     */
    ObjectProperty<LocalDate> departureDateProperty();

    /**
     * Gets the departure date.
     */
    default LocalDate getDepartureDate() {
        return departureDateProperty().get();
    }

    /**
     * Sets the departure date. This updates return shuttle availability.
     */
    default void setDepartureDate(LocalDate date) {
        departureDateProperty().set(date);
    }

    /**
     * Sets the number of days for parking price calculation.
     */
    void setDaysCount(int days);

    /**
     * Gets the number of days for parking price calculation.
     */
    int getDaysCount();

    // ========================================
    // PARKING OPTIONS
    // ========================================

    /**
     * Returns all parking options.
     */
    List<ParkingOption> getParkingOptions();

    /**
     * Returns all selected parking options.
     */
    default List<ParkingOption> getSelectedParkingOptions() {
        return getParkingOptions().stream()
            .filter(ParkingOption::isSelected)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Returns true if there are any parking options available.
     */
    default boolean hasParkingOptions() {
        return !getParkingOptions().isEmpty();
    }

    /**
     * Returns true if all parking options are sold out.
     */
    default boolean areAllParkingOptionsSoldOut() {
        List<ParkingOption> options = getParkingOptions();
        return !options.isEmpty() && options.stream().allMatch(ParkingOption::isSoldOut);
    }

    /**
     * Returns true if any parking option is available (not sold out).
     */
    default boolean hasAvailableParkingOption() {
        return getParkingOptions().stream().anyMatch(ParkingOption::isAvailable);
    }

    /**
     * Returns the default parking option (from ItemPolicy), or null if none.
     */
    default ParkingOption getDefaultParkingOption() {
        return getParkingOptions().stream()
            .filter(opt -> opt.isDefault() && opt.isAvailable())
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns the first available parking option, or null if all sold out.
     */
    default ParkingOption getFirstAvailableParkingOption() {
        return getParkingOptions().stream()
            .filter(ParkingOption::isAvailable)
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns the parking enabled property (main checkbox state).
     * When enabled, user wants parking. When disabled, no parking selected.
     */
    BooleanProperty parkingEnabledProperty();

    /**
     * Returns true if parking is enabled (main checkbox checked).
     */
    default boolean isParkingEnabled() {
        return parkingEnabledProperty().get();
    }

    /**
     * Sets whether parking is enabled (main checkbox state).
     */
    default void setParkingEnabled(boolean enabled) {
        parkingEnabledProperty().set(enabled);
    }

    /**
     * Returns the currently selected parking type property.
     */
    javafx.beans.property.ObjectProperty<ParkingOption> selectedParkingTypeProperty();

    /**
     * Returns the currently selected parking type, or null if none.
     */
    default ParkingOption getSelectedParkingType() {
        return selectedParkingTypeProperty().get();
    }

    /**
     * Sets the currently selected parking type.
     */
    default void setSelectedParkingType(ParkingOption option) {
        selectedParkingTypeProperty().set(option);
    }

    /**
     * Clears all parking options.
     */
    void clearParkingOptions();

    /**
     * Adds a parking option to the section.
     */
    void addParkingOption(ParkingOption option);

    // ========================================
    // SHUTTLE OPTIONS
    // ========================================

    /**
     * Returns all shuttle options.
     */
    List<ShuttleOption> getShuttleOptions();

    /**
     * Returns all selected shuttle options.
     */
    default List<ShuttleOption> getSelectedShuttleOptions() {
        return getShuttleOptions().stream()
            .filter(ShuttleOption::isSelected)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Returns true if there are any shuttle options available.
     */
    default boolean hasShuttleOptions() {
        return !getShuttleOptions().isEmpty();
    }

    /**
     * Clears all shuttle options.
     */
    void clearShuttleOptions();

    /**
     * Adds a shuttle option to the section.
     */
    void addShuttleOption(ShuttleOption option);

    // ========================================
    // COMBINED OPTIONS CHECK
    // ========================================

    /**
     * Returns true if there are any options (parking or shuttle) available.
     */
    default boolean hasAnyOptions() {
        return hasParkingOptions() || hasShuttleOptions();
    }

    /**
     * Clears all options (parking and shuttle).
     */
    default void clearAllOptions() {
        clearParkingOptions();
        clearShuttleOptions();
    }

    // ========================================
    // DATA POPULATION
    // ========================================

    /**
     * Populates the section with parking and transport options from PolicyAggregate.
     * Loads PARKING and TRANSPORT items from ScheduledItems.
     *
     * @param policyAggregate the policy data containing scheduledItems and rates
     */
    void populateFromPolicyAggregate(PolicyAggregate policyAggregate);

    // ========================================
    // CALLBACKS
    // ========================================

    /**
     * Sets a callback to be invoked when any selection changes.
     * This allows the booking form to re-book items when options are selected/deselected.
     *
     * @param callback the callback to invoke on selection change
     */
    void setOnSelectionChanged(Runnable callback);

    // ========================================
    // PRICING
    // ========================================

    /**
     * Returns the total parking cost based on selected options.
     * Takes into account per-day pricing and days count.
     */
    default int getTotalParkingCost() {
        int daysCount = getDaysCount();
        return getSelectedParkingOptions().stream()
            .mapToInt(opt -> opt.isPerDay() ? opt.getPrice() * daysCount : opt.getPrice())
            .sum();
    }

    /**
     * Returns the total shuttle cost based on selected options.
     */
    default int getTotalShuttleCost() {
        return getSelectedShuttleOptions().stream()
            .mapToInt(ShuttleOption::getPrice)
            .sum();
    }

    /**
     * Returns the total transport cost (parking + shuttle).
     */
    default int getTotalTransportCost() {
        return getTotalParkingCost() + getTotalShuttleCost();
    }

    // ========================================
    // RESET
    // ========================================

    /**
     * Resets the section to its initial state (all options deselected).
     */
    @Override
    void reset();

    // ========================================
    // SELECTION STATE BINDING
    // ========================================

    /**
     * Binds this section to the centralized BookingSelectionState.
     * Changes in the section will be pushed to the state.
     *
     * @param selectionState the centralized state to bind to
     */
    default void bindToSelectionState(BookingSelectionState selectionState) {
        if (selectionState == null) return;

        // Set up listener to push parking changes to state
        // Listen to each parking option's selected property
        for (ParkingOption parkingOption : getParkingOptions()) {
            parkingOption.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    if (!selectionState.getSelectedParkingOptions().contains(parkingOption)) {
                        selectionState.addSelectedParkingOption(parkingOption);
                    }
                } else {
                    selectionState.getSelectedParkingOptions().remove(parkingOption);
                }
            });
        }

        // Set up listener to push shuttle changes to state
        for (ShuttleOption shuttleOption : getShuttleOptions()) {
            shuttleOption.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    if (!selectionState.getSelectedShuttleOptions().contains(shuttleOption)) {
                        selectionState.addSelectedShuttleOption(shuttleOption);
                    }
                } else {
                    selectionState.getSelectedShuttleOptions().remove(shuttleOption);
                }
            });
        }

        // Initialize state from current section values
        selectionState.clearSelectedParkingOptions();
        selectionState.clearSelectedShuttleOptions();
        for (ParkingOption opt : getSelectedParkingOptions()) {
            selectionState.addSelectedParkingOption(opt);
        }
        for (ShuttleOption opt : getSelectedShuttleOptions()) {
            selectionState.addSelectedShuttleOption(opt);
        }
    }
}
