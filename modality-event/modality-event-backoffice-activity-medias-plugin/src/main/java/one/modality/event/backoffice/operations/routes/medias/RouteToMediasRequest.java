package one.modality.event.backoffice.operations.routes.medias;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.event.backoffice.activities.medias.routing.MediasRouting;

public final class RouteToMediasRequest extends RoutePushRequest implements HasOperationCode {

    public RouteToMediasRequest(BrowsingHistory browsingHistory) {
        super(MediasRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToMedias";
    }
}
