package one.modality.hotel.backoffice.accommodation;

import dev.webfx.extras.util.animation.Animations;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
public final class AccommodationBorderPane {

    public static BorderPane createAccommodationBorderPane(AccommodationGantt<?> accommodationGantt, TodayAccommodationStatus todayAccommodationStatus) {
        BorderPane borderPane = new BorderPane();

        GridPane attendeeLegend = AttendeeLegend.createLegend();
        GridPane statusBar = todayAccommodationStatus.createStatusBar();

        borderPane.setLeft(attendeeLegend); // Left first, so it's behind center when animating it
        borderPane.setCenter(accommodationGantt.buildCanvasContainer());

        attendeeLegend.setVisible(false);
        double[] prefWidth = { -1 };
        UiScheduler.scheduleInAnimationFrame(() -> {
            prefWidth[0] = attendeeLegend.prefWidth(-1);
            attendeeLegend.setMinWidth(0);
            attendeeLegend.setPrefWidth(0);
        }, 4);

        CheckBox allRoomsCheckBox = new CheckBox("All rooms");
        allRoomsCheckBox.setSelected(false);
        accommodationGantt.parentsProvidedProperty().bind(allRoomsCheckBox.selectedProperty());

        CheckBox legendCheckBox = new CheckBox("Show Legend");
        legendCheckBox.setOnAction(e -> {
            attendeeLegend.setVisible(true);
            Animations.animateProperty(attendeeLegend.prefWidthProperty(), legendCheckBox.isSelected() ? prefWidth[0] + 5 : 0);
        });

        HBox bottomBar = new HBox(10, allRoomsCheckBox, legendCheckBox, statusBar);
        bottomBar.setBackground(new Background(new BackgroundFill(Color.web("#e0dcdc"), null, null)));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(statusBar, Priority.ALWAYS);
        borderPane.setBottom(bottomBar);

        return borderPane;
    }

}
