package one.modality.hotel.backoffice.activities.household;

import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.time.layout.bar.LocalDateBar;
import dev.webfx.extras.time.layout.impl.ObjectBounds;
import dev.webfx.extras.time.window.TimeWindowUtil;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.markers.EntityHasDate;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AttendanceBlock;
import one.modality.hotel.backoffice.accommodation.AttendanceGantt;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;
import one.modality.hotel.backoffice.operations.entities.resourceconfiguration.MarkBedAsCleanedRequest;

import java.time.LocalDate;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

final class HouseholdView {

    private final static Color OCCUPIED_COLOR = Color.rgb(130, 135, 136);
    private final static Color CLEANING_NEEDED_COLOR = Color.rgb(255, 3, 5);
    private final static Color CLEANED_DONE_COLOR = Color.rgb(65, 186, 77);
    private final static Color NEVER_OCCUPIED_COLOR = Color.ORANGE;

    private final AccommodationPresentationModel pm;
    private final ObservableList<Attendance> attendances = FXCollections.observableArrayList();
    private final ResourceConfigurationLoader resourceConfigurationLoader;
    private final AttendanceGantt attendanceGantt;

    HouseholdView(AccommodationPresentationModel pm, HouseholdActivity activity) {
        this.pm = pm;
        resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
        attendanceGantt = new AttendanceGantt(
                pm, // Presentation model
                attendances, // attendances observable list that we provide as input to AttendanceGantt for bar conversion
                resourceConfigurationLoader.getResourceConfigurations()) // the provided parent rooms
        {  // We also override getBarColor() to show checked-in attendees as gray
            {
                barsLayout.setSelectionEnabled(true);
                barsLayout.selectedChildProperty().addListener((observable, oldValue, bar) -> {
                    if (bar != null) {
                        AttendanceBlock block = bar.getInstance();
                        if (block.isCheckedIn()) {
                            if (bar.getEndTime().isAfter(FXToday.getToday()))
                                return;
                            ResourceConfiguration roomConfiguration = block.getRoomConfiguration();
                            int bedIndex = barsLayout.getRowIndexInParentRow(bar);
                            LocalDate lastCleaningDate = roomConfiguration.getLastCleaningDate();
                            boolean needsCleaning = (lastCleaningDate == null || lastCleaningDate.isBefore(bar.getEndTime()));
                            if (needsCleaning)
                                activity.executeOperation(new MarkBedAsCleanedRequest(roomConfiguration, bedIndex, (Pane) activity.getNode()));
                        }
                        // Resetting the selection to null, so that the user can select the same bar again
                        barsLayout.setSelectedChild(null);
                    }
                });
                parentsCanvasDrawer.<ResourceConfiguration>setChildRowHeaderClickHandler((rc, bedIndex) -> {
                    System.out.println(rc.getName() + " - bed " + (bedIndex + 1) + " - lastCleaning: " + rc.getLastCleaningDate());
                    LocalDate[] occupiedStartTime = { null }, occupiedEndTime = { null };
                    findBedLastOccupiedDates(rc, bedIndex, occupiedStartTime, occupiedEndTime);
                    System.out.println("[" + occupiedStartTime[0] + ", " + occupiedEndTime[0] + "]");
                    if (occupiedEndTime[0] != null) {
                        TimeWindowUtil.setTimeWindowCenter(pm, occupiedEndTime[0], barsLayout.getTimeProjector().getTemporalUnit());
                    }
                });
            }
            @Override
            protected Color getBarColor(LocalDateBar<AttendanceBlock> bar, Bounds b) {
                AttendanceBlock block = bar.getInstance();
                if (block.isCheckedIn()) {
                    int bedIndex = barsLayout.getRowIndexInParentRow(b);
                    bar.getInstance().getAttendance().getDocumentLine().setBedNumber(bedIndex);
                    return getBedCleaningColor(block.getRoomConfiguration(), bedIndex, bar.getStartTime(), bar.getEndTime());
                }
                return super.getBarColor(bar, b);
            }

            @Override
            protected void drawBed(Integer rowIndex, Bounds b, GraphicsContext gc) {
                super.drawBed(rowIndex, b, gc);
                // Trick: the passed Bounds is actually an ObjectBounds whose object is the parent (here = room = ResourceConfiguration)
                // That's how we get the room.
                ResourceConfiguration rc = ((ObjectBounds<ResourceConfiguration>) b).getObject();
                LocalDate[] occupiedStartTime = { null }, occupiedEndTime = { null };
                findBedLastOccupiedDates(rc, rowIndex, occupiedStartTime, occupiedEndTime);
                gc.setFill(getBedCleaningColor(rc, rowIndex, occupiedStartTime[0], occupiedEndTime[0]));
                gc.fillOval(b.getMaxX() - 10, b.getCenterY() - 3, 6, 6);
            }
        };
    }

