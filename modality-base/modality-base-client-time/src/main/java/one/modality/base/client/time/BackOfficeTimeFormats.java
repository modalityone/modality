package one.modality.base.client.time;

import java.time.format.FormatStyle;
import java.time.format.TextStyle;

/**
 * @author Bruno Salmon
 */
public interface BackOfficeTimeFormats {

    /*
    Accepted format classes for LocalTime, LocalTime, LocalDateTime & MonthDay are FormatStyle or DateTimeFormatter.
    Accepted format class for DayOfWeek, Month & MonthYear is TextStyle only.

    TextStyle.FULL & TextStyle.FULL_STANDALONE -> Full text, typically the full description
    Ex: Monday

    TextStyle.SHORT & TextStyle.SHORT_STANDALONE -> Short text, typically an abbreviation
    Ex: Mon

    TextStyle.NARROW & TextStyle.NARROW_STANDALONE -> Narrow text, typically a single letter
    Ex: M


    FormatStyle.FULL -> Full text style, with the most detail.
    Ex DATE:
        EN: Sunday, March 16, 2025
        DE: Sonntag, 16. März 2025
        FR: dimanche 16 mars 2025
        ES: domingo, 16 de marzo de 2025
        PT: domingo, 16 de março de 2025
        VI: Chủ Nhật, 16 tháng 3, 2025
        ZH: 2025年3月16日星期日
    Ex TIME: 3:30:42pm

    FormatStyle.LONG -> Long text style, with lots of detail.
    Ex DATE:
        EN: March 16, 2025
        DE: 16. März 2025
        FR: 16 mars 2025
        ES: 16 de marzo de 2025
        PT: 16 de março de 2025
        VI: 16 tháng 3, 2025
        ZH: 2025年3月16日
    Ex TIME:

    FormatStyle.MEDIUM -> Medium text style, with some detail
    Ex DATE:
        EN: Mar 16, 2025
        DE: 16.03.2025
        FR: 16 mars 2025
        ES: 16 mar 2025
        PT: 16 de mar. de 2025
        VI: 16 thg 3, 2025
        ZH: 2025年3月16日
    Ex TIME:

    FormatStyle.SHORT -> Short text style, typically numeric
    Ex DATE:
        EN: 3/16/25
        DE: 16.03.25
        FR: 16/03/2025
        ES: 16/3/25
        PT: 16/03/2025
        VI: 16/3/25
        ZH: 2025/3/16
    Ex TIME:
    */

    TextStyle DATE_INTERVAL_MONTH_FORMAT = TextStyle.FULL;
    FormatStyle MEDIA_DATE_FORMAT = FormatStyle.SHORT;
    FormatStyle MEDIA_TIME_FORMAT = FormatStyle.SHORT;
    FormatStyle MEDIA_DATE_LONG_FORMAT = FormatStyle.LONG;
    FormatStyle PROGRAM_DAY_TEMPLATE_MONTH_DAY_FORMAT = FormatStyle.LONG;
    FormatStyle RECURRING_EVENT_OPENING_DATE_FORMAT = FormatStyle.SHORT;
    FormatStyle RECURRING_EVENT_OPENING_TIME_FORMAT = FormatStyle.SHORT;
    FormatStyle RECURRING_EVENT_SCHEDULED_ITEM_DATE_FORMAT = FormatStyle.LONG;
    FormatStyle RECURRING_EVENT_SCHEDULED_ITEM_TIME_FORMAT = FormatStyle.SHORT;
    FormatStyle RECURRING_EVENT_ATTENDANCE_DATE_FORMAT = FormatStyle.LONG;

    FormatStyle ACCOMMODATION_STATUS_DATE_FORMAT = FormatStyle.SHORT;
    FormatStyle ROOM_STATUS_DATE_FORMAT = FormatStyle.SHORT;
    FormatStyle ALTER_ROOM_DATE_FORMAT = FormatStyle.SHORT;
    FormatStyle ANNUAL_SCHEDULE_DATE_FORMAT = FormatStyle.SHORT;

}
