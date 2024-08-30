package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;

final class ShowBookingEditorExecutor {

    static Future<Void> executeRequest(ShowBookingEditorRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        return Future.succeededFuture();
    }
}
