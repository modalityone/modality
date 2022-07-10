package org.modality_project.base.shared.entities.impl;

import org.modality_project.base.shared.entities.Document;
import dev.webfx.stack.framework.shared.orm.entity.EntityId;
import dev.webfx.stack.framework.shared.orm.entity.EntityStore;
import dev.webfx.stack.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class DocumentImpl extends DynamicEntity implements Document {

    public DocumentImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Document> {
        public ProvidedFactory() {
            super(Document.class, DocumentImpl::new);
        }
    }
}
