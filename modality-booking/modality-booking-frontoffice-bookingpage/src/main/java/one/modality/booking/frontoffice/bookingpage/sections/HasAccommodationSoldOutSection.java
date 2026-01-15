package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.List;
import java.util.function.Consumer;

/**
 * Interface for the "Accommodation Sold Out Recovery" section.
 *
 * <p>This section is displayed when the user's selected accommodation becomes
 * unavailable (sold out) during booking submission. It shows the original selection
 * and available alternatives, allowing the user to choose a different option.</p>
 *
 * <p>Design principles (from UX mockup):</p>
 * <ul>
 *   <li>Warm amber tone (not alarming red) - "bump in the road, not disaster"</li>
 *   <li>Show what was originally selected with "Sold Out" badge</li>
 *   <li>Immediately offer alternatives using existing card components</li>
 *   <li>Preserve user's progress (other selections remain unchanged)</li>
 *   <li>Single action required - select new accommodation, then retry submission</li>
 * </ul>
 *
 * @author Claude Code
 * @see HasAccommodationSelectionSection
 */
public interface HasAccommodationSoldOutSection extends BookingFormSection {

    // === Configuration ===

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

    // === Original Selection Info ===

    /**
     * Sets information about the accommodation that is now sold out.
     *
     * @param itemName     Name of the sold-out accommodation (e.g., "Single Room")
     * @param originalPrice Total price that was shown for the sold-out option (in cents)
     */
    void setOriginalSelection(String itemName, int originalPrice);

    /**
     * Sets the event name for context in the explanation message.
     *
     * @param eventName Name of the event (e.g., "US Summer Festival 2025")
     */
    void setEventName(String eventName);

    /**
     * Sets the number of nights for calculating total accommodation price.
     * Used to display "X for Y nights" secondary price on each option card.
     *
     * @param nights Number of nights the user is booking
     */
    void setNumberOfNights(int nights);

    // === Alternative Options ===

    /**
     * Sets the list of alternative accommodation options.
     * The sold-out item should already be filtered out.
     *
     * @param options List of available accommodation options
     */
    void setAlternativeOptions(List<HasAccommodationSelectionSection.AccommodationOption> options);

    /**
     * Clears all alternative options.
     */
    void clearOptions();

    /**
     * Returns the selected alternative option property.
     */
    ObjectProperty<HasAccommodationSelectionSection.AccommodationOption> selectedOptionProperty();

    /**
     * Returns the currently selected alternative option, or null if none selected.
     */
    default HasAccommodationSelectionSection.AccommodationOption getSelectedOption() {
        return selectedOptionProperty().get();
    }

    // === Callbacks ===

    /**
     * Sets the callback for when an alternative option is selected.
     *
     * @param callback Consumer that receives the selected option
     */
    void setOnOptionSelected(Consumer<HasAccommodationSelectionSection.AccommodationOption> callback);

    // === Validation ===

    /**
     * Returns an observable boolean indicating if this section is valid.
     * The section is valid when an alternative accommodation option is selected.
     */
    @Override
    ObservableBooleanValue validProperty();

    /**
     * Returns a validation message to display when the section is invalid.
     * Typically "Please select an accommodation option".
     */
    String getValidationMessage();
}
