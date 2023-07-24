package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

import one.modality.base.shared.entities.Person;

/**
 * @author Bruno Salmon
 */
public final class PersonImpl extends DynamicEntity implements Person {

    public PersonImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Person> {
        public ProvidedFactory() {
            super(Person.class, PersonImpl::new);
        }
    }
}
