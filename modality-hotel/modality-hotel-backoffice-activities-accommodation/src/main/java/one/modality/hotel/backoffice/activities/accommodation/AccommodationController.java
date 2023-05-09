package one.modality.hotel.backoffice.activities.accommodation;

import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Event;

import java.util.List;

public interface AccommodationController {
    void setEvents(List<Event> events);

    Color getEventColor(Event event);
}
