package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.event.frontoffice.medias.EventThumbnailView;

import java.util.List;

/**
 * @author Bruno Salmon
 */
final class EventsOfYearView {

    private static final double BOX_WIDTH = 450;

    private final int year;
    private final List<Event> events;
    private final BrowsingHistory history;

    private final VBox container = new VBox();

    public EventsOfYearView(int year, List<Event> events, BrowsingHistory history) {
        this.year = year;
        this.events = events;
        this.history = history;
        buildUi();
    }

    public VBox getView() {
        return container;
    }

    private void buildUi() {
        ColumnsPane columnsPane = new ColumnsPane(20, 50);
        //columnsPane.setMaxWidth(BOX_WIDTH * 2 + 50);
        columnsPane.setMaxColumnCount(2);
        columnsPane.setAlignment(Pos.TOP_LEFT);

        columnsPane.getChildren().setAll(Collections.map(events, event -> {
            VBox eventView = new EventThumbnailView(event, EventThumbnailView.ItemType.ITEM_TYPE_AUDIO).getView();
            eventView.setMaxWidth(BOX_WIDTH);
            eventView.setMinHeight(100);
            eventView.setCursor(Cursor.HAND);
            eventView.setOnMouseClicked(e -> showRecordingsForEvent(event));
            return eventView;
        }));

        Label currentYearLabel = new Label(String.valueOf(year));
        currentYearLabel.setMinWidth(50);

        CollapsePane collapsePane = new CollapsePane(columnsPane);
        Node chevronNode = CollapsePane.armChevron(CollapsePane.createBlackChevron(), collapsePane);

        HBox yearArrowLine = new HBox(10, currentYearLabel, chevronNode);
        yearArrowLine.setPadding(new Insets(30, 0, 0, 0));
        collapsePane.setPadding(new Insets(30, 0, 70, 0));

        container.getChildren().addAll(
            yearArrowLine,
            collapsePane
        );
    }

    private void showRecordingsForEvent(Event event) {
        history.push(EventAudioPlaylistRouting.getEventRecordingsPlaylistPath(event,null));
    }

}
