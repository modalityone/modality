package one.modality.catering.backoffice2018.operations.entities.allocationrule;

import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Event;
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