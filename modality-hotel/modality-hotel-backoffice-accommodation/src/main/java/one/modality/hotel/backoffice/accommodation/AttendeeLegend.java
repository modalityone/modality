package one.modality.hotel.backoffice.accommodation;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public final class AttendeeLegend {

  private static final double COLOR_BLOCK_SIZE = 20;

  public static GridPane createLegend() {
    List<AttendeeCategory> categories =
        Stream.of(AttendeeCategory.values())
            .sorted(Comparator.comparing(AttendeeCategory::getText))
            .collect(Collectors.toList());

    GridPane gridPane = new GridPane();
    gridPane.setHgap(5);
    gridPane.setVgap(5);
    gridPane.setAlignment(Pos.CENTER_LEFT);
    for (AttendeeCategory category : categories) {
      Rectangle rectangle = new Rectangle(COLOR_BLOCK_SIZE, COLOR_BLOCK_SIZE);
      rectangle.setFill(category.getColor());
      Text label = new Text(category.getText());
      gridPane.add(rectangle, 0, category.ordinal());
      gridPane.add(label, 1, category.ordinal());
    }
    return gridPane;
  }
}
