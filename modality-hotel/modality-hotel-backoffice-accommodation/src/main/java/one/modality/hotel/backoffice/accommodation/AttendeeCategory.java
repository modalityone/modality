package one.modality.hotel.backoffice.accommodation;

import dev.webfx.platform.util.Numbers;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Document;

/**
 * @author Dan Newman
 */
public enum AttendeeCategory {

    GUEST("Guest", Color.rgb(154, 77, 152)),
    RESIDENT("Resident", Color.rgb(218,201,46)),
    RESIDENTS_FAMILY("Resident's family", Color.rgb(65,186,77)),
    SPECIAL_GUEST("Special guest", Color.web("#F400A1")), // Hollywood cerise // Color.web("#C51E3A")), // Cardinal
    VOLUNTEER("Volunteer", Color.web("#008B8B")); // Dark cyan // Color.rgb(17,95,24));

    private final String text;
    private final Color color;

    AttendeeCategory(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }

    public static AttendeeCategory fromDocument(Document document) {
        String fullName = document.getFullName().toLowerCase();
        if (fullName.contains("gen-la") || fullName.contains("birch"))
            return AttendeeCategory.SPECIAL_GUEST;
        if (Numbers.toInteger(document.getEventId().getPrimaryKey()) == 480) // 480 = Working visit
            return AttendeeCategory.VOLUNTEER;
        return AttendeeCategory.GUEST;
    }

    public static final String SPECIAL_GUEST_DOCUMENT_CONDITION = "lower(d.person_name) like '%gen-la%' or lower(d.person_name) like '%birch%'";
    public static final String VOLUNTEER_DOCUMENT_CONDITION = "d.event=480";
    public static final String RESIDENT_DOCUMENT_CONDITION = "false";
    public static final String RESIDENTS_FAMILY_DOCUMENT_CONDITION = "false";
    public static final String GUEST_DOCUMENT_CONDITION = "!(" + SPECIAL_GUEST_DOCUMENT_CONDITION + ") and "
            + "!(" + VOLUNTEER_DOCUMENT_CONDITION + ") and "
            + "!(" + RESIDENT_DOCUMENT_CONDITION + ") and "
            + "!(" + RESIDENTS_FAMILY_DOCUMENT_CONDITION + ")";

}
