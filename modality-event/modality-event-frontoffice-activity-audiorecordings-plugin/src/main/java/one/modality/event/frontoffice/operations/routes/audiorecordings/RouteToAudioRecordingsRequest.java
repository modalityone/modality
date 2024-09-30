package one.modality.event.frontoffice.operations.routes.audiorecordings;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.event.frontoffice.activities.audiorecordings.routing.AudioRecordingsRouting;

public final class RouteToAudioRecordingsRequest extends RoutePushRequest implements HasOperationCode {

    public RouteToAudioRecordingsRequest(BrowsingHistory browsingHistory) {
        super(AudioRecordingsRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToAudioRecordings";
    }
}
