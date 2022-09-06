package one.modality.ecommerce.backoffice.operations.entities.documentline;

import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import dev.webfx.platform.async.Future;

final class AddNewDocumentLineExecutor {

    static Future<Void> executeRequest(AddNewDocumentLineRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document documentLine, Pane parentContainer) {
        // Not yet implemented
        return Future.succeededFuture();
    }
}