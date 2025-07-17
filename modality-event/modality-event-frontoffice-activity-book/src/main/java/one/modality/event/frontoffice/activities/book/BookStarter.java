package one.modality.event.frontoffice.activities.book;

import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowhistory.WindowHistory;
import one.modality.base.frontoffice.utility.browser.BrowserUtil;
import one.modality.base.shared.entities.Event;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.lifecycle.EventLifeCycle;
import one.modality.event.frontoffice.activities.book.event.BookEventRouting;

/**
 * @author Bruno Salmon
 */
public final class BookStarter {

    public static void startBookEvent(Event event) {
        event.onExpressionLoaded("kbs3,bookingFormUrl")
                .onSuccess(ignored -> UiScheduler.runInUiThread(() -> {
                    if (EventLifeCycle.isKbs3Event(event)) {
                        startBookKbs3Event(event);
                    } else {
                        startBookKbs2Event(event);
                    }
                }));
    }

    private static void startBookKbs2Event(Event event) {
        String bookingFormUrl = event.evaluate("bookingFormUrl");
        bookingFormUrl = bookingFormUrl.replace("{host}", "kadampabookings.org");
        //BrowserUtil.chooseHowToOpenWebsite(bookingFormUrl); // Was ok for KMC courses, but not NKT Festivals
        BrowserUtil.openExternalBrowser(bookingFormUrl);
    }

    private static void startBookKbs3Event(Event event) {
        FXEvent.setEvent(event); // Not required but helps to start faster as the event is already loaded
        WindowHistory.getProvider().push(BookEventRouting.getBookEventPath(event));
    }

}
