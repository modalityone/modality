package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.util.collection.Collections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Media;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
final class VideosOfDayView {

    private static final double BOX_WIDTH = 200;

    private final LocalDate day;
    private final List<Attendance> dayAttendances;
    private final List<Media> medias;
    private final Node parent;
    private final Consumer<Node> nodeShower;

    private final VBox container = new VBox();

    public VideosOfDayView(LocalDate day, List<Attendance> dayAttendances, List<Media> medias, Node parent, Consumer<Node> nodeShower) {
        this.day = day;
        this.dayAttendances = dayAttendances;
        this.medias = medias;
        this.parent = parent;
        this.nodeShower = nodeShower;
        buildUi();
    }

    Region getView() {
        return container;
    }

    private void buildUi() {
        ColumnsPane columnsPane = new ColumnsPane();
        columnsPane.setFixedColumnWidth(BOX_WIDTH);
        columnsPane.setHgap(50);
        columnsPane.setVgap(20);
        columnsPane.setAlignment(Pos.TOP_LEFT);
        //columnsPane.setMinWidth(BOX_WIDTH);

        columnsPane.getChildren().setAll(
            Collections.map(dayAttendances, a -> new VideoOfSessionView(a, medias, parent, nodeShower).getView())
        );

        Label dateLabel = Bootstrap.strong(new Label(day.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))));
        dateLabel.setMinWidth(150);

        CollapsePane collapsePane = new CollapsePane(columnsPane);
        Node chevronNode = CollapsePane.armChevron(CollapsePane.createPlainChevron(Color.BLACK), collapsePane);

        HBox top = new HBox(10, dateLabel, chevronNode);
        top.setPadding(new Insets(15, 0, 30, 0));

        container.getChildren().setAll(
            top,
            collapsePane
        );
    }

}
