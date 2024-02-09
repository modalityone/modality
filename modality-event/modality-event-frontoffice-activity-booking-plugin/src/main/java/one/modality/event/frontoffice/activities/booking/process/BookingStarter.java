package one.modality.event.frontoffice.activities.booking.process;

import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.windowhistory.WindowHistory;
import one.modality.base.shared.entities.Event;
import one.modality.event.frontoffice.activities.booking.browser.BrowserUtil;
import one.modality.event.frontoffice.activities.booking.fx.FXEvent;
import one.modality.event.frontoffice.activities.booking.fx.FXEventAggregate;
import one.modality.event.frontoffice.activities.booking.process.recurring.RecurringEventRouting;

/**
 * @author Bruno Salmon
 */
public final class BookingStarter {

    public static void startEventBooking(Event event) {
        event.onExpressionLoaded("kbs3,bookingFormUrl")
                .onSuccess(ignored -> UiScheduler.runInUiThread(() -> {
                    if (Booleans.isTrue(event.evaluate("kbs3"))) {
                        startKbs3EventBooking(event);
                    } else {
                        startKbs2EventBooking(event);
                    }
                }));
    }

    private static void startKbs2EventBooking(Event event) {
        String bookingFormUrl = (String) event.evaluate("bookingFormUrl");
        bookingFormUrl = bookingFormUrl.replace("{host}", "kadampabookings.org");
        BrowserUtil.chooseHowToOpenWebsite(bookingFormUrl);
    }

    private static void startKbs3EventBooking(Event event) {
        FXEvent.setEvent(event);
        FXEventAggregate.setEventAggregate(new EventAggregate(event));
        WindowHistory.getProvider().push(RecurringEventRouting.getRecurringEventPath(event));
    }

}
