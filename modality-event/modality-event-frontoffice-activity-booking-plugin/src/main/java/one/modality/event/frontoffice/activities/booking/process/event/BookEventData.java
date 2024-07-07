package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.beans.property.*;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.client.event.fx.FXEvent;
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
    private StringProperty formattedBalanceProperty = new SimpleStringProperty();
    private StringProperty formattedTotalProperty = new SimpleStringProperty();
    private StringProperty formattedBalanceOnPreviousBookingProperty = new SimpleStringProperty();
    private StringProperty formattedTotalOnPreviousBookingProperty = new SimpleStringProperty();
    private BooleanProperty isBalanceNull = new SimpleBooleanProperty();
    private IntegerProperty balanceProperty = new SimpleIntegerProperty(-1)  {
        @Override
        protected void invalidated() {
            formattedBalanceProperty.setValue(EventPriceFormatter.formatWithCurrency(balanceProperty.getValue(), FXEvent.getEvent()));
            formattedTotalProperty.setValue(EventPriceFormatter.formatWithCurrency(getTotalPrice(), FXEvent.getEvent()));
            isBalanceNull.setValue(balanceProperty.getValue()==0);
        }
    };
    private IntegerProperty balanceOnPreviousBookingProperty = new SimpleIntegerProperty(0) {
        @Override
        protected void invalidated() {
            formattedBalanceOnPreviousBookingProperty.setValue(EventPriceFormatter.formatWithCurrency(balanceOnPreviousBookingProperty.getValue(), FXEvent.getEvent()));
            formattedTotalOnPreviousBookingProperty.setValue(EventPriceFormatter.formatWithCurrency(balanceOnPreviousBookingProperty.getValue()+ getDocumentAggregate().getDeposit(),FXEvent.getEvent()));
        }
    };


    public void setCurrentBooking(WorkingBooking currentBooking) {
        this.currentBooking = currentBooking;
        reinitialiseCurrentPriceCalculator();
        priceCalculatorPastOption = new PriceCalculator(currentBooking.getInitialDocumentAggregate());
    }

    public StringProperty getFormattedBalanceProperty() {
        return formattedBalanceProperty;
    }

    public void setBalanceProperty(int balanceProperty) {
        this.balanceProperty.set(balanceProperty);
    }

    public int getBalance() {
        return balanceProperty.getValue();
    }

    public void reinitialiseCurrentPriceCalculator() {
        priceCalculatorCurrentOption = new PriceCalculator(currentBooking::getLastestDocumentAggregate);
    }

    public WorkingBooking getCurrentBooking() {
        return currentBooking;
    }

    public void updateGeneralBalance() {
        balanceProperty.set(calculateBalance());
    }

    public int getTotalPrice() {
        return getPriceCalculatorForCurrentOption().calculateTotalPrice();
    }

    public boolean isDepositOnPreviousBookingComplete() {
        return (priceCalculatorPastOption.calculateTotalPrice()-getDocumentAggregate().getDeposit()==0);
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

    public BooleanProperty isBalanceNullProperty() {
        return isBalanceNull;
    }

    public int calculateBalance() {
        return getPriceCalculatorForCurrentOption().calculateTotalPrice() - getDocumentAggregate().getDeposit();
    }

    public int getBalanceOnPreviousBooking() {
        return balanceOnPreviousBookingProperty.get();
    }

    public IntegerProperty balanceOnPreviousBookingProperty() {
        return balanceOnPreviousBookingProperty;
    }

    public void setBalanceOnPreviousBooking(int balanceOnPreviousBookingProperty) {
        this.balanceOnPreviousBookingProperty.set(balanceOnPreviousBookingProperty);
    }

    public int getRate() {
        return getCurrentBooking().getPolicyAggregate().getRates().get(0).getPrice();
    }

    public String getFormattedBalanceOnPreviousBooking() {
        return formattedBalanceOnPreviousBookingProperty.get();
    }

    public StringProperty getFormattedBalanceOnPreviousBookingProperty() {
        return formattedBalanceOnPreviousBookingProperty;
    }

    public void setFormattedBalanceOnPreviousBookingProperty(String formattedBalanceOnPreviousBookingProperty) {
        this.formattedBalanceOnPreviousBookingProperty.set(formattedBalanceOnPreviousBookingProperty);
    }

    public StringProperty getFormattedTotalOnPreviousBookingProperty() {
        return formattedTotalOnPreviousBookingProperty;
    }

    public StringProperty getFormattedTotalProperty() {
        return formattedTotalProperty;
    }

}
