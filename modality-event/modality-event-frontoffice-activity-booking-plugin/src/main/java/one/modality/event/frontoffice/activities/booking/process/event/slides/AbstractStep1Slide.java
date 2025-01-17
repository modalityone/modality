package one.modality.event.frontoffice.activities.booking.process.event.slides;

import one.modality.event.client.recurringevents.BookableDatesUi;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

public abstract class AbstractStep1Slide extends StepSlide {


    protected AbstractStep1Slide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    public abstract void onWorkingBookingLoaded();

    public abstract BookableDatesUi getBookableDatesUi();
}

