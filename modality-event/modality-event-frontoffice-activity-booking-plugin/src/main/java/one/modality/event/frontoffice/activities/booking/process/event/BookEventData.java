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
    private PriceCalculator priceCalculator;
    private final ObjectProperty<Object> bookingReferenceProperty = new SimpleObjectProperty<>();

    public void setCurrentBooking(WorkingBooking currentBooking) {
        this.currentBooking = currentBooking;
        priceCalculator = new PriceCalculator(currentBooking);
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

    public PriceCalculator getPriceCalculator() {
        return priceCalculator;
    }

    public void setPriceCalculator(PriceCalculator priceCalculator) {
        this.priceCalculator = priceCalculator;
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
