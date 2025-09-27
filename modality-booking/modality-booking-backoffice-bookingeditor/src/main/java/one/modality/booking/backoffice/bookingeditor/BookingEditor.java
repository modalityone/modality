package one.modality.booking.backoffice.bookingeditor;

import dev.webfx.extras.exceptions.UserCancellationException;
import dev.webfx.extras.operation.OperationUtil;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import one.modality.booking.backoffice.bookingeditor.spi.BookingEditorProvider;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
public interface BookingEditor {

    Node buildUi();

    ReadOnlyBooleanProperty hasChanges();

    Future<Void> saveChanges();

    static Future<Void> editBooking(Document document, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DocumentService.loadDocumentWithPolicy(document)
            .onFailure(promise::fail)
            .inUiThread()
            .onSuccess(policyAndDocumentAggregates -> {
                PolicyAggregate policyAggregate = policyAndDocumentAggregates.getPolicyAggregate(); // never null
                DocumentAggregate existingBooking = policyAndDocumentAggregates.getDocumentAggregate(); // might be null
                WorkingBooking workingBooking = new WorkingBooking(policyAggregate, existingBooking);

                BookingEditor bookingEditor = BookingEditorProvider.bestSuitableBookingEditor(workingBooking);
                DialogContent dialogContent = new DialogContent()
                    .setHeaderText("BookingDetails")
                    .setContent(bookingEditor.buildUi());
                //We disable the save button at the beginning
                Button saveButton = dialogContent.getPrimaryButton();
                saveButton.disableProperty().bind(bookingEditor.hasChanges().not());
                Button cancelButton = dialogContent.getSecondaryButton();

                //here we validate
                boolean[] executing = {false};
                DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                    executing[0] = true;
                    OperationUtil.turnOnButtonsWaitModeDuringExecution(
                        bookingEditor.saveChanges()
                            .onFailure(dialogCallback::showException)
                            .onSuccess(ignored -> dialogCallback.closeDialog())
                        , saveButton, cancelButton);
                });

                DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer)
                    .addCloseHook(() -> {
                        if (!executing[0])
                            promise.fail(new UserCancellationException());
                    });
            });
        return promise.future();
    }

}
