package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public interface EntityHasStartTime extends Entity, HasStartTime {

    @Override
    default LocalTime getStartTime() {
        return (LocalTime) getFieldValue("startTime");
    }

    @Override
    default void setStartTime(LocalTime startTime) {
        setFieldValue("startTime", startTime);
    }

}
