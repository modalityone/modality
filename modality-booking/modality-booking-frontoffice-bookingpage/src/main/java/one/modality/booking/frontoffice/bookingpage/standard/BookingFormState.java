package one.modality.booking.frontoffice.bookingpage.standard;

import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.sections.HasMemberSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.HasYourInformationSection;

/**
 * Holds the transient state of a booking form session.
 * This encapsulates the user's progress through the booking flow.
 *
 * <p>State includes:</p>
 * <ul>
 *   <li>The logged-in person (null if guest checkout)</li>
 *   <li>The selected member to book for</li>
 *   <li>Pending new user data (for guest checkout)</li>
 *   <li>Navigation flags like allowMemberReselection</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class BookingFormState {

    private final WorkingBookingProperties workingBookingProperties;

    // Authentication state
    private Person loggedInPerson;

    // Member selection state
    private HasMemberSelectionSection.MemberInfo selectedMember;
    private HasYourInformationSection.NewUserData pendingNewUserData;

    // Navigation flags
    private boolean allowMemberReselection = false;

    /**
     * Creates a new booking form state.
     *
     * @param workingBookingProperties The working booking properties from the activity
     */
    public BookingFormState(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    // === Working Booking ===

    public WorkingBookingProperties getWorkingBookingProperties() {
        return workingBookingProperties;
    }

    // === Logged-in Person ===

    public void setLoggedInPerson(Person person) {
        this.loggedInPerson = person;
    }

    public boolean isLoggedIn() {
        return loggedInPerson != null;
    }

    // === Selected Member ===

    public HasMemberSelectionSection.MemberInfo getSelectedMember() {
        return selectedMember;
    }

    public void setSelectedMember(HasMemberSelectionSection.MemberInfo member) {
        this.selectedMember = member;
    }

    // === Pending New User (Guest Checkout) ===

    public HasYourInformationSection.NewUserData getPendingNewUserData() {
        return pendingNewUserData;
    }

    public void setPendingNewUserData(HasYourInformationSection.NewUserData data) {
        this.pendingNewUserData = data;
    }

    public boolean hasPendingNewUser() {
        return pendingNewUserData != null;
    }

    // === Navigation Flags ===

    public void setAllowMemberReselection(boolean allow) {
        this.allowMemberReselection = allow;
    }

    // === Reset ===

    /**
     * Resets all state for a new booking flow.
     * Called when the user completes a booking and wants to make another,
     * or when they reset the form.
     */
    public void reset() {
        loggedInPerson = null;
        selectedMember = null;
        pendingNewUserData = null;
        allowMemberReselection = false;
        if (workingBookingProperties != null) {
            workingBookingProperties.getWorkingBooking().cancelChanges();
        }
    }

    /**
     * Prepares state for booking another person (same user, different attendee).
     * Called when the user clicks "Book Another Person" from Pending Bookings.
     * Uses startNewBooking() to create a fresh Document entity instead of reverting
     * to a previously submitted booking.
     */
    public void prepareForNewBooking() {
        selectedMember = null;
        pendingNewUserData = null;
        allowMemberReselection = true;
        if (workingBookingProperties != null) {
            // Start a completely fresh booking (new Document), not just revert changes
            workingBookingProperties.getWorkingBooking().startNewBooking();
        }
    }

}
