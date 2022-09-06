package one.modality.base.client.aggregates.person;

import one.modality.base.shared.entities.Person;
import dev.webfx.stack.orm.entity.EntityStore;

/**
 * @author Bruno Salmon
 */
public interface PersonAggregate {

    static PersonAggregate get(EntityStore store) {
        return PersonAggregateImpl.get(store);
    }

    static PersonAggregate getOrCreate(EntityStore store) {
        return PersonAggregateImpl.getOrCreate(store);
    }

    Person getPreselectionProfilePerson();

}
