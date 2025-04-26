package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.player.Player;
import one.modality.base.shared.entities.Media;

import java.util.List;

/**
 * @author Bruno Salmon
 */
record AudioColumnsContext(List<Media> publishedMedias, Player audioPlayer) {

}
