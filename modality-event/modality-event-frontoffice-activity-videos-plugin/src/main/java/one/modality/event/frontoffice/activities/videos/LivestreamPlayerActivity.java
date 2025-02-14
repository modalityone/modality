package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.StartOptionsBuilder;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItem;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.medias.MediaConsumptionRecorder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author Bruno Salmon
 */
final class LivestreamPlayerActivity extends AbstractVideoPlayerActivity {

    private final ObjectProperty<Object> eventIdProperty = new SimpleObjectProperty<>();
    private Event currentEvent;
    private final SimpleObjectProperty<String> livestreamUrlProperty = new SimpleObjectProperty<>();
    private EntityList<ScheduledItem> todayScheduledItems;
    private final Player livestreamPlayer = AllPlayers.createAllVideoPlayer();
    private MediaConsumptionRecorder mediaConsumptionRecorder;

    public LivestreamPlayerActivity() {
        //We relaunch every 14 hours the request (in case the user never close the page, and to make sure the coherence of MediaConsumption is ok)
        Scheduler.schedulePeriodic(14 * 3600 * 1000, this::startLogic);
        FXProperties.runOnPropertyChange(videoScheduledItem -> {
            if (mediaConsumptionRecorder != null)
                mediaConsumptionRecorder.stop();
            mediaConsumptionRecorder = new MediaConsumptionRecorder(livestreamPlayer, true, () -> videoScheduledItem, () -> null);
            mediaConsumptionRecorder.start();
        }, scheduledVideoItemProperty);
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
            entityStore.<ScheduledItem>executeQuery("select event.(name, shortDescription, livestreamUrl), date, programScheduledItem.(startTime, endTime), " +
                        " (select id from Attendance where scheduledItem=si.bookableScheduledItem and documentLine.document.person=? limit 1) as attendanceId " +
                        " from ScheduledItem si" +
                        " where si.event.id=? and exists(select Attendance a where documentLine.(!cancelled and document.(person=? and confirmed and price_balance<=0)) and exists(select ScheduledItem where bookableScheduledItem=a.scheduledItem and item.family.code=?)) "
                        + " and si.item.code=?"
                        + " and si.date=?"
                        + " and si.programScheduledItem.endTime>?",
                        userPersonId, eventId, userPersonId, KnownItemFamily.VIDEO.getCode(), KnownItem.VIDEO.getCode(), todayInEventTimezone, nowInEventTimezone.plusMinutes(30))
                .onFailure(Console::log)
                .onSuccess(scheduledItems -> Platform.runLater(() -> {
                    todayScheduledItems = scheduledItems;
                    if (!scheduledItems.isEmpty()) {
                        ScheduledItem firstScheduledItem = todayScheduledItems.get(0);
                        currentEvent = firstScheduledItem.getEvent();
                        livestreamUrlProperty.set(currentEvent.getLivestreamUrl());
                        // The livestream url is always the same for the event, but we still need to determine which
                        // session is being played for the MediaConsumption management. To do this, we will set
                        // scheduledVideoItemProperty with the scheduled item corresponding to the played session.
                        //TODO: not tested for now because no event to test
                        todayScheduledItems.forEach(scheduledItem -> { // iterating today sessions
                            ScheduledItem programScheduledItem = scheduledItem.getProgramScheduledItem();
                            LocalDateTime scheduledItemStart = scheduledItem.getDate().atTime(programScheduledItem.getStartTime());
                            LocalDateTime scheduledItemEnd = scheduledItem.getDate().atTime(programScheduledItem.getEndTime());
                            // If we are in the middle of the session, we set scheduledVideoItemProperty now
                            if (Times.isBetween(nowInEventTimezone, scheduledItemStart, scheduledItemEnd)) {
                                // this will trigger the MediaConsumption management (see constructor)
                                scheduledVideoItemProperty.set(scheduledItem);
                            } else if (nowInEventTimezone.isBefore(scheduledItemStart)) { // future session
                                // We postpone the
                                long startInMs = ChronoUnit.MILLIS.between(scheduledItemStart, nowInEventTimezone);
                                UiScheduler.scheduleDelay(startInMs, () -> {
                                    if (todayScheduledItems == scheduledItems) // Double-checking that there was no startLogic() calls after this one
                                        scheduledVideoItemProperty.set(scheduledItem);
                                });
                            }
                        });
                    }
                }));

        }, eventIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    public Node buildUi() {
        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

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
        Node videoView = livestreamPlayer.getMediaView();
        playersVBoxContainer.getChildren().add(videoView);


        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************
        // Auto starting the video for each requested session
        FXProperties.runNowAndOnPropertyChange(this::updateSessionTitleAndVideoPlayerContent, livestreamUrlProperty);
        return node;
    }

    @Override
    protected void syncHeader() {
        if (currentEvent != null) {
            eventLabel.setText(currentEvent.getName());
            eventDescriptionHtmlText.setText(currentEvent.getShortDescription());
            if (currentEvent != null)
                updatePicture(currentEvent);
        }
        sessionTitleLabel.setText("Livestream Session"); // i18n???
    }

    @Override
    protected void syncPlayerContent() {
        String livestreamUrl = livestreamUrlProperty.get();
        if (livestreamUrl != null) {
            boolean autoPlay = true;
            livestreamPlayer.getMediaView().setVisible(true);
            livestreamPlayer.setMedia(livestreamPlayer.acceptMedia(livestreamUrl));
            livestreamPlayer.setStartOptions(new StartOptionsBuilder()
                .setAutoplay(autoPlay)
                .setAspectRatioTo16by9() // should be read from metadata but hardcoded for now
                .build());
            livestreamPlayer.play();
        } else {
            livestreamPlayer.setMedia(null);
            livestreamPlayer.resetToInitialState();
        }
    }
}
