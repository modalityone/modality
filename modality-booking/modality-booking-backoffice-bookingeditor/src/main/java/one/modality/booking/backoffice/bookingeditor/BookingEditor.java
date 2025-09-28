package one.modality.booking.backoffice.bookingeditor;

import dev.webfx.extras.async.AsyncDialog;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.platform.async.Future;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import one.modality.booking.backoffice.bookingeditor.spi.BookingEditorProvider;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;

/**
 * @author Bruno Salmon
 */
public interface BookingEditor {

    Node buildUi();

    Future<Void> saveChanges();

    // Static methods

    static Future<Void> editBooking(Document document, Pane parentContainer) {
        return WorkingBooking.loadWorkingBooking(document)
            .inUiThread()
            .compose(workingBooking -> {
                WorkingBookingProperties workingBookingProperties = new WorkingBookingProperties(workingBooking);
                BookingEditor bookingEditor = BookingEditorProvider.bestSuitableBookingEditor(workingBooking);
                DialogContent dialogContent = new DialogContent()
                    .setHeaderText("BookingDetails")
                    .setContent(bookingEditor.buildUi());
                // We don't allow the user to save if there are no changes made on the booking
                Button saveButton = dialogContent.getPrimaryButton();
                saveButton.disableProperty().bind(workingBookingProperties.hasChangesProperty().not());

                return AsyncDialog.showDialogWithAsyncOperationOnPrimaryButton(dialogContent, parentContainer, bookingEditor::saveChanges);
            });
    }

}
