package one.modality.booking.frontoffice.bookingpage.pages.teaching;

import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.time.format.LocalizedTime;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.BookablePeriod;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.base.shared.knownitems.KnownItemI18nKeys;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.booking.frontoffice.bookingform.BookingFormI18nKeys;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class OnlineTeachingPage implements BookingFormPage {

    private final GridPane optionsGridPane = BookingElements.createOptionsGridPane(true);
    private final Label videoRecordingMessageLabel = BookingElements.createWordingLabel();
    private final VBox container = BookingElements.createFormPageVBox(true,
        BookingElements.createSecondaryWordingLabel(BookingFormI18nKeys.BookingOptions),
        optionsGridPane,
        videoRecordingMessageLabel
    );

    @Override
    public Object getTitleI18nKey() {
        return KnownItemI18nKeys.TeachingsOnline;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        return workingBooking.isNewBooking() || !workingBooking.getPolicyAggregate().getBookablePeriods(KnownItemFamily.TEACHING).isEmpty();
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        List<BookablePeriod> bookablePeriods = workingBooking.getPolicyAggregate()
            .getBookablePeriods(KnownItemFamily.TEACHING, I18nKeys.upperCaseFirstChar(BookingFormI18nKeys.wholeEvent));
        ToggleGroup teachingOptionsToggleGroup = new ToggleGroup();
        optionsGridPane.getChildren().clear();
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.NEVER);
        optionsGridPane.getColumnConstraints().setAll(c1);
        for (int i = 0, n = bookablePeriods.size(); i < n; i++) {
            BookablePeriod bookablePeriod = bookablePeriods.get(i);
            TeachingPeriodOption teachingPeriodOption = new TeachingPeriodOption(bookablePeriod, workingBooking);
            RadioButton radioButton = teachingPeriodOption.getRadioButton();
            radioButton.setToggleGroup(teachingOptionsToggleGroup);
            optionsGridPane.add(radioButton, 0, i);
            optionsGridPane.add(teachingPeriodOption.getPeriodLabel(), 1, i);
            optionsGridPane.add(teachingPeriodOption.getPriceLabel(),  2, i);
            if (n == 1) // If there is only one option, we select it by default
                radioButton.setSelected(true);
        }
        // We display at the bottom an explanation of the VOD expiration date if the event has one (ex: Festivals)
        LocalDateTime vodExpirationDate = workingBooking.getPolicyAggregate().getEvent().getVodExpirationDate();
        if (vodExpirationDate != null)
            I18nControls.bindI18nProperties(videoRecordingMessageLabel, BookingPageI18nKeys.VideoRecordingExpirationMessage1,
                LocalizedTime.formatLocalDateTimeProperty(vodExpirationDate, FrontOfficeTimeFormats.MEDIA_EXPIRATION_DATE_TIME_FORMAT));
        else // or an explanation there are no video recordings (ex: Empowerment weekends)
            I18nControls.bindI18nProperties(videoRecordingMessageLabel, BookingPageI18nKeys.NoVideoRecordingMessage);
    }

}
