package one.modality.booking.backoffice.bookingeditor.teaching;

import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.collection.Collections;
import javafx.scene.Node;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.backoffice.bookingeditor.BookingEditor;
import one.modality.booking.client.scheduleditemsselector.WorkingBookingSyncer;
import one.modality.booking.client.selecteditemsselector.box.BoxScheduledItemsSelector;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingHistoryHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public class TeachingBookingEditor implements BookingEditor {

    private final WorkingBooking workingBooking;
    private final BoxScheduledItemsSelector boxScheduledItemsSelector = new BoxScheduledItemsSelector(true, false);

    public TeachingBookingEditor(WorkingBooking workingBooking) {
        this.workingBooking = workingBooking;
        // We cover the cases of recurring events (ex: GP classes or STTP), as well as online Festivals.
        // In all these cases, we use RecurringEventSchedule to display the teaching sessions (but not the audio
        // recordings).
        boxScheduledItemsSelector.setSelectableScheduledItems(
            Collections.filter(workingBooking.getScheduledItemsOnEvent(),
                scheduledItem -> scheduledItem.getItem().getFamily().isTeaching())
            , true);
        List<LocalDate> alreadyBookedSelectedDates = workingBooking
            .getScheduledItemsAlreadyBooked().stream()
            .map(ScheduledItem::getDate)
            .collect(Collectors.toList());
        boxScheduledItemsSelector.addSelectedDates(alreadyBookedSelectedDates);
        // We keep the working booking in sync with the selected dates - this keeps hasChangesProperty up to date in
        // WorkingBookingProperties which is used to reflect the user changes and enable the Save button.
        ObservableLists.runOnListChange(ignored ->
                syncWorkingBookingFromScheduledItemsSelector()
        , boxScheduledItemsSelector.getSelectedDates());
    }

    @Override
    public Node buildUi() {
        return boxScheduledItemsSelector.buildUi();
    }

    private void syncWorkingBookingFromScheduledItemsSelector() {
        WorkingBookingSyncer.syncWorkingBookingFromScheduledItemsSelector(workingBooking, boxScheduledItemsSelector, false);
    }

    @Override
    public Future<Void> saveChanges() {
        syncWorkingBookingFromScheduledItemsSelector();
        WorkingBookingHistoryHelper historyHelper = new WorkingBookingHistoryHelper(workingBooking);
        return workingBooking.submitChanges(historyHelper.generateHistoryComment())
            .mapEmpty();
    }
}
