package one.modality.booking.frontoffice.bookingpage.pages.audiorecording;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.client.time.ModalityDates;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.frontoffice.bookingelements.BookingElements;

import java.util.List;

/**
 * @author Bruno Salmon
 */
final class AudioRecordingLanguageAndPeriodOption {

    private final CheckBox bookButton = BookingElements.optionLabel(new CheckBox());
    private final Label periodLabel = BookingElements.createPeriodLabel();
    private final Label priceLabel = BookingElements.createPriceLabel();

    AudioRecordingLanguageAndPeriodOption(Item audioRecordingItem, List<ScheduledItem> scheduledAudioRecordingItems, WorkingBooking workingBooking) {
        I18nEntities.bindTranslatedEntityToTextProperty(bookButton, audioRecordingItem);
        periodLabel.setText(ModalityDates.formatHasDateSeries(scheduledAudioRecordingItems));
        BookingElements.setupPeriodOption(scheduledAudioRecordingItems, priceLabel, bookButton.selectedProperty(), workingBooking);
    }

    ButtonBase getBookButton() {
        return bookButton;
    }

    Label getPeriodLabel() {
        return periodLabel;
    }

    Label getPriceLabel() {
        return priceLabel;
    }

}
