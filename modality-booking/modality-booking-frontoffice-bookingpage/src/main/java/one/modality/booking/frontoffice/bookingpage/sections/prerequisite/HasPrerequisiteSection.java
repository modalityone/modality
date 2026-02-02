package one.modality.booking.frontoffice.bookingpage.sections.prerequisite;

import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.ResettableSection;

/**
 * Interface for prerequisite/confirmation sections.
 * These sections require user confirmation before proceeding with a booking.
 *
 * <p>Prerequisite sections typically display important information that
 * the user must acknowledge before continuing, such as:</p>
 * <ul>
 *   <li>Residency requirements</li>
 *   <li>Event-specific restrictions</li>
 *   <li>Terms and conditions acknowledgment</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see DefaultPrerequisiteSection
 */
public interface HasPrerequisiteSection extends BookingFormSection, ResettableSection {

    /**
     * Returns whether the user has confirmed the prerequisite.
     *
     * @return true if the user has confirmed, false otherwise
     */
    boolean isConfirmed();

    /**
     * Sets the confirmation state.
     *
     * @param confirmed true to mark as confirmed, false to clear
     */
    void setConfirmed(boolean confirmed);
}
