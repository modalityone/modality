package one.modality.hotel.backoffice.accommodation;

import dev.webfx.platform.util.Numbers;
import javafx.scene.paint.Color;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.event.client.theme.EventTheme;

import java.util.Objects;

/**
 * @author Dan Newman
 */
public final class AttendanceBlock implements AccommodationBlock {

    private final Attendance attendance;
    private final ResourceConfiguration resourceConfiguration;
    private final AttendeeCategory attendeeCategory;

    public AttendanceBlock(Attendance attendance) {
        this.attendance = attendance;
        this.resourceConfiguration = attendance.getScheduledResource().getResourceConfiguration();
        attendeeCategory = AttendeeCategory.fromDocument(getDocument());
     }

    public Attendance getAttendance() {
        return attendance;
    }

    public Color getBlockColor() {
        if (attendeeCategory == AttendeeCategory.GUEST) {
            if (Objects.equals(FXGanttSelection.getGanttSelectedObject(), getDocument().getEvent()))
                return EventTheme.getEventBackgroundColor(getDocument().getEvent(), true);
        }
        return attendeeCategory.getColor();
    }

    private Document getDocument() {
        return getDocumentLine().getDocument();
    }

    public DocumentLine getDocumentLine() {
        return attendance.getDocumentLine();
    }

    public String getPersonName() {
        String name = getDocument().getFullName();
        if (attendeeCategory == AttendeeCategory.GUEST) {
            Object pk = getDocument().getEventId().getPrimaryKey();
            if (Numbers.toInteger(pk) != 356)
                name += " (" + pk + ")";
        }
        return name;
    }

    @Override
    public ResourceConfiguration getRoomConfiguration() {
        return resourceConfiguration;
    }

    public boolean isCheckedIn() {
        return getDocument().isArrived();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttendanceBlock that = (AttendanceBlock) o;

        if (!getPersonName().equals(that.getPersonName())) return false;
        return resourceConfiguration.equals(that.resourceConfiguration);
    }

    @Override
    public int hashCode() {
        int result = resourceConfiguration.hashCode();
        result = 31 * result + (getPersonName().hashCode());
        return result;
    }
}
