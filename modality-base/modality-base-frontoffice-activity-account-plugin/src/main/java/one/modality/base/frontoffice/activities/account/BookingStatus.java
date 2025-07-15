package one.modality.base.frontoffice.activities.account;

import one.modality.base.shared.entities.Document;

/**
 * Booking Status
 * Incomplete: there is still some action you need to take e.g. minimum deposit has not been paid, roommate has not been named.
 * In Progress: all necessary information has been submitted. The Festival Registration team is confirming that the information is correct and that your booking options are available. This stage may take some time especially when booking initially opens due to the number of bookings received. You will receive an email with a link to your booking cart once minimum payment has been successfully processed. Please keep this information in a secure place so that you can access your booking cart later.
 * Confirmed: closer to the Festival, you will receive a confirmation letter from the Festival Registration team. Please bring this letter with you to register upon arrival.
 * Complete: A booking status will only change to complete when full payment has been received and all required information received with no pending changes.
 * @author Bruno Salmon
 */
public enum BookingStatus {

    // Incomplete = payment_required or action_required
    INCOMPLETE("BookingStatusIncomplete"),
    CANCELLED("BookingStatusCancelled"),
    IN_PROGRESS("BookingStatusInProgress"),
    CONFIRMED("BookingStatusConfirmed"),
    COMPLETE("BookingStatusComplete");

    public static final String BOOKING_REQUIRED_FIELDS = "price_net,price_deposit,price_minDeposit,cancelled,confirmed,arrived";

    private final String i18nKey;

    BookingStatus(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public static BookingStatus ofBooking(Document booking) {
        Integer priceDeposit = booking.getPriceDeposit();
        if (priceDeposit < booking.getPriceMinDeposit())
            return BookingStatus.INCOMPLETE;
        if (booking.isCancelled())
            return BookingStatus.CANCELLED;
        if (!booking.isConfirmed() && !booking.isArrived())
            return BookingStatus.IN_PROGRESS;
        if (priceDeposit >= booking.getPriceNet())
            return BookingStatus.COMPLETE;
        return BookingStatus.CONFIRMED;
    }

    public static String getBookingStatusExpression() {
        return "price_net < price_minDeposit ? 'INCOMPLETE' : cancelled ? 'CANCELLED' : !confirmed && !arrived ? 'IN_PROGRESS' : price_deposit >= price_net ? 'COMPLETE' : 'CONFIRMED'";
    }

    public static String getBookingStatusOrderExpression(boolean asc) {
        return getBookingStatusExpression()
            .replace("'INCOMPLETE'", String.valueOf(INCOMPLETE.ordinal()))
            .replace("'CANCELLED'", String.valueOf(CANCELLED.ordinal()))
            .replace("'IN_PROGRESS'", String.valueOf(IN_PROGRESS.ordinal()))
            .replace("'CONFIRMED'", String.valueOf(CONFIRMED.ordinal()))
            .replace("'COMPLETE'", String.valueOf(COMPLETE.ordinal()))
            + (asc ? " desc" : "")
            ;
    }
}
