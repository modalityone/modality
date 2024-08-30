package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import one.modality.base.shared.entities.Document;
import one.modality.crm.client.controls.personaldetails.BookingPersonalDetailsPanel;

final class EditDocumentPersonalDetailsExecutor {

    static Future<Void> executeRequest(EditDocumentPersonalDetailsRequest rq) {
        return execute(rq.getDocument(), rq.getButtonSelectorParameters());
    }

    private static Future<Void> execute(Document document, ButtonSelectorParameters buttonSelectorParameters) {
        BookingPersonalDetailsPanel.editBookingPersonalDetails(document, buttonSelectorParameters);
        return Future.succeededFuture();
    }
}
