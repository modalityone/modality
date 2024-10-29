package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.time.Times;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Bruno Salmon
 */
final class SessionVideoThumbnailView {

    private static final int TEACHING_BOX_WIDTH = 200;

    private final ScheduledItem scheduledVideoItem;
    private final BrowsingHistory browsingHistory;

    private final VBox container = new VBox(10);

    public SessionVideoThumbnailView(ScheduledItem scheduledVideoItem, BrowsingHistory browsingHistory) {
        this.scheduledVideoItem = scheduledVideoItem;
        this.browsingHistory = browsingHistory;
        buildUi();
    }

    Node getView() {
        return container;
    }

    private void buildUi() {
        ImageView imageView = new ImageView();
        // Optional: Set the preferred size or fit the image to the view
        imageView.setFitWidth(TEACHING_BOX_WIDTH);  // Set the width
        imageView.setPreserveRatio(true);  // Preserve aspect ratio
        StackPane imageStackPane = new StackPane();

        Label titleLabel = new Label();
        Label timeLabel = new Label();
        titleLabel.setWrapText(true);
        VBox timeAndTitleVBox = new VBox(timeLabel, titleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        SVGPath playVideoIcon = SvgIcons.createVideoPlaySVGPath();
        MonoPane playVideoButton = SvgIcons.createButtonPane(playVideoIcon);

        HBox descriptionLine = new HBox(timeAndTitleVBox, spacer, playVideoButton);
        descriptionLine.setAlignment(Pos.TOP_LEFT);

        container.setMaxWidth(TEACHING_BOX_WIDTH);
        container.setMinWidth(TEACHING_BOX_WIDTH);
        container.getChildren().setAll(
            imageStackPane,
            descriptionLine
        );

        Label delayedLabel = Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.VideoDelayed));
        delayedLabel.setTextFill(Color.WHITE);
        HBox delayedLine = new HBox(delayedLabel);
        delayedLine.setAlignment(Pos.CENTER);
        delayedLine.setBackground(Background.fill(Color.RED));
        delayedLine.setMinWidth(TEACHING_BOX_WIDTH);
        delayedLine.setMaxHeight(25);

        Label unavailableLabel = Bootstrap.textDanger( I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.Unavailable));
        Label expiredLabel     = Bootstrap.textDanger( I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.Expired));
        Label availableLabel   = Bootstrap.textSuccess(I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.Available));

        // --

        Timeline timeline = scheduledVideoItem.getParent().getTimeline();
        Event event = scheduledVideoItem.getEvent();
        String imageUrl = scheduledVideoItem.getParent().getItem().getImageUrl();

        String title = scheduledVideoItem.getParent().getName();
        String time = timeline.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                      timeline.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        imageView.setImage(new Image(imageUrl, true));
        boolean isPlayable = false;

        if (scheduledVideoItem.isVodDelayed()) {
            imageStackPane.getChildren().setAll(imageView, delayedLine);
            imageStackPane.setAlignment(Pos.BOTTOM_CENTER);
        } else {
            LocalDateTime expirationDate = event.getVodExpirationDate();
            //We look if the current video is expired
            if (scheduledVideoItem.getExpirationDate() != null) {
                expirationDate = scheduledVideoItem.getExpirationDate();
            }
            Label stateLabel;
            if (Booleans.isNotTrue(scheduledVideoItem.getFieldValue(EventVideosWallActivity.VIDEO_SCHEDULED_ITEM_DYNAMIC_BOOLEAN_FIELD_HAS_PUBLISHED_MEDIAS))) {
                stateLabel = unavailableLabel;
            } else if (expirationDate != null && Times.isPast(expirationDate)) {
                //TODO: when we know how we will manage the timezone, we adapt to take into account the different timezone
                //TODO: when a push notification is sent we have to update this also.
                stateLabel = expiredLabel;
            } else {
                stateLabel = availableLabel;
                isPlayable = true;
                //TODO manage when we have several media for one videoScheduledItem
            }

            Bootstrap.small(stateLabel);
            stateLabel.setBackground(Background.fill(Color.LIGHTGRAY));
            stateLabel.setPadding(new Insets(4));
            imageStackPane.getChildren().setAll(imageView, stateLabel);
            imageStackPane.setAlignment(Pos.TOP_LEFT);
        }

        titleLabel.setText(title);
        timeLabel.setText(time);

        SvgIcons.setSVGPathFill(playVideoIcon, isPlayable ? Color.RED : Color.LIGHTGRAY);

        SvgIcons.armButton(playVideoButton, !isPlayable ? null : this::playVideo);
    }

    private void playVideo() {
        browsingHistory.push(SessionVideoPlayerRouting.getVideoOfSessionPath(scheduledVideoItem));
    }
}
