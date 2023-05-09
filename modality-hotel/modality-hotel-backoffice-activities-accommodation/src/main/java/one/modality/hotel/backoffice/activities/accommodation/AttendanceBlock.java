package one.modality.hotel.backoffice.activities.accommodation;

import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;

import java.time.LocalDate;

/**
 * @author Dan Newman
 */
public final class AttendanceBlock {

    private final Attendance attendance;
    private final ResourceConfiguration resourceConfiguration;

    public AttendanceBlock(Attendance attendance) {
        this.attendance = attendance;
        this.resourceConfiguration = attendance.getResourceConfiguration();
    }

    public String getPersonName() {
        return attendance.getDocumentLine().getDocument().getFullName();
    }

    public ResourceConfiguration getResourceConfiguration() {
        return resourceConfiguration;
    }

    public Event getEvent() {
        return attendance.getEvent();
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
