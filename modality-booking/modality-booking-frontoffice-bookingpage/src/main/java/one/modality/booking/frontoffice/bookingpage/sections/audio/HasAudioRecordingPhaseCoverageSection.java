package one.modality.booking.frontoffice.bookingpage.sections.audio;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.EventPhaseCoverage;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;
import one.modality.booking.frontoffice.bookingpage.standard.BookingSelectionState;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interface for an audio recording phase coverage selection section.
 * This section allows users to select from different phase coverage options
 * for audio recordings (e.g., Full Festival, Weekend 1 Only, Weekend 2 Only).
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Displays available phase coverage options as radio button cards</li>
 *   <li>Shows date range and price for each option</li>
 *   <li>Single selection (radio group behavior)</li>
 *   <li>Optional "No Audio Recordings" option</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 * @see EventPhaseCoverage
 */
public interface HasAudioRecordingPhaseCoverageSection extends BookingFormSection, ResettableSection {

    /**
     * Data class representing an audio recording phase coverage option.
     * Contains the phase coverage entity, the audio recording Item (language),
     * and the list of ScheduledItems to book when selected.
     */
    class AudioRecordingPhaseOption {
        private final EventPhaseCoverage phaseCoverage;
        private final Item audioRecordingItem;  // The Item (language) for this option
        private final List<ScheduledItem> scheduledItems;  // ScheduledItems to book when selected
        private final String name;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int price;  // Calculated using PriceCalculator
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final BooleanProperty available = new SimpleBooleanProperty(true);  // Whether this option can be selected

        /**
         * Creates a phase option for a specific PhaseCoverage and audio recording Item (language).
         *
         * @param phaseCoverage the phase coverage (date range)
         * @param audioRecordingItem the Item representing the audio recording (language)
         * @param scheduledItems the ScheduledItems within this phase for this Item
         * @param name display name (e.g., "Full Festival - English")
         * @param startDate start date of the phase
         * @param endDate end date of the phase
         * @param price calculated price for this option
         */
        public AudioRecordingPhaseOption(EventPhaseCoverage phaseCoverage, Item audioRecordingItem,
                                         List<ScheduledItem> scheduledItems, String name,
                                         LocalDate startDate, LocalDate endDate, int price) {
            this.phaseCoverage = phaseCoverage;
            this.audioRecordingItem = audioRecordingItem;
            this.scheduledItems = scheduledItems != null ? scheduledItems : Collections.emptyList();
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
            this.price = price;
        }

        /**
         * Creates a "No Audio Recordings" option (null phaseCoverage and item).
         */
        public AudioRecordingPhaseOption(String name) {
            this.phaseCoverage = null;
            this.audioRecordingItem = null;
            this.scheduledItems = Collections.emptyList();
            this.name = name;
            this.startDate = null;
            this.endDate = null;
            this.price = 0;
        }

        public EventPhaseCoverage getPhaseCoverage() { return phaseCoverage; }
        public Item getAudioRecordingItem() { return audioRecordingItem; }
        public List<ScheduledItem> getScheduledItems() { return scheduledItems; }
        public String getName() { return name; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public int getPrice() { return price; }

        public BooleanProperty selectedProperty() { return selected; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean value) { selected.set(value); }

        public BooleanProperty availableProperty() { return available; }
        public boolean isAvailable() { return available.get(); }
        public void setAvailable(boolean value) { available.set(value); }

        /**
         * Returns true if this is the "No Audio Recordings" option.
         */
        public boolean isNoRecordingOption() {
            return phaseCoverage == null && audioRecordingItem == null;
        }

        /**
         * Returns true if this option has a date range to display.
         */
        public boolean hasDateRange() {
            return startDate != null && endDate != null;
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

    // === Date Binding (for availability) ===

    /**
     * Returns the arrival date property.
     * Used to determine which phase options are available.
     */
    ObjectProperty<LocalDate> arrivalDateProperty();

    /**
     * Gets the arrival date.
     */
    default LocalDate getArrivalDate() {
        return arrivalDateProperty().get();
    }

    /**
     * Sets the arrival date. This updates phase option availability.
     */
    default void setArrivalDate(LocalDate date) {
        arrivalDateProperty().set(date);
    }

    /**
     * Returns the departure date property.
     * Used to determine which phase options are available.
     */
    ObjectProperty<LocalDate> departureDateProperty();

    /**
     * Gets the departure date.
     */
    default LocalDate getDepartureDate() {
        return departureDateProperty().get();
    }

    /**
     * Sets the departure date. This updates phase option availability.
     */
    default void setDepartureDate(LocalDate date) {
        departureDateProperty().set(date);
    }

    /**
     * Updates the availability of phase options based on booked dates.
     * A phase option is available only if the user's stay covers ALL days in the phase.
     */
    void updateOptionsAvailability();

    // === Data Population ===

    /**
     * Populates the section with phase coverage options from PolicyAggregate.
     * Uses PolicyAggregate.getAudioRecordingPhaseCoverages() to get available options.
     *
     * @param policyAggregate the policy data containing phase coverages and rates
     */
    void populateFromPolicyAggregate(PolicyAggregate policyAggregate);

    // === Selection ===

    /**
     * Returns the property holding the currently selected option.
     */
    ObjectProperty<AudioRecordingPhaseOption> selectedOptionProperty();

    /**
     * Gets the currently selected phase option, or null if none selected.
     */
    default AudioRecordingPhaseOption getSelectedOption() {
        return selectedOptionProperty().get();
    }

    /**
     * Sets a callback to be notified when a phase option is selected.
     * The callback receives the selected option (null if "No Audio Recordings" is selected).
     */
    void setOnOptionSelected(Consumer<AudioRecordingPhaseOption> callback);

    // === Options Access ===

    /**
     * Returns all available phase coverage options.
     */
    List<AudioRecordingPhaseOption> getOptions();

    /**
     * Clears all options from the section.
     */
    void clearOptions();

    /**
     * Adds a phase coverage option to the section.
     */
    void addOption(AudioRecordingPhaseOption option);

    // === Reset ===

    /**
     * Resets the section to its initial state (selects "No Audio Recordings" if available).
     */
    @Override
    void reset();

    // === Selection State Binding ===

    /**
     * Binds this section to the centralized BookingSelectionState.
     * Changes in the section will be pushed to the state.
     *
     * @param selectionState the centralized state to bind to
     */
    default void bindToSelectionState(BookingSelectionState selectionState) {
        if (selectionState == null) return;

        // Push changes from section to state
        selectedOptionProperty().addListener((obs, oldVal, newVal) -> {
            selectionState.setSelectedAudioPhase(newVal);
        });

        // Initialize state from current section value
        if (getSelectedOption() != null) {
            selectionState.setSelectedAudioPhase(getSelectedOption());
        }
    }
}
