package one.modality.event.frontoffice.activities.booking.process.event.slides;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.BookingForm;

/**
 * @author Bruno Salmon
 */
final class Step1BookingFormSlide extends StepSlide {

    private final ObjectProperty<BookingForm> bookingFormProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            Node bookingFormUi = get().buildUi();
            if (bookingFormUi != null)
                mainVbox.getChildren().setAll(bookingFormUi);
            else {
                mainVbox.getChildren().clear();
            }
        }
    };

    public Step1BookingFormSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    void setBookingForm(BookingForm bookingForm) {
        bookingFormProperty.set(bookingForm);
    }

    boolean isEmpty() {
        return mainVbox.getChildren().isEmpty();
    }

    @Override
    protected void buildSlideUi() {
        // Content will be actually reset later when the booking form is set
    }
}
