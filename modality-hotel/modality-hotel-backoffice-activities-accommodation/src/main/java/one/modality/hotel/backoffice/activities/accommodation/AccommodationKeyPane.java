package one.modality.hotel.backoffice.activities.accommodation;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccommodationKeyPane extends VBox {

    private List<Event> events = Collections.emptyList();

    public void setEvents(List<Event> events) {
        this.events = new ArrayList<>(events);
        getChildren().clear();
        for (Event event : events) {
            Label label = new Label(event.getName());
            label.setTextFill(getEventColor(event));
            getChildren().add(label);
        }
    }

    public Color getEventColor(Event event) {
        int index = events.indexOf(event);
        return getColor(index);
    }

    private Color getColor(int index) {
        switch (index) {
            case 0: return Color.BLUE;
            case 1: return Color.GREEN;
            case 2: return Color.RED;
            case 3: return Color.AQUA;
            default: return Color.LIGHTGRAY;
        }
    }
}
