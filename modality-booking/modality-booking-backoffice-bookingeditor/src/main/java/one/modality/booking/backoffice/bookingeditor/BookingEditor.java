package one.modality.booking.backoffice.bookingeditor;

import dev.webfx.extras.async.AsyncDialog;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import javafx.beans.binding.BooleanExpression;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import one.modality.booking.backoffice.bookingeditor.spi.BookingEditorProvider;
import one.modality.booking.client.workingbooking.FXPersonToBook;
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
                boolean newBooking = workingBooking.isNewBooking();
                if (newBooking) {
                    FXProperties.setEvenIfBound(FXPersonToBook.personToBookProperty(), null);
                    workingBooking.bookWholeEvent();
                }
                BookingEditor bookingEditor = BookingEditorProvider.bestSuitableBookingEditor(workingBooking);
                // TODO: display a message when no suitable booking editor could be found
                DialogContent dialogContent = new DialogContent()
                    .setHeaderText(newBooking ? "New booking" : "Booking Options")
                    .setSaveCancel()
                    .setContent(bookingEditor.buildUi());
                // We don't allow the user to save if there are no changes made on the booking
                Button saveButton = dialogContent.getPrimaryButton();
                BooleanExpression canSaveProperty = workingBooking.isNewBooking() ?
                    FXPersonToBook.personToBookProperty().isNotNull() :
                    workingBookingProperties.hasChangesProperty();
                saveButton.disableProperty().bind(canSaveProperty.not());

                return AsyncDialog.showDialogWithAsyncOperationOnPrimaryButton(dialogContent, parentContainer, bookingEditor::saveChanges);
            });
    }

}
