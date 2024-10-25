package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.base.shared.entities.Media;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.client.mediaview.MediaInfoView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class RecordingsOfEventView {

    private final Event event;
    private final Runnable backRunnable;

    private final VBox container = new VBox(10);

    public RecordingsOfEventView(Event event, Runnable backRunnable) {
        this.event = event;
        this.backRunnable = backRunnable;
        buildUi();
    }

    public VBox getView() {
        return container;
    }

    private void buildUi() {
        List<AudioRecordingMediaInfoView> playerList = new ArrayList<>();
        HBox firstLine = new HBox();
        firstLine.setAlignment(Pos.CENTER_LEFT);
        firstLine.setSpacing(50);
        container.getChildren().add(firstLine);
        MonoPane backArrow = new MonoPane(SvgIcons.createBackArrow());
        backArrow.setPadding(new Insets(20));
        backArrow.setCursor(Cursor.HAND);
        backArrow.setOnMouseClicked(e -> {
            playerList.forEach(MediaInfoView::stopPlayer);
            backRunnable.run();
        });
        firstLine.getChildren().add(backArrow);
        Label eventTitleLabel = new Label(event.getName());
        eventTitleLabel.getStyleClass().add(Bootstrap.H2);
        eventTitleLabel.getStyleClass().add(Bootstrap.STRONG);
        Label titleDescriptionLabel = new Label(event.getShortDescription());
        VBox titleVBox = new VBox(eventTitleLabel, titleDescriptionLabel);
        titleVBox.setAlignment(Pos.CENTER_LEFT);
        firstLine.getChildren().add(titleVBox);
        firstLine.setPadding(new Insets(0, 0, 100, 0));

        event.getStore().executeQueryBatch(
                new EntityStoreQuery("select scheduledItem.(date, parent.(name, timeline.(startTime, endTime)), event), documentLine.document.ref from Attendance where documentLine.(document.(event= ? and person=? and price_balance<=0) and item.family.code=?) order by scheduledItem.date",
                    new Object[]{event.getId(), FXUserPersonId.getUserPersonId(), KnownItemFamily.AUDIO_RECORDING.getCode()}),
                new EntityStoreQuery("select url, scheduledItem.(date, event), published, durationMillis from Media where scheduledItem.(event= ? and item.family.code=? and online) and published",
                    new Object[]{event.getId(), KnownItemFamily.AUDIO_RECORDING.getCode()}))
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                EntityList<Attendance> attendances = entityLists[0];
                EntityList<Media> medias = entityLists[1];

                if (attendances.isEmpty()) {
                    Label noContentLabel = Bootstrap.h3(Bootstrap.textWarning(I18nControls.bindI18nProperties(new Label(), AudioRecordingsI18nKeys.NoAudioRecordingForThisEvent)));
                    noContentLabel.setPadding(new Insets(150, 0, 100, 0));
                    container.getChildren().add(noContentLabel);
                } else {
                    attendances.forEach(attendance -> {
                        RecordingOfSessionView recordingOfSessionView = new RecordingOfSessionView(attendance, medias);
                        AudioRecordingMediaInfoView mediaView = recordingOfSessionView.getMediaView();
                        if (mediaView != null)
                            playerList.add(mediaView);
                        container.getChildren().add(recordingOfSessionView.getView());
                    });
                }
            }));
    }
}
