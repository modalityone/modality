package mongoose.shared.entities.impl;

import mongoose.shared.entities.Attendance;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

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
