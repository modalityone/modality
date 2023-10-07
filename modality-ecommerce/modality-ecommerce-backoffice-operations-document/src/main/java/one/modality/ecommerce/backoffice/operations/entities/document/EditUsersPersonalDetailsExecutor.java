package one.modality.ecommerce.backoffice.operations.entities.document;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Person;
import one.modality.crm.client.controls.personaldetails.BookingPersonalDetailsPanel;

final class EditUsersPersonalDetailsExecutor {

    static Future<Void> executeRequest(EditUsersPersonalDetailsRequest rq) {
        return execute(rq.getPerson(), rq.getButtonFactoryMixin(), rq.getParentContainer());
    }

    private static Future<Void> execute(Person person, ButtonFactoryMixin buttonFactoryMixin, Pane parentContainer) {
        BookingPersonalDetailsPanel.editBookingPersonalDetails(person, buttonFactoryMixin, parentContainer);
        return Future.succeededFuture();
    }
}
