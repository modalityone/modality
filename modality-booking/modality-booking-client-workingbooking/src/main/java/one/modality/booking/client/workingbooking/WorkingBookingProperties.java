package one.modality.booking.client.workingbooking;

import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.*;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class WorkingBookingProperties {

    // These fields are constant once set, but not declared final because they are not set in the constructor, they are
    // set later in the setWorkingBooking() method (called only once).
    private WorkingBooking workingBooking;
    private PriceCalculator previousBookingPriceCalculator;
    private PriceCalculator latestBookingPriceCalculator;

    public WorkingBookingProperties() {
    }

    public WorkingBookingProperties(WorkingBooking workingBooking) {
        setWorkingBooking(workingBooking);
    }

    private final BooleanProperty hasChangesProperty = new SimpleBooleanProperty();

    public BooleanExpression hasChangesProperty() {
        return hasChangesProperty;
    }

    public boolean hasChanges() {
        return hasChangesProperty.get();
    }

    private Scheduled updateAllScheduled;

    // Deposit (of both the latest booking and previous booking)
    private final StringProperty formattedDepositProperty = new SimpleStringProperty();
    private final StringProperty formattedDepositWithoutCurrencyProperty = new SimpleStringProperty();
    private final IntegerProperty depositProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            setFormattedDeposit(EventPriceFormatter.formatWithCurrency(get(), getEvent()));
            setFormattedDepositWithoutCurrency(EventPriceFormatter.formatWithoutCurrency(get(), false));
        }
    };

    // Total (of the latest booking)
    private final StringProperty formattedTotalProperty = new SimpleStringProperty();
    private final StringProperty formattedTotalWithoutCurrencyProperty = new SimpleStringProperty();
    private final IntegerProperty totalProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            setFormattedTotal(EventPriceFormatter.formatWithCurrency(get(), getEvent()));
            setFormattedTotalWithoutCurrency(EventPriceFormatter.formatWithoutCurrency(get(), false));
        }
    };

    // Min deposit (of the latest booking)
    private final StringProperty formattedMinDepositProperty = new SimpleStringProperty();
    private final StringProperty formattedMinDepositWithoutCurrencyProperty = new SimpleStringProperty();
    private final IntegerProperty minDepositProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            setFormattedMinDeposit(EventPriceFormatter.formatWithCurrency(get(), getEvent()));
            setFormattedMinDepositWithoutCurrency(EventPriceFormatter.formatWithoutCurrency(get(), false));
        }
    };

    // Balance (of the latest booking)
    private final StringProperty formattedBalanceProperty = new SimpleStringProperty();
    private final StringProperty formattedBalanceWithoutCurrencyProperty = new SimpleStringProperty();
    private final IntegerProperty balanceProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            setFormattedBalance(EventPriceFormatter.formatWithCurrency(get(), getEvent()));
            setFormattedBalanceWithoutCurrency(EventPriceFormatter.formatWithoutCurrency(get(), false));
        }
    };

    // Previous total (of the booking loaded from the database)
    private final StringProperty formattedPreviousTotalProperty = new SimpleStringProperty();
    private final IntegerProperty previousTotalProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            setFormattedPreviousTotal(EventPriceFormatter.formatWithCurrency(get(), getEvent()));
        }
    };

    // Previous balance (of the booking loaded from the database)
    private final StringProperty formattedPreviousBalanceProperty = new SimpleStringProperty();
    private final IntegerProperty previousBalanceProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            formattedPreviousBalanceProperty.setValue(EventPriceFormatter.formatWithCurrency(previousBalanceProperty.get(), getEvent()));
            formattedPreviousTotalProperty.setValue(EventPriceFormatter.formatWithCurrency(previousBalanceProperty.get() + getDocumentAggregate().getDeposit(), getEvent()));
        }
    };

    // Booking reference
    private final ObjectProperty<Object> bookingReferenceProperty = new SimpleObjectProperty<>();

    // Version number (arbitrary number that increases each time there is a change in the working booking - useful for reacting to changes)
    private final IntegerProperty versionNumberProperty = new SimpleIntegerProperty();

    // Submittable (indicates if the booking is suitable to submit, which is the case if there is a balance to pay, or it has changes on document lines)
    private final BooleanBinding submittableProperty = Bindings.createBooleanBinding(() ->
            getBalance() > 0 || getWorkingBooking().hasDocumentLineChanges()
        , versionNumberProperty());

    public void setWorkingBooking(WorkingBooking workingBooking) {
        this.workingBooking = workingBooking;
        latestBookingPriceCalculator = new PriceCalculator(workingBooking::getLastestDocumentAggregate);
        previousBookingPriceCalculator = new PriceCalculator(workingBooking::getInitialDocumentAggregate);
        DocumentAggregate initialDocumentAggregate = workingBooking.getInitialDocumentAggregate();
        if (initialDocumentAggregate != null) {
            setBookingReference(initialDocumentAggregate.getDocument().getRef());
        }
        updateAll(); // Updated all price properties
        setVersionNumber(0);
        hasChangesProperty.bind(workingBooking.hasChangesProperty());
        // And listening to further changes to automatically keep these properties updated
        ObservableLists.runOnListChange(c -> {
            if (updateAllScheduled == null) {
                updateAllScheduled = UiScheduler.scheduleDeferred(() -> {
                    updateAll();
                    incrementVersionNumber();
                    updateAllScheduled = null;
                });
            }
            incrementVersionNumber();
        }, workingBooking.getDocumentChanges());
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
        updateMinDeposit();
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

    public ReadOnlyStringProperty formattedDepositProperty() {
        return formattedDepositProperty;
    }

    private void setFormattedDeposit(String formattedDeposit) {
        formattedDepositProperty.set(formattedDeposit);
    }

    public String getFormattedDeposit() {
        return formattedDepositProperty.getValue();
    }

    public ReadOnlyStringProperty formattedDepositWithoutCurrencyProperty() {
        return formattedDepositWithoutCurrencyProperty;
    }

    private void setFormattedDepositWithoutCurrency(String formattedDeposit) {
        formattedDepositWithoutCurrencyProperty.set(formattedDeposit);
    }

    public String getFormattedDepositWithoutCurrency() {
        return formattedDepositWithoutCurrencyProperty.getValue();
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

    private void setFormattedTotalWithoutCurrency(String formattedTotal) {
        formattedTotalWithoutCurrencyProperty.set(formattedTotal);
    }

    public String getFormattedTotalWithoutCurrency() {
        return formattedTotalWithoutCurrencyProperty.getValue();
    }

    public ReadOnlyStringProperty formattedTotalWithoutCurrencyProperty() {
        return formattedTotalWithoutCurrencyProperty;
    }


    // Min deposit

    public int getMinDeposit() {
        return minDepositProperty.get();
    }

    public ReadOnlyIntegerProperty minDepositProperty() {
        return minDepositProperty;
    }

    private void setMinDeposit(int value) {
        minDepositProperty.set(value);
    }

    public int calculateMinDeposit() {
        return getLatestBookingPriceCalculator().calculateMinDeposit();
    }

    public void updateMinDeposit() {
        setMinDeposit(calculateMinDeposit());
    }

    public ReadOnlyStringProperty formattedMinDepositProperty() {
        return formattedMinDepositProperty;
    }

    private void setFormattedMinDeposit(String formattedValue) {
        formattedMinDepositProperty.set(formattedValue);
    }

    public String getFormattedMinDeposit() {
        return formattedMinDepositProperty.getValue();
    }

    public ReadOnlyStringProperty formattedMinDepositWithoutCurrencyProperty() {
        return formattedMinDepositWithoutCurrencyProperty;
    }

    private void setFormattedMinDepositWithoutCurrency(String formattedValue) {
        formattedMinDepositWithoutCurrencyProperty.set(formattedValue);
    }

    public String getFormattedMinDepositWithoutCurrency() {
        return formattedMinDepositWithoutCurrencyProperty.getValue();
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

    public ReadOnlyStringProperty formattedBalanceProperty() {
        return formattedBalanceProperty;
    }

    private void setFormattedBalance(String formattedBalance) {
        formattedBalanceProperty.set(formattedBalance);
    }

    public String getFormattedBalance() {
        return formattedBalanceProperty.getValue();
    }

    public ReadOnlyStringProperty formattedBalanceWithoutCurrencyProperty() {
        return formattedBalanceWithoutCurrencyProperty;
    }

    private void setFormattedBalanceWithoutCurrency(String formattedBalanceWithoutCurrency) {
        formattedBalanceWithoutCurrencyProperty.set(formattedBalanceWithoutCurrency);
    }

    public String getFormattedBalanceWithoutCurrency() {
        return formattedBalanceWithoutCurrencyProperty.getValue();
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

    public ReadOnlyStringProperty formattedPreviousTotalProperty() {
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

    public ReadOnlyStringProperty formattedPreviousBalanceProperty() {
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


    // Version number

    public int getVersionNumber() {
        return versionNumberProperty.get();
    }

    public ReadOnlyIntegerProperty versionNumberProperty() {
        return versionNumberProperty;
    }

    private void setVersionNumber(int versionNumber) {
        versionNumberProperty.set(versionNumber);
    }

    private void incrementVersionNumber() {
        setVersionNumber(getVersionNumber() + 1);
    }


    // Submittable

    public BooleanBinding submittableProperty() {
        return submittableProperty;
    }

    // Shorthand methods to workingBooking

    public List<ScheduledItem> getAlreadyBookedScheduledItems() {
        return workingBooking.getAlreadyBookedScheduledItems();
    }

    public List<ScheduledItem> getScheduledItemsOnEvent() {
        return workingBooking.getPolicyScheduledItems();
    }

    public int getDailyRatePrice() {
        return workingBooking.getDailyRatePrice();
    }

    // TODO: there should be a separate workingBooking for the different cases, including for the whole event

    public int getWholeEventPrice() {
        return workingBooking.getWholeEventPrice();
    }

    public int getWholeEventNoDiscountPrice() {
        return workingBooking.getWholeEventNoDiscountPrice();
    }

}
