package one.modality.base.client.time;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.markers.HasLocalDate;

import java.time.LocalDate;
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

}
