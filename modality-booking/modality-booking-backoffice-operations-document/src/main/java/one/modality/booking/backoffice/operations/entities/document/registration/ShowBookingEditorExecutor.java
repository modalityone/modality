package one.modality.booking.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import one.modality.booking.backoffice.bookingeditor.BookingEditor;

/**
 * @author David Hello
 */
final class ShowBookingEditorExecutor {

    static Future<Void> executeRequest(ShowBookingEditorRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        return BookingEditor.editBooking(document, parentContainer);
    }
}
