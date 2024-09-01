package one.modality.event.frontoffice.activities.booking.process.event;

import javafx.beans.property.*;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.frontoffice.activities.booking.PriceCalculator;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;

import java.util.List;

public class WorkingBookingProperties {

    private WorkingBooking workingBooking;
    private PriceCalculator previousBookingPriceCalculator;
    private PriceCalculator latestBookingPriceCalculator;

    // Deposit (of both the latest booking and previous booking)
    private final StringProperty formattedDepositProperty = new SimpleStringProperty();
    private final IntegerProperty depositProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            setFormattedDeposit(EventPriceFormatter.formatWithCurrency(getDeposit(), getEvent()));
        }
    };

    // Total (of the latest booking)
    private final StringProperty formattedTotalProperty = new SimpleStringProperty();
    private final IntegerProperty totalProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            setFormattedTotal(EventPriceFormatter.formatWithCurrency(getTotal(), getEvent()));
        }
    };

    // Balance (of the latest booking)
    private final StringProperty formattedBalanceProperty = new SimpleStringProperty();
    private final IntegerProperty balanceProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            int balance = getBalance();
            setFormattedBalance(EventPriceFormatter.formatWithCurrency(balance, getEvent()));
        }
    };

    // Previous total (of the booking loaded from database)
    private final StringProperty formattedPreviousTotalProperty = new SimpleStringProperty();
    private final IntegerProperty previousTotalProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            setFormattedPreviousTotal(EventPriceFormatter.formatWithCurrency(getPreviousTotal(), getEvent()));
        }
    };

    // Previous balance (of the booking loaded from database)
    private final StringProperty formattedPreviousBalanceProperty = new SimpleStringProperty();
    private final IntegerProperty previousBalanceProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            formattedPreviousBalanceProperty.setValue(EventPriceFormatter.formatWithCurrency(previousBalanceProperty.getValue(), getEvent()));
            formattedPreviousTotalProperty.setValue(EventPriceFormatter.formatWithCurrency(previousBalanceProperty.getValue() + getDocumentAggregate().getDeposit(), getEvent()));
        }
    };

    // Booking reference
    private final ObjectProperty<Object> bookingReferenceProperty = new SimpleObjectProperty<>();

    public void setWorkingBooking(WorkingBooking workingBooking) {
        this.workingBooking = workingBooking;
        latestBookingPriceCalculator = new PriceCalculator(workingBooking::getLastestDocumentAggregate);
        previousBookingPriceCalculator = new PriceCalculator(workingBooking::getInitialDocumentAggregate);
        DocumentAggregate initialDocumentAggregate = workingBooking.getInitialDocumentAggregate();
        if (initialDocumentAggregate != null) {
            setBookingReference(initialDocumentAggregate.getDocument().getRef());
        }
        updateAll();
    }

    public WorkingBooking getWorkingBooking() {
        return workingBooking;
    }

    public Event getEvent() {
        return workingBooking.getEvent();
    }

    public Object getDocumentPrimaryKey() {
        return workingBooking.getDocumentPrimaryKey();
    }

    private PriceCalculator getPreviousBookingPriceCalculator() {
        return previousBookingPriceCalculator;
    }

    private PriceCalculator getLatestBookingPriceCalculator() {
        return latestBookingPriceCalculator;
    }

    public DocumentAggregate getDocumentAggregate() {
        return workingBooking.getLastestDocumentAggregate();
    }

    public PolicyAggregate getPolicyAggregate() {
        return workingBooking.getPolicyAggregate();
    }


    public void updateAll() {
        updateDeposit();
        updateTotal();
        updateBalance();
        updatePreviousTotal();
        updatePreviousBalance();
    }


    // Deposit

    public int getDeposit() {
        return depositProperty.get();
    }

    public ReadOnlyIntegerProperty depositProperty() {
        return depositProperty;
    }

    private void setDeposit(int deposit) {
        depositProperty.set(deposit);
    }

    public int calculateDeposit() {
        return getDocumentAggregate().getDeposit();
    }

    public void updateDeposit() {
        setDeposit(calculateDeposit());
    }

    public StringProperty formattedDepositProperty() {
        return formattedDepositProperty;
    }

    private void setFormattedDeposit(String formattedDeposit) {
        formattedDepositProperty.set(formattedDeposit);
    }

    public String getFormattedDeposit() {
        return formattedDepositProperty.getValue();
    }


    // Total

    public int getTotal() {
        return totalProperty.get();
    }

    public ReadOnlyIntegerProperty totalProperty() {
        return totalProperty;
    }

    private void setTotal(int total) {
        totalProperty.set(total);
    }

    public int calculateTotal() {
        return getLatestBookingPriceCalculator().calculateTotalPrice();
    }

    public void updateTotal() {
        setTotal(calculateTotal());
    }

    public StringProperty formattedTotalProperty() {
        return formattedTotalProperty;
    }

    private void setFormattedTotal(String formattedTotal) {
        formattedTotalProperty.set(formattedTotal);
    }

    public String getFormattedTotal() {
        return formattedTotalProperty.getValue();
    }


    // Balance

    public int getBalance() {
        return balanceProperty.getValue();
    }

    public ReadOnlyIntegerProperty balanceProperty() {
        return balanceProperty;
    }

    private void setBalance(int balance) {
        this.balanceProperty.set(balance);
    }

    public void updateBalance() {
        setBalance(calculateBalance());
    }

    public int calculateBalance() {
        return calculateTotal() - calculateDeposit();
    }

    public StringProperty formattedBalanceProperty() {
        return formattedBalanceProperty;
    }

    private void setFormattedBalance(String formattedBalance) {
        formattedBalanceProperty.set(formattedBalance);
    }

    public String getFormattedBalance() {
        return formattedBalanceProperty.getValue();
    }


    // Previous total

    public int getPreviousTotal() {
        return previousTotalProperty.get();
    }

    public ReadOnlyIntegerProperty previousTotalProperty() {
        return previousTotalProperty;
    }

    private void setPreviousTotal(int previousTotal) {
        previousTotalProperty.set(previousTotal);
    }

    public int calculatePreviousTotal() {
        return previousBookingPriceCalculator.calculateTotalPrice();
    }

    public void updatePreviousTotal() {
        setPreviousTotal(calculatePreviousTotal());
    }

    public StringProperty formattedPreviousTotalProperty() {
        return formattedPreviousTotalProperty;
    }

    private void setFormattedPreviousTotal(String formattedPreviousTotal) {
        formattedPreviousTotalProperty.set(formattedPreviousTotal);
    }

    public String getFormattedPreviousTotal() {
        return formattedPreviousTotalProperty.getValue();
    }


    // Previous balance

    public int getPreviousBalance() {
        return previousBalanceProperty.getValue();
    }

    public ReadOnlyIntegerProperty previousBalanceProperty() {
        return previousBalanceProperty;
    }

    private void setPreviousBalance(int previousBalance) {
        this.previousBalanceProperty.set(previousBalance);
    }

    public void updatePreviousBalance() {
        setPreviousBalance(calculatePreviousBalance());
    }

    public int calculatePreviousBalance() {
        return calculatePreviousTotal() - calculateDeposit();
    }

    public StringProperty formattedPreviousBalanceProperty() {
        return formattedPreviousBalanceProperty;
    }

    private void setFormattedPreviousBalance(String formattedPreviousBalance) {
        formattedPreviousBalanceProperty.set(formattedPreviousBalance);
    }

    public String getFormattedPreviousBalance() {
        return formattedPreviousBalanceProperty.getValue();
    }


    // Booking reference

    public ObjectProperty<Object> bookingReferenceProperty() {
        return bookingReferenceProperty;
    }

    public void setBookingReference(Object bookingReference) {
        bookingReferenceProperty.set(bookingReference);
    }

    public Object getBookingReference() {
        return bookingReferenceProperty.getValue();
    }

    // Shorthand methods to workingBooking

    public List<ScheduledItem> getScheduledItemsAlreadyBooked() {
        return workingBooking.getScheduledItemsAlreadyBooked();
    }

    public List<ScheduledItem> getScheduledItemsOnEvent() {
        return workingBooking.getScheduledItemsOnEvent();
    }

    public int getDailyRatePrice() {
        return workingBooking.getDailyRatePrice();
    }

    public int getWholeEventPrice() {
        return workingBooking.getWholeEventPrice();
    }

    public int getWholeEventNoDiscountPrice() {
        return workingBooking.getWholeEventNoDiscountPrice();
    }

}
