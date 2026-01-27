package one.modality.booking.frontoffice.bookingpage.sections.accommodation;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import one.modality.base.shared.entities.Item;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import one.modality.booking.frontoffice.bookingpage.standard.BookingSelectionState;

import java.util.function.Consumer;

/**
 * Interface for the "Accommodation Selection" section of a booking form.
 * This section displays accommodation/room options and allows selection.
 *
 * <p>Used for festivals and events where users need to choose their room type.</p>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasAccommodationSelectionSection extends BookingFormSection, ResettableSection {

    /**
     * Availability status for an accommodation option.
     */
    enum AvailabilityStatus {
        /** Room is available for booking */
        AVAILABLE,
        /** Limited rooms remaining */
        LIMITED,
        /** Room is sold out */
        SOLD_OUT
    }

    /**
     * Constraint type for an accommodation option.
     */
    enum ConstraintType {
        /** No constraint */
        NONE,
        /** Only available when booking the full festival/event */
        FULL_EVENT_ONLY,
        /** Requires a minimum number of nights */
        MIN_NIGHTS
    }

    /**
     * Data class representing an accommodation option.
     */
    class AccommodationOption {
        private final Object itemId;
        private final Item itemEntity;
        private final String name;
        private final String description;
        private final int pricePerNight;
        private final AvailabilityStatus availability;
        private final ConstraintType constraintType;
        private final String constraintLabel;
        private final int minNights;
        private final boolean isDayVisitor;
        private final String imageUrl;
        private final boolean perPerson;  // true = price per person, false = price per room
        private final int preCalculatedTotalPrice;  // Pre-calculated total price from WorkingBooking (-1 = not set)
        private final boolean earlyArrivalAllowed;  // true if early arrival before main event is allowed
        private final boolean lateDepartureAllowed; // true if late departure after main event is allowed

        // Full constructor with all parameters including early/late arrival restrictions
        public AccommodationOption(Object itemId, Item itemEntity, String name, String description,
                                   int pricePerNight, AvailabilityStatus availability,
                                   ConstraintType constraintType, String constraintLabel,
                                   int minNights, boolean isDayVisitor, String imageUrl,
                                   boolean perPerson, int preCalculatedTotalPrice,
                                   boolean earlyArrivalAllowed, boolean lateDepartureAllowed) {
            this.itemId = itemId;
            this.itemEntity = itemEntity;
            this.name = name;
            this.description = description;
            this.pricePerNight = pricePerNight;
            this.availability = availability;
            this.constraintType = constraintType;
            this.constraintLabel = constraintLabel;
            this.minNights = minNights;
            this.isDayVisitor = isDayVisitor;
            this.imageUrl = imageUrl;
            this.perPerson = perPerson;
            this.preCalculatedTotalPrice = preCalculatedTotalPrice;
            this.earlyArrivalAllowed = earlyArrivalAllowed;
            this.lateDepartureAllowed = lateDepartureAllowed;
        }

        // Constructor with pre-calculated price (defaults to early/late allowed)
        public AccommodationOption(Object itemId, Item itemEntity, String name, String description,
                                   int pricePerNight, AvailabilityStatus availability,
                                   ConstraintType constraintType, String constraintLabel,
                                   int minNights, boolean isDayVisitor, String imageUrl,
                                   boolean perPerson, int preCalculatedTotalPrice) {
            this(itemId, itemEntity, name, description, pricePerNight, availability,
                 constraintType, constraintLabel, minNights, isDayVisitor, imageUrl, perPerson, preCalculatedTotalPrice,
                 true, true);
        }

        public AccommodationOption(Object itemId, Item itemEntity, String name, String description,
                                   int pricePerNight, AvailabilityStatus availability,
                                   ConstraintType constraintType, String constraintLabel,
                                   int minNights, boolean isDayVisitor, String imageUrl,
                                   boolean perPerson) {
            this(itemId, itemEntity, name, description, pricePerNight, availability,
                 constraintType, constraintLabel, minNights, isDayVisitor, imageUrl, perPerson, -1);
        }

        // Constructor without perPerson (defaults to true for backward compatibility)
        public AccommodationOption(Object itemId, Item itemEntity, String name, String description,
                                   int pricePerNight, AvailabilityStatus availability,
                                   ConstraintType constraintType, String constraintLabel,
                                   int minNights, boolean isDayVisitor, String imageUrl) {
            this(itemId, itemEntity, name, description, pricePerNight, availability,
                 constraintType, constraintLabel, minNights, isDayVisitor, imageUrl, true, -1);
        }

        // Simple constructor for common cases
        public AccommodationOption(Object itemId, String name, String description,
                                   int pricePerNight, AvailabilityStatus availability) {
            this(itemId, null, name, description, pricePerNight, availability,
                 ConstraintType.NONE, null, 0, false, null, true, -1);
        }

        public Object getItemId() { return itemId; }
        public Item getItemEntity() { return itemEntity; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getPricePerNight() { return pricePerNight; }
        public AvailabilityStatus getAvailability() { return availability; }
        public ConstraintType getConstraintType() { return constraintType; }
        public String getConstraintLabel() { return constraintLabel; }
        public int getMinNights() { return minNights; }
        public boolean isDayVisitor() { return isDayVisitor; }
        public String getImageUrl() { return imageUrl; }
        public boolean isPerPerson() { return perPerson; }
        public int getPreCalculatedTotalPrice() { return preCalculatedTotalPrice; }
        public boolean hasPreCalculatedPrice() { return preCalculatedTotalPrice >= 0; }
        public boolean isEarlyArrivalAllowed() { return earlyArrivalAllowed; }
        public boolean isLateDepartureAllowed() { return lateDepartureAllowed; }

        public boolean isAvailable() {
            return availability != AvailabilityStatus.SOLD_OUT;
        }

        public boolean hasConstraint() {
            return constraintType != ConstraintType.NONE;
        }

        /**
         * Returns true if this accommodation has date restrictions (no early arrival or no late departure).
         */
        public boolean hasDateRestrictions() {
            return !earlyArrivalAllowed || !lateDepartureAllowed;
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

    /**
     * Sets the total teaching price for the full event.
     * Used to display total cost (teaching + accommodation) on each option.
     */
    void setFullEventTeachingPrice(int price);

    /**
     * Sets the number of nights for the full event.
     * Used to calculate total accommodation cost.
     */
    void setFullEventNights(int nights);

    /**
     * Sets the total meals price for the full event.
     * Used to display total cost (teaching + accommodation + meals) on each option.
     */
    void setFullEventMealsPrice(int price);

    // === Data Management ===

    /**
     * Adds an accommodation option to the list.
     */
    void addAccommodationOption(AccommodationOption option);

    /**
     * Clears all accommodation options from the list.
     */
    void clearOptions();

    /**
     * Sets the currently selected option by item ID.
     */
    void setSelectedOption(Object itemId);

    /**
     * Returns the selected option property.
     */
    ObjectProperty<AccommodationOption> selectedOptionProperty();

    /**
     * Returns the currently selected option, or null if none selected.
     */
    default AccommodationOption getSelectedOption() {
        return selectedOptionProperty().get();
    }

    // === Callbacks ===

    /**
     * Sets the callback for when an option is selected.
     */
    void setOnOptionSelected(Consumer<AccommodationOption> callback);

    /**
     * Sets the callback for when the continue button is pressed.
     */
    void setOnContinuePressed(Runnable callback);

    /**
     * Sets the callback for when the back button is pressed.
     */
    void setOnBackPressed(Runnable callback);

    // === Validation ===

    /**
     * Returns an observable boolean indicating if this section is valid.
     * The section is valid when an accommodation option is selected.
     */
    @Override
    ObservableBooleanValue validProperty();

    // === Data Population ===

    /**
     * Populates accommodation options from PolicyAggregate data.
     * Checks availability using ScheduledItem.guestsAvailability and
     * constraints using ItemPolicy.minDay.
     *
     * @param policyAggregate the policy data containing scheduledItems and itemPolicies
     * @param limitedThreshold availability count below which to show as LIMITED (e.g., 5)
     */
    void populateFromPolicyAggregate(PolicyAggregate policyAggregate, int limitedThreshold);

    // === Selection State Binding ===

    /**
     * Binds this section to a BookingSelectionState.
     * When bound, the section will push selection changes to the state,
     * and can optionally read initial values from the state.
     *
     * <p>This supports the Selection Model Pattern where sections are pure UI
     * components and the selection state is the source of truth for user choices.</p>
     *
     * @param selectionState the selection state to bind to
     */
    default void bindToSelectionState(BookingSelectionState selectionState) {
        // Default implementation: set up bidirectional binding
        if (selectionState != null) {
            // Push changes from section to state
            selectedOptionProperty().addListener((obs, oldVal, newVal) -> {
                selectionState.setSelectedAccommodation(newVal);
            });
            // Initialize state from current section value
            if (getSelectedOption() != null) {
                selectionState.setSelectedAccommodation(getSelectedOption());
            }
        }
    }

    // === Reset ===

    /**
     * Resets the section to its initial state.
     * Default implementation clears the selected option.
     */
    @Override
    default void reset() {
        setSelectedOption(null);
    }
}
