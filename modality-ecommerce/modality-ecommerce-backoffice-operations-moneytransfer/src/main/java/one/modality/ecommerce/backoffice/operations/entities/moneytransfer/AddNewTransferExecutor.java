package one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

import dev.webfx.platform.async.Future;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;

final class AddNewTransferExecutor {

  static Future<Void> executeRequest(AddNewTransferRequest rq) {
    return execute(rq.getDocument(), rq.getParentContainer());
  }

  private static Future<Void> execute(Document documentLine, Pane parentContainer) {
    // Not yet implemented
    return Future.succeededFuture();
  }
}
