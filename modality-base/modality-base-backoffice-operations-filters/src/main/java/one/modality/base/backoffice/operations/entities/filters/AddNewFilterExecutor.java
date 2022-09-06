package one.modality.base.backoffice.operations.entities.filters;

import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.platform.async.Future;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Filter;

final class AddNewFilterExecutor {

    static final String FILTER_EXPRESSION_COLUMNS =
            "name,description,isCondition,isGroup,active,activityName,class,alias,fields,whereClause,groupByClause,havingClause,limitClause";

    static Future<Void> executeRequest(AddNewFilterRequest rq) {
        return execute(rq.getEntityStore(), rq.getParentContainer());
    }

    private static Future<Void> execute(EntityStore entityStore, Pane parentContainer) {
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        Filter insertEntity = updateStore.insertEntity(Filter.class);
        insertEntity.setIsColumns(false);

        EntityPropertiesSheet.editEntity(insertEntity, FILTER_EXPRESSION_COLUMNS, parentContainer);

        return Future.succeededFuture();
    }
}