package one.modality.base.client.time;

import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.markers.HasLocalDate;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class ModalityDates {

    public static String formatDateSeries(List<LocalDate> dates) {
        StringBuilder s = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        LocalDate firstDateSequence = null;
        LocalDate lastDateSequence = null;

        for (LocalDate date : dates) {
            if (firstDateSequence == null) {
                firstDateSequence = date;
                s.append(formatter.format(firstDateSequence));
            } else if (date.toEpochDay() - lastDateSequence.toEpochDay() > 1) {
                if (!lastDateSequence.equals(firstDateSequence)) {
                    s.append("-").append(formatter.format(lastDateSequence));
                }
                firstDateSequence = date;
                s.append(",").append(formatter.format(firstDateSequence));
            }
            lastDateSequence = date;
        }

        if (lastDateSequence != null && !lastDateSequence.equals(firstDateSequence)) {
            s.append("-").append(formatter.format(lastDateSequence));
        }

        String result = s.toString();
        if (result.length() > 127) {
            result = result.substring(0, 63) + "…" + result.substring(result.length() - 63);
        }

        return result;
    }

    public static String formatHasDateSeries(List<? extends HasLocalDate> hasDates) {
        return formatDateSeries(Collections.map(hasDates, HasLocalDate::getDate));
    }

    public static String formatDateInterval(LocalDate date1, LocalDate date2) {
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
    }

}
