package one.modality.booking.client.workingbooking;

import dev.webfx.kit.util.properties.FXProperties;
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

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class WorkingBookingProperties {

    // These fields are constant once set, but not declared final because they are not set in the constructor, they are
    // set later in the setWorkingBooking() method (called only once).
    private WorkingBooking workingBooking;

    // Booking reference
    private final ObjectProperty<Object> bookingReferenceProperty = new SimpleObjectProperty<>();
    // Version number (arbitrary number that increases each time there is a change in the working booking - useful for reacting to changes)
    private final IntegerProperty versionNumberProperty = new SimpleIntegerProperty();
    private Scheduled updateAllScheduled;
    {
        // Listening to changes on the working booking to automatically keep all properties updated
        FXProperties.runOnPropertyChange(() -> {
            if (updateAllScheduled == null) {
                updateAllScheduled = UiScheduler.scheduleDeferred(() -> {
                    updateAll();
                    updateAllScheduled = null;
                });
            }
        }, versionNumberProperty);
    }

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

    // Deposit (of both the latest booking and previous booking)
    private final StringProperty formattedDepositProperty = new SimpleStringProperty();
    private final StringProperty formattedDepositWithoutCurrencyProperty = new SimpleStringProperty();
    private final IntegerProperty depositProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            formattedDepositProperty.set(EventPriceFormatter.formatWithCurrency(get(), getEvent()));
            formattedDepositWithoutCurrencyProperty.set(EventPriceFormatter.formatWithoutCurrency(get(), false));
        }
    };

    // Total (of the latest booking)
    private final StringProperty formattedTotalProperty = new SimpleStringProperty();
    private final StringProperty formattedTotalWithoutCurrencyProperty = new SimpleStringProperty();
    private final IntegerProperty totalProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            formattedTotalProperty.set(EventPriceFormatter.formatWithCurrency(get(), getEvent()));
            formattedTotalWithoutCurrencyProperty.set(EventPriceFormatter.formatWithoutCurrency(get(), false));
        }
    };

    // Min deposit (of the latest booking)
    private final StringProperty formattedMinDepositProperty = new SimpleStringProperty();
    private final StringProperty formattedMinDepositWithoutCurrencyProperty = new SimpleStringProperty();
    private final IntegerProperty minDepositProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            formattedMinDepositProperty.set(EventPriceFormatter.formatWithCurrency(get(), getEvent()));
            formattedMinDepositWithoutCurrencyProperty.set(EventPriceFormatter.formatWithoutCurrency(get(), false));
        }
    };

    // Balance (of the latest booking)
    private final StringProperty formattedBalanceProperty = new SimpleStringProperty();
    private final StringProperty formattedBalanceWithoutCurrencyProperty = new SimpleStringProperty();
    private final IntegerProperty balanceProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            formattedBalanceProperty.set(EventPriceFormatter.formatWithCurrency(get(), getEvent()));
            formattedBalanceWithoutCurrencyProperty.set(EventPriceFormatter.formatWithoutCurrency(get(), false));
        }
    };

    // Previous total (of the booking loaded from the database)
    private final StringProperty formattedPreviousTotalProperty = new SimpleStringProperty();
    private final IntegerProperty previousTotalProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            formattedPreviousTotalProperty.set(EventPriceFormatter.formatWithCurrency(get(), getEvent()));
        }
    };

    // Previous balance (of the booking loaded from the database)
    private final StringProperty formattedPreviousBalanceProperty = new SimpleStringProperty();
    private final IntegerProperty previousBalanceProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            formattedPreviousBalanceProperty.setValue(EventPriceFormatter.formatWithCurrency(previousBalanceProperty.get(), getEvent()));
        }
    };

    // Submittable (indicates if the booking is suitable to submit, which is the case if there is a balance to pay, or it has changes on document lines)
    private final BooleanBinding submittableProperty = Bindings.createBooleanBinding(() ->
            getBalance() > 0 || getWorkingBooking().hasDocumentLineChanges()
        , versionNumberProperty);

    public void setWorkingBooking(WorkingBooking workingBooking) {
        this.workingBooking = workingBooking;
        DocumentAggregate initialDocumentAggregate = workingBooking.getInitialDocumentAggregate();
        if (initialDocumentAggregate != null) {
            setBookingReference(initialDocumentAggregate.getDocument().getRef());
        }
        hasChangesProperty.bind(workingBooking.hasChangesProperty());
        versionNumberProperty.bind(workingBooking.versionProperty()); // Should update all properties already unless it's identical to previous
        updateAll(); // Updated all properties in case the version number is the same as the last working booking
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

    public void updateDeposit() {
        depositProperty.set(workingBooking.calculateDeposit());
    }

    public ReadOnlyStringProperty formattedDepositProperty() {
        return formattedDepositProperty;
    }

    public String getFormattedDeposit() {
        return formattedDepositProperty.getValue();
    }

    public ReadOnlyStringProperty formattedDepositWithoutCurrencyProperty() {
        return formattedDepositWithoutCurrencyProperty;
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

    public void updateTotal() {
        totalProperty.set(workingBooking.calculateTotal());
    }

    public StringProperty formattedTotalProperty() {
        return formattedTotalProperty;
    }

    public String getFormattedTotal() {
        return formattedTotalProperty.getValue();
    }

    public String getFormattedTotalWithoutCurrency() {
        return formattedTotalWithoutCurrencyProperty.getValue();
    }

    public ReadOnlyStringProperty formattedTotalWithoutCurrencyProperty() {
        return formattedTotalWithoutCurrencyProperty;
    }

    public int calculateNoDiscountTotal() { // TODO: Move this to a property
        return workingBooking.calculateNoDiscountTotal();
    }


    // Min deposit

    public int getMinDeposit() {
        return minDepositProperty.get();
    }

    public ReadOnlyIntegerProperty minDepositProperty() {
        return minDepositProperty;
    }

    public void updateMinDeposit() {
        minDepositProperty.set(workingBooking.calculateMinDeposit());
    }

    public ReadOnlyStringProperty formattedMinDepositProperty() {
        return formattedMinDepositProperty;
    }

    public String getFormattedMinDeposit() {
        return formattedMinDepositProperty.getValue();
    }

    public ReadOnlyStringProperty formattedMinDepositWithoutCurrencyProperty() {
        return formattedMinDepositWithoutCurrencyProperty;
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

    public void updateBalance() {
        balanceProperty.set(workingBooking.calculateBalance());
    }

    public ReadOnlyStringProperty formattedBalanceProperty() {
        return formattedBalanceProperty;
    }

    public String getFormattedBalance() {
        return formattedBalanceProperty.getValue();
    }

    public ReadOnlyStringProperty formattedBalanceWithoutCurrencyProperty() {
        return formattedBalanceWithoutCurrencyProperty;
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

    public void updatePreviousTotal() {
        previousTotalProperty.set(workingBooking.calculatePreviousTotal());
    }

    public ReadOnlyStringProperty formattedPreviousTotalProperty() {
        return formattedPreviousTotalProperty;
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

    public void updatePreviousBalance() {
        this.previousBalanceProperty.set(workingBooking.calculatePreviousBalance());
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
