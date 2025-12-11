package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import one.modality.base.shared.entities.BookablePeriod;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.List;

/**
 * Interface for an audio recording selection section of a booking form.
 * This section allows users to optionally select audio recordings for their booking.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Displays available audio recording items as selectable cards</li>
 *   <li>Calculates prices based on the selected programme dates</li>
 *   <li>Supports multi-selection of recordings</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasAudioRecordingSection extends BookingFormSection, ResettableSection {

    /**
     * Data class holding a selected recording item with its price.
     */
    class SelectedRecording {
        private final Item item;

        public SelectedRecording(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }

    }

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

    /**
     * Sets the selected programme period for price calculation.
     */
    void setSelectedProgramme(BookablePeriod programme);

    /**
     * Returns the list of ScheduledItems for a specific audio recording item.
     */
    List<ScheduledItem> getScheduledItemsForRecording(Item recordingItem);

    /**
     * Resets the section to its initial state (clears all selections).
     */
    void reset();
}
