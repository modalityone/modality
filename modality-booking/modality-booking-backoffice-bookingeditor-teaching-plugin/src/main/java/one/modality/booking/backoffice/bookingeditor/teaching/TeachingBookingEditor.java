package one.modality.booking.backoffice.bookingeditor.teaching;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
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
    private final CheckBox freeOfChargeCheckBox = new CheckBox("Free of charge");

    TeachingBookingEditor(WorkingBooking workingBooking) {
        super(workingBooking, KnownItemFamily.TEACHING);
        // Final subclasses should call this method
        initiateUiAndSyncFromWorkingBooking();
    }

    @Override
    public Node buildUi() {
        return embedInFamilyFrame(
            new VBox(10,
                // We make the day texts bold in the box selector. Note: works on the web but not on OpenJFX
                Bootstrap.strong(boxScheduledItemsSelector.buildUi()),
                freeOfChargeCheckBox
            )
        );
    }

    @Override
    protected void initiateUiAndSyncFromWorkingBooking() {
        WorkingBookingSyncer.syncScheduledItemsSelectorFromPolicyAndBookedDates(boxScheduledItemsSelector,
            getPolicyFamilyScheduledItems(), getBookedTeachingDates());
        // We keep the working booking in sync with the selected dates - this keeps hasChangesProperty up to date in
        // WorkingBookingProperties which is used to reflect the user changes and enable the Save button.
        ObservableLists.runOnListChange(ignored ->
                syncWorkingBookingFromUi()
            , boxScheduledItemsSelector.getSelectedDates());
        freeOfChargeCheckBox.setSelected(workingBooking.isTeachingFreeOfCharge());
        FXProperties.runOnPropertyChange(workingBooking::applyTeachingFreeOfCharge, freeOfChargeCheckBox.selectedProperty());
    }

    @Override
    public void syncWorkingBookingFromUi() {
        WorkingBookingSyncer.syncWorkingBookingFromScheduledItemsSelector(workingBooking, boxScheduledItemsSelector, false);
    }

}
