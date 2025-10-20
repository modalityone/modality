package one.modality.booking.backoffice.bookingeditor.family;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.border.BorderFactory;
import dev.webfx.platform.async.Future;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import one.modality.booking.backoffice.bookingeditor.BookingEditor;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingHistoryHelper;

/**
 * @author Bruno Salmon
 */
public abstract class BookingEditorBase implements BookingEditor {

    protected final WorkingBooking workingBooking;

    protected BookingEditorBase(WorkingBooking workingBooking) {
        this.workingBooking = workingBooking;
        // Final subclasses should call this method
        // initiateUiAndSyncFromWorkingBooking();
    }

    protected abstract void initiateUiAndSyncFromWorkingBooking();

    public abstract void syncWorkingBookingFromUi();

    @Override
    public Future<Void> saveChanges() {
        syncWorkingBookingFromUi();
        WorkingBookingHistoryHelper historyHelper = new WorkingBookingHistoryHelper(workingBooking);
        return workingBooking.submitChanges(historyHelper.generateHistoryComment())
            .mapEmpty();
    }

    protected static Node embedInFrame(Node content, Object i18nKey) {
        Label label = Bootstrap.textSecondary(Bootstrap.strong(I18nControls.newLabel(i18nKey)));
        label.setPadding(new Insets(5));
        label.setBackground(Background.fill(Color.WHITE));
        StackPane.setAlignment(label, Pos.TOP_LEFT);
        label.setTranslateY(-35);
        StackPane frame = new StackPane(content, label);
        frame.setBorder(BorderFactory.newBorder(Color.LIGHTGRAY, 10));
        frame.setPadding(new Insets(20, 15, 15, 15));
        return frame;
    }

}
