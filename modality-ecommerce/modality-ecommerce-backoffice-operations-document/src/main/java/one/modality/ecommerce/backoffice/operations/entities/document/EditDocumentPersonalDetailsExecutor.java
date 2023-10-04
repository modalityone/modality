package one.modality.ecommerce.backoffice.operations.entities.document;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import one.modality.crm.client.controls.personaldetails.BookingPersonalDetailsPanel;

final class EditDocumentPersonalDetailsExecutor {

    static Future<Void> executeRequest(EditDocumentPersonalDetailsRequest rq) {
        return execute(rq.getDocument(), rq.getButtonFactoryMixin(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, ButtonFactoryMixin buttonFactoryMixin, Pane parentContainer) {
        BookingPersonalDetailsPanel.editBookingPersonalDetails(document, buttonFactoryMixin, parentContainer);
        return Future.succeededFuture();
    }
}
