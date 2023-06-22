package one.modality.event.frontoffice.activities.booking.routing;

import one.modality.base.frontoffice.states.GeneralPM;

public class BookingRouting {
    private final static String PATH = GeneralPM.BOOKING_PATH;

    public static String getPath() {
        return PATH;
    }
}
