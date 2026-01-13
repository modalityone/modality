package one.modality.hotel.backoffice.activities.reception;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.LocalDate;

/**
 * Presentation model for the Reception Dashboard.
 * Manages all reactive state for the dashboard UI including:
 * - Tab selection and filtering
 * - Guest data and statistics
 * - Search and event filtering
 * - Bulk selection mode
 * - Modal states
 *
 * @author David Hello
 * @author Claude Code
 */
public class ReceptionPresentationModel extends OrganizationDependentGenericTablePresentationModel {

    // ==========================================
    // Tab Selection
    // ==========================================

    /**
     * Enum representing the available tabs in the guest list.
     */
    public enum Tab {
        ARRIVING,
        NO_SHOWS,
        DEPARTING,
        IN_HOUSE,
        UNPAID,
        CHECKED_OUT,
        ALL
    }

    private final ObjectProperty<Tab> selectedTabProperty = new SimpleObjectProperty<>(Tab.ARRIVING);
    public ObjectProperty<Tab> selectedTabProperty() { return selectedTabProperty; }
    public Tab getSelectedTab() { return selectedTabProperty.get(); }
    public void setSelectedTab(Tab tab) { selectedTabProperty.set(tab); }

    // ==========================================
    // Date Context
    // ==========================================

    private final ObjectProperty<LocalDate> currentDateProperty = new SimpleObjectProperty<>(LocalDate.now());
    public ObjectProperty<LocalDate> currentDateProperty() { return currentDateProperty; }
    public LocalDate getCurrentDate() { return currentDateProperty.get(); }
    public void setCurrentDate(LocalDate date) { currentDateProperty.set(date); }

    // ==========================================
    // Search & Filtering
    // ==========================================

    private final StringProperty searchTextProperty = new SimpleStringProperty("");
    public StringProperty searchTextProperty() { return searchTextProperty; }
    public String getSearchText() { return searchTextProperty.get(); }
    public void setSearchText(String text) { searchTextProperty.set(text); }

    /** Event filter - null means "All guests", specific event ID for event filtering */
    private final ObjectProperty<Object> eventFilterProperty = new SimpleObjectProperty<>(null);
    public ObjectProperty<Object> eventFilterProperty() { return eventFilterProperty; }
    public Object getEventFilter() { return eventFilterProperty.get(); }
    public void setEventFilter(Object eventFilter) { eventFilterProperty.set(eventFilter); }

    /** Special value for "Independent stays" filter */
    public static final String INDEPENDENT_STAYS_FILTER = "INDEPENDENT";

    // ==========================================
    // Statistics Counts
    // ==========================================

    private final IntegerProperty arrivingCountProperty = new SimpleIntegerProperty(0);
    public IntegerProperty arrivingCountProperty() { return arrivingCountProperty; }
    public int getArrivingCount() { return arrivingCountProperty.get(); }
    public void setArrivingCount(int count) { arrivingCountProperty.set(count); }

    private final IntegerProperty noShowCountProperty = new SimpleIntegerProperty(0);
    public IntegerProperty noShowCountProperty() { return noShowCountProperty; }
    public int getNoShowCount() { return noShowCountProperty.get(); }
    public void setNoShowCount(int count) { noShowCountProperty.set(count); }

    private final IntegerProperty departingCountProperty = new SimpleIntegerProperty(0);
    public IntegerProperty departingCountProperty() { return departingCountProperty; }
    public int getDepartingCount() { return departingCountProperty.get(); }
    public void setDepartingCount(int count) { departingCountProperty.set(count); }

    private final IntegerProperty inHouseCountProperty = new SimpleIntegerProperty(0);
    public IntegerProperty inHouseCountProperty() { return inHouseCountProperty; }
    public int getInHouseCount() { return inHouseCountProperty.get(); }
    public void setInHouseCount(int count) { inHouseCountProperty.set(count); }

    private final IntegerProperty unpaidCountProperty = new SimpleIntegerProperty(0);
    public IntegerProperty unpaidCountProperty() { return unpaidCountProperty; }
    public int getUnpaidCount() { return unpaidCountProperty.get(); }
    public void setUnpaidCount(int count) { unpaidCountProperty.set(count); }

    private final IntegerProperty checkedOutCountProperty = new SimpleIntegerProperty(0);
    public IntegerProperty checkedOutCountProperty() { return checkedOutCountProperty; }
    public int getCheckedOutCount() { return checkedOutCountProperty.get(); }
    public void setCheckedOutCount(int count) { checkedOutCountProperty.set(count); }

    private final IntegerProperty allCountProperty = new SimpleIntegerProperty(0);
    public IntegerProperty allCountProperty() { return allCountProperty; }
    public int getAllCount() { return allCountProperty.get(); }
    public void setAllCount(int count) { allCountProperty.set(count); }

    // ==========================================
    // Guest Data Lists
    // ==========================================

    private final ObservableList<Document> allGuests = FXCollections.observableArrayList();
    public ObservableList<Document> getAllGuests() { return allGuests; }

    private final ObservableList<Document> filteredGuests = FXCollections.observableArrayList();
    public ObservableList<Document> getFilteredGuests() { return filteredGuests; }

    // ==========================================
    // Bulk Selection Mode
    // ==========================================

    private final BooleanProperty bulkModeProperty = new SimpleBooleanProperty(false);
    public BooleanProperty bulkModeProperty() { return bulkModeProperty; }
    public boolean isBulkMode() { return bulkModeProperty.get(); }
    public void setBulkMode(boolean bulkMode) { bulkModeProperty.set(bulkMode); }

