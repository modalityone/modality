package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Interface for a shuttle/transport options section with grouped UI.
 * This section displays airport shuttle options (outbound/return trips) in a single
 * grouped container, following the design pattern from the JSX mockup.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Displays shuttle options grouped in a single container with header</li>
 *   <li>Shows outbound trip (to venue) and return trip (from venue)</li>
 *   <li>Trips are only available when arrival/departure date matches the scheduled date</li>
 *   <li>Independent checkbox selection for each trip</li>
 *   <li>Total price shown in header when any trip is selected</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasShuttleOptionsSection extends BookingFormSection, ResettableSection {

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
        private final String name;  // Display name from Item (e.g., "JFK → Glen Spey")
        private final String description;  // Optional description
        private final int price;  // Price in cents
        private final LocalDate scheduledDate;  // Date when this shuttle runs
        private final ShuttleDirection direction;
        private final List<ScheduledItem> scheduledItems;  // ScheduledItems to book when selected
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final BooleanProperty available = new SimpleBooleanProperty(false);  // Available based on date matching

        /**
         * Creates a shuttle option for a specific trip.
         *
         * @param itemId the Item primary key
         * @param itemEntity the Item entity
         * @param name display name (from database)
         * @param description optional description
         * @param price price in cents
         * @param scheduledDate the date this shuttle runs
         * @param direction outbound or return
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

    // === Configuration ===

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

    // === Date Binding ===

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

    // === Selection Properties ===

    /**
     * Property for outbound shuttle selection state.
     */
    BooleanProperty shuttleOutboundSelectedProperty();

    /**
     * Returns true if an outbound shuttle is selected.
     */
    default boolean needsShuttleOutbound() {
        return shuttleOutboundSelectedProperty().get();
    }

    /**
     * Property for return shuttle selection state.
     */
    BooleanProperty shuttleReturnSelectedProperty();

    /**
     * Returns true if a return shuttle is selected.
     */
    default boolean needsShuttleReturn() {
        return shuttleReturnSelectedProperty().get();
    }

    // === Data Population ===

    /**
     * Populates the section with shuttle options from PolicyAggregate.
     * Loads transport items (KnownItemFamily.TRANSPORT) and their ScheduledItems.
     *
     * @param policyAggregate the policy data containing scheduledItems and rates
     */
    void populateFromPolicyAggregate(PolicyAggregate policyAggregate);

    // === Options Access ===

    /**
     * Returns all shuttle options.
     */
    List<ShuttleOption> getOptions();

    /**
     * Returns all selected shuttle options.
     */
    default List<ShuttleOption> getSelectedOptions() {
        return getOptions().stream()
            .filter(ShuttleOption::isSelected)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Returns true if there are any shuttle options available.
     */
    default boolean hasOptions() {
        return !getOptions().isEmpty();
    }

    /**
     * Clears all options from the section.
     */
    void clearOptions();

    /**
     * Adds a shuttle option to the section.
     */
    void addOption(ShuttleOption option);

    // === Callbacks ===

    /**
     * Sets a callback to be invoked when any shuttle selection changes.
     * This allows the booking form to re-book items when shuttles are selected/deselected.
     *
     * @param callback the callback to invoke on selection change
     */
    void setOnSelectionChanged(Runnable callback);

    // === Pricing ===

    /**
     * Returns the total shuttle cost based on selected options.
     */
    default int getTotalShuttleCost() {
        return getSelectedOptions().stream()
            .mapToInt(ShuttleOption::getPrice)
            .sum();
    }

    // === Reset ===

    /**
     * Resets the section to its initial state (all shuttles deselected).
     */
    @Override
    void reset();
}
