package one.modality.booking.frontoffice.bookingpage.sections.existing;

import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.sections.member.HasMemberSelectionSection;
import one.modality.ecommerce.document.service.DocumentAggregate;

import java.util.function.Consumer;

/**
 * Interface for the "Existing Booking" section of a General Program booking form.
 * This section is shown when the user has household members with existing bookings for the event.
 *
 * <p>The section allows users to:</p>
 * <ul>
 *   <li>Select an existing booking to modify (add more classes)</li>
 *   <li>Select a member without a booking to create a new one</li>
 * </ul>
 *
 * <p>This section auto-skips (via isApplicableToBooking) when there are no existing bookings
 * and no other household members to book for.</p>
 *
 * @author Claude
 * @see BookingFormSection
 */
public interface HasExistingBookingSection extends BookingFormSection {

    /**
     * The type of selection made by the user.
     */
    enum SelectionType {
        /** User wants to modify an existing booking (add more classes) */
        MODIFY_EXISTING_BOOKING,
        /** User wants to create a new booking for a member */
        CREATE_NEW_BOOKING
    }

    /**
     * Sets the callback for when the selection type changes.
     * Called when user switches between modifying an existing booking
     * and creating a new booking for another member.
     *
     * @param callback the callback to invoke with the new selection type
     */
    void setOnSelectionTypeChanged(Consumer<SelectionType> callback);

    /**
     * Sets the callback for when an existing booking is selected for modification.
     * The DocumentAggregate contains the full booking data needed to recreate
     * the WorkingBooking with the existing booking's state.
     *
     * @param callback the callback to invoke with the selected DocumentAggregate
     */
    void setOnDocumentAggregateSelected(Consumer<DocumentAggregate> callback);

    /**
     * Sets the callback for when a member is selected for a new booking.
     * The MemberInfo contains the person's details for the new booking.
     *
     * @param callback the callback to invoke with the selected member info
     */
    void setOnMemberSelected(Consumer<HasMemberSelectionSection.MemberInfo> callback);

    /**
     * Sets the callback for when the continue button is pressed.
     * Called after the user has made a selection and wants to proceed.
     *
     * @param callback the callback to invoke
     */
    void setOnContinuePressed(Runnable callback);
}
