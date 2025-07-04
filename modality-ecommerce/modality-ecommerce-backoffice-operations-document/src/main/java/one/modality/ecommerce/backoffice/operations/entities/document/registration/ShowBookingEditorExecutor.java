package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.extras.util.alert.AlertUtil;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.exceptions.UserCancellationException;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.client.recurringevents.RecurringEventSchedule;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.client.workingbooking.WorkingBookingHistoryHelper;
import one.modality.event.client.booking.WorkingBookingSyncer;

import java.time.LocalDate;
import java.util.stream.Collectors;

final class ShowBookingEditorExecutor {


    static Future<Void> executeRequest(ShowBookingEditorRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        Promise<Void> promise = Promise.promise();
        DocumentService.loadDocumentWithPolicy(document)
            .onFailure(Console::log)
            .onSuccess(policyAndDocumentAggregates -> UiScheduler.runInUiThread(() -> {
                PolicyAggregate policyAggregate = policyAndDocumentAggregates.getPolicyAggregate(); // never null
                DocumentAggregate existingBooking = policyAndDocumentAggregates.getDocumentAggregate(); // may be null
                WorkingBooking workingBooking = new WorkingBooking(policyAggregate, existingBooking);

                UiScheduler.runInUiThread(() -> {
                    RecurringEventSchedule recurringEventSchedule = new RecurringEventSchedule();
                    recurringEventSchedule.setScheduledItems(workingBooking.getScheduledItemsOnEvent(), true);
                    recurringEventSchedule.addSelectedDates(workingBooking.getScheduledItemsAlreadyBooked().stream().map(ScheduledItem::getDate).collect(Collectors.toList()));
                    workingBooking.getScheduledItemsAlreadyBooked();
                    Pane schedule = recurringEventSchedule.buildUi();
                    DialogContent dialogContent = new DialogContent().setHeaderText("BookingDetails").setContent(schedule);
                    boolean[] executing = {false};
                    DialogCallback dc = DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer);
                    //We disable the save button at the beginning
                    Button saveButton = dialogContent.getPrimaryButton();
                    saveButton.setDisable(true);
                    //Here we disable the save button if there are no changes in the booking dates
                    recurringEventSchedule.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> {
                        saveButton.setDisable(recurringEventSchedule.getSelectedDates().equals(workingBooking.getScheduledItemsAlreadyBooked().stream().map(ScheduledItem::getDate).collect(Collectors.toList())));
                        if (recurringEventSchedule.getSelectedDates().isEmpty()) {
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
                        WorkingBookingSyncer.syncWorkingBookingFromEventSchedule(workingBooking,recurringEventSchedule,false);
                        WorkingBookingHistoryHelper historyHelper = new WorkingBookingHistoryHelper(workingBooking.getAttendanceAdded(),workingBooking.getAttendanceRemoved());
                        workingBooking.submitChanges(historyHelper.buildHistory())
                            .onSuccess(ignored->dialogCallback.closeDialog())
                            .onFailure(e-> {
                                dialogCallback.showException(e);
                            });
                    });
                });

            }));
        return promise.future();
    }

    private static void reportException(DialogCallback dialogCallback, Pane parentContainer, Throwable cause) {
        if (dialogCallback != null)
            dialogCallback.showException(cause);
        else
            AlertUtil.showExceptionAlert(cause, parentContainer.getScene().getWindow());
    }
}
