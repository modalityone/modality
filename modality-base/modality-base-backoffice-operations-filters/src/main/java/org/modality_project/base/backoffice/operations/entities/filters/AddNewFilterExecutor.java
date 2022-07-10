package org.modality_project.base.backoffice.operations.entities.filters;

import dev.webfx.stack.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.stack.framework.shared.orm.entity.EntityStore;
import dev.webfx.stack.framework.shared.orm.entity.UpdateStore;
import dev.webfx.stack.platform.async.Future;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Filter;

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