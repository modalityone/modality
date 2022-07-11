package org.modality_project.catering.backoffice.operations.entities.allocationrule;

import javafx.scene.layout.Pane;
import dev.webfx.stack.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.stack.framework.shared.orm.entity.Entity;
import dev.webfx.stack.async.Future;

final class EditAllocationRuleExecutor {

    static Future<Void> executeRequest(EditAllocationRuleRequest rq) {
        return execute(rq.getAllocationRule(), rq.getParentContainer());
    }

    private static Future<Void> execute(Entity allocationRule, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(allocationRule, "<default>", parentContainer);
        return Future.succeededFuture();
    }
}
