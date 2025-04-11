package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.scene.text.Text;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.HasEndTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

final class VideoColumnsRenderers {

    static {
        ValueRendererRegistry.registerValueRenderer("videoDate", (date, context) -> {
            Text text1 = new Text();
            text1.textProperty().bind(LocalizedTime.formatMonthDayProperty((LocalDate) date, FrontOfficeTimeFormats.VOD_TODAY_MONTH_DAY_FORMAT));
            return text1;
        });
        ValueRendererRegistry.registerValueRenderer("videoStatus", (value, context) -> {
            String i18nKey1 = getVideoStatusI18nKey((ScheduledItem) value);
            return I18nControls.newLabel(I18nKeys.upperCase(i18nKey1));
        });
        ValueRendererRegistry.registerValueRenderer("videoTimeRange", (timeRange, context) -> {
            Object[] times = (Object[]) timeRange;
            Text text = new Text();
            text.textProperty().bind(LocalizedTime.formatLocalTimeRangeProperty((LocalTime) times[0], (LocalTime) times[1], FrontOfficeTimeFormats.VIDEO_DAY_TIME_FORMAT));
            return text;
        });
        ValueRendererRegistry.registerValueRenderer("videoWatchButton", (value, context) -> {
            String i18nKey = getVideoStatusI18nKey((ScheduledItem) value);
            return VideosI18nKeys.Available.equals(i18nKey) ? Bootstrap.dangerButton(I18nControls.newButton(VideosI18nKeys.Watch)) : new Text();
        });

    }

    private static String getVideoStatusI18nKey(ScheduledItem scheduledItem) {
        Event event = scheduledItem.getEvent();
        LocalDateTime expirationDate = Objects.coalesce(scheduledItem.getExpirationDate(), event.getVodExpirationDate());
        String i18nKey = VideosI18nKeys.OnTime;
        if (expirationDate != null && Times.isPast(expirationDate, Event.getEventClock())) {
            i18nKey = VideosI18nKeys.Expired;
        } else if (scheduledItem.isPublished()) {
            i18nKey = VideosI18nKeys.Available;
        } else if (scheduledItem.isVodDelayed()) {
            i18nKey = VideosI18nKeys.VideoDelayed;
        } else {
            HasEndTime endTimeHolder = scheduledItem.getProgramScheduledItem();
            if (!event.isRecurringWithVideo())
                endTimeHolder = scheduledItem.getProgramScheduledItem().getTimeline();
            LocalDateTime sessionEnd = scheduledItem.getDate().atTime(endTimeHolder.getEndTime());
            if (Event.nowInEventTimezone().isAfter(sessionEnd)) {
                int vodProcessingTimeMinute = Objects.coalesce( event.getVodProcessingTimeMinutes(), 60);
                if (Event.nowInEventTimezone().isAfter(sessionEnd.plusMinutes(vodProcessingTimeMinute)))
                    i18nKey = VideosI18nKeys.VideoDelayed;
                else
                    i18nKey = VideosI18nKeys.RecordingSoonAvailable;
            }
        }
        return i18nKey;
    }

    static void registerRenderers() {
        // Actually done (only once) in static initializer above
    }

}
