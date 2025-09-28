package one.modality.booking.backoffice.bookingeditor;

import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.platform.async.Future;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import dev.webfx.extras.async.AsyncDialog;
import one.modality.base.shared.entities.Document;
import one.modality.booking.backoffice.bookingeditor.spi.BookingEditorProvider;
import one.modality.booking.client.workingbooking.WorkingBooking;

/**
 * @author Bruno Salmon
 */
public interface BookingEditor {

    Node buildUi();

    ReadOnlyBooleanProperty hasChanges();

    Future<Void> saveChanges();

    // Static methods

    static Future<Void> editBooking(Document document, Pane parentContainer) {
        return WorkingBooking.loadWorkingBooking(document)
            .inUiThread()
            .compose(workingBooking -> {
                BookingEditor bookingEditor = BookingEditorProvider.bestSuitableBookingEditor(workingBooking);
                DialogContent dialogContent = new DialogContent()
                    .setHeaderText("BookingDetails")
                    .setContent(bookingEditor.buildUi());
                //We disable the save button at the beginning
                Button saveButton = dialogContent.getPrimaryButton();
                saveButton.disableProperty().bind(bookingEditor.hasChanges().not());

                return AsyncDialog.showDialogWithAsyncOperationOnPrimaryButton(dialogContent, parentContainer, bookingEditor::saveChanges);
            });
    }

}
