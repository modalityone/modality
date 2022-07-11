package org.modality_project.base.backoffice.operations.entities.filters;

import dev.webfx.stack.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.stack.async.Future;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Filter;

final class EditFieldsExecutor {

    static Future<Void> executeRequest(EditFieldsRequest rq) {
        return execute(rq.getFilter(), rq.getParentContainer());
    }

    private static Future<Void> execute(Filter filter, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(filter, AddNewFieldsExecutor.EXPRESSION_COLUMNS, parentContainer);
        return Future.succeededFuture();
    }
}