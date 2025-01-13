package one.modality.event.frontoffice.activities.booking.process.event.slides.sttp;

import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Hyperlink;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.event.client.recurringevents.BookableDatesUi;
import one.modality.event.client.recurringevents.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.WorkingBookingProperties;
import one.modality.event.frontoffice.activities.booking.process.event.slides.AbstractStep1Slide;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Step1BookSttpSlide extends AbstractStep1Slide {

    private final HtmlText eventDescription = new HtmlText();
    private final BooleanProperty noDatesBookedProperty = new SimpleBooleanProperty();
    private final Hyperlink selectAllClassesHyperlink = I18nControls.bindI18nTextProperty(new Hyperlink(), BookingI18nKeys.SelectAllClasses);
    private final SimpleBookableDateUI bookableDateUI = new SimpleBookableDateUI();
    public Step1BookSttpSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    @Override
    protected void buildSlideUi() {
        //Nothing to do since we won't display this slide
    }


    public void onWorkingBookingLoaded() {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();

        //We test if there is already a booking for that event.
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        if(!workingBooking.getScheduledItemsAlreadyBooked().isEmpty()) {
            displayCheckoutSlide();
        }
        else {
            List<ScheduledItem> scheduledItemsOnEvent = workingBookingProperties.getScheduledItemsOnEvent();
            //We are looking all the scheduledItem for the event, and book all of them.
            // Computing non-selectable and already booked dates for the purpose of styling the event schedule
            List<LocalDate> nonSelectableDate = new ArrayList<>();
            List<LocalDate> alreadyBookedDate = new ArrayList<>();
            scheduledItemsOnEvent.forEach(si -> {
                LocalDate localDate = si.getDate();
                if (workingBookingProperties.getScheduledItemsAlreadyBooked().stream()
                    .map(ScheduledItem::getDate)
                    .anyMatch(date -> date.equals(localDate))) {
                    //Here there is already a date booked in this another booking
                    nonSelectableDate.add(localDate);
                    alreadyBookedDate.add(localDate);
                } else if (localDate.isBefore(LocalDate.now())) {
                    //here the date is past
                    nonSelectableDate.add(localDate);
                }
            });


            // Synchronizing the event schedule from the working booking (will select the dates newly added in the working booking)
            getBookEventActivity().syncEventScheduleFromWorkingBooking();

            // Arming the "Select all classes" hyperlink. We create a list of dates that will contain all the selectable
            // dates = the ones that are not in the past, and not already booked
            List<LocalDate> allSelectableDates = Collections.map(scheduledItemsOnEvent, ScheduledItem::getDate);
            allSelectableDates.removeAll(nonSelectableDate);
        }
        displayCheckoutSlide();
    }

    @Override
    public BookableDatesUi getBookableDatesUi() {
        return bookableDateUI;
    }
}
