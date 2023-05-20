package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.platform.util.Numbers;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.ResourceConfiguration;

/**
 * @author Dan Newman
 */
public final class AttendanceBlock {

    private final Attendance attendance;
    private final ResourceConfiguration resourceConfiguration;
    private final AttendeeCategory attendeeCategory;

    public AttendanceBlock(Attendance attendance) {
        this.attendance = attendance;
        this.resourceConfiguration = attendance.getResourceConfiguration();
        if (getPersonName().contains("Gen-la") || getPersonName().contains("Birch"))
            attendeeCategory = AttendeeCategory.SPECIAL_GUEST;
        else if (Numbers.toInteger(getDocument().getEventId().getPrimaryKey()) == 480) // 480 = Working visit
            attendeeCategory = AttendeeCategory.VOLUNTEER;
        else
            attendeeCategory = AttendeeCategory.GUEST;
    }

    private Document getDocument() {
        return attendance.getDocumentLine().getDocument();
    }

    public String getPersonName() {
        return getDocument().getFullName();
    }

    public ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }

    public AttendeeCategory getAttendeeCategory() {
        return attendeeCategory;
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
