package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.frontoffice.activities.booking.PriceCalculator;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BookEventData {

    private WorkingBooking currentBooking;
    private PriceCalculator priceCalculatorPastOption;
    private PriceCalculator priceCalculatorCurrentOption;
    private final ObjectProperty<Object> bookingReferenceProperty = new SimpleObjectProperty<>();

    public void setCurrentBooking(WorkingBooking currentBooking) {
        this.currentBooking = currentBooking;
        priceCalculatorCurrentOption = new PriceCalculator(currentBooking::getLastestDocumentAggregate);
        priceCalculatorPastOption = new PriceCalculator(currentBooking.getInitialDocumentAggregate());
    }

    public WorkingBooking getCurrentBooking() {
        return currentBooking;
    }


    public List<ScheduledItem> getScheduledItemsAlreadyBooked() {
        DocumentAggregate initialDocumentAggregate = currentBooking.getInitialDocumentAggregate();
        if (initialDocumentAggregate == null) {
            return Collections.emptyList();
        }
        return initialDocumentAggregate.getAttendancesStream()
                .map(Attendance::getScheduledItem)
                .collect(Collectors.toList());
    }

    public Object getBookingReference() {
        return bookingReferenceProperty.get();
    }

    public ObjectProperty<Object> bookingReferenceProperty() {
        return bookingReferenceProperty;
    }

    public void setBookingReference(Object bookingReference) {
        bookingReferenceProperty.set(bookingReference);
    }

    public PriceCalculator getPriceCalculatorForPastOption() {
        return priceCalculatorPastOption;
    }

    public PriceCalculator getPriceCalculatorForCurrentOption() {
        return priceCalculatorCurrentOption;
    }

    public void setPriceCalculatorForCurrentOption(PriceCalculator priceCalculator) {
        this.priceCalculatorCurrentOption = priceCalculator;
    }

    public DocumentAggregate getDocumentAggregate() {
        return currentBooking.getLastestDocumentAggregate();
    }

    public PolicyAggregate getPolicyAggregate() {
        return currentBooking.getPolicyAggregate();
    }

    public List<ScheduledItem> getScheduledItemsOnEvent() {
        return getPolicyAggregate().getScheduledItems();
    }

    public Object getDocumentPrimaryKey() {
        return currentBooking.getDocumentPrimaryKey();
    }

}
