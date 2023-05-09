package one.modality.hotel.backoffice.activities.accommodation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import one.modality.base.shared.entities.Event;

import java.util.*;
import java.util.stream.Collectors;

public class AccommodationKeyPane extends VBox {

    private static final double COLOR_BLOCK_HEIGHT = 20;

    private Map<Event, Color> eventColors = new HashMap<>();

    public void setEvents(List<Event> events) {
        List<Event> sortedEvents = events.stream()
                .distinct()
                .sorted((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()))
                .collect(Collectors.toList());

        List<HBox> rows = new ArrayList<>(sortedEvents.size());
        for (Event event : sortedEvents) {
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
        if (!eventColors.containsKey(event)) {
            int nextColorIndex = eventColors.size();
            Color color = getColor(nextColorIndex);
            eventColors.put(event, color);
        }
        return eventColors.get(event);
    }

    private Color getColor(int index) {
        switch (index) {
            case 0: return Color.rgb(8, 148, 212);
            case 1: return Color.rgb(123, 163, 60);
            case 2: return Color.rgb(189, 116, 177);
            case 3: return Color.BLUE;
            case 4: return Color.GREEN;
            case 5: return Color.RED;
            case 6: return Color.AQUA;
            default:
                Random random = new Random(index);
                return Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        }
    }
}
