package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.util.collection.Collections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
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
    private boolean isPanelVisible;

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
        VBox currentDayVBox = new VBox();
        currentDayVBox.setSpacing(30);
        HBox currentLine = new HBox();
        currentLine.setSpacing(40);
        currentLine.setPadding(new Insets(15, 0, 30, 0));
        currentDayVBox.getChildren().add(currentLine);
        ColumnsPane videoListColumnsPane = new ColumnsPane();
        videoListColumnsPane.setMaxWidth(850);
        videoListColumnsPane.setMaxColumnCount(4);
        videoListColumnsPane.setMinColumnWidth(BOX_WIDTH - 40);
        videoListColumnsPane.setHgap(50);
        videoListColumnsPane.setVgap(20);
        videoListColumnsPane.setAlignment(Pos.TOP_LEFT);

        videoListColumnsPane.getChildren().setAll(
            Collections.map(dayAttendances, a -> new VideoOfSessionView(a, medias, parent, nodeShower).getView())
        );

        MonoPane toggledPane = new MonoPane(videoListColumnsPane);
        String currentDateToString = day.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        Label dateLabel = Bootstrap.strong(new Label(currentDateToString));
        currentLine.getChildren().add(dateLabel);
        SVGPath topArrow = SvgIcons.createTopPointingChevron();
        MonoPane arrowButtonMonoPane = new MonoPane(topArrow);
        currentLine.setAlignment(Pos.CENTER_LEFT);
        currentLine.getChildren().add(arrowButtonMonoPane);
        SVGPath bottomArrow = SvgIcons.createBottomPointingChevron();
        arrowButtonMonoPane.setCursor(Cursor.HAND);
        arrowButtonMonoPane.setOnMouseClicked(e -> {
            isPanelVisible = !isPanelVisible;
            toggledPane.setVisible(isPanelVisible);
            toggledPane.setManaged(isPanelVisible);
            arrowButtonMonoPane.getChildren().setAll(isPanelVisible ? topArrow : bottomArrow);
        });

        container.getChildren().setAll(
            currentDayVBox,
            toggledPane
        );
    }

}
