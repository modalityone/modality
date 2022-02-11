package mongoose.base.shared.entities.impl;

import mongoose.base.shared.entities.Teacher;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class TeacherImpl extends DynamicEntity implements Teacher {

    public TeacherImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Teacher> {
        public ProvidedFactory() {
            super(Teacher.class, TeacherImpl::new);
        }
    }
}
