package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.util.collection.Collections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
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

    private static final int BOX_WIDTH = 200;

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

    Node getView() {
        return container;
    }

    private void buildUi() {

        ColumnsPane columnsPane = new ColumnsPane();
        columnsPane.setMaxWidth(850);
        columnsPane.setMaxColumnCount(4);
        columnsPane.setMinColumnWidth(BOX_WIDTH - 40);
        columnsPane.setHgap(50);
        columnsPane.setVgap(20);
        columnsPane.setAlignment(Pos.TOP_LEFT);

        columnsPane.getChildren().setAll(
            Collections.map(dayAttendances, a -> new VideoOfSessionView(a, medias, parent, nodeShower).getView())
        );

        Label dateLabel = Bootstrap.strong(new Label(day.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))));

        SVGPath topArrowIcon = SvgIcons.createTopPointingChevron();
        SVGPath bottomArrow = SvgIcons.createBottomPointingChevron();
        MonoPane arrowButtonMonoPane = SvgIcons.createToggleButtonPane(topArrowIcon, bottomArrow, true, isPanelVisible -> {
            columnsPane.setVisible(isPanelVisible);
            columnsPane.setManaged(isPanelVisible);
        });

        HBox top = new HBox(40, dateLabel, arrowButtonMonoPane);
        top.setPadding(new Insets(15, 0, 30, 0));

        container.getChildren().setAll(
            top,
            columnsPane
        );
    }

}
