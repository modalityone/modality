package one.modality.event.frontoffice.medias;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.lciimpl.EntityDomainReader;
import one.modality.base.client.entities.functions.I18nFunction;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;

import java.time.Duration;

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

    public static String formatDuration(Duration duration) {
        //TODO: use LocalizedTime instead
        if (duration == null)
            return "xx:xx";
        return formatDuration((int) duration.toHours(), ((int) duration.toMinutes()) % 60, ((int) duration.toSeconds()) % 60);
    }

    public static String formatDuration(javafx.util.Duration javafxDuration) {
        if (javafxDuration == null || javafxDuration.isIndefinite() || javafxDuration.isUnknown())
            return "xx:xx";
        return formatDuration((int) javafxDuration.toHours(), (int) javafxDuration.toMinutes(), ((int) javafxDuration.toSeconds()) % 60);
    }

    public static String formatDuration(int hours, int minutes, int seconds) {
        return (hours == 0 ? "" : (hours < 10 ? "0" : "") + hours + ":") + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
}
