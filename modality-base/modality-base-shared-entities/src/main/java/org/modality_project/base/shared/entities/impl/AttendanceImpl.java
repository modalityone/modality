package org.modality_project.base.shared.entities.impl;

import org.modality_project.base.shared.entities.Attendance;
import dev.webfx.stack.framework.shared.orm.entity.EntityId;
import dev.webfx.stack.framework.shared.orm.entity.EntityStore;
import dev.webfx.stack.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

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
