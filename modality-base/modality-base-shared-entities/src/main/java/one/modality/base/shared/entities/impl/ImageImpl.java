package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.Image;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class ImageImpl extends DynamicEntity implements Image {

    public ImageImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Image> {
        public ProvidedFactory() {
            super(Image.class, ImageImpl::new);
        }
    }
}
