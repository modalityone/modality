package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entities;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class VideoOfSessionView {

    private static final int TEACHING_BOX_WIDTH = 200;

    private final Attendance attendance;
    private final List<Media> medias;
    private final Node parent;
    private final Consumer<Node> nodeShower;

    private final VBox container = new VBox();

    public VideoOfSessionView(Attendance attendance, List<Media> medias, Node parent, Consumer<Node> nodeShower) {
        this.attendance = attendance;
        this.medias = medias;
        this.parent = parent;
        this.nodeShower = nodeShower;
        buildUi();
    }

    Node getView() {
        return container;
    }

    private void buildUi() {
        Timeline timeline = attendance.getScheduledItem().getParent().getTimeline();
        ScheduledItem videoScheduledItem = attendance.getScheduledItem();
        Event event = videoScheduledItem.getEvent();
        LocalDate day = videoScheduledItem.getDate();
        String currentDateToString = day.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        boolean isPlayable = false;

        container.setSpacing(10);
        container.setMaxWidth(TEACHING_BOX_WIDTH);
        container.setMinWidth(TEACHING_BOX_WIDTH);
        String imageUrl = attendance.getScheduledItem().getParent().getItem().getImageUrl();

        ImageView imageView = new ImageView(new javafx.scene.image.Image(imageUrl, true));
        // Optional: Set the preferred size or fit the image to the view
        imageView.setFitWidth(TEACHING_BOX_WIDTH);  // Set the width
        imageView.setPreserveRatio(true);  // Preserve aspect ratio
        StackPane imageStackPane = new StackPane(imageView);

        Label stateLabel;
        String title = videoScheduledItem.getParent().getName();
        String time = timeline.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " " +
                      timeline.getEndTime().format(DateTimeFormatter.ofPattern(" - HH:mm"));

        List<Media> currentMediaList = medias.stream()
            .filter(media -> media.getScheduledItem() != null && Entities.sameId(videoScheduledItem, media.getScheduledItem()))
            .collect(Collectors.toList());

        if (videoScheduledItem.isVodDelayed()) {
            Label delayedLabel = Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.VideoDelayed));
            delayedLabel.setTextFill(Color.WHITE);

            BackgroundFill backgroundFill = new BackgroundFill(Color.RED, null, null);
            Background background = new Background(backgroundFill);
            HBox delayedLine = new HBox();
            delayedLine.setAlignment(Pos.CENTER);
            delayedLine.setBackground(background);
            delayedLine.getChildren().add(delayedLabel);
            delayedLine.setMinWidth(TEACHING_BOX_WIDTH);
            delayedLine.setMaxHeight(25);
            imageStackPane.getChildren().add(delayedLine);
            imageStackPane.setAlignment(Pos.BOTTOM_CENTER);
        } else {
            LocalDateTime expirationDate = event.getVodExpirationDate();
            //We look if the current video is expired
            if (videoScheduledItem.getExpirationDate() != null) {
                expirationDate = videoScheduledItem.getExpirationDate();
            }
            if (currentMediaList.isEmpty() || !currentMediaList.get(0).isPublished()) {
                stateLabel = Bootstrap.textDanger(I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.Unavailable));
            } else if (expirationDate != null && Times.isPast(expirationDate)) {
                //TODO: when we know how we will manage the timezone, we adapt to take into account the different timezone
                //TODO: when a push notification is sent we have to update this also.
                stateLabel = I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.Expired);
                stateLabel.getStyleClass().add(Bootstrap.TEXT_DANGER);
            } else {
                stateLabel = I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.Available);
                stateLabel.getStyleClass().add(Bootstrap.TEXT_SUCCESS);
                isPlayable = true;
                //TODO manage when we have several media for one videoScheduledItem
            }

            Bootstrap.small(stateLabel);
            stateLabel.setBackground(Background.fill(Color.LIGHTGRAY));
            stateLabel.setPadding(new Insets(4));
            imageStackPane.getChildren().addAll(stateLabel);
            imageStackPane.setAlignment(Pos.TOP_LEFT);
        }

        container.getChildren().add(imageStackPane);
        HBox descriptionLine = new HBox();
        container.getChildren().add(descriptionLine);

        Label titleLabel = new Label(title);
        Label timeLabel = new Label(time);
        titleLabel.setWrapText(true);
        VBox timeAndTitleVBox = new VBox();
        descriptionLine.getChildren().add(timeAndTitleVBox);
        timeAndTitleVBox.getChildren().add(timeLabel);
        timeAndTitleVBox.getChildren().add(titleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        descriptionLine.getChildren().add(spacer);

        SVGPath playVideoIcon = SvgIcons.setSVGPathFill(SvgIcons.createVideoPlaySVGPath(), isPlayable ? Color.RED : Color.LIGHTGRAY);
        MonoPane playVideoButton = SvgIcons.createButtonPane(playVideoIcon, !isPlayable ? null : () -> {
            String url = currentMediaList.get(0).getUrl();
            VBox videoPlayerVBox = new VideoOfSessionPlayerView(currentDateToString + " - " + time + ": " + title, url, parent, nodeShower).getView();
            nodeShower.accept(videoPlayerVBox);
        });

        descriptionLine.getChildren().add(playVideoButton);
        descriptionLine.setAlignment(Pos.TOP_LEFT);
    }
}
