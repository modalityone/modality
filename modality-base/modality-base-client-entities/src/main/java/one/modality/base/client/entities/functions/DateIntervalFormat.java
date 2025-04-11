package one.modality.base.client.entities.functions;

import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.type.Type;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.orm.expression.lci.DomainReader;
import dev.webfx.stack.orm.expression.terms.function.Function;
import one.modality.base.client.time.BackOfficeTimeFormats;

import java.time.LocalDate;
import java.time.Month;

/**
 * @author Bruno Salmon
 */
public final class DateIntervalFormat extends Function {

    public DateIntervalFormat() {
        super("dateIntervalFormat", new String[] {"date1", "date2"}, new Type[] {null, null}, PrimType.STRING, true);
    }

    @Override
    public Object evaluate(Object argument, DomainReader domainReader) {
        try {
            Object[] arguments = (Object[]) argument;

            LocalDate date1 = Times.toLocalDate(arguments[0]);
            LocalDate date2 = Times.toLocalDate(arguments[1]);

            if (date1 == null || date2 == null)
                return null;

            int day1 = date1.getDayOfMonth();
            Month month1 = date1.getMonth();
            String month1Name = LocalizedTime.formatMonth(month1, BackOfficeTimeFormats.DATE_INTERVAL_MONTH_FORMAT);
            int day2 = date2.getDayOfMonth();
            Month month2 = date2.getMonth();
            int year2 = date2.getYear();
            StringBuilder sb = new StringBuilder();
            if (month1 == month2) {
                sb.append(day1);
                if (day2 != day1)
                    sb.append('-').append(day2);
                sb.append(' ').append(month1Name);
            } else
                sb.append(day1).append(' ').append(month1Name).append(" - ").append(day2).append(' ').append(LocalizedTime.formatMonth(month2, BackOfficeTimeFormats.DATE_INTERVAL_MONTH_FORMAT));
            if (year2 != LocalDate.now().getYear())
                sb.append(' ').append(year2);
            return sb.toString();
        } catch (Exception e) {
            return argument;
        }
    }
}
