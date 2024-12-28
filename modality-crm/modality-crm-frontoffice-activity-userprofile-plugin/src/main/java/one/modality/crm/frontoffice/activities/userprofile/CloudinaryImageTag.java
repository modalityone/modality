package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.stack.i18n.I18n;

public class CloudinaryImageTag {

    public static Object getPersonImageTag(Object personId) {
        return "persons/person-" + personId;
    }

    public static Object getEventImageTag(Object eventId) {
        return "events/event-" + eventId;
    }

    public static Object getEventCoverImageTag(Object eventId, Object I18nLanguage) {
        if (I18n.getLanguage() == null || "en".equals(I18n.getLanguage().toString())) {
            return "events/event-" + eventId + "-cover.jpg";
        } else return "events/event-" + eventId + "-cover-" + I18nLanguage.toString() + ".jpg";
    }
}
