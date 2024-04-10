package one.modality.base.shared.entities.markers;

import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public interface HasEndTime {

    LocalTime getEndTime();

    void setEndTime(LocalTime endTime);

}
