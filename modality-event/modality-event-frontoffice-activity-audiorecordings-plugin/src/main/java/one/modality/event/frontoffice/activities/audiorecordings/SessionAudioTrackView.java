package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entities;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class SessionAudioTrackView {

    private final ScheduledItem scheduledAudioItem;
    private final List<Media> publishedMedias;

    private AudioRecordingMediaInfoView mediaView;

    private final VBox container = new VBox();

    public SessionAudioTrackView(ScheduledItem scheduledAudioItem, List<Media> publishedMedias) {
        this.scheduledAudioItem = scheduledAudioItem;
        this.publishedMedias = publishedMedias.stream()
            .filter(media -> media.getScheduledItem() != null && Entities.sameId(scheduledAudioItem, media.getScheduledItem()))
            .collect(Collectors.toList());
        buildUi();
    }

    VBox getView() {
        return container;
    }

    AudioRecordingMediaInfoView getMediaView() {
        return mediaView;
    }

    private void buildUi() {
        String title = scheduledAudioItem.getParent().getName();
        Timeline timeline = scheduledAudioItem.getParent().getTimeline();
        LocalDate date = scheduledAudioItem.getDate();
        LocalTime startTime = timeline.getStartTime();

        Label dateLabel = Bootstrap.strong(new Label(date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + " - " +
                                                     startTime.format(DateTimeFormatter.ofPattern("HH:mm"))));
        Label titleLabel = new Label(title);
        container.getChildren().addAll(
            dateLabel,
            titleLabel
        );
        //Here we should have only one media for audio
        if (publishedMedias.isEmpty()) {
            Label noMediaLabel = I18nControls.bindI18nProperties(new Label(), AudioRecordingsI18nKeys.AudioRecordingNotYetPublished);
            noMediaLabel.getStyleClass().add(Bootstrap.TEXT_WARNING);
            container.getChildren().add(noMediaLabel);
        } else {
            String url = publishedMedias.get(0).getUrl();
            AudioMedia audioMedia = new AudioMedia();
            audioMedia.setAudioUrl(url);
            audioMedia.setTitle(title);
            audioMedia.setDate(LocalDateTime.of(date, startTime));
            audioMedia.setDurationMillis(publishedMedias.get(0).getDurationMillis());
            mediaView = new AudioRecordingMediaInfoView();
            mediaView.setMediaInfo(audioMedia);
            container.getChildren().add(mediaView.getView());

        }
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setMaxWidth(800);
        separator.setPadding(new Insets(40, 0, 0, 0));
        container.getChildren().add(separator);
    }

}
