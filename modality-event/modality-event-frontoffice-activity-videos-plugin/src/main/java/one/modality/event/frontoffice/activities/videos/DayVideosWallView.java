package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class DayVideosWallView {

    private static final double BOX_WIDTH = 200;

    private final LocalDate day;
    private final List<ScheduledItem> dayScheduledVideos;
    private final BrowsingHistory browsingHistory;

    private final VBox container = new VBox();
    private final CollapsePane collapsePane = new CollapsePane();

    public DayVideosWallView(LocalDate day, List<ScheduledItem> dayScheduledVideos, BrowsingHistory browsingHistory) {
        this.day = day;
        this.dayScheduledVideos = dayScheduledVideos;
        this.browsingHistory = browsingHistory;
        buildUi();
    }

    Region getView() {
        return container;
    }

    void setCollapsed(boolean collapsed) {
        collapsePane.setCollapsed(collapsed);
    }

    private void buildUi() {
        ColumnsPane columnsPane = new ColumnsPane(50, 20);
        columnsPane.setFixedColumnWidth(BOX_WIDTH);

        columnsPane.getChildren().setAll(
            Collections.map(dayScheduledVideos, a -> new SessionVideoThumbnailView(a, browsingHistory).getView())
        );

        Label dateLabel = Bootstrap.strong(new Label(day.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))));
        dateLabel.setMinWidth(150);

        collapsePane.setContent(columnsPane);
        Node chevronNode = CollapsePane.armChevron(CollapsePane.createPlainChevron(Color.BLACK), collapsePane);

        HBox dateAndChevronTopLine = new HBox(10, dateLabel, chevronNode);

        container.getChildren().setAll(
            dateAndChevronTopLine,
            collapsePane
        );
        container.setAlignment(Pos.CENTER);

        collapsePane.setPadding(new Insets(30, 0, 20, 0));
        collapsePane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0, 1, 0))));
    }

}
