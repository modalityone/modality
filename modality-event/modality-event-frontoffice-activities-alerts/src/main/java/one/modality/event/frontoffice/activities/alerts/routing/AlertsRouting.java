package one.modality.event.frontoffice.activities.alerts.routing;

import one.modality.base.frontoffice.states.GeneralPM;

public class AlertsRouting {
    private final static String PATH = GeneralPM.ALERTS_PATH;

    public static String getPath() {
        return PATH;
    }
}
