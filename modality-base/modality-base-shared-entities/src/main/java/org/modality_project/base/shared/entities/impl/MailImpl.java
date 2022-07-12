package org.modality_project.base.shared.entities.impl;

import org.modality_project.base.shared.entities.Mail;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class MailImpl extends DynamicEntity implements Mail {

    public MailImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Mail> {
        public ProvidedFactory() {
            super(Mail.class, MailImpl::new);
        }
    }
}
