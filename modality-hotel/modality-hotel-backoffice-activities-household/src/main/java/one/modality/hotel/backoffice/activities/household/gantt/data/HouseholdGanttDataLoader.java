package one.modality.hotel.backoffice.activities.household.gantt.data;

import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttPresenter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Data loader for the Household Gantt view.
 * Loads rooms and bookings from the database using reactive queries.
 *
 * This class coordinates between:
 * - ResourceConfiguration (room data)
 * - Attendance + DocumentLine (booking data)
 * - The presentation model (organization filter)
 * - The gantt presenter (time window)
 */
public final class HouseholdGanttDataLoader {

    /**
     * Site ID filter for resource queries.
     * Currently hardcoded to filter for a specific site within the organization.
     * TODO: Make this configurable via AccommodationPresentationModel if multi-site support is needed.
     * Set to null to disable site filtering and show all sites in the organization.
     */
    private static final Integer SITE_ID_FILTER = 1671;

    private final AccommodationPresentationModel pm;
    private final ObservableList<ResourceConfiguration> resourceConfigurations = FXCollections.observableArrayList();
    private final ObservableList<Attendance> attendances = FXCollections.observableArrayList();
    private Object mixin; // Keep reference for reloading

    /**
     * Constructor
     *
     * @param pm The presentation model (provides organization context and time window)
     * @param ganttPresenter The gantt presenter (unused, kept for compatibility)
     */
    public HouseholdGanttDataLoader(AccommodationPresentationModel pm, GanttPresenter ganttPresenter) {
        this.pm = pm;
        // Note: ganttPresenter parameter kept for compatibility but not stored
        // We use pm.timeWindowStartProperty() and pm.timeWindowEndProperty() instead
    }

    /**
     * Gets the observable list of resource configurations (rooms).
     */
    public ObservableList<ResourceConfiguration> getResourceConfigurations() {
        return resourceConfigurations;
    }

    /**
     * Gets the observable list of attendances (bookings).
     */
    public ObservableList<Attendance> getAttendances() {
        return attendances;
    }

    /**
     * Starts the reactive data loading logic.
     * Must be called after construction to begin loading data.
     *
     * @param mixin The mixin object for reactive chain lifecycle
     */
    public void startLogic(Object mixin) {
        this.mixin = mixin;
        startRoomQuery();
        startAttendanceQuery();
    }

    /**
     * Reloads data for the current time window.
     * Called when the user navigates the gantt view.
     */
    public void reload() {
        if (mixin != null) {
            // Rooms don't change, so we only need to reload attendances
            startAttendanceQuery();
        }
    }

    /**
     * Starts the query to load rooms.
     */
    private void startRoomQuery() {
        // Query 1: Load all ResourceConfigurations (room configurations) for this organization
        // ResourceConfiguration has: resource (room), item (room type), max (bed count)
        ReactiveEntitiesMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
            .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,resource.name,item.(name,family.(name,ord)),max', orderBy: 'item.family.ord,item.name,name'}")
            // Filter by organization and optional site ID
            // Only get current configurations (no end date or future configs)
            .always(pm.organizationIdProperty(), org -> {
                if (SITE_ID_FILTER != null) {
                    return where("resource.site.organization=? and resource.site=? and (endDate is null or endDate >= current_date())", org, SITE_ID_FILTER);
                } else {
                    return where("resource.site.organization=? and (endDate is null or endDate >= current_date())", org);
                }
            })
            .storeEntitiesInto(resourceConfigurations)
            .start();
    }

    /**
     * Starts the query to load attendances for the current time window.
     * This query is REACTIVE to time window changes via PM's time window properties.
     */
    private void startAttendanceQuery() {
        // Query 2: Load bookings (attendances) for the time window
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
            // Fetch attendance data with document line, guest info, and room assignment
            // Use documentLine.resourceConfiguration (old data) and scheduledResource.configuration (new data)
            .always("{class: 'Attendance', alias: 'a', " +
                "fields: 'date," +
                "documentLine.document.(arrived,person_firstName,person_lastName,event.name,request)," +
                "documentLine.(cleaned,resourceConfiguration.(name,item.(name,family.name)))," +
                "scheduledResource.configuration.(name,item.(name,family.name))', " +
                "where: 'documentLine.(!cancelled and !document.cancelled)'}")
            // Filter by organization
            .always(pm.organizationIdProperty(), org ->
                where("documentLine.document.event.organization=?", org))
            // Filter by date range - REACTIVE to BOTH time window properties!
            // When either start or end changes, this will recompute
            .always(pm.timeWindowStartProperty(), start -> {
                LocalDate end = pm.getTimeWindowEnd();

                if (start == null || end == null) {
                    return null;
                }

                LocalDate queryStart = start.minus(1, ChronoUnit.DAYS);
                LocalDate queryEnd = end.plus(1, ChronoUnit.DAYS);
                return where("a.date >= ? and a.date <= ?", queryStart, queryEnd);
            })
            .storeEntitiesInto(attendances)
            .start();
    }
}
