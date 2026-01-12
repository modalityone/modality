package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface EventSelection extends Entity,
    EntityHasEvent,
    EntityHasName,
    EntityHasLabel
{
    String inPerson = "inPerson";
    String online = "online";
    String part1 = "part1";
    String part2 = "part2";
    String part3 = "part3";

    default void setInPerson(Object value) {
        setFieldValue(inPerson, value);
    }

    default Boolean isInPerson() {
        return getBooleanFieldValue(inPerson);
    }

    default void setOnline(Object value) {
        setFieldValue(online, value);
    }

    default Boolean isOnline() {
        return getBooleanFieldValue(online);
    }

    default void setPart1(Object value) {
        setForeignField(part1, value);
    }

    default EntityId getPart1Id() {
        return getForeignEntityId(part1);
    }

    default EventPart getPart1() {
        return getForeignEntity(part1);
    }

    default void setPart2(Object value) {
        setForeignField(part2, value);
    }

    default EntityId getPart2Id() {
        return getForeignEntityId(part2);
    }

    default EventPart getPart2() {
        return getForeignEntity(part2);
    }

    default void setPart3(Object value) {
        setForeignField(part3, value);
    }

    default EntityId getPart3Id() {
        return getForeignEntityId(part3);
    }

    default EventPart getPart3() {
        return getForeignEntity(part3);
    }

}