    private Color getBedCleaningColor(ResourceConfiguration rc, int bedIndex, LocalDate occupiedStartTime, LocalDate occupiedEndTime) {
        LocalDate today = FXToday.getToday();
        if (occupiedStartTime != null && occupiedStartTime.isBefore(today) && occupiedEndTime != null && occupiedEndTime.isAfter(today))
            return OCCUPIED_COLOR;
        LocalDate lastCleaningDate = getLastBedCleaningDate(rc, bedIndex);
        if (occupiedEndTime != null && (lastCleaningDate == null || lastCleaningDate.isBefore(occupiedEndTime)))
            return CLEANING_NEEDED_COLOR;
        if (lastCleaningDate == null)
            return NEVER_OCCUPIED_COLOR;
        return CLEANED_DONE_COLOR;
    }

    private void findBedLastOccupiedDates(ResourceConfiguration rc, int bedIndex, LocalDate[] occupiedStartTime, LocalDate[] occupiedEndTime) {
        occupiedStartTime[0] = attendances.stream()
                .filter(a -> a.getScheduledResource().getResourceConfiguration() == rc && a.getDocumentLine().getDocument().isArrived() && getBedNumber(a) == bedIndex)
                .map(EntityHasDate::getDate)
                .min(LocalDate::compareTo)
                .orElse(null);
        occupiedEndTime[0] = attendances.stream()
                .filter(a -> a.getScheduledResource().getResourceConfiguration() == rc && a.getDocumentLine().getDocument().isArrived() && getBedNumber(a) == bedIndex)
                .map(EntityHasDate::getDate)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    private int getBedNumber(Attendance a) {
        Integer bedNumber = a.getDocumentLine().getBedNumber();
        return bedNumber == null ? 0 : bedNumber;
    }

    private LocalDate getLastBedCleaningDate(ResourceConfiguration rc, int bedIndex) {
        return rc.getLastCleaningDate();
    }

    AttendanceGantt getAttendanceGantt() {
        return attendanceGantt;
    }

    void startLogic(Object mixin) {
        resourceConfigurationLoader.startLogic(mixin);
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
                .always("{class: 'Attendance', alias: 'a', fields: 'date,documentLine.document.(arrived,person_firstName,person_lastName,event.id),scheduledResource.configuration.(name,item.name,lastCleaningDate),documentLine.document.event.name'}")
                //.always(where("scheduledResource.configuration.(lastCleaningDate == null or lastCleaningDate < date)"))
                // Order is important for TimeBarUtil
                .always(orderBy("scheduledResource.configuration.item.ord,scheduledResource.configuration.name,documentLine.document.person_lastName,documentLine.document.person_firstName,date"))
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("documentLine.document.event.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(pm.timeWindowEndProperty(),   endDate   -> where("a.date +1 >= ? and a.date -1 <= ? or a.documentLine.document.arrived and a.scheduledResource.configuration.(lastCleaningDate == null or lastCleaningDate < a.date)", pm.getTimeWindowStart(), endDate))   // -1 is to avoid the round corners on right for bookings exceeding the time window
                // Storing the result directly in the events layer
                .storeEntitiesInto(attendances)
                // We are now ready to start
                .start();
    }
}
