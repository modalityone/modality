package one.modality.booking.backoffice.bookingeditor.teaching;

import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.collection.Collections;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
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
    private final BooleanProperty hasChangesProperty = new SimpleBooleanProperty(true);
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
        // We prevent the user from saving when there are no changes, or no dates are selected.
        ObservableList<LocalDate> selectedDates = boxScheduledItemsSelector.getSelectedDates();
        ObservableLists.runNowAndOnListChange(ignored ->
                hasChangesProperty.set(!selectedDates.equals(alreadyBookedSelectedDates) && !selectedDates.isEmpty())
            , selectedDates);
    }

    @Override
    public Node buildUi() {
        return boxScheduledItemsSelector.buildUi();
    }

    @Override
    public ReadOnlyBooleanProperty hasChanges() {
        return hasChangesProperty;
    }

    @Override
    public Future<Void> saveChanges() {
        WorkingBookingSyncer.syncWorkingBookingFromScheduledItemsSelector(workingBooking, boxScheduledItemsSelector, false);
        WorkingBookingHistoryHelper historyHelper = new WorkingBookingHistoryHelper(workingBooking);
        return workingBooking.submitChanges(historyHelper.generateHistoryComment())
            .mapEmpty();
    }
}
