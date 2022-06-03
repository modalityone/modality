package mongoose.base.backoffice.operations.entities.filters;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.platform.shared.async.Future;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Filter;

import static mongoose.base.backoffice.operations.entities.filters.AddNewFieldsExecutor.EXPRESSION_COLUMNS;

final class EditFieldsExecutor {

    static Future<Void> executeRequest(EditFieldsRequest rq) {
        return execute(rq.getFilter(), rq.getParentContainer());
    }

    private static Future<Void> execute(Filter filter, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(filter, EXPRESSION_COLUMNS, parentContainer);
        return Future.succeededFuture();
    }
}