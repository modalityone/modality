package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public interface EntityHasEndTime extends Entity, HasEndTime {

    @Override
    default LocalTime getEndTime() {
        return (LocalTime) getFieldValue("endTime");
    }

    @Override
    default void setEndTime(LocalTime endTime) {
        setFieldValue("endTime", endTime);
    }

}
