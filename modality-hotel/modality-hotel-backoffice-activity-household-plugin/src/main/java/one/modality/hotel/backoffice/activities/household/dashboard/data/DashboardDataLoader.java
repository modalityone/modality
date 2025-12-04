package one.modality.hotel.backoffice.activities.household.dashboard.data;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;

import java.time.LocalDate;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Data loader for the Household Dashboard view.
 * Loads bookings from the database using reactive queries.
 * <p>
 * This class follows the same pattern as HouseholdGanttDataLoader:
 * - Reactive queries that automatically update when parameters change
 * - Observable lists for UI binding
 * - Separation of data loading from business logic
 * <p>
 * PERFORMANCE OPTIMIZATION: Uses DocumentLine.startDate/endDate fields directly
 * instead of querying the Attendance table for most bookings. This significantly
 * reduces the number of records returned (one per booking vs one per day of stay).
 * <p>
 * ATTENDANCE GAP SUPPORT: For bookings with hasAttendanceGap=true, we also load
 * the individual Attendance records to determine the actual date segments.
 */
public final class DashboardDataLoader {

    /**
     * Minimum number of days to look ahead when loading data.
     * This ensures we can calculate "next check-in" dates for cleaning priority
     * even when the user is viewing a shorter date range.
     */
    private static final int MIN_LOOKAHEAD_DAYS = 7;

    private final AccommodationPresentationModel pm;
    private final IntegerProperty daysToDisplay;
    private final ObservableList<DocumentLine> documentLines = FXCollections.observableArrayList();
    private final ObservableList<Attendance> attendancesForGaps = FXCollections.observableArrayList();

    /**
     * Constructor.
     *
     * @param pm The presentation model (provides organization context)
     * @param daysToDisplay Property controlling how many days to display
     */
    public DashboardDataLoader(AccommodationPresentationModel pm, IntegerProperty daysToDisplay) {
        this.pm = pm;
        this.daysToDisplay = daysToDisplay;
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
        startDocumentLineQuery(mixin);
        startAttendanceGapQuery(mixin);
    }

    /**
     * Starts the query to load document lines (bookings) for the dashboard.
     * Uses startDate/endDate fields for efficient date range filtering.
     */
    private void startDocumentLineQuery(Object mixin) {
        ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(mixin)
                .always("{class: 'DocumentLine', alias: 'dl', " +
                        "fields: 'startDate,endDate,hasAttendanceGap,dates,cleaned,cancelled," +
                        "document.(arrived,person_firstName,person_lastName,person_male,event.name,request,cancelled)," +
                        "item.(name,family)," +
                        "resourceConfiguration.(name,item.(name,family),resource.(id,cleaningState,lastCleaningDate,lastInspectionDate))'," +
                        "where: 'resourceConfiguration is not null'}")
                .always(pm.organizationIdProperty(), org -> where("document.event.organization=?", org))
                .always(FXProperties.combine(FXToday.todayProperty(), daysToDisplay, (today, days) -> {
                    // Date range for bookings:
                    // - Start: yesterday (for checkout detection)
                    // - End: max of daysToDisplay or 7 days ahead (for next check-in calculation)
                    LocalDate start = today.minusDays(1);
                    int endOffset = Math.max(days.intValue(), MIN_LOOKAHEAD_DAYS);
                    LocalDate end = today.plusDays(endOffset);
                    // Find bookings that overlap with the date range: startDate <= end AND endDate >= start
                    return where("dl.startDate <= ? and dl.endDate >= ? and !cancelled and !document.cancelled", end, start);
                }), dql -> dql)
                .storeEntitiesInto(documentLines)
                .start();
    }

    /**
     * Starts the query to load Attendance records for bookings with gaps.
     * Only loads attendances for DocumentLines where hasAttendanceGap = true.
     */
    private void startAttendanceGapQuery(Object mixin) {
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
                .always("{class: 'Attendance', alias: 'a', " +
                        "fields: 'date,documentLine', " +
                        "where: 'documentLine.hasAttendanceGap = true and !documentLine.cancelled and !documentLine.document.cancelled'}")
                .always(pm.organizationIdProperty(), org ->
                        where("documentLine.document.event.organization=?", org))
                .always(FXProperties.combine(FXToday.todayProperty(), daysToDisplay, (today, days) -> {
                    LocalDate start = today.minusDays(1);
                    int endOffset = Math.max(days.intValue(), MIN_LOOKAHEAD_DAYS);
                    LocalDate end = today.plusDays(endOffset);
                    // Filter by both booking overlap AND attendance date within window
                    // This avoids loading all attendance records for long bookings
                    return where("a.date >= ? and a.date < ?",
                            start, end);
                }), dql -> dql)
                .storeEntitiesInto(attendancesForGaps)
                .start();
    }
}
