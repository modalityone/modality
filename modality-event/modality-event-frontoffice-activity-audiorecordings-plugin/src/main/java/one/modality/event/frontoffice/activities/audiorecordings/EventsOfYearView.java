package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.platform.util.collection.Collections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
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
        columnsPane.setMaxWidth(BOX_WIDTH * 2 + 50);
        columnsPane.setMaxColumnCount(2);
        columnsPane.setHgap(20);
        columnsPane.setVgap(50);
        columnsPane.setAlignment(Pos.TOP_LEFT);

        columnsPane.getChildren().setAll(Collections.map(events, event -> {
            VBox currentEventVBox = new EventThumbnailView(event).getView();
            currentEventVBox.setMaxWidth(BOX_WIDTH);
            currentEventVBox.setMinWidth(BOX_WIDTH);
            currentEventVBox.setMinHeight(100);
            currentEventVBox.setCursor(Cursor.HAND);
            currentEventVBox.setOnMouseClicked(e -> onEventClicked.accept(event));
            return currentEventVBox;
        }));

        Label currentYearLabel = new Label(String.valueOf(year));

        SVGPath topArrow = SvgIcons.createTopPointingChevron();
        SVGPath bottomArrow = SvgIcons.createBottomPointingChevron();
        MonoPane arrowButtonMonoPane = new MonoPane(topArrow);
        arrowButtonMonoPane.setCursor(Cursor.HAND);
        final boolean[] isPanelVisible = { true };
        arrowButtonMonoPane.setOnMouseClicked(e -> {
            isPanelVisible[0] = !isPanelVisible[0];
            columnsPane.setVisible(isPanelVisible[0]);
            columnsPane.setManaged(isPanelVisible[0]);
            arrowButtonMonoPane.getChildren().setAll(isPanelVisible[0] ? topArrow : bottomArrow);
        });

        HBox yearArrowLine = new HBox(40, currentYearLabel, arrowButtonMonoPane);
        yearArrowLine.setPadding(new Insets(100, 0, 30, 0));

        container.getChildren().addAll(
            yearArrowLine,
            columnsPane
        );
    }
}
