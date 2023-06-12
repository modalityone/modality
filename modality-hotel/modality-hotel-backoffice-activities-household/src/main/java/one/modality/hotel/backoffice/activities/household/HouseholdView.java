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
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.markers.EntityHasDate;
import one.modality.base.shared.entities.markers.EntityHasDocumentLine;
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
                                activity.executeOperation(new MarkBedAsCleanedRequest(roomConfiguration, bedIndex, bar.getEndTime(), (Pane) activity.getNode()));
                        }
                        // Resetting the selection to null, so that the user can select the same bar again
                        barsLayout.setSelectedChild(null);
                    }
                });
                parentsCanvasDrawer.<ResourceConfiguration>setChildRowHeaderClickHandler((rc, bedIndex) -> {
                    System.out.println(rc.getName() + " - bed " + (bedIndex + 1) + " - lastCleaning: " + rc.getLastCleaningDate());
                    DocumentLine documentLine = findLatestOccupiedDocumentLineBeforeToday(rc, bedIndex);
                    LocalDate checkInDate = getCheckInDate(rc, documentLine);
                    LocalDate checkOutDate = getCheckOutDate(rc, documentLine);
                    System.out.println("[" + checkInDate + ", " + checkOutDate + "]");
                    if (checkOutDate != null) {
                        TimeWindowUtil.setTimeWindowCenter(pm, checkOutDate, barsLayout.getTimeProjector().getTemporalUnit());
                    }
                });
            }
            @Override
            protected Color getBarColor(LocalDateBar<AttendanceBlock> bar, Bounds b) {
                AttendanceBlock block = bar.getInstance();
                if (block.isCheckedIn()) {
                    int bedIndex = barsLayout.getRowIndexInParentRow(b);
                    DocumentLine documentLine = bar.getInstance().getAttendance().getDocumentLine();
                    documentLine.setBedNumber(bedIndex);
                    if (documentLine == findLatestOccupiedDocumentLineBeforeToday(block.getRoomConfiguration(), bedIndex))
                        return getCleaningColor(block.getRoomConfiguration(), bedIndex, bar.getStartTime(), bar.getEndTime());
                }
                return super.getBarColor(bar, b);
            }

            @Override
            protected void drawBed(Integer rowIndex, Bounds b, GraphicsContext gc) {
                super.drawBed(rowIndex, b, gc);
                // Trick: the passed Bounds is actually an ObjectBounds whose object is the parent, so here it's the room
                // i.e. ResourceConfiguration.
                ResourceConfiguration rc = ((ObjectBounds<ResourceConfiguration>) b).getObject();
                DocumentLine documentLine = findLatestOccupiedDocumentLineBeforeToday(rc, rowIndex);
                gc.setFill(getCleaningColor(rc, documentLine, rowIndex));
                gc.fillOval(b.getMaxX() - 10, b.getCenterY() - 3, 6, 6);
            }
        };
    }

    private Color getCleaningColor(ResourceConfiguration rc, DocumentLine documentLine, int bedIndex) {
        LocalDate checkInDate = getCheckInDate(rc, documentLine);
        LocalDate checkOutDate = getCheckOutDate(rc, documentLine);
        return getCleaningColor(rc, bedIndex, checkInDate, checkOutDate);
    }

    private Color getCleaningColor(ResourceConfiguration rc, int bedIndex, LocalDate lastCheckInDate, LocalDate lastCheckOutDate) {
        LocalDate today = FXToday.getToday();
        if (lastCheckInDate != null && lastCheckInDate.isBefore(today) && lastCheckOutDate != null && lastCheckOutDate.isAfter(today))
            return OCCUPIED_COLOR;
        LocalDate lastCleaningDate = getLastBedCleaningDate(rc, bedIndex);
        if (lastCheckOutDate != null && (lastCleaningDate == null || lastCleaningDate.isBefore(lastCheckOutDate)))
            return CLEANING_NEEDED_COLOR;
        if (lastCleaningDate == null)
            return NEVER_OCCUPIED_COLOR;
        return CLEANED_DONE_COLOR;
    }

    private DocumentLine findLatestOccupiedDocumentLineBeforeToday(ResourceConfiguration rc, int bedIndex) {
        return attendances.stream()
                .filter(a -> a.getScheduledResource().getResourceConfiguration() == rc && !a.getDate().isAfter(FXToday.getToday()))
                .map(EntityHasDocumentLine::getDocumentLine)
                .distinct()
                .filter(dl -> dl.getDocument().isArrived() && getBedNumber(dl) == bedIndex)
                .max((dl1, dl2) -> getCheckOutDate(rc, dl1).compareTo(getCheckOutDate(rc, dl2)))
                .orElse(null);
    }

    // Note: in KBS2, a.scheduledResource.resourceConfiguration refers to the global site, while dl.resourceConfiguration
    // refers to the event site.

    private LocalDate getCheckInDate(ResourceConfiguration rc, DocumentLine dl) { // rc = global site
        if (dl == null)
            return null;
        LocalDate checkInDate = dl.getLocalDateFieldValue("checkInDate");
        if (checkInDate == null) {
            checkInDate = attendances.stream()
                    .filter(a -> a.getScheduledResource().getResourceConfiguration() == rc && a.getDocumentLine() == dl)
                    .map(EntityHasDate::getDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);
            dl.setFieldValue("checkInDate", checkInDate);
        }
        return checkInDate;
    }

    private LocalDate getCheckOutDate(ResourceConfiguration rc, DocumentLine dl) { // rc = global site
        if (dl == null)
            return null;
        LocalDate checkOutDate = dl.getLocalDateFieldValue("checkOutDate");
        if (checkOutDate == null) {
            checkOutDate = attendances.stream()
                    .filter(a -> a.getScheduledResource().getResourceConfiguration() == rc && a.getDocumentLine() == dl)
                    .map(EntityHasDate::getDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            dl.setFieldValue("checkOutDate", checkOutDate);
        }
        return checkOutDate;
    }

    private int getBedNumber(DocumentLine dl) {
        Integer bedNumber = dl.getBedNumber();
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
                .always(pm.timeWindowEndProperty(), endDate -> where("a.date +1 >= ? and a.date -1 <= ? or a.documentLine.document.arrived and a.scheduledResource.configuration.(lastCleaningDate == null or lastCleaningDate < a.date)", pm.getTimeWindowStart(), endDate))   // -1 is to avoid the round corners on right for bookings exceeding the time window
                // Storing the result directly in the events layer
                .storeEntitiesInto(attendances)
                // We are now ready to start
                .start();
    }
}
