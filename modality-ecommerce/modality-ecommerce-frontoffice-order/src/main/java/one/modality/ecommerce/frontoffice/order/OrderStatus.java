package one.modality.ecommerce.frontoffice.order;

import dev.webfx.platform.console.Console;
import one.modality.base.shared.entities.Document;

/**
 * Order Status:
 * Incomplete: there is still some action you need to take e.g., minimum deposit has not been paid, roommate has not been named.
 * In Progress: all necessary information has been submitted. The Festival Registration team is confirming that the information is correct and that your booking options are available. This stage may take some time especially when booking initially opens due to the number of bookings received. You will receive an email with a link to your booking cart once minimum payment has been successfully processed. Please keep this information in a secure place so that you can access your booking cart later.
 * Confirmed: closer to the Festival, you will receive a confirmation letter from the Festival Registration team. Please bring this letter with you to register upon arrival.
 * Complete: A booking status will only change to complete when full payment has been received and all required information received with no pending changes.
 *
 * @author Bruno Salmon
 */
public enum OrderStatus {

    // Incomplete = payment_required or action_required
    INCOMPLETE(OrderI18nKeys.OrderStatusIncomplete),
    IN_PROGRESS(OrderI18nKeys.OrderStatusInProgress),
    CONFIRMED(OrderI18nKeys.OrderStatusConfirmed),
    COMPLETE(OrderI18nKeys.OrderStatusComplete),
    CANCELLED(OrderI18nKeys.OrderStatusCancelled);

    public static final String BOOKING_REQUIRED_FIELDS = "price_net,price_deposit,price_minDeposit,cancelled,confirmed,arrived";

    private final Object i18nKey;

    OrderStatus(Object i18nKey) {
        this.i18nKey = i18nKey;
    }

    public Object getI18nKey() {
        return i18nKey;
    }

    public static OrderStatus ofDocument(Document document) {
        Integer priceDeposit = document.getPriceDeposit();
        Integer priceMinDeposit = document.getPriceMinDeposit();
        Boolean cancelled = document.isCancelled();
        Boolean confirmed = document.isConfirmed();
        Boolean arrived = document.isArrived();
        Integer priceNet = document.getPriceNet();
        if (priceNet == null || priceDeposit == null || priceMinDeposit == null || cancelled == null || confirmed == null || arrived == null) {
            Console.log("⚠️ Booking status cannot be computed (so returning INCOMPLETE) because some required fields are null: " + document);
            return OrderStatus.INCOMPLETE;
        }
        if (priceDeposit < priceMinDeposit)
            return OrderStatus.INCOMPLETE;
        if (cancelled)
            return OrderStatus.CANCELLED;
        if (!confirmed && !arrived)
            return OrderStatus.IN_PROGRESS;
        if (priceDeposit >= priceNet)
            return OrderStatus.COMPLETE;
        return OrderStatus.CONFIRMED;
    }

    public static String getBookingStatusExpression() {
        return "price_deposit < price_minDeposit ? 'INCOMPLETE' : !confirmed && !arrived ? 'IN_PROGRESS' : price_deposit >= price_net ? 'COMPLETE' : !cancelled ? 'CONFIRMED' : 'CANCELLED' ";
    }

    public static String getBookingStatusOrderExpression(boolean asc) {
        return getBookingStatusExpression()
            .replace("'INCOMPLETE'", String.valueOf(INCOMPLETE.ordinal()))
            .replace("'IN_PROGRESS'", String.valueOf(IN_PROGRESS.ordinal()))
            .replace("'CONFIRMED'", String.valueOf(CONFIRMED.ordinal()))
            .replace("'COMPLETE'", String.valueOf(COMPLETE.ordinal()))
            .replace("'CANCELLED'", String.valueOf(CANCELLED.ordinal()))
            + (asc ? " asc" : " desc")
            ;
    }
}
