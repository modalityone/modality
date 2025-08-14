package one.modality.hotel.backoffice.activities.household;

import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.time.layout.bar.LocalDateBar;
import dev.webfx.extras.time.layout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.time.layout.impl.ObjectBounds;
import dev.webfx.extras.time.window.TimeWindowUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.cache.client.LocalStorageCache;
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
import one.modality.base.shared.entities.markers.EntityHasDocumentLine;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AttendanceBlock;
import one.modality.hotel.backoffice.accommodation.AttendanceGantt;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;
import one.modality.hotel.backoffice.operations.entities.documentline.MarkAsCleanedRequest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
        {
            { // Additional constructor code for settings specifics to the Household

                // The user can click on bars. A bar represents the nights spent and is (arbitrarily) associated to a bed.
                // When the user clicks on a bar after the guest left, it's to tell the system that the bed has been
                // cleaned.
                barsLayout.setSelectionEnabled(true);
                FXProperties.runOnPropertyChange(selectedBar -> {
                    if (selectedBar != null) { // ignoring null (which happens when resetting selection in last line of this code)
                        AttendanceBlock block = selectedBar.getInstance();
                        // The user can click on bars only when the guest has arrived
                        if (block.isCheckedIn()) {
                            // Also, the person must have left already in order to mark the bed as cleaned, so we ignore
                            // any click on a stay that has not yet ended.
                            if (selectedBar.getEndTime().isAfter(FXToday.getToday()))
                                return;
                            DocumentLine documentLine = block.getDocumentLine();
                            // The bed needs cleaning if there is no previous cleaning date, or if the cleaning was prior to the stay
                            boolean needsCleaning = !documentLine.isCleaned();
                            // If the bed needs cleaning, we request the execution of a MarkBedAsCleaned operation:
                            if (needsCleaning)
                                // Note: the operation will first show a confirmation dialog, before actually updating the cleaning date for the bed
                                activity.executeOperation(new MarkAsCleanedRequest(documentLine, (Pane) activity.getNode()));
                        }
                        // Resetting the selection to null, so that the user can select the same bar again
                        barsLayout.setSelectedChild(null);
                    }
                }, barsLayout.selectedChildProperty());
                // We made the child row header clickable (the area where the bed is displayed via drawBed()). When the
                // user clicks on that area, we move the time window to reveal the latest stay that occupied that bed
                // (the stay bar will appear so that the last day will be in the center of the time window). This will
                // give the possibility to click on that bar to eventually mark the bed as cleaned.
                parentsCanvasDrawer.<ResourceConfiguration>setChildRowHeaderClickHandler((rc, bedIndex) -> {
                    DocumentLine documentLine = findDocumentLineBeforeTodayHoldingCurrentBedCleaningState(rc, bedIndex);
                    LocalDate checkOutDate = getCheckOutDate(documentLine, rc);
                    if (checkOutDate != null) {
                        //LocalDate checkInDate = getCheckInDate(documentLine, rc);
                        //System.out.println(rc.getName() + " - bed " + (bedIndex + 1) + ": " + documentLine.getDocument().getFullName() + " [" + checkInDate + ", " + checkOutDate + "]");
                        TimeWindowUtil.setTimeWindowCenter(pm, checkOutDate, barsLayout.getTimeProjector().getTemporalUnit());
                    }
                });
            }

            // We also override getBarColor() to show checked-in attendees as gray
            @Override
            protected Color getBarColor(LocalDateBar<AttendanceBlock> bar, Bounds b) {
                AttendanceBlock block = bar.getInstance();
                DocumentLine documentLine = bar.getInstance().getDocumentLine();
                if (block.isCheckedIn() || documentLine.isCleaned()) {
                    int bedIndex = barsLayout.getRowIndexInParentRow(b);
                    documentLine.setBedNumber(bedIndex);
                    if (documentLine.isCleaned() || documentLine == findDocumentLineBeforeTodayHoldingCurrentBedCleaningState(block.getRoomConfiguration(), bedIndex))
                        return getCleaningColor(documentLine, block.getRoomConfiguration());
                }
                return super.getBarColor(bar, b);
            }

            @Override
            protected void drawBed(Integer rowIndex, Bounds b, GraphicsContext gc) {
                // We first draw the bed without the color circle
                super.drawBed(rowIndex, b, gc);
                // And then we add the color circle indicating the cleaning state
                // Trick: the passed Bounds is actually an ObjectBounds whose object is the parent, so here it's the room
                // i.e. ResourceConfiguration.
                ResourceConfiguration rc = ((ObjectBounds<ResourceConfiguration>) b).getObject();
                LocalDateBar<AttendanceBlock> bar = findBarBeforeTodayHoldingCurrentBedCleaningState(rc, rowIndex, barsLayout);
                DocumentLine documentLine = bar == null ? null : bar.getInstance().getDocumentLine();//findDocumentLineBeforeTodayHoldingCurrentBedCleaningState(rc, rowIndex);
                gc.setFill(getCleaningColor(documentLine, rc));
                gc.fillOval(b.getMaxX() - 10, b.getCenterY() - 3, 6, 6);
            }
        };
    }

    private Color getCleaningColor(DocumentLine documentLine, ResourceConfiguration rc) {
        if (documentLine == null)
            return NEVER_OCCUPIED_COLOR;
        if (documentLine.isCleaned())
            return CLEANED_DONE_COLOR;
        LocalDate checkInDate = getCheckInDate(documentLine, rc);
        LocalDate checkOutDate = getCheckOutDate(documentLine, rc);
        LocalDate today = FXToday.getToday();
        if (checkInDate != null && checkInDate.isBefore(today) && checkOutDate != null && checkOutDate.isAfter(today))
            return OCCUPIED_COLOR;
        return CLEANING_NEEDED_COLOR;
    }

    private DocumentLine findDocumentLineBeforeTodayHoldingCurrentBedCleaningState(ResourceConfiguration rc, int bedIndex) {
        return attendances.stream()
                .filter(a -> a.getScheduledResource().getResourceConfiguration() == rc && !a.getDate().isAfter(FXToday.getToday()))
                .map(EntityHasDocumentLine::getDocumentLine)
                .distinct()
                .filter(dl -> dl.getDocument().isArrived() && getBedNumber(dl) == bedIndex)
                .max((dl1, dl2) -> compareDocumentLinesRegardingCleaning(dl1, dl2, rc))
                .orElse(null);
    }

    private LocalDateBar<AttendanceBlock> findBarBeforeTodayHoldingCurrentBedCleaningState(ResourceConfiguration rc, int bedIndex, LocalDateGanttLayout<LocalDateBar<AttendanceBlock>> barsLayout) {
        return barsLayout.streamChildrenInParentRowAtRowIndex(rc, bedIndex)
                .filter(b -> !b.getEndTime().isAfter(FXToday.getToday()))
                .filter(b -> b.getInstance().isCheckedIn())
                .max((b1, b2) -> compareDocumentLinesRegardingCleaning(b1.getInstance().getDocumentLine(), b2.getInstance().getDocumentLine(), rc))
                .orElse(null);
    }

    private int compareDocumentLinesRegardingCleaning(DocumentLine dl1, DocumentLine dl2, ResourceConfiguration rc) {
        boolean cleaned1 = dl1.isCleaned();
        boolean cleaned2 = dl2.isCleaned();
        if (cleaned1 != cleaned2)
            return cleaned1 ? -1 : +1;
        LocalDate checkOutDate1 = getCheckOutDate(dl1, rc);
        LocalDate checkOutDate2 = getCheckOutDate(dl2, rc);
        int compareTo = checkOutDate1.compareTo(checkOutDate2);
        return cleaned1 ? compareTo : -compareTo;
    }

    // Note: in KBS2, a.scheduledResource.resourceConfiguration refers to the global site, while dl.resourceConfiguration
    // refers to the event site (in KBS3 they will be identical and refer to the global site).

    private LocalDate getCheckInDate(DocumentLine dl, ResourceConfiguration rc) { // rc = global site
        if (dl == null)
            return null;
        LocalDate checkInDate = dl.getLocalDateFieldValue("checkInDate");
        if (checkInDate == null) {
            checkInDate = attendances.stream()
                    .filter(a -> a.getScheduledResource().getResourceConfiguration() == rc && a.getDocumentLine() == dl)
                    .map(EntityHasLocalDate::getDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);
            dl.setFieldValue("checkInDate", checkInDate);
        }
        return checkInDate;
    }

    private LocalDate getCheckOutDate(DocumentLine dl, ResourceConfiguration rc) { // rc = global site
        if (dl == null)
            return null;
        LocalDate checkOutDate = dl.getLocalDateFieldValue("checkOutDate");
        if (checkOutDate == null) {
            //System.out.println("Computing checkout date for " + dl.getDocument().getFullName());
            checkOutDate = attendances.stream()
                    .filter(a -> a.getScheduledResource().getResourceConfiguration() == rc && a.getDocumentLine() == dl)
                    .map(EntityHasLocalDate::getDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            dl.setFieldValue("checkOutDate", checkOutDate);
            //System.out.println("Checkout date = " + checkOutDate);
        }
        return checkOutDate;
    }

    private int getBedNumber(DocumentLine dl) {
        Integer bedNumber = dl.getBedNumber();
        return bedNumber == null ? 0 : bedNumber;
    }

    AttendanceGantt getAttendanceGantt() {
        return attendanceGantt;
    }

    void startLogic(Object mixin) {
        resourceConfigurationLoader.startLogic(mixin);
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
                .always( // language=JSON5
                    "{class: 'Attendance', alias: 'a', fields: 'date,documentLine.document.(arrived,person_firstName,person_lastName,event.name),documentLine.cleaned,scheduledResource.configuration.(name,item.name)', where: 'a.(scheduledResource != null and documentLine.(!cancelled and !document.cancelled))'}")
                //.always(where("scheduledResource.configuration.(lastCleaningDate == null or lastCleaningDate < date)"))
                // Order is important for TimeBarUtil
                .always(orderBy("scheduledResource.configuration.item.ord,scheduledResource.configuration.name,documentLine.document.id,date"))
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("documentLine.document.event.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(pm.timeWindowEndProperty(), endDate -> where("a.date >= ? and (a.date +1 >= ? and a.date -1 <= ? or a.date <= ? and !a.documentLine.cleaned)", FXToday.getToday().minus(2, ChronoUnit.MONTHS), pm.getTimeWindowStart(), endDate, FXToday.getToday())) // +1/-1 is to avoid the round corners on right for bookings exceeding the time window
                // Storing the result directly in the events layer
                .storeEntitiesInto(attendances)
                .setResultCacheEntry(LocalStorageCache.get().getCacheEntry("cache-householdAttendance"))
                // We are now ready to start
                .start();
    }
}
