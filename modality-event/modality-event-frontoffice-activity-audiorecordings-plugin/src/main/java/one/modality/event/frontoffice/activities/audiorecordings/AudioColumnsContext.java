package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.player.Player;
import one.modality.base.shared.entities.Media;

import java.util.List;

/**
 * @author Bruno Salmon
 */
final class AudioColumnsContext {

    private List<Media> publishedMedias;
    private Player audioPlayer;

    public AudioColumnsContext(List<Media> publishedMedias, Player audioPlayer) {
        this.publishedMedias = publishedMedias;
        this.audioPlayer = audioPlayer;
    }

    public List<Media> getPublishedMedias() {
        return publishedMedias;
    }

    public Player getAudioPlayer() {
        return audioPlayer;
    }
}
