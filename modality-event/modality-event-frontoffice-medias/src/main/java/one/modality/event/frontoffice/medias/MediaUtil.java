package one.modality.event.frontoffice.medias;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.lciimpl.EntityDomainReader;
import one.modality.base.client.entities.functions.I18nFunction;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;

public class MediaUtil {

    public static String translate(Entity entity) {
        return translate(entity, null);
    }

    public static String translate(Entity entity, Object language) {
        if (entity == null)
            return null;
        return (String) I18nFunction.evaluate(entity, language, new EntityDomainReader<>(entity.getStore()));
    }

    public static EntityHasStartAndEndTime getStartAndEndTimeHolder(ScheduledItem scheduledItem) {
        // Start & end times can be specified at different places (listed in order of priority):
        // - videoScheduledItem
        // - videoScheduledItem.timeline
        // - videoScheduledItem.programScheduledItem
        // - videoScheduledItem.programScheduledItem.timeline
        EntityHasStartAndEndTime startAndEndTimeHolder = scheduledItem;
        if (startAndEndTimeHolder.getStartTime() == null) {
            startAndEndTimeHolder = scheduledItem.getTimeline();
            if (startAndEndTimeHolder == null || startAndEndTimeHolder.getStartTime() == null) {
                ScheduledItem programScheduledItem = scheduledItem.getProgramScheduledItem();
                startAndEndTimeHolder = programScheduledItem;
                if (startAndEndTimeHolder.getStartTime() == null) {
                    startAndEndTimeHolder = programScheduledItem.getTimeline();
                }
            }
        }
        return startAndEndTimeHolder;
    }
}
