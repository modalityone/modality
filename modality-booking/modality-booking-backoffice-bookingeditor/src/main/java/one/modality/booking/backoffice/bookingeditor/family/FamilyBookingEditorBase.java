package one.modality.booking.backoffice.bookingeditor.family;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.border.BorderFactory;
import dev.webfx.platform.util.collection.Collections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.util.ScheduledItems;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBooking;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public abstract class FamilyBookingEditorBase extends BookingEditorBase {

    protected final KnownItemFamily knownItemFamily;

    public FamilyBookingEditorBase(WorkingBooking workingBooking, KnownItemFamily knownItemFamily) {
        super(workingBooking);
        this.knownItemFamily = knownItemFamily;
        // Final subclasses should call this method
        // initiateUiAndSyncFromWorkingBooking();
    }

    // These methods are not directly used by this class but are provided to be used by the subclasses.

    protected List<ScheduledItem> getPolicyFamilyScheduledItems() {
        return workingBooking.getPolicyAggregate().filterScheduledItemsOfFamily(knownItemFamily);
    }

    protected List<ScheduledItem> getAlreadyBookedFamilyScheduledItems() {
        return getAlreadyBookedFamilyScheduledItems(knownItemFamily);
    }

    protected List<ScheduledItem> getAlreadyBookedFamilyScheduledItems(KnownItemFamily family) {
        return workingBooking.getAlreadyBookedFamilyScheduledItems(family);
    }

    protected List<Attendance> getBookedAttendances() {
        return workingBooking.getBookedAttendances();
    }

    protected List<ScheduledItem> getBookedScheduledItems() {
        return ScheduledItems.fromAttendances(getBookedAttendances());
    }

    protected List<ScheduledItem> getBookedFamilyScheduledItems(KnownItemFamily family) {
        return ScheduledItems.filterFamily(getBookedScheduledItems(), family);
    }

    protected List<ScheduledItem> getBookedTeachingScheduledItems() {
        return getBookedFamilyScheduledItems(KnownItemFamily.TEACHING);
    }

    protected List<LocalDate> getAlreadyBookedFamilyDates() {
        return getAlreadyBookedFamilyDates(knownItemFamily);
    }

    protected List<LocalDate> getAlreadyBookedFamilyDates(KnownItemFamily family) {
        return Collections.map(getAlreadyBookedFamilyScheduledItems(family), ScheduledItem::getDate);
    }

    protected Node embedInFamilyFrame(Node content) {
        return embedInFrame(content, getFamily().getI18nKey());
    }

    protected Node embedInFrame(Node content, Object i18nKey) {
        Label familyLabel = Bootstrap.textSecondary(Bootstrap.strong(I18nControls.newLabel(i18nKey)));
        familyLabel.setPadding(new Insets(5));
        familyLabel.setBackground(Background.fill(Color.WHITE));
        StackPane.setAlignment(familyLabel, Pos.TOP_LEFT);
        familyLabel.setTranslateY(-35);
        StackPane frame = new StackPane(content, familyLabel);
        frame.setBorder(BorderFactory.newBorder(Color.LIGHTGRAY, 10));
        frame.setPadding(new Insets(20, 15, 15, 15));
        return frame;
    }

    public KnownItemFamily getFamily() {
        return knownItemFamily;
    }
}
