package one.modality.hotel.backoffice.activities.reception.data;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.hotel.backoffice.activities.reception.ReceptionPresentationModel;
import one.modality.hotel.backoffice.activities.reception.ReceptionPresentationModel.Tab;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Optimized data loader for the Reception Dashboard.
 *
 * Performance optimizations:
 * 1. TAB-BASED LAZY LOADING - Only loads data relevant to the active tab
 * 2. SERVER-SIDE FILTERING - Pushes all filtering to database queries
 * 3. TODAY-FOCUSED QUERIES - Check-in/checkout only loads today's data
 * 4. MINIMAL FIELD LOADING - Only fetches fields needed for list view
 * 5. CACHED LOOKUPS - Reuses computed data across tab switches
 * 6. CONSOLIDATED QUERIES - Related data loaded in batches
 *
 * @author David Hello
 * @author Claude Code
 */
public final class ReceptionDataLoader {

    // Minimal fields for list view - detail fields loaded on demand
    private static final String DOCUMENT_LIST_FIELDS =
            "ref,person_firstName,person_lastName,person_email,person_phone," +
            "event.(id,name),confirmed,arrived,cancelled,checkedOut," +
            "price_net,price_deposit,flagged";

    // Fields for document lines (room info)
    private static final String DOCUMENT_LINE_FIELDS =
            "document,startDate,endDate,cancelled," +
            "resourceConfiguration.(name,resource.name)";

    private final ReceptionPresentationModel pm;
    private Object mixin;

    // Current tab's data - changes when tab switches
    private final ObservableList<Document> currentTabDocuments = FXCollections.observableArrayList();

    // All tabs' data - loaded upfront
    private final ObservableList<Document> arrivingDocuments = FXCollections.observableArrayList();
    private final ObservableList<Document> departingDocuments = FXCollections.observableArrayList();
    private final ObservableList<Document> inHouseDocuments = FXCollections.observableArrayList();
    private final ObservableList<Document> noShowsDocuments = FXCollections.observableArrayList();
    private final ObservableList<Document> checkedOutDocuments = FXCollections.observableArrayList();
    private final ObservableList<Document> unpaidDocuments = FXCollections.observableArrayList();
    private final ObservableList<Document> allDocuments = FXCollections.observableArrayList();

    // Track loading completion
    private int pendingLoads = 0;

    // Cached lookup data (persists across tab switches for performance)
    private final Map<Object, LocalDate> documentArrivalDates = new HashMap<>();
    private final Map<Object, LocalDate> documentDepartureDates = new HashMap<>();
    private final Map<Object, String> documentRoomNames = new HashMap<>();
    private final Map<Object, String> documentRoomTypes = new HashMap<>();
    private final Map<Object, Integer> documentPaidAmounts = new HashMap<>();

    // Document lines for current view
    private final ObservableList<DocumentLine> documentLines = FXCollections.observableArrayList();

    // Today's transfers for cash register (always loaded - small dataset)
    private final ObservableList<MoneyTransfer> todayTransfers = FXCollections.observableArrayList();

    // Track which tab data is currently loaded
    private final ObjectProperty<Tab> loadedTab = new SimpleObjectProperty<>(null);
    private LocalDate loadedDate = null;

    public ReceptionDataLoader(ReceptionPresentationModel pm) {
        this.pm = pm;
    }

    /**
     * Gets the observable list of documents for the current tab.
     */
    public ObservableList<Document> getDocuments() {
        return currentTabDocuments;
    }

    /**
     * Gets the arrival date for a document (from cached lookup).
     */
    public LocalDate getArrivalDate(Document doc) {
        return documentArrivalDates.get(doc.getId().getPrimaryKey());
    }

    /**
     * Gets the departure date for a document (from cached lookup).
     */
    public LocalDate getDepartureDate(Document doc) {
        return documentDepartureDates.get(doc.getId().getPrimaryKey());
    }

    /**
     * Gets the room name for a document (from cached lookup).
     */
    public String getRoomName(Document doc) {
        return documentRoomNames.get(doc.getId().getPrimaryKey());
    }

