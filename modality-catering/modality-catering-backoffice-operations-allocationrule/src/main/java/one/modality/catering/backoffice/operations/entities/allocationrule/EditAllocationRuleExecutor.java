package one.modality.catering.backoffice.operations.entities.allocationrule;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;

import javafx.scene.layout.Pane;

final class EditAllocationRuleExecutor {

    static Future<Void> executeRequest(EditAllocationRuleRequest rq) {
        return execute(rq.getAllocationRule(), rq.getParentContainer());
    }

    private static Future<Void> execute(Entity allocationRule, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(allocationRule, "<default>", parentContainer);
        return Future.succeededFuture();
    }
}
