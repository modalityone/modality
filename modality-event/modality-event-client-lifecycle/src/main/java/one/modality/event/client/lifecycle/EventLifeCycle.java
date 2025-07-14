package one.modality.event.client.lifecycle;

import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.time.Times;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.stack.orm.entity.Entities;
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
        if (!Booleans.booleanValue(event.isKbs3()))
            return false;
        // Excluding hybride KBS2/KBS3 events (kbs3 flag is on for audio and video consumption but not true KBS3 events
        // from the booking point of view)
        int eventId = Numbers.toInteger(Entities.getPrimaryKey(event));
        return eventId != 1549    // Spring 25 in-person
               && eventId != 1604 // Spring 25 online
               && eventId != 1550 // Summer 25 in-person
               && eventId != 1605 // Summer 25 online
               && eventId != 1551 // Fall 25 in-person
            ;
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
