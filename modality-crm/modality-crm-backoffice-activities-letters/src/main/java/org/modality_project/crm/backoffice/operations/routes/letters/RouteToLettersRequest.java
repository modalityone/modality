package org.modality_project.crm.backoffice.operations.routes.letters;

import org.modality_project.crm.backoffice.activities.letters.routing.LettersRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToLettersRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToLetters";

    public RouteToLettersRequest(Object eventId, BrowsingHistory history) {
        super(LettersRouting.getEventLettersPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
