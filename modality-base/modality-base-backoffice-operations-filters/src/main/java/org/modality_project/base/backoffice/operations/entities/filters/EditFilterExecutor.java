package org.modality_project.base.backoffice.operations.entities.filters;

import dev.webfx.stack.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.stack.async.Future;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Filter;

import static org.modality_project.base.backoffice.operations.entities.filters.AddNewFilterExecutor.FILTER_EXPRESSION_COLUMNS;

final class EditFilterExecutor {

    static Future<Void> executeRequest(EditFilterRequest rq) {
        return execute(rq.getFilter(), rq.getParentContainer());
    }

    private static Future<Void> execute(Filter filter, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(filter, FILTER_EXPRESSION_COLUMNS, parentContainer);
        return Future.succeededFuture();
    }
}