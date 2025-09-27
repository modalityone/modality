package one.modality.booking.backoffice.operations.entities.document.registration;

import dev.webfx.extras.exceptions.UserCancellationException;
import dev.webfx.extras.operation.OperationUtil;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.collection.Collections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingHistoryHelper;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.booking.client.scheduleditemsselector.WorkingBookingSyncer;
import one.modality.booking.client.selecteditemsselector.box.BoxScheduledItemsSelector;

import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * @author David Hello
 */
final class ShowBookingEditorExecutor {

    static Future<Void> executeRequest(ShowBookingEditorRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DocumentService.loadDocumentWithPolicy(document)
            .onFailure(Console::log)
            .inUiThread()
            .onSuccess(policyAndDocumentAggregates -> {
                PolicyAggregate policyAggregate = policyAndDocumentAggregates.getPolicyAggregate(); // never null
                DocumentAggregate existingBooking = policyAndDocumentAggregates.getDocumentAggregate(); // might be null
                WorkingBooking workingBooking = new WorkingBooking(policyAggregate, existingBooking);

                // We cover the cases of recurring events (ex: GP classes or STTP), as well as online Festivals.
                // In all these cases, we use RecurringEventSchedule to display the teaching sessions (but not the audio
                // recordings).
                BoxScheduledItemsSelector boxScheduledItemsSelector = new BoxScheduledItemsSelector(true, false);
                boxScheduledItemsSelector.setSelectableScheduledItems(Collections.filter(workingBooking.getScheduledItemsOnEvent(), scheduledItem -> scheduledItem.getItem().getFamily().isTeaching()), true);
                boxScheduledItemsSelector.addSelectedDates(workingBooking.getScheduledItemsAlreadyBooked().stream().map(ScheduledItem::getDate).collect(Collectors.toList()));
                workingBooking.getScheduledItemsAlreadyBooked();
                Pane schedule = boxScheduledItemsSelector.buildUi();
                DialogContent dialogContent = new DialogContent().setHeaderText("BookingDetails").setContent(schedule);
                boolean[] executing = {false};
                DialogCallback dc = DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer);
                //We disable the save button at the beginning
                Button saveButton = dialogContent.getPrimaryButton();
                saveButton.setDisable(true);
                //Here we disable the save button if there are no changes in the booking dates
                boxScheduledItemsSelector.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> {
                    saveButton.setDisable(boxScheduledItemsSelector.getSelectedDates().equals(workingBooking.getScheduledItemsAlreadyBooked().stream().map(ScheduledItem::getDate).collect(Collectors.toList())));
                    if (boxScheduledItemsSelector.getSelectedDates().isEmpty()) {
                        //If there is no selection, we prevent to save
                        saveButton.setDisable(true);
                    }
                });

                //Here we cancel
                dc.addCloseHook(() -> {
                    if (!executing[0])
                        promise.fail(new UserCancellationException());
                });
                //here we validate
                DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                    executing[0] = true;
                    WorkingBookingSyncer.syncWorkingBookingFromScheduledItemsSelector(workingBooking, boxScheduledItemsSelector, false);
                    WorkingBookingHistoryHelper historyHelper = new WorkingBookingHistoryHelper(workingBooking);
                    OperationUtil.turnOnButtonsWaitModeDuringExecution(
                        workingBooking.submitChanges(historyHelper.generateHistoryComment())
                            .onSuccess(ignored -> dialogCallback.closeDialog())
                            .onFailure(dialogCallback::showException)
                        , saveButton, dialogContent.getSecondaryButton() /* = cancel button */);
                });

            });
        return promise.future();
    }
}
