package one.modality.booking.frontoffice.bookingpage.sections.options;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;
import one.modality.booking.frontoffice.bookingpage.standard.BookingSelectionState;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.time.LocalTime;
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
public interface HasAdditionalOptionsSection extends BookingFormSection, ResettableSection {

    /**
     * Parking type options.
     * @deprecated Parking is now handled by {@link HasTransportSection}. Use
     *             {@link HasTransportSection#getSelectedParkingType()} instead.
     */
    @Deprecated
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

    /**
     * Represents a ceremony option loaded from ScheduledItem.
     * Contains temporal information (date, time) and comment.
     * Ceremonies are always free - no price field needed.
     */
    class CeremonyOption {
        private final Object scheduledItemId;
        private final ScheduledItem scheduledItem;
        private final Item item;
        private final String name;
        private final LocalDate date;
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final String comment;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        public CeremonyOption(Object scheduledItemId, ScheduledItem scheduledItem, Item item,
                              String name, LocalDate date, LocalTime startTime, LocalTime endTime,
                              String comment) {
            this.scheduledItemId = scheduledItemId;
            this.scheduledItem = scheduledItem;
            this.item = item;
            this.name = name;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.comment = comment;
        }

        public Object getScheduledItemId() { return scheduledItemId; }
        public ScheduledItem getScheduledItem() { return scheduledItem; }
        public Item getItem() { return item; }
        public String getName() { return name; }
        public LocalDate getDate() { return date; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public String getComment() { return comment; }

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

    // === Parking (DEPRECATED - use HasTransportSection instead) ===

    /**
     * Property for parking request.
     * @deprecated Parking is now handled by {@link HasTransportSection}. Use
     *             {@link HasTransportSection#parkingEnabledProperty()} instead.
     */
    @Deprecated
    BooleanProperty needsParkingProperty();

    /**
     * Gets whether parking is requested.
     * @deprecated Parking is now handled by {@link HasTransportSection}. Use
     *             {@link HasTransportSection#isParkingEnabled()} instead.
     */
    @Deprecated
    default boolean needsParking() {
        return needsParkingProperty().get();
    }

    /**
     * Sets whether parking is requested.
     * @deprecated Parking is now handled by {@link HasTransportSection}. Use
     *             {@link HasTransportSection#setParkingEnabled(boolean)} instead.
     */
    @Deprecated
    default void setNeedsParking(boolean needs) {
        needsParkingProperty().set(needs);
    }

    /**
     * Property for parking type.
     * @deprecated Parking is now handled by {@link HasTransportSection}. Use
     *             {@link HasTransportSection#selectedParkingTypeProperty()} instead.
     */
    @Deprecated
    ObjectProperty<ParkingType> parkingTypeProperty();

    /**
     * Gets the parking type.
     * @deprecated Parking is now handled by {@link HasTransportSection}. Use
     *             {@link HasTransportSection#getSelectedParkingType()} instead.
     */
    @Deprecated
    default ParkingType getParkingType() {
        return parkingTypeProperty().get();
    }

    /**
     * Sets the parking type.
     * @deprecated Parking is now handled by {@link HasTransportSection}. Use
     *             {@link HasTransportSection#setSelectedParkingType(HasTransportSection.ParkingOption)} instead.
     */
    @Deprecated
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

    // === Ceremony Options API ===

    /**
     * Returns all ceremony options loaded from ScheduledItems.
     */
    List<CeremonyOption> getCeremonyOptions();

    /**
     * Returns all selected ceremony options.
     */
    default List<CeremonyOption> getSelectedCeremonyOptions() {
        return getCeremonyOptions().stream()
            .filter(CeremonyOption::isSelected)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Clears all ceremony options.
     */
    void clearCeremonyOptions();

    /**
     * Adds a single ceremony option.
     */
    void addCeremonyOption(CeremonyOption option);

    // === Selection State Binding ===

    /**
     * Binds this section to the centralized BookingSelectionState.
     * Changes in the section will be pushed to the state.
     *
     * @param selectionState the centralized state to bind to
     */
    default void bindToSelectionState(BookingSelectionState selectionState) {
        if (selectionState == null) return;

        // Set up listener to push additional option changes to state
        for (AdditionalOption option : getOptions()) {
            option.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    if (!selectionState.getSelectedAdditionalOptions().contains(option)) {
                        selectionState.addSelectedAdditionalOption(option);
                    }
                } else {
                    selectionState.getSelectedAdditionalOptions().remove(option);
                }
            });
        }

        // Set up listener to push ceremony option changes to state
        for (CeremonyOption option : getCeremonyOptions()) {
            option.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    if (!selectionState.getSelectedCeremonyOptions().contains(option)) {
                        selectionState.addSelectedCeremonyOption(option);
                    }
                } else {
                    selectionState.getSelectedCeremonyOptions().remove(option);
                }
            });
        }

        // Initialize state from current section values
        selectionState.clearSelectedAdditionalOptions();
        selectionState.clearSelectedCeremonyOptions();
        for (AdditionalOption opt : getSelectedOptions()) {
            selectionState.addSelectedAdditionalOption(opt);
        }
        for (CeremonyOption opt : getSelectedCeremonyOptions()) {
            selectionState.addSelectedCeremonyOption(opt);
        }
    }

    // === Reset ===

    /**
     * Resets the section to its initial state.
     * Default implementation deselects all options and ceremonies, and resets deprecated properties.
     */
    @Override
    default void reset() {
        // Deselect all additional options
        for (AdditionalOption opt : getOptions()) {
            opt.setSelected(false);
        }
        // Deselect all ceremony options
        for (CeremonyOption opt : getCeremonyOptions()) {
            opt.setSelected(false);
        }
        // Reset deprecated properties
        setNeedsAssistedListening(false);
        setNeedsParking(false);
        setNeedsShuttleOutbound(false);
        setNeedsShuttleReturn(false);
    }
}
