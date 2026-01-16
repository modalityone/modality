package one.modality.base.shared.entities.util;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class Attendances {

    public static LocalDate getDate(Attendance a) {
        LocalDate date = a.getDate();
        if (date != null)
            return date;

        ScheduledItem scheduledItem = a.getScheduledItem();
        if (scheduledItem != null)
            return scheduledItem.getDate();

        return null;
    }

    public static Site getSite(Attendance a) {
        ScheduledItem scheduledItem = a.getScheduledItem();
        Site site  = scheduledItem != null ? scheduledItem.getSite() : null;
        if (site == null) {
            DocumentLine documentLine = a.getDocumentLine();
            if (documentLine != null)
                site = documentLine.getSite();
        }
        return site;
    }

    public static Item getItem(Attendance a) {
        ScheduledItem scheduledItem = a.getScheduledItem();
        Item item  = scheduledItem != null ? scheduledItem.getItem() : null;
        if (item == null) {
            DocumentLine documentLine = a.getDocumentLine();
            if (documentLine != null)
                item = documentLine.getItem();
        }
        return item;
    }

    public static boolean isOfSiteAndItem(Attendance a, Site site, Item item) {
        return Entities.samePrimaryKey(getSite(a), site) && Entities.samePrimaryKey(getItem(a), item);
    }

    public static boolean attendanceMatchesDateOrScheduledItem(Attendance a, Object dateOrScheduledItem) {
        if (dateOrScheduledItem instanceof ScheduledItem scheduledItem) {
            return Entities.sameId(a.getScheduledItem(), scheduledItem) || Objects.equals(a.getDate(), scheduledItem.getDate());
        }
        // dateOrScheduledItem is a LocalDate
        return Objects.equals(a.getDate(), dateOrScheduledItem);
    }
}
