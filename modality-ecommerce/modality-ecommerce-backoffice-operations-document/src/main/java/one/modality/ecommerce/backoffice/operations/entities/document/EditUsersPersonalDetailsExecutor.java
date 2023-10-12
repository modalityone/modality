package one.modality.ecommerce.backoffice.operations.entities.document;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import one.modality.base.shared.entities.Person;
import one.modality.crm.client.controls.personaldetails.BookingPersonalDetailsPanel;

final class EditUsersPersonalDetailsExecutor {

    static Future<Void> executeRequest(EditUsersPersonalDetailsRequest rq) {
        return execute(rq.getPerson(), rq.getButtonSelectorParameters());
    }

    private static Future<Void> execute(Person person, ButtonSelectorParameters buttonSelectorParameters) {
        BookingPersonalDetailsPanel.editBookingPersonalDetails(person, buttonSelectorParameters);
        return Future.succeededFuture();
    }
}
