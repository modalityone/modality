package one.modality.event.frontoffice.activities.booking.process;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class EventAggregate {

    private final Event event;

    private List<ScheduledItem> scheduledItems;

    public EventAggregate(Event event) {
        this.event = event;
    }

    public Future<Void> load() {
        if (scheduledItems != null)
            return Future.succeededFuture();
        Promise<Void> promise = Promise.promise();
        event.getStore()
                .<ScheduledItem>executeListQuery("scheduledItems", "select site, item, date, startTime, available, timeline.startTime from ScheduledItem where event=? and online order by date", event)
                .onFailure(promise::fail)
                .onSuccess(items -> {
                    scheduledItems = items;
                    promise.complete();
                });
        return promise.future();
    }

    public List<ScheduledItem> getScheduledItems() {
        return scheduledItems;
    }
}
