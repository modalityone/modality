package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.player.StartOptionsBuilder;
import dev.webfx.extras.player.multi.MultiPlayer;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.activities.audiorecordings.AudioRecordingsI18nKeys;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Bruno Salmon
 */
final class SessionVideoPlayerActivity extends AbstractVideoPlayerActivity {


    private Label videoExpirationLabel;

    @Override
    public void onResume() {
        super.onResume();
        // Restarting the session video player (if relevant) when reentering this activity. This will also ensure that
        // any possible previous playing player (ex: podcast) will be paused if/when the session video player restarts.
        //updateSessionTitleAndVideoPlayerState();
    }

    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object scheduledVideoItemId = scheduledVideoItemIdProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (scheduledVideoItemId == null || userPersonId == null) {
                publishedMedias.clear();
                scheduledVideoItemProperty.set(null); // Will update UI
            } else {
                entityStore.executeQueryBatch(
                        new EntityStoreQuery("select name, expirationDate, comment, date, startTime, endTime, programScheduledItem.(name, startTime, endTime, timeline.(startTime, endTime)), event.(name, shortDescription, vodExpirationDate, type.recurringItem)" +
                            " from ScheduledItem si" +
                            " where id=? and published and exists(select Attendance where scheduledItem=si.bookableScheduledItem and documentLine.(!cancelled and document.(person=? and price_balance<=0)))",
                            new Object[]{scheduledVideoItemId, userPersonId}),
                        new EntityStoreQuery("select url" +
                            " from Media" +
                            " where scheduledItem.(id=? and online)",
                            new Object[]{scheduledVideoItemId}))
                    .onFailure(Console::log)
                    .onSuccess(entityLists -> Platform.runLater(() -> {
                        Collections.setAll(publishedMedias, entityLists[1]);
                        scheduledVideoItemProperty.set((ScheduledItem) Collections.first(entityLists[0]));  // Will update UI
                    }));
            }
        }, scheduledVideoItemIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    public Node buildUi() {
        Node node = super.buildUi();
        videoExpirationLabel = I18nControls.newLabel(AudioRecordingsI18nKeys.AvailableUntil);
        videoExpirationLabel.setPadding(new Insets(30, 0, 0, 0));
        titleVBox.getChildren().add(videoExpirationLabel);

        sessionDescriptionVBox.setPadding(new Insets(100, 20, 0, 20));


        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************
        // Auto starting the video for each requested session
        FXProperties.runNowAndOnPropertyChange(this::updateSessionTitleAndVideoPlayerState, scheduledVideoItemProperty);
        return node;
    }

    protected void updateModelFromContextParameters() {
        scheduledVideoItemIdProperty.set(Numbers.toInteger(getParameter(SessionVideoPlayerRouting.SCHEDULED_VIDEO_ITEM_ID_PARAMETER_NAME)));
    }


    protected void syncHeader() {
        ScheduledItem scheduledVideoItem = scheduledVideoItemProperty.get();
        Media firstMedia = Collections.first(publishedMedias);
        if (scheduledVideoItem != null && firstMedia != null) { // may not yet be loaded on first call
            String title = scheduledVideoItem.getProgramScheduledItem().getName();
            //If the name of the video scheduledItem has been overwritten, we use it, otherwise, we use the name of the programScheduledItem
            if (scheduledVideoItem.getName() != null && !scheduledVideoItem.getName().isBlank()) {
                title = scheduledVideoItem.getName();
            }

            String url = firstMedia.getUrl();
            eventLabel.setText(scheduledVideoItem.getEvent().getName());
            eventDescriptionHtmlText.setText(scheduledVideoItem.getEvent().getShortDescription());
            LocalDateTime startTime;
            if (scheduledVideoItem.getEvent().getType().getRecurringItem() != null) {
                startTime = LocalDateTime.of(scheduledVideoItem.getDate(), scheduledVideoItem.getProgramScheduledItem().getStartTime());
            } else {
                startTime = LocalDateTime.of(scheduledVideoItem.getDate(), scheduledVideoItem.getProgramScheduledItem().getTimeline().getStartTime());
            }
            if (startTime != null)
                sessionTitleLabel.setText(title + " (" + startTime.format(DateTimeFormatter.ofPattern("d MMMM, yyyy ' - ' HH:mm")) + ")");
            else {
                sessionTitleLabel.setText(title + " (" + scheduledVideoItem.getDate().format(DateTimeFormatter.ofPattern("d MMMM, yyyy")) + ")");
            }
            sessionCommentLabel.setText(scheduledVideoItem.getComment());
            if (scheduledVideoItem.getComment() != null) {
                sessionCommentLabel.setManaged(true);
            } else {
                sessionCommentLabel.setManaged(false);
            }
            Event event = scheduledVideoItem.getEvent();
            updatePicture(event);

            //We look at the expiration Date on the scheduledItem, if it is null, we look at the expiration date on the event
            LocalDateTime expirationDate = scheduledVideoItem.getExpirationDate();
            if (expirationDate == null)
                expirationDate = scheduledVideoItem.getEvent().getVodExpirationDate();
            if (expirationDate != null) {
                if (LocalDateTime.now().isBefore(expirationDate))
                    I18nControls.bindI18nProperties(videoExpirationLabel, VideosI18nKeys.VideoAvailableUntil, expirationDate.format(DateTimeFormatter.ofPattern("d MMMM, yyyy ' - ' HH:mm")));
                else
                    I18nControls.bindI18nProperties(videoExpirationLabel, VideosI18nKeys.VideoExpiredSince, expirationDate.format(DateTimeFormatter.ofPattern("d MMMM, yyyy ' - ' HH:mm")));
                videoExpirationLabel.setVisible(true);
            } else {
                videoExpirationLabel.setVisible(false);
            }
        }
    }

    @Override
    protected void syncPlayerContent() {
        // Create a Player for each Media, and initialize it.
        boolean autoPlay = true;

        playersVBoxContainer.getChildren().clear();
        for (Media mediaEntity : publishedMedias) {
            MultiPlayer currentVideoPlayer = AllPlayers.createAllVideoPlayer();
            currentVideoPlayer.setMedia(currentVideoPlayer.acceptMedia(mediaEntity.getUrl()));
            currentVideoPlayer.setStartOptions(new StartOptionsBuilder()
                .setAutoplay(autoPlay)
                .setAspectRatioTo16by9() // should be read from metadata but hardcoded for now
                .build());
            Node videoView = currentVideoPlayer.getMediaView();
            playersVBoxContainer.getChildren().add(videoView);
            currentVideoPlayer.play();
            // we autoplay only the first video
            autoPlay = false;
        }
    }

}
