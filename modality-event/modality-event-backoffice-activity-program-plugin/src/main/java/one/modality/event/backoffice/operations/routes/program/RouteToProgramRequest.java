package one.modality.event.backoffice.operations.routes.program;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.event.backoffice.activities.program.routing.ProgramRouting;

public final class RouteToProgramRequest extends RoutePushRequest implements HasOperationCode {

    public RouteToProgramRequest(BrowsingHistory browsingHistory) {
        super(ProgramRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToProgram";
    }
}
