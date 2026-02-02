package one.modality.event.frontoffice.medias;

import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.Players;
import dev.webfx.extras.player.Status;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.shutdown.Shutdown;
import dev.webfx.platform.shutdown.ShutdownEvent;
import dev.webfx.platform.util.stopwatch.StopWatch;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.MediaConsumption;
import one.modality.base.shared.entities.ScheduledItem;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
public class MediaConsumptionRecorder {

    private final Supplier<ScheduledItem> scheduledItemSupplier;
    private final Supplier<Media> mediaSupplier;
    private final Player player;
    private final boolean download;
    private final boolean livestream;
    // StopWatch that will be running only while the player is playing to record the user playing duration
    private final StopWatch playingStopWatch = StopWatch.createSystemMillisStopWatch();
    private boolean started;
    private MediaConsumption playingMediaConsumption;
    private Consumer<ShutdownEvent> shutdownHook;

    private MediaConsumptionRecorder(ScheduledItem scheduledItem, Media media) { // for download
        scheduledItemSupplier = () -> scheduledItem;
        mediaSupplier = () -> media;
        player = null;
        download = true;
        livestream = false;
    }

    public MediaConsumptionRecorder(Player player, boolean livestream, Supplier<ScheduledItem> scheduledItemSupplier, Supplier<Media> mediaSupplier) { // for audio, video or livestream
        this.scheduledItemSupplier = scheduledItemSupplier;
        this.mediaSupplier = mediaSupplier;
        this.player = player;
        this.download = false;
        this.livestream = livestream;
    }

    public Player getPlayer() {
        return player;
    }

    public void start() {
        if (started)
            return;
        if (download) {
            recordNewMediaConsumption();
        } else if (player != null) {
            FXProperties.runNowAndOnPropertiesChange(() -> {
                ScheduledItem playerScheduledItem = scheduledItemSupplier.get();
                Media playerMedia = mediaSupplier.get();
                // Checking that the audio player is still the playing player
                Player thisSelectablePlayer = Players.getSelectedPlayer(player);
                Player playingPlayer = player.getPlayerGroup().getPlayingPlayer();
                if ((playingPlayer != null || playingMediaConsumption != null) // ignoring this case which happens before the player starts playing
                    && !Players.sameSelectedPlayer(playingPlayer, thisSelectablePlayer) // Happens when the user started another player (ex: podcast, video, etc...)
                    && !(playingPlayer == null && thisSelectablePlayer.getStatus() == Status.PAUSED)) { // Ignoring, however, when the player has simply been paused
                    playerScheduledItem = null;
                    playerMedia = null;
                }
                // If we detected that our track is no more playing, we record the playing duration
                if (playingMediaConsumption != null && (
                    !Entities.sameId(playerScheduledItem, playingMediaConsumption.getScheduledItem()) ||
                    !Entities.sameId(playerMedia, playingMediaConsumption.getMedia()) ||
                    player.getStatus() == Status.STOPPED)) {
                    playingStopWatch.off();
                    recordPlayingMediaConsumptionFinalDuration();
                }
                boolean isPlaying = Players.isMaybePlaying(player);
                if (playingMediaConsumption == null && playerScheduledItem != null && isPlaying) {
                    recordNewMediaConsumption();
                }
                if (playingMediaConsumption != null) {
                    if (isPlaying) {
                        playingStopWatch.on();
                    } else
                        playingStopWatch.off();
                }
            }, player.getPlayerGroup().playingPlayerProperty(), player.statusProperty());
        }
        started = true;
    }

    public void stop() {
        if (playingMediaConsumption != null)
            recordNewMediaConsumption();
    }

    private void recordNewMediaConsumption() {
        ScheduledItem scheduledItem = scheduledItemSupplier.get();
        if (scheduledItem == null)
            return;
        UpdateStore updateStore = UpdateStore.createAbove(scheduledItem.getStore());
        MediaConsumption mediaConsumption = updateStore.insertEntity(MediaConsumption.class);
        mediaConsumption.setMedia(mediaSupplier.get());
        mediaConsumption.setScheduledItem(scheduledItem);
        // The attendance is supposed to be loaded as a dynamic field called attendanceId (see EventAudioPlaylistActivity)
        mediaConsumption.setAttendance(scheduledItem.getFieldValue("attendanceId"));
        mediaConsumption.setDownloaded(download);
        mediaConsumption.setPlayed(!download && !livestream);
        mediaConsumption.setLivestreamed(livestream);
        playingMediaConsumption = mediaConsumption;
        if (download)
            updateStore.submitChanges();
        else {
            playingStopWatch.reset();
            playingStopWatch.on();
            // We ignore tracks played less than 5 s, so we postpone the storage
            Scheduler.scheduleDelay(5000, () -> {
                if (playingMediaConsumption == mediaConsumption) { // double-check if the user didn't play another track in the meantime
                    if (Players.isMaybePlaying(player)) { // and that the player is still playing (or maybe playing).
                        // The track has been played more than 5 s, so we can now store the media consumption
                        updateStore.submitChanges();
                        // For the duration record, in addition to the player listener set up in start(), we install
                        // a shutdown hook in case the user closes the app (which also cause the track to stop).
                        if (shutdownHook == null) {
                            shutdownHook = Shutdown.addShutdownHook(e ->
                                recordPlayingMediaConsumptionFinalDuration());
                        }
                    } else
                        playingMediaConsumption = null;
                }
            });
        }
    }

    private void recordPlayingMediaConsumptionFinalDuration() {
        if (playingMediaConsumption != null && !playingMediaConsumption.isNew()) {
            playingMediaConsumption.setDurationMillis(playingStopWatch.getStopWatchElapsedTime());
            UpdateStore updateStore = (UpdateStore) playingMediaConsumption.getStore();
            updateStore.submitChanges();
            Shutdown.removeShutdownHook(shutdownHook);
            shutdownHook = null;
            playingMediaConsumption = null;
        }
    }

    public static void recordDownloadMediaConsumption(ScheduledItem scheduledItem, Media media) {
        new MediaConsumptionRecorder(scheduledItem, media).start();
    }

}
