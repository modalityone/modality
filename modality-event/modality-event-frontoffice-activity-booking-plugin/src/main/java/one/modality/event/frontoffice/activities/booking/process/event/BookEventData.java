package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.frontoffice.activities.booking.PriceCalculator;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;

import java.util.List;

public class BookEventData {
    private PriceCalculator priceCalculator = null;
    private DocumentAggregate documentAggregate = null;
    private List<ScheduledItem> scheduledItemsAlreadyBooked = null;
    private List<ScheduledItem> scheduledItemsOnEvent = null;
    private WorkingBooking currentBooking = null;
    private IntegerProperty bookingNumber = new SimpleIntegerProperty(0);
    private PolicyAggregate policyAggrate = null;
    private Object documentPrimaryKey = null;
    private int totalPrice = 0;

    public BookEventData() {
        priceCalculator = null;
        documentAggregate = null;
        scheduledItemsAlreadyBooked = null;
        scheduledItemsOnEvent = null;
        currentBooking = null;
        bookingNumber = new SimpleIntegerProperty(0);
        policyAggrate = null;
    }

    public List<ScheduledItem> getScheduledItemsAlreadyBooked() {
        return scheduledItemsAlreadyBooked;
    }

    public void setScheduledItemsAlreadyBooked(List<ScheduledItem> scheduledItemsAlreadyBook) {
        scheduledItemsAlreadyBooked = scheduledItemsAlreadyBook;
    }

    public WorkingBooking getCurrentBooking() {
        return currentBooking;
    }

    public void setCurrentBooking(WorkingBooking currentBooking) {
        this.currentBooking = currentBooking;
    }

    public int getBookingNumber() {
        return bookingNumber.get();
    }

    public IntegerProperty bookingNumberProperty() {
        return bookingNumber;
    }

    public void setBookingNumber(int bookingNumb) {
        bookingNumber.set(bookingNumb);
    }

    public PriceCalculator getPriceCalculator() {
        return priceCalculator;
    }

    public void setPriceCalculator(PriceCalculator priceCalculator) {
        this.priceCalculator = priceCalculator;
    }

    public DocumentAggregate getDocumentAggregate() {
        return documentAggregate;
    }

    public void setDocumentAggregate(DocumentAggregate documentAggregate) {
        this.documentAggregate = documentAggregate;
    }

    public void setPolicyAggregate(PolicyAggregate pa) {
        policyAggrate = pa;
    }

    public PolicyAggregate getPolicyAggrate() {
        return policyAggrate;
    }

    public List<ScheduledItem> getScheduledItemsOnEvent() {
        return scheduledItemsOnEvent;
    }

    public void setScheduledItemsOnEvent(List<ScheduledItem> scheduledItemsOnEvent) {
        this.scheduledItemsOnEvent = scheduledItemsOnEvent;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

    public void setDocumentPrimaryKey(Object documentPrimaryKey) {
        this.documentPrimaryKey = documentPrimaryKey;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}
