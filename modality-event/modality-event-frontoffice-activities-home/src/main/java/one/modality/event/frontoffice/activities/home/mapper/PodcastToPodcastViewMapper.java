package one.modality.event.frontoffice.activities.home.mapper;

import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Podcast;
import one.modality.event.frontoffice.activities.home.views.PodcastView;

public class PodcastToPodcastViewMapper implements IndividualEntityToObjectMapper<Podcast, Node> {

    VBox page;
    Podcast podcast;
    PodcastView view;
    Podcast lastPodcast;

    public PodcastToPodcastViewMapper(VBox page, Podcast podcast) {
        this.page = page;
        this.podcast = podcast;
        onEntityChangedOrReplaced(podcast);
    }

    @Override
    public Node getMappedObject() {
        return view.getView();
    }

    @Override
    public void onEntityChangedOrReplaced(Podcast entity) {
        if (podcast != lastPodcast) {
            this.view = new PodcastView(lastPodcast = podcast);
            this.view.buildView(page);
        }
    }

    @Override
    public void onEntityRemoved(Podcast entity) {

    }
}
