package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public interface EntityHasStartTime extends Entity, HasStartTime {

    String startTime = "startTime";

    @Override
    default LocalTime getStartTime() {
        return (LocalTime) getFieldValue(startTime);
    }

    @Override
    default void setStartTime(LocalTime value) {
        setFieldValue(startTime, value);
    }

}