    private final ObservableList<Document> selectedGuests = FXCollections.observableArrayList();
    public ObservableList<Document> getSelectedGuests() { return selectedGuests; }

    // ==========================================
    // Current Selection (single guest)
    // ==========================================

    private final ObjectProperty<Document> currentGuestProperty = new SimpleObjectProperty<>(null);
    public ObjectProperty<Document> currentGuestProperty() { return currentGuestProperty; }
    public Document getCurrentGuest() { return currentGuestProperty.get(); }
    public void setCurrentGuest(Document guest) { currentGuestProperty.set(guest); }

    // ==========================================
    // Event List for Filter Dropdown
    // ==========================================

    private final ObservableList<Event> events = FXCollections.observableArrayList();
    public ObservableList<Event> getEvents() { return events; }

    // ==========================================
    // Cash Register State
    // ==========================================

    private final BooleanProperty registerOpenProperty = new SimpleBooleanProperty(true);
    public BooleanProperty registerOpenProperty() { return registerOpenProperty; }
    public boolean isRegisterOpen() { return registerOpenProperty.get(); }
    public void setRegisterOpen(boolean open) { registerOpenProperty.set(open); }

    private final DoubleProperty cashTotalProperty = new SimpleDoubleProperty(0.0);
    public DoubleProperty cashTotalProperty() { return cashTotalProperty; }
    public double getCashTotal() { return cashTotalProperty.get(); }
    public void setCashTotal(double total) { cashTotalProperty.set(total); }

    private final DoubleProperty cardTotalProperty = new SimpleDoubleProperty(0.0);
    public DoubleProperty cardTotalProperty() { return cardTotalProperty; }
    public double getCardTotal() { return cardTotalProperty.get(); }
    public void setCardTotal(double total) { cardTotalProperty.set(total); }

    // ==========================================
    // Fire List Counts
    // ==========================================

    private final IntegerProperty dayGuestCountProperty = new SimpleIntegerProperty(0);
    public IntegerProperty dayGuestCountProperty() { return dayGuestCountProperty; }
    public int getDayGuestCount() { return dayGuestCountProperty.get(); }
    public void setDayGuestCount(int count) { dayGuestCountProperty.set(count); }

    private final IntegerProperty nightGuestCountProperty = new SimpleIntegerProperty(0);
    public IntegerProperty nightGuestCountProperty() { return nightGuestCountProperty; }
    public int getNightGuestCount() { return nightGuestCountProperty.get(); }
    public void setNightGuestCount(int count) { nightGuestCountProperty.set(count); }

    private final IntegerProperty residentCountProperty = new SimpleIntegerProperty(0);
    public IntegerProperty residentCountProperty() { return residentCountProperty; }
    public int getResidentCount() { return residentCountProperty.get(); }
    public void setResidentCount(int count) { residentCountProperty.set(count); }

    // ==========================================
    // Pagination
    // ==========================================

    private final IntegerProperty currentPageProperty = new SimpleIntegerProperty(1);
    public IntegerProperty currentPageProperty() { return currentPageProperty; }
    public int getCurrentPage() { return currentPageProperty.get(); }
    public void setCurrentPage(int page) { currentPageProperty.set(page); }

    private final IntegerProperty pageSizeProperty = new SimpleIntegerProperty(25);
    public IntegerProperty pageSizeProperty() { return pageSizeProperty; }
    public int getPageSize() { return pageSizeProperty.get(); }
    public void setPageSize(int size) { pageSizeProperty.set(size); }

    private final IntegerProperty totalPagesProperty = new SimpleIntegerProperty(1);
    public IntegerProperty totalPagesProperty() { return totalPagesProperty; }
    public int getTotalPages() { return totalPagesProperty.get(); }
    public void setTotalPages(int pages) { totalPagesProperty.set(pages); }

    // ==========================================
    // Loading State
    // ==========================================

    private final BooleanProperty loadingProperty = new SimpleBooleanProperty(false);
    public BooleanProperty loadingProperty() { return loadingProperty; }
    public boolean isLoading() { return loadingProperty.get(); }
    public void setLoading(boolean loading) { loadingProperty.set(loading); }

    // ==========================================
    // FX Bindings Setup
    // ==========================================

    /**
     * Sets up FX bindings for organization-dependent data.
     * Should be called when the activity starts.
     */
    public void doFXBindings() {
        organizationIdProperty().bind(FXOrganizationId.organizationIdProperty());
    }

    // ==========================================
    // Utility Methods
    // ==========================================

    /**
     * Clears all selections and resets to default state.
     */
    public void clearSelection() {
        selectedGuests.clear();
        setCurrentGuest(null);
        setBulkMode(false);
    }

    /**
     * Toggles a guest in the bulk selection list.
     */
    public void toggleGuestSelection(Document guest) {
        if (selectedGuests.contains(guest)) {
            selectedGuests.remove(guest);
        } else {
            selectedGuests.add(guest);
        }
    }

    /**
     * Selects all guests in the current filtered list.
     */
    public void selectAllGuests() {
        selectedGuests.setAll(filteredGuests);
    }

    /**
     * Deselects all guests.
     */
    public void deselectAllGuests() {
        selectedGuests.clear();
    }

    /**
     * Gets the count for the currently selected tab.
     */
    public int getCountForTab(Tab tab) {
        switch (tab) {
            case ARRIVING: return getArrivingCount();
            case NO_SHOWS: return getNoShowCount();
            case DEPARTING: return getDepartingCount();
            case IN_HOUSE: return getInHouseCount();
            case UNPAID: return getUnpaidCount();
            case CHECKED_OUT: return getCheckedOutCount();
            case ALL: return getAllCount();
            default: return 0;
        }
    }
}
