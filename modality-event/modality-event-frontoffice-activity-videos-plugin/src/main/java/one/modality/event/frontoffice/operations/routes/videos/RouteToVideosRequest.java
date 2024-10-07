package one.modality.event.frontoffice.operations.routes.videos;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.event.frontoffice.activities.videos.routing.VideosRouting;

public final class RouteToVideosRequest extends RoutePushRequest implements HasOperationCode {

    public RouteToVideosRequest(BrowsingHistory browsingHistory) {
        super(VideosRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToVideos";
    }
}
