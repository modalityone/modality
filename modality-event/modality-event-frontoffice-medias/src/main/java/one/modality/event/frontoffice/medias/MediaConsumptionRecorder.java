package one.modality.event.frontoffice.medias;

import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.Players;
import dev.webfx.extras.player.Status;
import dev.webfx.extras.player.multi.MultiPlayer;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.shutdown.Shutdown;
import dev.webfx.platform.util.stopwatch.StopWatch;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.MediaConsumption;
import one.modality.base.shared.entities.ScheduledItem;

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
    private MediaConsumption playingMediaConsumption;
    // StopWatch that will be running only while player is playing in order to record the user playing duration
    private final StopWatch playingStopWatch = StopWatch.createSystemMillisStopWatch();

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

    public void start() {
        if (download) {
            recordNewMediaConsumption();
        } else if (player != null) {
            FXProperties.runNowAndOnPropertiesChange(() -> {
                ScheduledItem playerScheduledItem = scheduledItemSupplier.get();
                Media playerMedia = mediaSupplier.get();
                // Checking that the audio player is still the playing player
                Player thisSelectablePlayer = player instanceof MultiPlayer ? ((MultiPlayer) player).getSelectedPlayer() : player;
                Player playingPlayer = player.getPlayerGroup().getPlayingPlayer();
                if ((playingPlayer != null || playingMediaConsumption != null) // ignoring this case which happens before the player starts playing
                    && playingPlayer != thisSelectablePlayer) { // Happens when the user started another player (ex: podcast, video, etc...)
                    playerScheduledItem = null;
                    playerMedia = null;
                }
                // If we detected that our track is no more playing, we record the playing duration
                if (playingMediaConsumption != null && (
                    !Entities.sameId(playerScheduledItem, playingMediaConsumption.getScheduledItem()) ||
                    !Entities.sameId(playerMedia, playingMediaConsumption.getMedia()) ||
                    player.getStatus() == Status.STOPPED)) {
                    playingStopWatch.off();
                    recordPlayingMediaConsumptionDuration();
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
    }

    public void stop() {
        if (playingMediaConsumption != null)
            recordNewMediaConsumption();
    }

    private void recordNewMediaConsumption() {
        ScheduledItem scheduledItem = scheduledItemSupplier.get();
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
            Shutdown.addShutdownHook(this::recordPlayingMediaConsumptionDuration);
            Scheduler.scheduleDelay(5000, () -> {
                if (playingMediaConsumption == mediaConsumption) {
                    if (Players.isMaybePlaying(player))
                        updateStore.submitChanges();
                    else
                        playingMediaConsumption = null;
                }
            });
        }
    }

    private void recordPlayingMediaConsumptionDuration() {
        if (playingMediaConsumption != null && !playingMediaConsumption.isNew()) {
            playingMediaConsumption.setDurationMillis(playingStopWatch.getStopWatchElapsedTime());
            UpdateStore updateStore = (UpdateStore) playingMediaConsumption.getStore();
            updateStore.submitChanges();
            playingMediaConsumption = null;
            if (!Shutdown.isShuttingDown())
                Shutdown.removeShutdownHook(this::recordPlayingMediaConsumptionDuration);
        }
    }

    public static void recordDownloadMediaConsumption(ScheduledItem scheduledItem, Media media) {
        new MediaConsumptionRecorder(scheduledItem, media).start();
    }

}
