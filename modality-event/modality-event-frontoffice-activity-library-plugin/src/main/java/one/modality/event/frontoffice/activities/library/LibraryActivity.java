package one.modality.event.frontoffice.activities.library;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import one.modality.base.client.application.RoutingActions;
import one.modality.event.frontoffice.activities.audiorecordings.AudioRecordingsRouting;
import one.modality.event.frontoffice.activities.videos.VideosRouting;

final class LibraryActivity extends ViewDomainActivityBase implements OperationActionFactoryMixin {

    @Override
    public Node buildUi() {
        Hyperlink recordingsLink = ActionBinder.newActionHyperlink(RoutingActions.newRoutingAction(AudioRecordingsRouting.RouteToAudioRecordingsRequest::new, this));
        Hyperlink videosLink     = ActionBinder.newActionHyperlink(RoutingActions.newRoutingAction(VideosRouting.RouteToVideosRequest::new, this));
        VBox container = new VBox(30, recordingsLink, videosLink);
        container.setAlignment(Pos.CENTER);
        return container;
    }

}
