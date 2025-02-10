package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.player.StartOptionsBuilder;
import dev.webfx.extras.player.multi.MultiPlayer;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.*;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author Bruno Salmon
 */
final class LivestreamPlayerActivity extends AbstractVideoPlayerActivity {

    private final ObjectProperty<Object> eventIdProperty = new SimpleObjectProperty<>();
    private final MultiPlayer sessionVideoPlayer = AllPlayers.createAllVideoPlayer();
    private final SimpleObjectProperty<String> livestreamUrlProperty = new SimpleObjectProperty<>();
    private Event currentEvent;
    private EntityList<ScheduledItem> todayScheduledItemsList;

    public LivestreamPlayerActivity() {
        //We relaunch every 14 hours the request (in case the user never close the page, and to make sure the coherence of MediaConsumption is ok)
        Scheduler.schedulePeriodic(1000 * 3600 * 14, this::startLogic);
    }

    @Override
    protected void updateModelFromContextParameters() {
        eventIdProperty.set(Numbers.toInteger(getParameter(LivestreamPlayerRouting.EVENT_ID_PARAMETER_NAME)));
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object eventId = eventIdProperty.get();
            LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
            LocalDate todayInEventTimezone = Event.todayInEventTimezone();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            entityStore.<ScheduledItem>executeQuery(
                    new EntityStoreQuery("select event.(name, shortDescription, livestreamUrl), date, programScheduledItem.(startTime, endTime), " +
                        " (select id from Attendance where scheduledItem=si.bookableScheduledItem and documentLine.document.person=? limit 1) as attendanceId " +
                        " from ScheduledItem si" +
                        " where si.event.id=? and exists(select Attendance a where documentLine.(!cancelled and document.(person=? and confirmed and price_balance<=0)) and exists(select ScheduledItem where bookableScheduledItem=a.scheduledItem and item.family.code=?)) "
                        + " and si.item.code=?"
                        + " and si.date=?"
                        + " and si.programScheduledItem.endTime>?",
                        new Object[]{userPersonId, eventId, userPersonId, KnownItemFamily.VIDEO.getCode(), KnownItem.VIDEO.getCode(), todayInEventTimezone, nowInEventTimezone.plusMinutes(30)}))
                .onFailure(Console::log)
                .onSuccess(scheduledItemList -> Platform.runLater(() -> {
                    if (!scheduledItemList.isEmpty()) {
                        todayScheduledItemsList = scheduledItemList;
                        ScheduledItem firstScheduledItem = todayScheduledItemsList.get(0);
                        currentEvent = firstScheduledItem.getEvent();
                        UpdateStore updateStore = UpdateStore.createAbove(currentEvent.getStore());
                        livestreamUrlProperty.set(currentEvent.getLivestreamUrl());
                        //Here we update the MediaConsumption table.
                        //1st case: the scheduledItem has started yet but not finished
                        ScheduledItem programScheduledItem = firstScheduledItem.getProgramScheduledItem();
                        LocalDateTime firstScheduledItemStart = firstScheduledItem.getDate().atTime(programScheduledItem.getStartTime());
                        LocalDateTime firstScheduledItemEnd = firstScheduledItem.getDate().atTime(programScheduledItem.getEndTime());
                        Object attendanceId = firstScheduledItem.getFieldValue("attendanceId");
                        if (Times.isBetween(nowInEventTimezone, firstScheduledItemStart, firstScheduledItemEnd)) {
                            MediaConsumption mediaConsumption = updateStore.insertEntity(MediaConsumption.class);
                            mediaConsumption.setAttendance(attendanceId);
                            mediaConsumption.setLivestreamed(true);
                            mediaConsumption.setScheduledItem(firstScheduledItem);
                            updateStore.submitChanges();
                        }
                        //2nd case: the remaining scheduledItem of the days have not started yet. We schedule the MediaConsumption insertion
                        // (in case the user doesn't refresh the page until the next video. If the next video is an another day, we have refreshed the startLogic after 14h in the constructor)
                        //TODO: not tested for now because no event to test
                        todayScheduledItemsList.forEach(currentScheduledItem -> {
                            LocalDateTime scheduledItemStart = currentScheduledItem.getDate().atTime(currentScheduledItem.getProgramScheduledItem().getStartTime());
                            if (nowInEventTimezone.isBefore(scheduledItemStart)) {
                                long startInMs = ChronoUnit.MILLIS.between(scheduledItemStart, nowInEventTimezone);
                                Object currentAttendanceId = currentScheduledItem.getFieldValue("attendanceId");
                                Scheduler.scheduleDelay(startInMs, () -> {
                                    MediaConsumption mediaConsumption = updateStore.insertEntity(MediaConsumption.class);
                                    mediaConsumption.setAttendance(currentAttendanceId);
                                    mediaConsumption.setLivestreamed(true);
                                    mediaConsumption.setScheduledItem(currentScheduledItem);
                                    updateStore.submitChanges();
                                });
                            }
                        });
                    }
                }));

        }, eventIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    public Node buildUi() {

        Node node = super.buildUi();
        //On the livestream view, we have another element between the title and the video, which is a
        //VBox to display the live message
        VBox liveMessageVBox = new VBox(20);
        liveMessageVBox.setAlignment(Pos.CENTER);
        Label liveMessageTitleLabel = I18nControls.newLabel(VideosI18nKeys.LiveAnnouncementsTitle);
        Label liveMessageLabel = I18nControls.newLabel(VideosI18nKeys.LiveAnnouncements);
        liveMessageLabel.setWrapText(true);
        liveMessageLabel.setAlignment(Pos.CENTER);
        liveMessageVBox.getChildren().addAll(liveMessageTitleLabel, liveMessageLabel);

        sessionDescriptionVBox.setPadding(new Insets(0, 20, 0, 20));
        sessionCommentLabel.managedProperty().bind(sessionCommentLabel.textProperty().isNotEmpty());

        //We add it after the headerHBox
        pageContainer.getChildren().add(pageContainer.getChildren().indexOf(headerHBox) + 1, liveMessageVBox);
        Node videoView = sessionVideoPlayer.getMediaView();
        playersVBoxContainer.getChildren().add(videoView);


        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************
        // Auto starting the video for each requested session
        FXProperties.runNowAndOnPropertyChange(this::updateSessionTitleAndVideoPlayerState, livestreamUrlProperty);
        return node;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restarting the session video player (if relevant) when reentering this activity. This will also ensure that
        // any possible previous playing player (ex: podcast) will be paused if/when the session video player restarts.
        //updateSessionTitleAndVideoPlayerState();
    }


    @Override
    protected void syncHeader() {
        if (currentEvent != null) {
            eventLabel.setText(currentEvent.getName());
            eventDescriptionHtmlText.setText(currentEvent.getShortDescription());
            if (currentEvent != null)
                updatePicture(currentEvent);
        }
        sessionTitleLabel.setText("Livestream Session");
    }

    @Override
    protected void syncPlayerContent() {
        if (livestreamUrlProperty.get() != null) {
            boolean autoPlay = true;
            sessionVideoPlayer.getMediaView().setVisible(true);
            sessionVideoPlayer.setMedia(sessionVideoPlayer.acceptMedia(livestreamUrlProperty.get()));
            sessionVideoPlayer.setStartOptions(new StartOptionsBuilder()
                .setAutoplay(autoPlay)
                .setAspectRatioTo16by9() // should be read from metadata but hardcoded for now
                .build());
            sessionVideoPlayer.play();
        } else {
            sessionVideoPlayer.setMedia(null);
            sessionVideoPlayer.resetToInitialState();
        }
    }
}
