package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.util.List;

/**
 * Interface for the "Additional Options" section of a booking form.
 * This section allows selection of additional services like parking, shuttle, and accessibility options.
 *
 * <p>Options are loaded dynamically from PolicyAggregate based on available Items in the database.</p>
 *
 * <p>Supported item families:</p>
 * <ul>
 *   <li>PARKING - Parking options</li>
 *   <li>TRANSPORT - Shuttle/transport options</li>
 *   <li>Any other additional service items configured for the event</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasAdditionalOptionsSection extends BookingFormSection {

    /**
     * Parking type options (legacy - kept for backward compatibility).
     */
    enum ParkingType {
        STANDARD,
        HANDICAP
    }

    /**
     * Represents a dynamically loaded additional option from the database.
     * Similar to AccommodationOption, this holds all the data needed to display and select an option.
     */
    class AdditionalOption {
        private final Object itemId;
        private final Item itemEntity;
        private final String name;
        private final String description;
        private final int price;  // Price in cents (per unit/day)
        private final KnownItemFamily itemFamily;  // The family this option belongs to
        private final boolean perDay;  // true = price per day, false = one-time
        private final boolean perPerson;  // true = price per person, false = flat rate
        private final String iconSvg;  // Optional SVG path for icon
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        public AdditionalOption(Object itemId, Item itemEntity, String name, String description,
                                int price, KnownItemFamily itemFamily, boolean perDay,
                                boolean perPerson, String iconSvg) {
            this.itemId = itemId;
            this.itemEntity = itemEntity;
            this.name = name;
            this.description = description;
            this.price = price;
            this.itemFamily = itemFamily;
            this.perDay = perDay;
            this.perPerson = perPerson;
            this.iconSvg = iconSvg;
        }

        public Object getItemId() { return itemId; }
        public Item getItemEntity() { return itemEntity; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getPrice() { return price; }
        public KnownItemFamily getItemFamily() { return itemFamily; }
        public boolean isPerDay() { return perDay; }
        public boolean isPerPerson() { return perPerson; }
        public String getIconSvg() { return iconSvg; }

        public BooleanProperty selectedProperty() { return selected; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean value) { selected.set(value); }
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

    /**
     * Sets the parking price per day.
     */
    void setParkingPricePerDay(int price);

    /**
     * Sets the shuttle price (one-way).
     */
    void setShuttlePrice(int price);

    /**
     * Sets the number of days for the booking (for calculating totals).
     */
    void setDaysCount(int count);

    // === Assisted Listening ===

    /**
     * Property for assisted listening device request.
     */
    BooleanProperty assistedListeningProperty();

    /**
     * Gets whether assisted listening is requested.
     */
    default boolean needsAssistedListening() {
        return assistedListeningProperty().get();
    }

    /**
     * Sets whether assisted listening is requested.
     */
    default void setNeedsAssistedListening(boolean needs) {
        assistedListeningProperty().set(needs);
    }

    // === Parking ===

    /**
     * Property for parking request.
     */
    BooleanProperty needsParkingProperty();

    /**
     * Gets whether parking is requested.
     */
    default boolean needsParking() {
        return needsParkingProperty().get();
    }

    /**
     * Sets whether parking is requested.
     */
    default void setNeedsParking(boolean needs) {
        needsParkingProperty().set(needs);
    }

    /**
     * Property for parking type.
     */
    ObjectProperty<ParkingType> parkingTypeProperty();

    /**
     * Gets the parking type.
     */
    default ParkingType getParkingType() {
        return parkingTypeProperty().get();
    }

    /**
     * Sets the parking type.
     */
    default void setParkingType(ParkingType type) {
        parkingTypeProperty().set(type);
    }

    // === Shuttle/Transport ===

    /**
     * Property for outbound shuttle request (to the event).
     */
    BooleanProperty shuttleOutboundProperty();

    /**
     * Gets whether outbound shuttle is requested.
     */
    default boolean needsShuttleOutbound() {
        return shuttleOutboundProperty().get();
    }

    /**
     * Sets whether outbound shuttle is requested.
     */
    default void setNeedsShuttleOutbound(boolean needs) {
        shuttleOutboundProperty().set(needs);
    }

    /**
     * Property for return shuttle request (from the event).
     */
    BooleanProperty shuttleReturnProperty();

    /**
     * Gets whether return shuttle is requested.
     */
    default boolean needsShuttleReturn() {
        return shuttleReturnProperty().get();
    }

    /**
     * Sets whether return shuttle is requested.
     */
    default void setNeedsShuttleReturn(boolean needs) {
        shuttleReturnProperty().set(needs);
    }

    // === Calculated Values ===

    /**
     * Returns the total parking cost based on days count.
     */
    int getTotalParkingCost();

    /**
     * Returns the total shuttle cost based on selections.
     */
    int getTotalShuttleCost();

    /**
     * Returns the total additional options cost.
     */
    default int getTotalAdditionalOptionsCost() {
        return getTotalParkingCost() + getTotalShuttleCost();
    }

    // === Dynamic Options API ===

    /**
     * Populates options from PolicyAggregate data.
     * This loads all available additional options from the database.
     *
     * @param policyAggregate the policy data containing scheduledItems and rates
     */
    void populateFromPolicyAggregate(PolicyAggregate policyAggregate);

    /**
     * Returns all dynamically loaded options.
     */
    List<AdditionalOption> getOptions();

    /**
     * Returns all selected options.
     */
    default List<AdditionalOption> getSelectedOptions() {
        return getOptions().stream()
            .filter(AdditionalOption::isSelected)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Returns options filtered by item family.
     */
    default List<AdditionalOption> getOptionsByFamily(KnownItemFamily family) {
        return getOptions().stream()
            .filter(opt -> opt.getItemFamily() == family)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Clears all options.
     */
    void clearOptions();

    /**
     * Adds a single option.
     */
    void addOption(AdditionalOption option);

    /**
     * Sets a callback to be invoked when any option selection changes.
     * This allows the booking form to re-book items when options are selected/deselected.
     *
     * @param callback the callback to invoke on selection change
     */
    void setOnSelectionChanged(Runnable callback);
}
