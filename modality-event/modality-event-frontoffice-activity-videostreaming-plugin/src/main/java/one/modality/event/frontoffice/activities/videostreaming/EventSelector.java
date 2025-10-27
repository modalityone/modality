package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.util.time.Times;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.knownitems.KnownItem;
import one.modality.event.frontoffice.medias.EventThumbnail;

/**
 * @author Bruno Salmon
 */
final class EventSelector {

    private static final double COLUMN_MIN_WIDTH = 200;
    private static final double COLUMN_MAX_WIDTH = 530; // Max width = unscaled thumbnail (533 px)

    private final ObjectProperty<Event> eventProperty;
    private final ObservableList<Event> eventsWithBookedVideos;

    private final CollapsePane eventsSelectionPane = new CollapsePane();

    EventSelector(ObjectProperty<Event> eventProperty, ObservableList<Event> eventsWithBookedVideos) {
        this.eventProperty = eventProperty;
        this.eventsWithBookedVideos = eventsWithBookedVideos;
        eventsSelectionPane.collapse(); // initially collapsed
    }

    Node buildUi() {
        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        // We display an event selection section only if there are more than 1 event with videos booked by the user
        Hyperlink selectEventLink = I18nControls.newHyperlink(VideoStreamingI18nKeys.SelectAnotherEvent);
        selectEventLink.setOnAction(e -> eventsSelectionPane.toggleCollapse());
        VBox eventsSelectionVBox = new VBox(10, selectEventLink, eventsSelectionPane);
        Layouts.setMinMaxHeightToPref(eventsSelectionVBox); // No need to compute min/max height as different to pref (layout computation optimization)
        eventsSelectionVBox.setAlignment(Pos.CENTER);
        eventsSelectionPane.setPadding(new Insets(50, 0, 200, 0));
        // Making the section visible only if there are more than 1 event with videos
        eventsSelectionVBox.visibleProperty().bind(eventsSelectionPane.contentProperty().isNotNull());
        Layouts.bindManagedToVisibleProperty(eventsSelectionVBox);


        // *************************************************************************************************************
        // ************************************** Reacting to the changes **********************************************
        // *************************************************************************************************************

        ObservableLists.runNowAndOnListChange(change -> {
            // If there are 2 events with videos or more, we populate the events selection pane
            if (eventsWithBookedVideos.stream().filter(e -> e.getVodExpirationDate() == null || Times.isFuture(e.getVodExpirationDate())).count() < 2) {
                eventsSelectionPane.setContent(null);
            } else {
                ColumnsPane columnsPane = new ColumnsPane(20, 50);
                columnsPane.setMinColumnWidth(COLUMN_MIN_WIDTH);
                columnsPane.setMaxColumnWidth(COLUMN_MAX_WIDTH);
                columnsPane.setMaxWidth(Double.MAX_VALUE);
                columnsPane.getStyleClass().add("audio-library"); // is audio-library good? (must be to have the same CSS rules as audio)
                for (Event event : eventsWithBookedVideos) {
                    EventThumbnail thumbnail = new EventThumbnail(event, KnownItem.VIDEO.getCode(), EventThumbnail.ItemType.ITEM_TYPE_VIDEO, true);
                    Button actionButton = thumbnail.getViewButton();
                    actionButton.setCursor(Cursor.HAND);
                    actionButton.setOnAction(e -> {
                        eventProperty.set(event);
                        eventsSelectionPane.collapse();
                    });
                    columnsPane.getChildren().add(thumbnail.getView());
                }
                eventsSelectionPane.setContent(columnsPane);
            }
        }, eventsWithBookedVideos);

        return eventsSelectionVBox;
    }
}
