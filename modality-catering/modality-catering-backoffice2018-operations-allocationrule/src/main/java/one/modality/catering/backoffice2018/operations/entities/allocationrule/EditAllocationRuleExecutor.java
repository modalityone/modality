package one.modality.catering.backoffice2018.operations.entities.allocationrule;

import javafx.scene.layout.Pane;
import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.platform.async.Future;

final class EditAllocationRuleExecutor {

    static Future<Void> executeRequest(EditAllocationRuleRequest rq) {
        return execute(rq.getAllocationRule(), rq.getParentContainer());
    }

    private static Future<Void> execute(Entity allocationRule, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(allocationRule, "<default>", parentContainer);
        return Future.succeededFuture();
    }
}
