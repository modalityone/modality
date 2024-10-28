package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.platform.util.collection.Collections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Event;
import one.modality.event.frontoffice.medias.EventThumbnailView;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
final class EventsOfYearView {

    private static final double BOX_WIDTH = 450;

    private final int year;
    private final List<Event> events;
    private final Consumer<Event> onEventClicked;

    private final VBox container = new VBox();

    public EventsOfYearView(int year, List<Event> events, Consumer<Event> onEventClicked) {
        this.year = year;
        this.events = events;
        this.onEventClicked = onEventClicked;
        buildUi();
    }

    public VBox getView() {
        return container;
    }

    private void buildUi() {
        ColumnsPane columnsPane = new ColumnsPane();
        //columnsPane.setMaxWidth(BOX_WIDTH * 2 + 50);
        columnsPane.setMaxColumnCount(2);
        columnsPane.setHgap(20);
        columnsPane.setVgap(50);
        columnsPane.setAlignment(Pos.TOP_LEFT);

        columnsPane.getChildren().setAll(Collections.map(events, event -> {
            VBox eventView = new EventThumbnailView(event).getView();
            eventView.setMaxWidth(BOX_WIDTH);
            //eventView.setMinWidth(BOX_WIDTH);
            eventView.setMinHeight(100);
            eventView.setCursor(Cursor.HAND);
            eventView.setOnMouseClicked(e -> onEventClicked.accept(event));
            return eventView;
        }));

        Label currentYearLabel = new Label(String.valueOf(year));
        currentYearLabel.setMinWidth(50);

        CollapsePane collapsePane = new CollapsePane(columnsPane);
        Node chevronNode = CollapsePane.armChevron(CollapsePane.createPlainChevron(Color.BLACK), collapsePane);

        HBox yearArrowLine = new HBox(10, currentYearLabel, chevronNode);
        yearArrowLine.setPadding(new Insets(30, 0, 0, 0));
        collapsePane.setPadding(new Insets(30, 0, 70, 0));

        container.getChildren().addAll(
            yearArrowLine,
            collapsePane
        );
    }
}
