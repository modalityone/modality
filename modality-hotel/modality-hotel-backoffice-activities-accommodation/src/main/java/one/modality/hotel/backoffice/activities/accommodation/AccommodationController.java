package one.modality.hotel.backoffice.activities.accommodation;

import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Event;

import java.util.List;

public interface AccommodationController {

    Color getEventColor(Event event);

    void setEntities(List<Attendance> attendances);
}
