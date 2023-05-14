package one.modality.hotel.backoffice.activities.accommodation;

import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dan Newman
 */
public enum AttendeeCategory {

    GUEST("Guest", Color.rgb(65,186,77)),
    RESIDENT("Resident", Color.rgb(131,135,136)),
    RESIDENTS_FAMILY("Resident's family", Color.rgb(17,95,24)),
    SPECIAL_GUEST("Special guest", Color.rgb(218,201,46)),
    VOLUNTEER("Volunteer", Color.rgb(150,124,55));

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
