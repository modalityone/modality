package one.modality.hotel.backoffice.accommodation;

import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledResource;

import java.util.List;

public interface AccommodationStatusBarUpdater {

    void setEntities(List<Attendance> attendances);

    void setAllScheduledResource(List<ScheduledResource> allScheduledResource);
}
