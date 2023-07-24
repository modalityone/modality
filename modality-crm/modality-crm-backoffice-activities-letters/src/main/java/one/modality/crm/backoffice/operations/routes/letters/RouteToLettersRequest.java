package one.modality.crm.backoffice.operations.routes.letters;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;

import one.modality.crm.backoffice.activities.letters.routing.LettersRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToLettersRequest extends RoutePushRequest implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToLetters";

    public RouteToLettersRequest(Object eventId, BrowsingHistory history) {
        super(LettersRouting.getEventLettersPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
