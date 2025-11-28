package one.modality.hotel.backoffice.activities.household.gantt.data;

import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;

import java.time.LocalDate;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Data loader for the Household Gantt view.
 * Loads rooms and bookings from the database using reactive queries.
 * <p>
 * PERFORMANCE OPTIMIZATION: Uses DocumentLine.startDate/endDate fields directly
 * instead of querying the Attendance table for most bookings. This significantly
 * reduces the number of records returned (one per booking vs one per day of stay).
 * <p>
 * ATTENDANCE GAP SUPPORT: For bookings with hasAttendanceGap=true, we also load
 * the individual Attendance records to determine the actual date segments
 * (nights when the guest stays vs gaps when they don't).
 * <p>
 * This class coordinates between:
 * - ResourceConfiguration (room data)
 * - DocumentLine with startDate/endDate (booking data)
 * - Attendance records (for gap bookings only)
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
    private final ObservableList<DocumentLine> documentLines = FXCollections.observableArrayList();
    private final ObservableList<Attendance> attendancesForGaps = FXCollections.observableArrayList();
    private Object mixin; // Keep reference for reloading

    /**
     * Constructor
     *
     * @param pm The presentation model (provides organization context and time window)
     */
    public HouseholdGanttDataLoader(AccommodationPresentationModel pm) {
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
     * Gets the observable list of document lines (bookings with start/end dates).
     */
    public ObservableList<DocumentLine> getDocumentLines() {
        return documentLines;
    }

    /**
     * Gets the observable list of attendances for bookings with gaps.
     * Only populated for DocumentLines where hasAttendanceGap = true.
     * Used by EntityDataAdapter to build date segments for gap bookings.
     */
    public ObservableList<Attendance> getAttendancesForGaps() {
        return attendancesForGaps;
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
        startDocumentLineQuery();
        startAttendanceGapQuery();
    }

    /**
     * Reloads data for the current time window.
     * Called when the user navigates the gantt view.
     */
    public void reload() {
        if (mixin != null) {
            // Rooms don't change, so we only need to reload document lines
            startDocumentLineQuery();
        }
    }

    /**
     * Starts the query to load rooms.
     */
    private void startRoomQuery() {
        // Query 1: Load all ResourceConfigurations (room configurations) for this organization
        // ResourceConfiguration has: resource (room with cleaning/inspection dates), item (room type), max (bed count)
        ReactiveEntitiesMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
            .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,resource.(id,name,cleaningState,lastCleaningDate,lastInspectionDate),item.(name,family.(name,ord)),max', orderBy: 'item.family.ord,item.name,name'}")
            // Filter by organization and optional site ID
            // Only get current configurations (no end date or future configs)
            .always(pm.organizationIdProperty(), org -> where("resource.site.organization=? and resource.site=? and (endDate is null or endDate >= current_date())", org, SITE_ID_FILTER))
            .storeEntitiesInto(resourceConfigurations)
            .start();
    }

    /**
     * Starts the query to load document lines (bookings) for the current time window.
     * This query is REACTIVE to time window changes via PM's time window properties.
     * <p>
     * PERFORMANCE OPTIMIZATION: Uses DocumentLine.startDate/endDate directly instead of
     * querying Attendance table. This returns one record per booking instead of one per day.
     * <p>
     * Includes hasAttendanceGap field to identify bookings that need Attendance records
     * for accurate date segment rendering.
     */
    private void startDocumentLineQuery() {
        // Query 2: Load bookings (document lines) for the time window
        // Uses startDate/endDate fields for date range filtering (much more efficient than Attendance)
        // Includes hasAttendanceGap to identify bookings needing Attendance records
        ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(mixin)
            // Fetch document line data with guest info and room assignment
            // Includes resource with lastCleaningDate/lastInspectionDate for room status
            .always("{class: 'DocumentLine', alias: 'dl', " +
                "fields: 'startDate,endDate,hasAttendanceGap," +
                "document.(arrived,person_firstName,person_lastName,event.name,request)," +
                "cleaned,resourceConfiguration.(name,item.(name,family.name),resource.(id,cleaningState,lastCleaningDate,lastInspectionDate))'," +
                "where: '!cancelled and !document.cancelled and resourceConfiguration is not null'}")
            // Filter by organization
            .always(pm.organizationIdProperty(), org ->
                where("document.event.organization=?", org))
            // Filter by date range - bookings that overlap with the time window
            // A booking overlaps if: startDate <= windowEnd AND endDate >= windowStart
            .always(pm.timeWindowStartProperty(), start -> {
                LocalDate end = pm.getTimeWindowEnd();

                if (start == null || end == null) {
                    return null;
                }

                // Include bookings that overlap with the time window
                return where("dl.startDate <= ? and dl.endDate >= ?", end, start);
            })
            .storeEntitiesInto(documentLines)
            .start();
    }

    /**
     * Starts the query to load Attendance records for bookings with gaps.
     * Only loads attendances for DocumentLines where hasAttendanceGap = true.
     * <p>
     * This allows us to build accurate date segments for bookings where the guest
     * doesn't stay on certain nights within their booking period.
     */
    private void startAttendanceGapQuery() {
        // Query 3: Load Attendance records for gap bookings only
        // These are used by EntityDataAdapter to build date segments
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
            .always("{class: 'Attendance', alias: 'a', " +
                "fields: 'date,documentLine', " +
                "where: 'documentLine.hasAttendanceGap = true and !documentLine.cancelled and !documentLine.document.cancelled'}")
            // Filter by organization
            .always(pm.organizationIdProperty(), org ->
                where("documentLine.document.event.organization=?", org))
            // Filter by date range - both booking overlap AND attendance date within window
            // This avoids loading all attendance records for long bookings
            .always(pm.timeWindowStartProperty(), start -> {
                LocalDate end = pm.getTimeWindowEnd();

                if (start == null || end == null) {
                    return null;
                }

                // Include only attendances within the time window for bookings that overlap
                return where("documentLine.startDate <= ? and documentLine.endDate >= ? and a.date >= ? and a.date < ?",
                        end, start, start, end);
            })
            .storeEntitiesInto(attendancesForGaps)
            .start();
    }
}
