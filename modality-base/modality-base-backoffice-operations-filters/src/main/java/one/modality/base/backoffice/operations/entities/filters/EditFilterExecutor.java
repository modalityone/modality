package one.modality.base.backoffice.operations.entities.filters;

import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.platform.async.Future;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Filter;

import static one.modality.base.backoffice.operations.entities.filters.AddNewFilterExecutor.FILTER_EXPRESSION_COLUMNS;

final class EditFilterExecutor {

    static Future<Void> executeRequest(EditFilterRequest rq) {
        return execute(rq.getFilter(), rq.getParentContainer());
    }

    private static Future<Void> execute(Filter filter, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(filter, FILTER_EXPRESSION_COLUMNS, parentContainer);
        return Future.succeededFuture();
    }
}