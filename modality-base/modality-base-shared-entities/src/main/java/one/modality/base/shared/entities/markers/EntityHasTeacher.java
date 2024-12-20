package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Teacher;

/**
 * @author Bruno Salmon
 */
public interface EntityHasTeacher extends Entity, HasTeacher {

    String teacher = "teacher";

    @Override
    default void setTeacher(Object value) {
        setForeignField(teacher, value);
    }

    @Override
    default EntityId getTeacherId() {
        return getForeignEntityId(teacher);
    }

    @Override
    default Teacher getTeacher() {
        return getForeignEntity(teacher);
    }

}