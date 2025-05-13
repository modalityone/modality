package one.modality.event.client.lifecycle;

import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.I18n;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.EventState;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public final class EventLifeCycle {

    public static boolean isPastEvent(Event event) {
        return Times.isPast(event.getEndDate(), Event.getEventClock());
    }

    public static boolean canBookNow(Event event) {
        if (isPastEvent(event))
            return false;
        LocalDateTime openingDate = event.getOpeningDate();
        if (openingDate != null && Times.isFuture(openingDate.minusHours(1), Event.getEventClock()))
            return false;
        if (isKbs3Event(event)) {
            EventState state = event.getState();
            return state == EventState.OPEN;
        }
        // KBS2 events
        return Booleans.isTrue(event.isLive());
    }

    public static boolean isKbs2Event(Event event) {
        return !isKbs3Event(event);
    }

    public static boolean isKbs3Event(Event event) {
        return Booleans.isTrue(event.isKbs3());
    }

    public static String getKbs2BookingFormUrl(Event event) {
        String url = event.evaluate("bookingFormUrl");
        if (url != null)
            url = url.replace("{host}", "kadampabookings.org");
        Object language = I18n.getLanguage();
        if (language instanceof String)
            url = url + "&lang=" + language;
        return url;
    }

}
