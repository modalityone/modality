package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public final class DocumentLineImpl extends DynamicEntity implements DocumentLine {

  public DocumentLineImpl(EntityId id, EntityStore store) {
    super(id, store);
  }

  public static final class ProvidedFactory extends EntityFactoryProviderImpl<DocumentLine> {
    public ProvidedFactory() {
      super(DocumentLine.class, DocumentLineImpl::new);
    }
  }
}
