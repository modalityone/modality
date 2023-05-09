package one.modality.hotel.backoffice.activities.accommodation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import one.modality.base.shared.entities.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccommodationKeyPane extends VBox {

    private static final double COLOR_BLOCK_HEIGHT = 20;
    private List<Event> events = Collections.emptyList();

    public void setEvents(List<Event> events) {
        this.events = new ArrayList<>(events);
        List<HBox> rows = new ArrayList<>(events.size());
        for (Event event : events) {
            Rectangle rectangle = new Rectangle(COLOR_BLOCK_HEIGHT, COLOR_BLOCK_HEIGHT);
            rectangle.setFill(getEventColor(event));
            Label label = new Label(event.getName());
            HBox row = new HBox(rectangle, label);
            row.setPadding(new Insets(8, 0, 0, 0));
            row.setAlignment(Pos.CENTER_LEFT);
            rows.add(row);
        }
        getChildren().setAll(rows);
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
