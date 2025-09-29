package one.modality.booking.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.booking.backoffice.bookingeditor.BookingEditor;

/**
 * @author David Hello
 */
final class ShowNewBookingEditorExecutor {

    static Future<Void> executeRequest(ShowNewBookingEditorRequest rq) {
        return execute(rq.getEvent(), rq.getParentContainer());
    }

    private static Future<Void> execute(Event event, Pane parentContainer) {
        UpdateStore updateStore = UpdateStore.createAbove(event.getStore());
        Document document = updateStore.insertEntity(Document.class);
        document.setEvent(event);
        return BookingEditor.editBooking(document, parentContainer);
    }
}
