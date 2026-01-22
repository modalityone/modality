package one.modality.booking.frontoffice.bookingpage.standard;

import dev.webfx.platform.async.Future;
import one.modality.booking.frontoffice.bookingpage.sections.DefaultSummarySection;
import one.modality.booking.frontoffice.bookingpage.sections.HasAccommodationSelectionSection;

import java.util.Collections;
import java.util.List;

/**
 * Simplified callbacks interface for form-specific behavior in standard booking forms.
 *
 * <p>Most booking form logic is handled automatically by {@link StandardBookingForm}:</p>
 * <ul>
 *   <li>State management via {@link BookingFormState}</li>
 *   <li>Household member loading via {@link HouseholdMemberLoader}</li>
 *   <li>Navigation between steps</li>
 *   <li>Payment processing</li>
 * </ul>
 *
 * <p>This interface only defines the hooks that vary between different booking forms.
 * Most forms only need to implement {@link #updateSummary()}
 * to add their custom price lines.</p>
 *
 * @author Bruno Salmon
 * @see StandardBookingForm
 * @see BookingFormState
 */
public interface StandardBookingFormCallbacks {

    // === Optional Hooks (with default implementations) ===

    /**
     * Updates the summary section with form-specific price lines and options.
     *
     * <p>This hook is called AFTER default price lines are added from WorkingBooking.
     * Override this to add additional custom price lines or modify the summary.</p>
     *
     * <p>The default implementation does nothing - price lines are auto-generated
     * from WorkingBooking document lines by StandardBookingForm.</p>
     *
     * <p>If you need custom formatting, override this method:</p>
     * <ul>
     *   <li>Add price lines using {@link DefaultSummarySection#addPriceLine}</li>
     *   <li>Add additional options using {@link DefaultSummarySection#addAdditionalOption}</li>
     *   <li>Set rate type if applicable using {@link DefaultSummarySection#setRateType}</li>
     * </ul>
     *
     */
    default void updateSummary() {
        // Default: no additional price lines needed - auto-generated from WorkingBooking
    }

    /**
     * Called when the booking should be submitted to the database.
     *
     * <p>Override this to implement form-specific submission logic.
     * The default implementation does nothing (for forms that handle submission elsewhere).</p>
     *
     * @return Future that completes when booking is submitted
     */
    default Future<Void> onSubmitBooking() {
        return Future.succeededFuture();
    }

    /**
     * Called after login success, before navigating to member selection.
     *
     * <p>Override this to perform form-specific actions after login.
     * The default implementation does nothing.</p>
     *
     */
    default void onAfterLogin() {
        // Default: no additional action needed
    }

    /**
     * Called after member selection, before navigating to summary.
     *
     * <p>Override this to perform form-specific actions after member selection.
     * The default implementation does nothing.</p>
     *
     */
    default void onAfterMemberSelected() {
        // Default: no additional action needed
    }

    /**
     * Called before the summary page is populated with price breakdown.
     *
     * <p>This hook is called immediately before updateSummaryWithAttendee(), which populates
     * the price breakdown from WorkingBooking document lines. Override this to book
     * form-specific items (accommodation, meals, options) into the WorkingBooking so they
     * appear in the price breakdown.</p>
     *
     * <p>This is the recommended place for forms with custom option selection to call
     * workingBooking.bookScheduledItems() for user-selected options.</p>
     *
     * <p>The default implementation does nothing.</p>
     */
    default void onBeforeSummary() {
        // Default: no additional action needed
    }

    /**
     * Called after payment completes, before navigating to confirmation.
     *
     * <p>Override this to perform form-specific actions after payment.
     * The default implementation does nothing.</p>
     *
     */
    default void onAfterPayment() {
        // Default: no additional action needed
    }

    /**
     * Called when preparing to register another person (from Pending Bookings page).
     *
     * <p>Override this to reset form-specific sections (e.g., audio recordings).
     * The WorkingBooking has already been reset via {@link BookingFormState#prepareForNewBooking()}.
     * The default implementation does nothing.</p>
     *
     */
    default void onPrepareNewBooking() {
        // Default: no additional action needed
    }

    /**
     * Called when entering the sold-out recovery page, BEFORE the page is displayed.
     *
     * <p>This callback is triggered when the sold-out recovery flow starts, giving the form
     * an opportunity to clean up the previous accommodation state before showing alternatives.
     * Override this to:</p>
     * <ul>
     *   <li>Cancel/remove old accommodation document lines from WorkingBooking</li>
     *   <li>Reset roommate info section data (isRoomBooker, visibility, etc.)</li>
     *   <li>Clear any form-specific accommodation state</li>
     * </ul>
     *
     * <p>This ensures the booking starts fresh when the user selects a new accommodation
     * option from the sold-out recovery dialog.</p>
     *
     * <p>The default implementation does nothing.</p>
     */
    default void onEnteringSoldOutRecovery() {
        // Default: no cleanup needed
    }

    /**
     * Called when accommodation selection needs to be updated due to sold-out error.
     *
     * <p>This callback is triggered when the server returns a SOLDOUT error during
     * booking submission because the selected accommodation became unavailable.
     * The form should update its accommodation selection with the new choice and
     * then call the provided continueToSummary runnable to navigate back to the
     * Summary page for retry.</p>
     *
     * <p>Override this to:</p>
     * <ul>
     *   <li>Update the form's accommodation section with the new selection</li>
     *   <li>Re-book items with the new accommodation</li>
     *   <li>Handle roommate info if provided (for double rooms or share accommodation)</li>
     *   <li>Call continueToSummary.run() when ready to retry submission</li>
     * </ul>
     *
     * <p>The default implementation just calls continueToSummary immediately.</p>
     *
     * @param newOption The newly selected accommodation option
     * @param roommateInfo Roommate information collected in the sold-out recovery dialog (may be null)
     * @param continueToSummary Runnable to call when ready to navigate back to Summary
     */
    default void onAccommodationSoldOutRecovery(HasAccommodationSelectionSection.AccommodationOption newOption, SoldOutRecoveryRoommateInfo roommateInfo, Runnable continueToSummary) {
        // Default: just continue to summary (forms should override to handle re-booking)
        continueToSummary.run();
    }

    // === Section Factory Hooks ===

    // === Data Classes ===

    /**
     * Result of a payment operation.
     */
    class PaymentResult {
        private final int amount;

        public PaymentResult(int amount) {
            this.amount = amount;
        }

        public int getAmount() { return amount; }
    }

    /**
     * Roommate information collected during sold-out recovery.
     * Encapsulates data needed to persist roommate info on accommodation document lines.
     */
    class SoldOutRecoveryRoommateInfo {
        private final boolean isRoomBooker;
        private final List<String> roommateNames;  // For room booker mode
        private final String roomOwnerName;        // For share accommodation mode

        public SoldOutRecoveryRoommateInfo(boolean isRoomBooker, List<String> roommateNames, String roomOwnerName) {
            this.isRoomBooker = isRoomBooker;
            this.roommateNames = roommateNames != null ? roommateNames : Collections.emptyList();
            this.roomOwnerName = roomOwnerName;
        }

        public boolean isRoomBooker() { return isRoomBooker; }
        public List<String> getRoommateNames() { return roommateNames; }
        public String getRoomOwnerName() { return roomOwnerName; }
        public boolean hasData() {
            return (isRoomBooker && !roommateNames.isEmpty()) || (!isRoomBooker && roomOwnerName != null && !roomOwnerName.isEmpty());
        }
    }
}
