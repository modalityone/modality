package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Teacher;

/**
 * @author Bruno Salmon
 */
public interface EntityHasTeacher extends Entity, HasTeacher {

    @Override
    default void setTeacher(Object teacher) {
        setForeignField("teacher", teacher);
    }

    @Override
    default EntityId getTeacherId() {
        return getForeignEntityId("teacher");
    }

    @Override
    default Teacher getTeacher() {
        return getForeignEntity("teacher");
    }

}
