package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.ui.controls.alert.AlertUtil;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.exceptions.UserCancellationException;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.client.recurringevents.RecurringEventSchedule;
import one.modality.event.client.recurringevents.WorkingBooking;

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

                    //Here we disable the save button if there are no changes in the booking dates
                    recurringEventSchedule.getSelectedDates().addListener((ListChangeListener<LocalDate>) change ->
                        dialogContent.getPrimaryButton().setDisable(recurringEventSchedule.getSelectedDates().equals(workingBooking.getScheduledItemsAlreadyBooked().stream().map(ScheduledItem::getDate).collect(Collectors.toList()))));

                    //Here we cancel
                    dc.addCloseHook(() -> {
                        if (!executing[0])
                            promise.fail(new UserCancellationException());
                    });
                    //here we validate
                    DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                        executing[0] = true;
                        Button executingButton = dialogContent.getPrimaryButton();
                        workingBooking.submitChanges("Change from backoffice");
                        executingButton.setDisable(false);
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
