package one.modality.base.shared.entities.impl;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.EventPart;
import one.modality.base.shared.entities.EventSelection;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class EventSelectionImpl extends DynamicEntity implements EventSelection {

    private List<EventPart> parts;

    public EventSelectionImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    @Override
    public List<EventPart> getParts() {
        if (parts == null)
            parts = Collections.removeNulls(Collections.listOf(getPart1(), getPart2(), getPart3()));
        return parts;
    }

    @Override
    public void setParts(List<EventPart> parts) {
        this.parts = parts;
        setPart1(Collections.get(parts, 0));
        setPart2(Collections.get(parts, 1));
        setPart3(Collections.get(parts, 2));
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<EventSelection> {
        public ProvidedFactory() {
            super(EventSelection.class, EventSelectionImpl::new);
        }
    }
}
