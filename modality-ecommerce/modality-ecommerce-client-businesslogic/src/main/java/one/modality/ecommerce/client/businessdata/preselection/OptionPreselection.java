package one.modality.ecommerce.client.businessdata.preselection;

import one.modality.base.shared.entities.Option;
import one.modality.hotel.shared.businessdata.time.DateTimeRange;
import one.modality.hotel.shared.businessdata.time.DayTimeRange;

/**
 * @author Bruno Salmon
 */
public final class OptionPreselection {
    private final Option option;
    private final DateTimeRange dateTimeRange;
    private final DayTimeRange dayTimeRange;

    OptionPreselection(Option option, String dateTimeRange, String dayTimeRange) {
        this(option, DateTimeRange.parse(dateTimeRange), dayTimeRange);
    }

    OptionPreselection(Option option, DateTimeRange dateTimeRange, String dayTimeRange) {
        this(option, dateTimeRange, DayTimeRange.parse(dayTimeRange));
    }

    OptionPreselection(Option option, DateTimeRange dateTimeRange, DayTimeRange dayTimeRange) {
        this.option = option;
        this.dateTimeRange = dateTimeRange;
        this.dayTimeRange = dayTimeRange;
    }

    public Option getOption() {
        return option;
    }

    public DateTimeRange getDateTimeRange() {
        return dateTimeRange;
    }

    public DayTimeRange getDayTimeRange() {
        return dayTimeRange;
    }
}
