package org.modality_project.ecommerce.backoffice.operations.entities.moneytransfer;

import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Document;
import dev.webfx.stack.platform.async.Future;

final class AddNewTransferExecutor {

    static Future<Void> executeRequest(AddNewTransferRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document documentLine, Pane parentContainer) {
        // Not yet implemented
        return Future.succeededFuture();
    }
}