package one.modality.base.backoffice.operations.entities.filters;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Filter;

final class EditFieldsExecutor {

  static Future<Void> executeRequest(EditFieldsRequest rq) {
    return execute(rq.getFilter(), rq.getParentContainer());
  }

  private static Future<Void> execute(Filter filter, Pane parentContainer) {
    EntityPropertiesSheet.editEntity(
        filter, AddNewFieldsExecutor.EXPRESSION_COLUMNS, parentContainer);
    return Future.succeededFuture();
  }
}
