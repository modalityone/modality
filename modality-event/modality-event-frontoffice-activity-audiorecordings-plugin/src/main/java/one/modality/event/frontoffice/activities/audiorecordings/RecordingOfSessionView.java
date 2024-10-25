package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entities;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class RecordingOfSessionView {

    private final Attendance attendance;
    private final List<Media> medias;

    private AudioRecordingMediaInfoView mediaView;

    private final VBox container = new VBox();

    public RecordingOfSessionView(Attendance attendance, List<Media> medias) {
        this.attendance = attendance;
        this.medias = medias;
        buildUi();
    }

    VBox getView() {
        return container;
    }

    AudioRecordingMediaInfoView getMediaView() {
        return mediaView;
    }

    private void buildUi() {
        Timeline timeline = attendance.getScheduledItem().getParent().getTimeline();
        //Console.log("--------------" + attendance.getDocumentLine().getDocument().getRef());
        ScheduledItem audioRecordScheduledItem = attendance.getScheduledItem();
        Label dateLabel = new Label(audioRecordScheduledItem.getDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) +
                                    timeline.getStartTime().format(DateTimeFormatter.ofPattern(" - HH:mm")));
        //   dateLabel.setPadding(new Insets(40,0,0,0));
        dateLabel.getStyleClass().add(Bootstrap.STRONG);
        String title = audioRecordScheduledItem.getParent().getName();
        Label titleLabel = new Label(title);
        container.getChildren().addAll(
            dateLabel,
            titleLabel
        );
        List<Media> currentMediaList = medias.stream()
            .filter(media -> media.getScheduledItem() != null && Entities.sameId(audioRecordScheduledItem, media.getScheduledItem()))
            .collect(Collectors.toList());
        //Here we should have only one media for audio
        if (currentMediaList.isEmpty()) {
            Label noMediaLabel = I18nControls.bindI18nProperties(new Label(), AudioRecordingsI18nKeys.AudioRecordingNotPublishedYet);
            noMediaLabel.getStyleClass().add(Bootstrap.TEXT_WARNING);
            container.getChildren().add(noMediaLabel);
        } else {
            String url = currentMediaList.get(0).getUrl();
            AudioMedia audioMedia = new AudioMedia();
            audioMedia.setAudioUrl(url);
            audioMedia.setTitle(title);
            audioMedia.setDate(LocalDateTime.of(audioRecordScheduledItem.getDate(), timeline.getStartTime()));
            audioMedia.setDurationMillis(currentMediaList.get(0).getDurationMillis());
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
