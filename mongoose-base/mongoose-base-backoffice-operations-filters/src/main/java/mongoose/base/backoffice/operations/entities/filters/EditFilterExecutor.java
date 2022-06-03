package mongoose.base.backoffice.operations.entities.filters;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.platform.shared.async.Future;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Filter;

import static mongoose.base.backoffice.operations.entities.filters.AddNewFilterExecutor.FILTER_EXPRESSION_COLUMNS;

final class EditFilterExecutor {

    static Future<Void> executeRequest(EditFilterRequest rq) {
        return execute(rq.getFilter(), rq.getParentContainer());
    }

    private static Future<Void> execute(Filter filter, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(filter, FILTER_EXPRESSION_COLUMNS, parentContainer);
        return Future.succeededFuture();
    }
}