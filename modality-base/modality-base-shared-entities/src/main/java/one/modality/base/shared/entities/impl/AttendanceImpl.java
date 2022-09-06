package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.Attendance;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class AttendanceImpl extends DynamicEntity implements Attendance {

    public AttendanceImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Attendance> {
        public ProvidedFactory() {
            super(Attendance.class, AttendanceImpl::new);
        }
    }
}
