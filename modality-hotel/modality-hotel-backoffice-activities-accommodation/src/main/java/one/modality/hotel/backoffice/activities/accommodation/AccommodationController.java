package one.modality.hotel.backoffice.activities.accommodation;

import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledResource;

import java.util.List;

public interface AccommodationController {

    void setEntities(List<Attendance> attendances);

    void setAllScheduledResource(List<ScheduledResource> allScheduledResource);
}
