package one.modality.hotel.backoffice.activities.accommodation;

import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;

import java.time.LocalDate;

/**
 * @author Dan Newman
 */
public final class AttendanceBlock {

    private final Attendance attendance;
    private final ResourceConfiguration resourceConfiguration;
    /*private final boolean available;
    private final boolean online;
    private final int remaining;*/

    public AttendanceBlock(Attendance attendance) {
        this.attendance = attendance;
        this.resourceConfiguration = attendance.getForeignEntity("scheduledResource").getForeignEntity("configuration");
    }

    public String getPersonName() {
        return attendance.getDocumentLine().getDocument().getFullName();
    }

    public LocalDate getDate() {
        return attendance.getDate();
    }

    public ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
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
