package one.modality.ecommerce.backoffice.operations.entities.document;

import dev.webfx.platform.async.Future;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;

final class SendLetterExecutor {

  static Future<Void> executeRequest(SendLetterRequest rq) {
    return execute(rq.getDocument(), rq.getParentContainer());
  }

  private static Future<Void> execute(Document document, Pane parentContainer) {
    Future<Void> future = Future.succeededFuture();
    return future;
  }
}
