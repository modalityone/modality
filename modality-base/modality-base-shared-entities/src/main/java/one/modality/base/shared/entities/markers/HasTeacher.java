package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Teacher;

/**
 * @author Bruno Salmon
 */
public interface HasTeacher {

    void setTeacher(Object teacher);

    EntityId getTeacherId();

    Teacher getTeacher();

}
