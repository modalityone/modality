package one.modality.booking.frontoffice.bookingpage.pages.audiorecording;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemI18nKeys;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.booking.frontoffice.bookingform.BookingFormI18nKeys;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;

import java.util.List;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public final class AudioRecordingPage implements BookingFormPage {

    private final GridPane gridPane = BookingElements.createOptionsGridPane(false);
    private final VBox container = BookingElements.createFormPageVBox(true,
        BookingElements.createWordingLabel(BookingPageI18nKeys.AudioRecordingTopMessage),
        BookingElements.createSecondaryWordingLabel(BookingFormI18nKeys.BookingOptions),
        gridPane
    );
    private WorkingBooking lastWorkingBooking;

    @Override
    public Object getTitleI18nKey() {
        return KnownItemI18nKeys.AudioRecordings;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        if (workingBooking == lastWorkingBooking)
            return;
        lastWorkingBooking = workingBooking;
        Map<Item, List<ScheduledItem>> audioRecordingItemsToScheduledItemsMap = workingBooking.getPolicyAggregate()
            .groupScheduledItemsByAudioRecordingItems();
        gridPane.getChildren().clear();
        for (Map.Entry<Item, List<ScheduledItem>> entry : audioRecordingItemsToScheduledItemsMap.entrySet()) {
            AudioRecordingLanguageAndPeriodOption audioRecordingLanguageAndPeriodOption =
                new AudioRecordingLanguageAndPeriodOption(entry.getKey(), entry.getValue(), workingBooking);
            int rowIndex = gridPane.getRowCount();
            gridPane.add(audioRecordingLanguageAndPeriodOption.getBookButton(),  0, rowIndex);
            gridPane.add(audioRecordingLanguageAndPeriodOption.getPeriodLabel(), 1, rowIndex);
            gridPane.add(audioRecordingLanguageAndPeriodOption.getPriceLabel(),  2, rowIndex);
        }
    }

    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        return !workingBooking.isPaymentRequestedByUser();
    }
}
