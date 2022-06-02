package mongoose.base.backoffice.operations.entities.filters;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.platform.shared.async.Future;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Filter;

final class EditFilterExecutor {

    static Future<Void> executeRequest(EditFilterRequest rq) {
        return execute(rq.getFilter(), rq.getParentContainer());
    }

    private static Future<Void> execute(Filter filter, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(filter, "name,description,isColumns,isCondition,isGroup,active,activityName,class,alias,columns,fields,whereClause,groupByClause,havingClause,orderByClause,limitClause,ord", parentContainer);
        return Future.succeededFuture();
    }
}