package one.modality.event.frontoffice.activities.booking.process;

import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.windowhistory.WindowHistory;
import one.modality.base.shared.entities.Event;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.frontoffice.activities.booking.browser.BrowserUtil;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventRouting;

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
        String bookingFormUrl = event.evaluate("bookingFormUrl");
        bookingFormUrl = bookingFormUrl.replace("{host}", "kadampabookings.org");
        BrowserUtil.chooseHowToOpenWebsite(bookingFormUrl);
    }

    private static void startKbs3EventBooking(Event event) {
        FXEvent.setEvent(event); // Not required but helps to start faster as the event is already loaded
        WindowHistory.getProvider().push(BookEventRouting.getBookEventPath(event));
    }

}
