package org.modality_project.catering.backoffice.operations.entities.allocationrule;

import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Event;
import dev.webfx.platform.async.Future;

final class AddNewAllocationRuleExecutor {

    static Future<Void> executeRequest(AddNewAllocationRuleRequest rq) {
        return execute(rq.getEvent(), rq.getParentContainer());
    }

    private static Future<Void> execute(Event documentLine, Pane parentContainer) {
        // Not yet implemented
        return Future.succeededFuture();
    }
}