package one.modality.booking.backoffice.bookingeditor.teaching;

import dev.webfx.kit.util.properties.ObservableLists;
import javafx.scene.Node;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.backoffice.bookingeditor.family.FamilyBookingEditorBase;
import one.modality.booking.client.scheduleditemsselector.WorkingBookingSyncer;
import one.modality.booking.client.selecteditemsselector.box.BoxScheduledItemsSelector;
import one.modality.booking.client.workingbooking.WorkingBooking;

/**
 * @author Bruno Salmon
 */
final class TeachingBookingEditor extends FamilyBookingEditorBase {

    private final BoxScheduledItemsSelector boxScheduledItemsSelector = new BoxScheduledItemsSelector(true, false);

    TeachingBookingEditor(WorkingBooking workingBooking) {
        super(workingBooking, KnownItemFamily.TEACHING);
        // Final subclasses should call this method
        initiateUiAndSyncFromWorkingBooking();
    }

    @Override
    public Node buildUi() {
        return boxScheduledItemsSelector.buildUi();
    }

    @Override
    protected void initiateUiAndSyncFromWorkingBooking() {
        WorkingBookingSyncer.syncScheduledItemsSelectorFromPolicyAndBookedDates(boxScheduledItemsSelector,
            getPolicyFamilyScheduledItems(), getAlreadyBookedFamilyDates());
        // We keep the working booking in sync with the selected dates - this keeps hasChangesProperty up to date in
        // WorkingBookingProperties which is used to reflect the user changes and enable the Save button.
        ObservableLists.runOnListChange(ignored ->
                syncWorkingBookingFromUi()
            , boxScheduledItemsSelector.getSelectedDates());
    }

    @Override
    public void syncWorkingBookingFromUi() {
        WorkingBookingSyncer.syncWorkingBookingFromScheduledItemsSelector(workingBooking, boxScheduledItemsSelector, false);
    }

}