    /**
     * Gets the room type for a document (from cached lookup).
     */
    public String getRoomType(Document doc) {
        return documentRoomTypes.get(doc.getId().getPrimaryKey());
    }

    /**
     * Gets the total paid amount for a document (from cached lookup).
     */
    public Integer getPaidAmount(Document doc) {
        return documentPaidAmounts.getOrDefault(doc.getId().getPrimaryKey(), 0);
    }

    /**
     * Derives the guest status based on document state and dates.
     * Uses the checkedOut field for proper checkout tracking.
     */
    public String deriveStatus(Document doc) {
        LocalDate today = FXToday.getToday();
        LocalDate arrivalDate = getArrivalDate(doc);
        LocalDate departureDate = getDepartureDate(doc);

        if (Boolean.TRUE.equals(doc.isCancelled())) {
            return "cancelled";
        }

        // Check if guest has checked out (using the checkedOut field)
        if (Boolean.TRUE.equals(doc.getCheckedOut())) {
            return "checked-out";
        }

        if (Boolean.TRUE.equals(doc.isArrived())) {
            // Guest is checked in and departure date is today
            if (departureDate != null && departureDate.equals(today)) {
                return "departing";
            }
            // Guest is in-house
            return "checked-in";
        }

        if (arrivalDate != null && arrivalDate.isBefore(today)) {
            return "no-show";
        }

        if (!Boolean.TRUE.equals(doc.isConfirmed())) {
            return "pre-booked";
        }

        if (arrivalDate != null && arrivalDate.equals(today)) {
            return "arriving";
        }

        return "expected";
    }

    /**
     * Starts the reactive data loading logic.
     * Loads all data upfront (no lazy loading) with loading indicator.
     */
    public void startLogic(Object mixin) {
        this.mixin = mixin;
        Console.log("[ReceptionDataLoader] startLogic called - loading all data upfront");

        // Set loading state
        pm.setLoading(true);

        // Load static/small datasets immediately
        startTodayTransfersQuery();

        // Load ALL tab data upfront (no lazy loading)
        loadAllTabsData();

        // Set up tab switching (just switches display, no data reload)
        setupTabSwitching();

        // Set up computed data bindings
        setupComputedDataBindings();

        // Load stats (lightweight server-side counts)
        loadStats();
    }

    /**
     * Loads all tabs' data upfront.
     */
    private void loadAllTabsData() {
        LocalDate today = FXToday.getToday();
        Console.log("[ReceptionDataLoader] Loading all tabs data upfront");

        // Track how many loads are pending
        pendingLoads = 7; // 7 tabs to load

        // Load data for each tab into their respective lists
        loadArrivingDataUpfront(today);
        loadDepartingDataUpfront(today);
        loadInHouseDataUpfront(today);
        loadNoShowsDataUpfront(today);
        loadCheckedOutDataUpfront(today);
        loadUnpaidDataUpfront(today);
        loadAllDataUpfront(today);

        // Load document lines for all data
        loadAllDocumentLines(today);

        // Safety timeout: hide spinner after 5 seconds even if not all callbacks fired
        // (handles case where some lists return empty and don't trigger change events)
        javafx.animation.PauseTransition timeout = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        timeout.setOnFinished(e -> {
            if (pm.isLoading()) {
                Console.log("[ReceptionDataLoader] Timeout reached, hiding spinner");
                pm.setLoading(false);
                switchToTabData(pm.getSelectedTab());
            }
        });
        timeout.play();
    }

    /**
     * Sets up tab switching - just switches display, no data reload needed.
     */
    private void setupTabSwitching() {
        // React to tab changes - switch displayed data
        pm.selectedTabProperty().addListener((obs, oldTab, newTab) -> {
            Console.log("[ReceptionDataLoader] Tab switched to: " + newTab);
            switchToTabData(newTab);
        });

        // Initialize with current tab's data
        switchToTabData(pm.getSelectedTab());

        // React to search text changes (filter existing data)
        pm.searchTextProperty().addListener((obs, oldVal, newVal) -> {
            updateFilteredGuests();
        });

        // React to event filter changes (requires data reload)
        pm.eventFilterProperty().addListener((obs, oldVal, newVal) -> {
            // Reload all data when event filter changes
            loadAllTabsData();
        });
    }

