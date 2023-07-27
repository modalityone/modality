package one.modality.ecommerce.client2018.businessdata.feesgroup;

import one.modality.ecommerce.client2018.businesslogic.feesgroup.FeesGroupLogic;
import one.modality.base.client2018.aggregates.event.EventAggregate;
import one.modality.base.shared.entities.Event;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.platform.async.Future;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public class FeesGroupsByEventStore {

    private final static Map<EntityId, FeesGroup[]> feesGroupsByEventMap = new HashMap<>();

    public static FeesGroup[] getEventFeesGroups(EventAggregate eventAggregate) {
        return getEventFeesGroups(eventAggregate.getEvent());
    }

    public static FeesGroup[] getEventFeesGroups(Event event) {
        return getEventFeesGroups(event.getId());
    }

    public static FeesGroup[] getEventFeesGroups(EntityId eventId) {
        FeesGroup[] feesGroups = feesGroupsByEventMap.get(eventId);
        if (feesGroups == null)
            feesGroupsByEventMap.put(eventId, feesGroups = FeesGroupLogic.createFeesGroups(EventAggregate.get(eventId)));
        return feesGroups;
    }

    public static Future<FeesGroup[]> onEventFeesGroups(EventAggregate eventAggregate) {
        return eventAggregate.onEvent().compose(FeesGroupsByEventStore::onEventFeesGroups);
    }

    public static Future<FeesGroup[]> onEventFeesGroups(Event event) {
        return onEventFeesGroups(event.getId());
    }

    public static Future<FeesGroup[]> onEventFeesGroups(EntityId eventId) {
        FeesGroup[] feesGroups = feesGroupsByEventMap.get(eventId);
        if (feesGroups != null)
            return Future.succeededFuture(feesGroups);
        return EventAggregate.get(eventId).onEventOptions().map(ignored -> getEventFeesGroups(eventId));
    }

}
