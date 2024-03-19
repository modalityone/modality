package one.modality.event.backoffice.operations.routes.recurringevents;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.event.backoffice.activities.recurringevents.routing.RecurringEventsRouting;

public final class RouteToRecurringEventsRequest extends RoutePushRequest implements HasOperationCode {

    public RouteToRecurringEventsRequest(BrowsingHistory browsingHistory) {
        super(RecurringEventsRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToRecurringEvents";
    }
}
