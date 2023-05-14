package one.modality.hotel.backoffice.activities.accommodation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccommodationKeyPane extends VBox {

    private static final double COLOR_BLOCK_HEIGHT = 20;

    public AccommodationKeyPane() {
        List<AttendeeCategory> categories = Stream.of(AttendeeCategory.values())
                .sorted(Comparator.comparing(AttendeeCategory::getText))
                .collect(Collectors.toList());

        List<HBox> rows = new ArrayList<>(categories.size());
        for (AttendeeCategory category : categories) {
            Rectangle rectangle = new Rectangle(COLOR_BLOCK_HEIGHT, COLOR_BLOCK_HEIGHT);
            rectangle.setFill(category.getColor());
            Label label = new Label(category.getText());
            HBox row = new HBox(rectangle, label);
            row.setPadding(new Insets(8, 0, 0, 0));
            row.setAlignment(Pos.CENTER_LEFT);
            rows.add(row);
        }
        getChildren().setAll(rows);
    }

}
