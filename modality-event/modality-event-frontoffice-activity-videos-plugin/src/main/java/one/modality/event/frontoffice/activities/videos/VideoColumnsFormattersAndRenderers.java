package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.type.PrimType;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.formatter.FormatterRegistry;
import javafx.scene.control.Button;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.time.LocalTime;

final class VideoColumnsFormattersAndRenderers {

    private static BrowsingHistory BROWSING_HISTORY;

    static {
        FormatterRegistry.registerFormatter("videoDate", PrimType.STRING, date -> LocalizedTime.formatMonthDayProperty((LocalDate) date, FrontOfficeTimeFormats.VOD_TODAY_MONTH_DAY_FORMAT));
        FormatterRegistry.registerFormatter("videoTimeRange", PrimType.STRING, timeRange -> {
            Object[] times = (Object[]) timeRange;
            return LocalizedTime.formatLocalTimeRangeProperty((LocalTime) times[0], (LocalTime) times[1], FrontOfficeTimeFormats.VIDEO_DAY_TIME_FORMAT);
        });
        ValueRendererRegistry.registerValueRenderer("videoStatus", (value, context) -> {
            ScheduledItem videoScheduledItem = (ScheduledItem) value;
            String i18nKey = VideoState.getVideoStatusI18nKey(videoScheduledItem);
            if (VideosI18nKeys.Available.equals(i18nKey)) {
                Button button = Bootstrap.dangerButton(I18nControls.newButton(VideosI18nKeys.Watch));
                button.setOnAction(e -> {
                    BROWSING_HISTORY.push(Page3LivestreamPlayerRouting.getLivestreamPath(videoScheduledItem.getEventId()));
                });
                return button;
            }
            return I18nControls.newLabel(I18nKeys.upperCase(i18nKey));
        });
    }

    static void registerRenderers(BrowsingHistory browsingHistory) {
        // Actually done (only once) in static initializer above
        BROWSING_HISTORY = browsingHistory;
    }

}
