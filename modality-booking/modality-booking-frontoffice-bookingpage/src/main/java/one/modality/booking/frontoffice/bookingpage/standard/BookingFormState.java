package one.modality.booking.frontoffice.bookingpage.standard;

import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.sections.member.HasMemberSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.user.HasYourInformationSection;

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
 *   <li>Stored user info for payment/confirmation flow</li>
 *   <li>Queue and sold-out recovery tracking flags</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class BookingFormState {

    private final WorkingBookingProperties workingBookingProperties;

    // Authentication state
    private Person loggedInPerson;
    private boolean hadLoggedInUser = false;  // Tracks if user was logged in (to detect logout)

    // Member selection state
    private HasMemberSelectionSection.MemberInfo selectedMember;
    private HasYourInformationSection.NewUserData pendingNewUserData;

    // Stored user info for payment/confirmation flow (persists after submission clears state)
    private String storedNewUserName;
    private String storedNewUserEmail;

    // Navigation flags
    private boolean allowMemberReselection = false;

    // Queue handling flags
    private boolean registrationConfirmedOpen = false;  // Server accepted submission

    // Sold-out recovery flags
    private boolean returnedFromSoldOutRecovery = false;  // Returning to Summary from recovery

    /**
     * Creates a new booking form state.
     *
     * @param workingBookingProperties The working booking properties from the activity
     */
    public BookingFormState(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
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

    // === Stored User Info (for payment/confirmation after submission) ===

    public String getStoredNewUserName() {
        return storedNewUserName;
    }

    public void setStoredNewUserName(String name) {
        this.storedNewUserName = name;
    }

    public String getStoredNewUserEmail() {
        return storedNewUserEmail;
    }

    public void setStoredNewUserEmail(String email) {
        this.storedNewUserEmail = email;
    }

    public void clearStoredNewUserInfo() {
        this.storedNewUserName = null;
        this.storedNewUserEmail = null;
    }

    // === Login Tracking ===

    public boolean hadLoggedInUser() {
        return hadLoggedInUser;
    }

    public void setHadLoggedInUser(boolean hadLoggedIn) {
        this.hadLoggedInUser = hadLoggedIn;
    }

    // === Queue Handling ===

    public boolean isRegistrationConfirmedOpen() {
        return registrationConfirmedOpen;
    }

    public void setRegistrationConfirmedOpen(boolean confirmed) {
        this.registrationConfirmedOpen = confirmed;
    }

    // === Sold-Out Recovery ===

    public boolean isReturnedFromSoldOutRecovery() {
        return returnedFromSoldOutRecovery;
    }

    public void setReturnedFromSoldOutRecovery(boolean returned) {
        this.returnedFromSoldOutRecovery = returned;
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
        storedNewUserName = null;
        storedNewUserEmail = null;
        hadLoggedInUser = false;
        registrationConfirmedOpen = false;
        returnedFromSoldOutRecovery = false;
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
        storedNewUserName = null;
        storedNewUserEmail = null;
        returnedFromSoldOutRecovery = false;
        // Note: hadLoggedInUser and registrationConfirmedOpen are NOT reset here
        // as they track session-level state, not per-booking state
        if (workingBookingProperties != null) {
            // Start a completely fresh booking (new Document), not just revert changes
            workingBookingProperties.getWorkingBooking().startNewBooking();
        }
    }

}
