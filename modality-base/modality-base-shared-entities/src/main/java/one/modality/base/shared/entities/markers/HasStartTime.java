package one.modality.base.shared.entities.markers;

import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public interface HasStartTime {

    LocalTime getStartTime();

    void setStartTime(LocalTime startTime);

}
