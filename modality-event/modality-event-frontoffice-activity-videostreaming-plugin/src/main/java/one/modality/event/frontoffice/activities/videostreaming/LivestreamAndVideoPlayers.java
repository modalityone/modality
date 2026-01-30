package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.panes.*;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.Players;
import dev.webfx.extras.player.StartOptionsBuilder;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.substitution.Substitutor;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.medias.MediaConsumptionRecorder;
import one.modality.event.frontoffice.medias.TimeZoneSwitch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class LivestreamAndVideoPlayers {

    private static final boolean DISABLE_COLLAPSE_VIDEO_PLAY = Booleans.booleanValue(Substitutor.substitute("${{DISABLE_COLLAPSE_VIDEO_PLAY}}"));

    private final ObjectProperty<Event> eventProperty;
    private final ObservableList<ScheduledItem> videoScheduledItems;
    private EntityStore entityStore;

    private final ObjectProperty<ScheduledItem> watchingVideoItemProperty = new SimpleObjectProperty<>(); // the Livestream or VOD to watch
    private final List<Media> watchMedias = new ArrayList<>(); // the medias of the VOD to watch

    private final CollapsePane videoCollapsePane = new CollapsePane(); // contains the video player(s): 1 for livestream, 1 per media for VOD
    private final StackPane decoratedVideoCollapsePane = CollapsePane.decorateCollapsePane(videoCollapsePane, true);
    private final List<MediaConsumptionRecorder> videoConsumptionRecorders = new ArrayList<>(); // the video consumption recorders (1 per player)

    private Player lastVideoPlayingPlayer; // the last playing player from this activity
    private String playingLivestreamUrl; // we memorize the livestream url to skip unnecessary multiple player creations

    public LivestreamAndVideoPlayers(ObjectProperty<Event> eventProperty, ObservableList<ScheduledItem> videoScheduledItems) {
        this.eventProperty = eventProperty;
        this.videoScheduledItems = videoScheduledItems;
        videoCollapsePane.collapse(); // initially collapsed - might be automatically expanded by scheduleAutoLivestream()
        decoratedVideoCollapsePane.setVisible(false); // will be visible if it contains at least a video or livestream

    }

    public void startLogic(EntityStore entityStore) {
        this.entityStore = entityStore;
        ObservableLists.runNowAndOnListChange(c -> {
            watchingVideoItemProperty.set(null);
            // We are now ready to populate the videos, but we postpone this for the 2 following reasons:
            // 1) The UI may not be completely built yet on low-end devices, and loading a video player now
            // could be heavy and freeze the UI even more.
            // 2) If there is a livestream now (or close), scheduleAutoLivestream() will auto-expand the
            // video player and auto-scroll to it, but the auto-scroll target position may not be stable at
            // this time (ex: the video table not finished building), causing a wrong final scroll position.
            UiScheduler.scheduleDelay(2000, () -> { // 2 seconds is a reasonable waiting time
                populateVideoPlayers(false); // will load the video player
                scheduleAutoLivestream(); // may auto-expand the video player if now is an appropriate time
            });
            //If the event is finished, we display the local date/time for the video
            if(eventProperty.get() != null && eventProperty.get().getEndDate().isBefore(LocalDate.now())) {
                TimeZoneSwitch.getGlobal().setEventLocalTimeSelected(true);
            }
        }, videoScheduledItems);
        // Later Media loading when the user wants to watch a specific video (this sets watchVideoItemProperty)
        FXProperties.runOnPropertiesChange(() -> {
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            watchMedias.clear();
            if (userPersonId == null || isUserWatchingLivestream()) {
                populateVideoPlayers(true); // livestream
            } else { // The VOD requires additional Media loading
                loadMediaAndWatch();
            }
        }, watchingVideoItemProperty, FXUserPersonId.userPersonIdProperty());
    }

    public Node buildUi(MonoPane pageContainer) {
        videoCollapsePane.prefWidthProperty().bind(pageContainer.widthProperty());

        // When the livestream collapse pane is collapsed, we pause the livestreamPlayer so the full-screen orange button
        // is not displayed
        if (!DISABLE_COLLAPSE_VIDEO_PLAY) {
            FXProperties.runNowAndOnPropertiesChange(() -> Platform.runLater(() -> { // Postponed to consider only the final state when both properties are changed
                Player playingPlayer = Players.getGlobalPlayerGroup().getPlayingPlayer();
                // commented as this prevents pausing VODs
                // if (playingPlayer != null && SceneUtil.hasAncestor(playingPlayer.getMediaView(), videoCollapsePane)) {
                lastVideoPlayingPlayer = playingPlayer;
                //}
                if (lastVideoPlayingPlayer != null) {
                    if (videoCollapsePane.isCollapsed()) {
                        Console.log("Pausing " + lastVideoPlayingPlayer);
                        lastVideoPlayingPlayer.pause();
                    } else {
                        MediaConsumptionRecorder videoConsumptionRecorder = Collections.findFirst(videoConsumptionRecorders,
                            vcr -> Players.sameSelectedPlayer(vcr.getPlayer(), lastVideoPlayingPlayer));
                        if (videoConsumptionRecorder != null)
                            videoConsumptionRecorder.start();
                        if (!lastVideoPlayingPlayer.isPlaying()) {
                            Console.log("⛔️⛔️⛔️⛔️⛔️ Playing " + lastVideoPlayingPlayer);
                            lastVideoPlayingPlayer.play();
                        }
                    }
                }
            }), videoCollapsePane.collapsedProperty(), Players.getGlobalPlayerGroup().playingPlayerProperty());
        }

        // Auto-scroll to the video player when it is expanded or watching a new video
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (videoCollapsePane.isExpanded()) { // auto-scroll only when expanded (might be automatically expanded before)
                UiScheduler.scheduleDeferred(() -> // the transition may start just after collapsedProperty is set, so we defer the call
                    // we wait for the possible transition to finish, and once finished, we scroll to the video player
                    FXProperties.onPropertyEquals(videoCollapsePane.transitingProperty(), Boolean.FALSE, x ->
                        scrollToVideoPlayer()));
            }
        }, videoCollapsePane.collapsedProperty(), watchingVideoItemProperty);

        return decoratedVideoCollapsePane;
    }

    private void scheduleAutoLivestream() {
        // We don't interrupt the user if he is already watching a video
        if (videoCollapsePane.isExpanded())
            return;

        // The livestream url is always the same for the event, but we still need to determine which
        // session is being played for the MediaConsumption management. To do this, we will set
        // `scheduledVideoItemProperty` with the scheduled item corresponding to the played session.
        for (ScheduledItem videoScheduledItem : videoScheduledItems) { // iterating video sessions
            VideoLifecycle videoLifecycle = new VideoLifecycle(videoScheduledItem);

            //If we're 20 minutes before or 30 minutes after the teaching, we display the livestream window
            if (isTimeToShowVideoAsLivestream(videoLifecycle)) {
                watchingVideoItemProperty.set(videoScheduledItem); // Stopping possible VOD and showing livestream instead
                videoCollapsePane.expand(); // Ensures the livestream player is showing
                UiScheduler.scheduleDelay(videoLifecycle.durationMillisBetweenNowAndShowLivestreamEnd(), this::scheduleAutoLivestream);
                return;
            } else if (videoLifecycle.isNowBeforeShowLivestreamStart()) {
                UiScheduler.scheduleDelay(videoLifecycle.durationMillisBetweenNowAndShowLivestreamStart(), this::scheduleAutoLivestream);
            }
        }
        // If we reach this point, it's because there is no livestream to show at the moment, so we collapse
        // videoCollapsePane (unless the user switched to a VOD in the meantime)
        if (isUserWatchingLivestream()) // livestream (not VOD)
            videoCollapsePane.collapse();
    }

    private void loadMediaAndWatch() {
        entityStore.<Media>executeQuery("select url from Media where scheduledItem=$1 order by id", getWatchingVideoItem())
            .onFailure(Console::log)
            .inUiThread()
            .onSuccess(mediaLists -> {
                Collections.setAll(watchMedias, mediaLists);
                populateVideoPlayers(true); // VOD
                videoCollapsePane.expand();
            });
    }

    private boolean isUserWatchingLivestream() {
        ScheduledItem watchingVideoItem = getWatchingVideoItem();
        // Note sure if watchingVideoItem == null is still relevant. TODO: investigate this.
        return watchingVideoItem == null || isTimeToShowVideoAsLivestream(new VideoLifecycle(watchingVideoItem));
    }

    private boolean isTimeToShowVideoAsLivestream(VideoLifecycle videoLifecycle) {
        ScheduledItem videoScheduledItem = videoLifecycle.getVideoScheduledItem();
        return videoScheduledItem != null && !VideoState.isVideoCancelled(videoScheduledItem) && !videoScheduledItem.isPublished() && videoLifecycle.isNowBetweenShowLivestreamStartAndShowLivestreamEnd();
    }

    // Called by the "Watch" button from the VideoFormattersAndRenderers
    void setWatchingVideo(VideoLifecycle watchingVideoLifecycle) {
        // If it's a different video from the one currently watched by the user, we set the watchingVideoItemProperty
        // and this will trigger all necessary consequent events (loading of media, expanding videoCollapsePane and
        // auto-scrolling to the video player).
        ScheduledItem watchingVideoItem = watchingVideoLifecycle.getVideoScheduledItem();
        if (!isSameVideoAsAlreadyWatching(watchingVideoLifecycle)) {
            watchingVideoItemProperty.set(watchingVideoItem);
        } else { // But if it's the same video, the next step depends on its current state.
            // Let's start with the particular case where the user just received the push notification that the video
            // has been published (the livestream video became a VOD). While this push updated its published field,
            // the associated media are still unloaded, so we need now to load them and start the first media.
            if (watchingVideoItem.isPublished() && watchMedias.isEmpty()) { // detection of the case explained above
                loadMediaAndWatch();
            } else if (videoCollapsePane.isExpanded()) // otherwise, if the player is already expanded and with the
                // correct video already inside, the only remaining thing to do is to scroll to that video player
                scrollToVideoPlayer();
            else // if it is collapsed, we expand it first, and the auto-scroll will happen just after that - see buildUi()
                videoCollapsePane.expand();
        }
    }

    boolean isSameVideoAsAlreadyWatching(VideoLifecycle videoLifecycle) {
        return Objects.areEquals(videoLifecycle.getVideoScheduledItem(), getWatchingVideoItem())
               || isUserWatchingLivestream() && !videoLifecycle.getVideoScheduledItem().isPublished() && videoLifecycle.isNowBetweenLiveNowStartAndSessionEnd();
    }

    ScheduledItem getWatchingVideoItem() {
        return watchingVideoItemProperty.get();
    }

    ObjectProperty<ScheduledItem> watchingVideoItemProperty() {
        return watchingVideoItemProperty;
    }

    private void scrollToVideoPlayer() {
        Controls.setVerticalScrollNodeWishedPosition(videoCollapsePane, VPos.CENTER);
        SceneUtil.scrollNodeToBeVerticallyVisibleOnScene(videoCollapsePane, false, true);
    }

    private void populateVideoPlayers(boolean willAutoplay) {
        Node videoContent = null;
        VBox videoVBox = new VBox(20);
        videoVBox.setAlignment(Pos.CENTER);

        boolean autoPlay = willAutoplay || videoCollapsePane.isExpanded();
        if (isUserWatchingLivestream()) { // Livestream
            Event event = eventProperty.get();
            String livestreamUrl = event == null ? null : event.getLivestreamUrl();
            if (livestreamUrl != null) {
                // populateVideoPlayers() might be called several times for the same livestream. If this happens,
                // we don't recreate the player each time but just keep the existing one.
                if (Objects.areEquals(livestreamUrl, playingLivestreamUrl)) {
                    return;
                }
                playingLivestreamUrl = livestreamUrl;
                Player livestreamPlayer = createVideoPlayer();
                LivestreamNotificationOverlay.addNotificationOverlayToLivestreamPlayer(livestreamPlayer, event);
                // Because we keep the same player for the same livestream url (without rebuilding it later),
                // we force the autoplay right now if this player doesn't have a play() api.
                autoPlay = autoPlay || !livestreamPlayer.getNavigationSupport().api();
                // Creating the video view
                videoContent = createVideoView(livestreamUrl, null, autoPlay, livestreamPlayer);
            }
        } else { // VOD
            playingLivestreamUrl = null;
            // Creating a Player for each Media and initializing it.
            VBox videoMediasVBox = new VBox(10);
            String comment = getWatchingVideoItem().getComment();
            one.modality.base.shared.entities.Label commentLabel = getWatchingVideoItem().getCommentLabel();
            Label commentUILabel = null;
            if (commentLabel != null) {
                commentUILabel = I18nEntities.newTranslatedEntityLabel(commentLabel);
            } else if (comment != null) {
                commentUILabel = new Label(comment);
            }
            if (commentUILabel != null) {
                commentUILabel.getStyleClass().add("video-comment");
                commentUILabel.setTextAlignment(TextAlignment.CENTER);
                Controls.setupTextWrapping(commentUILabel, true, false);
                videoVBox.getChildren().add(commentUILabel);
            }
            for (Media media : watchMedias) {
                Player videoPlayer = createVideoPlayer();
                Node videoView = createVideoView(media.getUrl(), media, autoPlay, videoPlayer);
                videoMediasVBox.getChildren().add(videoView);
                videoPlayer.getOverlayChildren(); // We don't add any overlay children yet, but after this "empty" call
                // videoPlayer.appRequestedOverlayChildren() will return true, and ModalityVideoOverlay will detect the
                // presence of an overlay and add the fullscreen button (which is the purpose of this empty call).
                autoPlay = false; // we autoplay only the first video
            }
            Layouts.setMinMaxHeightToPref(videoMediasVBox); // No need to compute min/max height as different to pref (layout computation optimization)
            videoContent = videoMediasVBox;
        }
        if (videoContent == null) {
            videoCollapsePane.setContent(null);
        } else {
            ScalePane videoContainer = new ScalePane(ScaleMode.FIT_WIDTH, videoContent);
            videoVBox.getChildren().add(videoContainer);
            videoCollapsePane.setContent(videoVBox);
        }
        decoratedVideoCollapsePane.setVisible(videoContent != null);
    }

    private Player createVideoPlayer() {
        // Because we create a new player, we stop all possible previous media consumption recorders.
        videoConsumptionRecorders.forEach(MediaConsumptionRecorder::stop);
        videoConsumptionRecorders.clear();
        // We create a multi-player that supports all video player plugins embed in Modality (Castr, VideoJS, Wistia, YouTube).
        return AllPlayers.createAllVideoPlayer();
    }

    private Node createVideoView(String url, Media media, boolean autoPlay, Player videoPlayer) {
        // Aspect ratio should be read from metadata but hardcoded for now
        double aspectRatio = 16d / 9d;
        if (url.contains("wistia"))   // Wistia is used only for the Festival play so far
            aspectRatio = 1085d / 595d; // This is the aspect ratio for the Life of Buddha play (hardcoded for now)
        videoPlayer.setMedia(videoPlayer.acceptMedia(url), new StartOptionsBuilder()
            .setAutoplay(autoPlay)
            .setAspectRatio(aspectRatio)
            .setFullscreen(false)
            .build());
        videoPlayer.displayVideo();
        boolean livestream = media == null;
        if (livestream)
            lastVideoPlayingPlayer = videoPlayer;
        MediaConsumptionRecorder videoConsumptionRecorder = new MediaConsumptionRecorder(videoPlayer, livestream, watchingVideoItemProperty::get, () -> media);
        videoConsumptionRecorders.add(videoConsumptionRecorder);
        if (autoPlay)
            videoConsumptionRecorder.start();
        // We embed the video player in an aspect ratio pane, so its vertical size is immediately known, which is
        // important for the correct computation of the auto-scroll position.
        return new AspectRatioPane(aspectRatio, videoPlayer.getMediaView());
    }
}
