package one.modality.hotel.backoffice.activities.household;

import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dan Newman
 */
public enum AttendeeCategory {

    GUEST("Guest", Color.rgb(131,135,136)),
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

    public static AttendeeCategory random() {
        List<AttendeeCategory> values = Arrays.asList(values());
        int r = (int) (Math.random() * values.size());
        return values.get(r);
    }

}
