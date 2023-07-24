package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Mail;

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
