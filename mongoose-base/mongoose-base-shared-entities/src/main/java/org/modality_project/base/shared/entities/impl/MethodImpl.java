package org.modality_project.base.shared.entities.impl;

import org.modality_project.base.shared.entities.Method;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class MethodImpl extends DynamicEntity implements Method {

    public MethodImpl(EntityId id, EntityStore store) {
        super(id, store);
    }


    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Method> {
        public ProvidedFactory() {
            super(Method.class, MethodImpl::new);
        }
    }
}