    /**
     * Switches the current display to show data for the specified tab.
     */
    private void switchToTabData(Tab tab) {
        ObservableList<Document> sourceList;
        switch (tab) {
            case ARRIVING:
                sourceList = arrivingDocuments;
                break;
            case DEPARTING:
                sourceList = departingDocuments;
                break;
            case IN_HOUSE:
                sourceList = inHouseDocuments;
                break;
            case NO_SHOWS:
                sourceList = noShowsDocuments;
                break;
            case CHECKED_OUT:
                sourceList = checkedOutDocuments;
                break;
            case UNPAID:
                sourceList = unpaidDocuments;
                break;
            case ALL:
            default:
                sourceList = allDocuments;
                break;
        }
        currentTabDocuments.setAll(sourceList);
        updateFilteredGuests();
    }

    /**
     * Called when a tab's data finishes loading.
     */
    private void onTabDataLoaded() {
        pendingLoads--;
        if (pendingLoads <= 0) {
            Console.log("[ReceptionDataLoader] All data loaded, hiding spinner");
            pm.setLoading(false);
            // Initialize display with current tab
            switchToTabData(pm.getSelectedTab());
        }
    }

    /**
     * Loads data specific to the selected tab.
     * Each tab has optimized queries that only fetch relevant documents.
     */
    private void loadTabData(Tab tab, LocalDate today) {
        Object orgId = pm.organizationIdProperty().get();
        if (orgId == null) {
            Console.log("[ReceptionDataLoader] No organization ID, skipping load");
            return;
        }

        Console.log("[ReceptionDataLoader] Loading data for tab: " + tab);

        switch (tab) {
            case ARRIVING:
                loadArrivingData(today);
                break;
            case DEPARTING:
                loadDepartingData(today);
                break;
            case IN_HOUSE:
                loadInHouseData(today);
                break;
            case NO_SHOWS:
                loadNoShowsData(today);
                break;
            case CHECKED_OUT:
                loadCheckedOutData(today);
                break;
            case UNPAID:
                loadUnpaidData(today);
                break;
            case ALL:
            default:
                loadAllData(today);
                break;
        }
    }

    /**
     * ARRIVING tab: Today's expected arrivals only.
     * Query: startDate = today AND !arrived AND !cancelled
     */
    private void loadArrivingData(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // Today's arrivals only - much smaller dataset
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and !d.arrived and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.startDate=? and !dl.cancelled)", t))
                .storeEntitiesInto(currentTabDocuments)
                .setResultCacheEntry("modality/hotel/reception/arriving")
                .start();

        loadDocumentLinesForToday(today, true); // arrival lines
    }

    /**
     * DEPARTING tab: Today's expected departures who haven't checked out yet.
     * Query: endDate = today AND arrived AND !cancelled AND !checkedOut
     */
    private void loadDepartingData(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // Today's departures who haven't checked out yet
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and d.arrived and !d.checkedOut and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.endDate=? and !dl.cancelled)", t))
                .storeEntitiesInto(currentTabDocuments)
                .setResultCacheEntry("modality/hotel/reception/departing")
                .start();

        loadDocumentLinesForToday(today, false); // departure lines
    }

    /**
     * IN_HOUSE tab: Currently checked-in guests (not yet checked out).
     * Query: arrived AND !checkedOut AND startDate <= today AND endDate >= today
     */
    private void loadInHouseData(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // Currently in house (not checked out)
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and d.arrived and !d.checkedOut and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.startDate<=? and dl.endDate>=? and !dl.cancelled)", t, t))
                .storeEntitiesInto(currentTabDocuments)
                .setResultCacheEntry("modality/hotel/reception/inhouse")
                .start();

        loadDocumentLinesForInHouse(today);
    }

    /**
     * NO_SHOWS tab: Guests who didn't arrive yesterday (or earlier).
     * Query: startDate < today AND !arrived AND !cancelled
     */
    private void loadNoShowsData(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // No-shows: expected in past but not arrived (limit to last 7 days)
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and !d.arrived and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.startDate<? and dl.startDate>=? and !dl.cancelled)",
                              t, t.minusDays(7)))
                .storeEntitiesInto(currentTabDocuments)
                .setResultCacheEntry("modality/hotel/reception/noshows")
                .start();

        loadDocumentLinesForNoShows(today);
    }

    /**
     * CHECKED_OUT tab: Guests who have been checked out (today or yesterday).
     * Query: checkedOut AND has room assignment (onsite only) AND recent departure
     */
    private void loadCheckedOutData(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // Guests who have checked out, onsite only (with room assignment), recent departures
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and d.arrived and d.checkedOut and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.endDate>=? and dl.endDate<=? and !dl.cancelled " +
                              "and dl.resourceConfiguration is not null)",
                              t.minusDays(1), t))
                .storeEntitiesInto(currentTabDocuments)
                .setResultCacheEntry("modality/hotel/reception/checkedout")
                .start();

        loadDocumentLinesForCheckedOut(today);
    }

    /**
     * UNPAID tab: Guests currently in-house (not checked out) with outstanding balance.
     * Query: arrived AND !checkedOut AND currently in-house AND price_net > SUM(payments)
     */
    private void loadUnpaidData(LocalDate today) {
        // For unpaid, we need to load guests with balance > 0
        // Only show guests who are currently onsite (arrived, not checked out, stay spans today)
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // Guests currently in-house (not checked out) with potential balance
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and d.arrived and !d.checkedOut and d.price_net>0 and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.startDate<=? and dl.endDate>=? and !dl.cancelled)", t, t))
                .storeEntitiesInto(currentTabDocuments)
                .setResultCacheEntry("modality/hotel/reception/unpaid")
                .start();

        loadMoneyTransfersForDocuments();
        loadDocumentLinesForUnpaid(today);
    }

    /**
     * ALL tab: All active guests (with pagination support).
     * Query: !cancelled AND within reasonable date range
     */
    private void loadAllData(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // All non-cancelled within date range
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.startDate<=? and dl.endDate>=? and !dl.cancelled)",
                              t.plusDays(7), t.minusDays(1)))
                .storeEntitiesInto(currentTabDocuments)
                .setResultCacheEntry("modality/hotel/reception/all")
                .start();

        loadDocumentLinesForAll(today);
    }

    // ==========================================
    // Upfront Loading Methods (load all data at startup)
    // ==========================================

    private void loadArrivingDataUpfront(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and !d.arrived and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.startDate=? and !dl.cancelled)", t))
                .storeEntitiesInto(arrivingDocuments)
                .setResultCacheEntry("modality/hotel/reception/arriving-upfront")
                .start();

        ObservableLists.runOnListChange(c -> onTabDataLoaded(), arrivingDocuments);
    }

    private void loadDepartingDataUpfront(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // Today's departures who haven't checked out yet
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and d.arrived and !d.checkedOut and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.endDate=? and !dl.cancelled)", t))
                .storeEntitiesInto(departingDocuments)
                .setResultCacheEntry("modality/hotel/reception/departing-upfront")
                .start();

        ObservableLists.runOnListChange(c -> onTabDataLoaded(), departingDocuments);
    }

    private void loadInHouseDataUpfront(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // Currently in house (not checked out)
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and d.arrived and !d.checkedOut and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.startDate<=? and dl.endDate>=? and !dl.cancelled)", t, t))
                .storeEntitiesInto(inHouseDocuments)
                .setResultCacheEntry("modality/hotel/reception/inhouse-upfront")
                .start();

        ObservableLists.runOnListChange(c -> onTabDataLoaded(), inHouseDocuments);
    }

    private void loadNoShowsDataUpfront(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and !d.arrived and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.startDate<? and dl.startDate>=? and !dl.cancelled)",
                              t, t.minusDays(7)))
                .storeEntitiesInto(noShowsDocuments)
                .setResultCacheEntry("modality/hotel/reception/noshows-upfront")
                .start();

        ObservableLists.runOnListChange(c -> onTabDataLoaded(), noShowsDocuments);
    }

    private void loadCheckedOutDataUpfront(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // Guests who have checked out, onsite only (with room assignment), recent departures
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and d.arrived and d.checkedOut and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.endDate>=? and dl.endDate<=? and !dl.cancelled " +
                              "and dl.resourceConfiguration is not null)",
                              t.minusDays(1), t))
                .storeEntitiesInto(checkedOutDocuments)
                .setResultCacheEntry("modality/hotel/reception/checkedout-upfront")
                .start();

        ObservableLists.runOnListChange(c -> onTabDataLoaded(), checkedOutDocuments);
    }

    private void loadUnpaidDataUpfront(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                // Guests currently in-house (not checked out) with potential balance
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and d.arrived and !d.checkedOut and d.price_net>0 and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.startDate<=? and dl.endDate>=? and !dl.cancelled)", t, t))
                .storeEntitiesInto(unpaidDocuments)
                .setResultCacheEntry("modality/hotel/reception/unpaid-upfront")
                .start();

        loadMoneyTransfersForDocuments();
        ObservableLists.runOnListChange(c -> onTabDataLoaded(), unpaidDocuments);
    }

    private void loadAllDataUpfront(LocalDate today) {
        ReactiveEntitiesMapper.<Document>createPushReactiveChain(mixin)
                .always("{class: 'Document', alias: 'd', fields: '" + DOCUMENT_LIST_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("d.event.organization=?", org))
                .ifNotNull(pm.eventFilterProperty(), eventFilter -> {
                    if (eventFilter instanceof one.modality.base.shared.entities.Event) {
                        return where("d.event=?", eventFilter);
                    }
                    return null;
                })
                .always(FXToday.todayProperty(), t ->
                        where("!d.cancelled and exists(" +
                              "select 1 from DocumentLine dl where dl.document=d " +
                              "and dl.startDate<=? and dl.endDate>=? and !dl.cancelled)",
                              t.plusDays(7), t.minusDays(1)))
                .storeEntitiesInto(allDocuments)
                .setResultCacheEntry("modality/hotel/reception/all-upfront")
                .start();

        ObservableLists.runOnListChange(c -> onTabDataLoaded(), allDocuments);
    }

    /**
     * Loads document lines for all active documents (comprehensive query).
     */
    private void loadAllDocumentLines(LocalDate today) {
        ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(mixin)
                .always("{class: 'DocumentLine', alias: 'dl', fields: '" + DOCUMENT_LINE_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("dl.document.event.organization=?", org))
                .always(FXToday.todayProperty(), t ->
                        where("dl.startDate<=? and dl.endDate>=? and !dl.cancelled " +
                              "and !dl.document.cancelled and dl.resourceConfiguration is not null",
                              t.plusDays(7), t.minusDays(7)))
                .storeEntitiesInto(documentLines)
                .setResultCacheEntry("modality/hotel/reception/lines-all-upfront")
                .start();
    }

    // ==========================================
    // Document Lines Loaders (optimized per tab)
    // ==========================================

    private void loadDocumentLinesForToday(LocalDate today, boolean isArrival) {
        ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(mixin)
                .always("{class: 'DocumentLine', alias: 'dl', fields: '" + DOCUMENT_LINE_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("dl.document.event.organization=?", org))
                .always(FXToday.todayProperty(), t -> {
                    String dateField = isArrival ? "dl.startDate" : "dl.endDate";
                    return where(dateField + "=? and !dl.cancelled and !dl.document.cancelled " +
                                "and dl.resourceConfiguration is not null", t);
                })
                .storeEntitiesInto(documentLines)
                .setResultCacheEntry("modality/hotel/reception/lines-" + (isArrival ? "arriving" : "departing"))
                .start();
    }

    private void loadDocumentLinesForInHouse(LocalDate today) {
        ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(mixin)
                .always("{class: 'DocumentLine', alias: 'dl', fields: '" + DOCUMENT_LINE_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("dl.document.event.organization=?", org))
                .always(FXToday.todayProperty(), t ->
                        where("dl.startDate<=? and dl.endDate>=? and !dl.cancelled " +
                              "and !dl.document.cancelled and dl.document.arrived and !dl.document.checkedOut " +
                              "and dl.resourceConfiguration is not null", t, t))
                .storeEntitiesInto(documentLines)
                .setResultCacheEntry("modality/hotel/reception/lines-inhouse")
                .start();
    }

    private void loadDocumentLinesForNoShows(LocalDate today) {
        ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(mixin)
                .always("{class: 'DocumentLine', alias: 'dl', fields: '" + DOCUMENT_LINE_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("dl.document.event.organization=?", org))
                .always(FXToday.todayProperty(), t ->
                        where("dl.startDate<? and dl.startDate>=? and !dl.cancelled " +
                              "and !dl.document.cancelled and !dl.document.arrived " +
                              "and dl.resourceConfiguration is not null", t, t.minusDays(7)))
                .storeEntitiesInto(documentLines)
                .setResultCacheEntry("modality/hotel/reception/lines-noshows")
                .start();
    }

    private void loadDocumentLinesForCheckedOut(LocalDate today) {
        ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(mixin)
                .always("{class: 'DocumentLine', alias: 'dl', fields: '" + DOCUMENT_LINE_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("dl.document.event.organization=?", org))
                .always(FXToday.todayProperty(), t ->
                        where("dl.endDate>=? and dl.endDate<=? and !dl.cancelled " +
                              "and !dl.document.cancelled and dl.document.arrived and dl.document.checkedOut " +
                              "and dl.resourceConfiguration is not null", t.minusDays(1), t))
                .storeEntitiesInto(documentLines)
                .setResultCacheEntry("modality/hotel/reception/lines-checkedout")
                .start();
    }

    private void loadDocumentLinesForUnpaid(LocalDate today) {
        ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(mixin)
                .always("{class: 'DocumentLine', alias: 'dl', fields: '" + DOCUMENT_LINE_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("dl.document.event.organization=?", org))
                .always(FXToday.todayProperty(), t ->
                        where("dl.startDate<=? and dl.endDate>=? and !dl.cancelled " +
                              "and !dl.document.cancelled and dl.document.arrived and !dl.document.checkedOut " +
                              "and dl.resourceConfiguration is not null", t, t))
                .storeEntitiesInto(documentLines)
                .setResultCacheEntry("modality/hotel/reception/lines-unpaid")
                .start();
    }

    private void loadDocumentLinesForAll(LocalDate today) {
        ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(mixin)
                .always("{class: 'DocumentLine', alias: 'dl', fields: '" + DOCUMENT_LINE_FIELDS + "'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("dl.document.event.organization=?", org))
                .always(FXToday.todayProperty(), t ->
                        where("dl.startDate<=? and dl.endDate>=? and !dl.cancelled " +
                              "and !dl.document.cancelled and dl.resourceConfiguration is not null",
                              t.plusDays(7), t.minusDays(1)))
                .storeEntitiesInto(documentLines)
                .setResultCacheEntry("modality/hotel/reception/lines-all")
                .start();
    }

    /**
     * Loads money transfers for current documents (lazy - only for UNPAID tab).
     * Only loads transfers for guests currently in-house.
     */
    private void loadMoneyTransfersForDocuments() {
        ObservableList<MoneyTransfer> transfers = FXCollections.observableArrayList();

        ReactiveEntitiesMapper.<MoneyTransfer>createPushReactiveChain(mixin)
                .always("{class: 'MoneyTransfer', alias: 'mt', fields: 'document,amount'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("mt.document.event.organization=?", org))
                .always(FXToday.todayProperty(), t ->
                        where("mt.document.arrived and exists(select 1 from DocumentLine dl where dl.document=mt.document " +
                              "and dl.startDate<=? and dl.endDate>=? and !dl.cancelled)", t, t))
                .storeEntitiesInto(transfers)
                .setResultCacheEntry("modality/hotel/reception/transfers-unpaid")
                .start();

        // Update payment cache when transfers load
        ObservableLists.runOnListChange(c -> {
            documentPaidAmounts.clear();
            for (MoneyTransfer mt : transfers) {
                if (mt.getDocument() != null) {
                    Object docKey = mt.getDocument().getId().getPrimaryKey();
                    Integer amount = mt.getAmount();
                    if (amount != null) {
                        documentPaidAmounts.merge(docKey, amount, Integer::sum);
                    }
                }
            }
            updateFilteredGuests();
        }, transfers);
    }

    // ==========================================
    // Static Data Loaders (small datasets, loaded once)
    // ==========================================

    /**
     * Loads today's money transfers for cash register display.
     * Small dataset - always loaded.
     */
    private void startTodayTransfersQuery() {
        ReactiveEntitiesMapper.<MoneyTransfer>createPushReactiveChain(mixin)
                .always("{class: 'MoneyTransfer', alias: 'mt', fields: 'document,amount,method,date'}")
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org ->
                        where("mt.document.event.organization=?", org))
                .always(FXToday.todayProperty(), today ->
                        where("mt.date>=? and mt.date<?", today.atStartOfDay(), today.plusDays(1).atStartOfDay()))
                .storeEntitiesInto(todayTransfers)
                .setResultCacheEntry("modality/hotel/reception/today-transfers")
                .start();
    }

    /**
     * Loads stats counts using optimized server-side queries.
     */
    private void loadStats() {
        LocalDate today = FXToday.getToday();

        // Create reactive stats queries that update when data changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            updateStatsFromCurrentData();
        }, pm.selectedTabProperty());

        // Also update when documents change
        ObservableLists.runOnListChange(c -> updateStatsFromCurrentData(), currentTabDocuments);
    }

    // ==========================================
    // Computed Data Bindings
    // ==========================================

    private void setupComputedDataBindings() {
        // Recompute document line data when lines change
        ObservableLists.runOnListChange(c -> recomputeDocumentLineData(), documentLines);

        // Update filtered guests when documents change
        ObservableLists.runOnListChange(c -> updateFilteredGuests(), currentTabDocuments);

        // Update cash register when today's transfers change
        ObservableLists.runOnListChange(c -> recomputeCashRegisterData(), todayTransfers);
    }

    private void recomputeDocumentLineData() {
        // Only clear and rebuild if we have new data
        Map<Object, LocalDate> newArrivals = new HashMap<>();
        Map<Object, LocalDate> newDepartures = new HashMap<>();
        Map<Object, String> newRoomNames = new HashMap<>();
        Map<Object, String> newRoomTypes = new HashMap<>();

        for (DocumentLine dl : documentLines) {
            if (dl.getDocument() == null) continue;
            Object docKey = dl.getDocument().getId().getPrimaryKey();

            LocalDate startDate = dl.getStartDate();
            LocalDate endDate = dl.getEndDate();

            // Use earliest start / latest end
            LocalDate existingStart = newArrivals.get(docKey);
            if (existingStart == null || (startDate != null && startDate.isBefore(existingStart))) {
                newArrivals.put(docKey, startDate);
            }

            LocalDate existingEnd = newDepartures.get(docKey);
            if (existingEnd == null || (endDate != null && endDate.isAfter(existingEnd))) {
                newDepartures.put(docKey, endDate);
            }

            if (dl.getResourceConfiguration() != null) {
                String roomType = dl.getResourceConfiguration().getName();
                if (roomType != null) {
                    newRoomTypes.put(docKey, roomType);
                }
                if (dl.getResourceConfiguration().getResource() != null) {
                    String roomName = dl.getResourceConfiguration().getResource().getName();
                    if (roomName != null) {
                        newRoomNames.put(docKey, roomName);
                    }
                }
            }
        }

        // Merge into existing cache (preserves data from other tabs)
        documentArrivalDates.putAll(newArrivals);
        documentDepartureDates.putAll(newDepartures);
        documentRoomNames.putAll(newRoomNames);
        documentRoomTypes.putAll(newRoomTypes);

        updateFilteredGuests();
    }

    private void updateFilteredGuests() {
        String searchText = pm.getSearchText() != null ? pm.getSearchText().toLowerCase().trim() : "";
        Tab currentTab = pm.getSelectedTab();

        pm.getFilteredGuests().clear();

        for (Document doc : currentTabDocuments) {
            // Apply search filter
            if (!searchText.isEmpty()) {
                String firstName = doc.getStringFieldValue("person_firstName");
                String lastName = doc.getStringFieldValue("person_lastName");
                String email = doc.getStringFieldValue("person_email");
                String fullName = ((firstName != null ? firstName : "") + " " +
                                   (lastName != null ? lastName : "")).toLowerCase();

                boolean matches = fullName.contains(searchText) ||
                        (email != null && email.toLowerCase().contains(searchText));

                if (!matches) continue;
            }

            // For UNPAID tab, apply balance filter
            if (currentTab == Tab.UNPAID) {
                Integer priceNet = doc.getPriceNet();
                Integer paidAmount = getPaidAmount(doc);
                int balance = (priceNet != null ? priceNet : 0) - paidAmount;
                if (balance <= 0) continue;
            }

            pm.getFilteredGuests().add(doc);
        }

        // Update all guests list
        pm.getAllGuests().setAll(currentTabDocuments.filtered(doc ->
                !Boolean.TRUE.equals(doc.isCancelled())));
    }

    private void updateStatsFromCurrentData() {
        LocalDate today = FXToday.getToday();

        int arriving = 0, noShows = 0, departing = 0, inHouse = 0, unpaid = 0, checkedOut = 0, all = 0;

        for (Document doc : currentTabDocuments) {
            if (Boolean.TRUE.equals(doc.isCancelled())) continue;

            String status = deriveStatus(doc);
            LocalDate arrivalDate = getArrivalDate(doc);
            LocalDate departureDate = getDepartureDate(doc);
            Integer priceNet = doc.getPriceNet();
            Integer paidAmount = getPaidAmount(doc);
            int balance = (priceNet != null ? priceNet : 0) - paidAmount;

            all++;

            if ("arriving".equals(status) || "expected".equals(status)) {
                if (arrivalDate != null && arrivalDate.equals(today)) {
                    arriving++;
                }
            }
            if ("no-show".equals(status)) {
                noShows++;
            }
            if ("departing".equals(status) ||
                    ("checked-in".equals(status) && departureDate != null && departureDate.equals(today))) {
                departing++;
            }
            if ("checked-in".equals(status) || "departing".equals(status)) {
                inHouse++;
            }
            if (balance > 0) {
                unpaid++;
            }
            if ("checked-out".equals(status) && departureDate != null &&
                    (departureDate.equals(today) || departureDate.equals(today.minusDays(1)))) {
                checkedOut++;
            }
        }

        pm.setArrivingCount(arriving);
        pm.setNoShowCount(noShows);
        pm.setDepartingCount(departing);
        pm.setInHouseCount(inHouse);
        pm.setUnpaidCount(unpaid);
        pm.setCheckedOutCount(checkedOut);
        pm.setAllCount(all);
    }

    private void recomputeCashRegisterData() {
        double cashTotal = 0.0;
        double cardTotal = 0.0;

        for (MoneyTransfer mt : todayTransfers) {
            Integer amount = mt.getAmount();
            if (amount == null) continue;

            Object method = mt.getMethod();
            String methodName = method != null ? method.toString().toLowerCase() : "";

            if (methodName.contains("cash")) {
                cashTotal += amount;
            } else {
                cardTotal += amount;
            }
        }

        pm.setCashTotal(cashTotal);
        pm.setCardTotal(cardTotal);
    }

    /**
     * Clears all cached data (call when switching organizations).
     */
    public void clearCache() {
        documentArrivalDates.clear();
        documentDepartureDates.clear();
        documentRoomNames.clear();
        documentRoomTypes.clear();
        documentPaidAmounts.clear();
        loadedTab.set(null);
        loadedDate = null;
    }
}
