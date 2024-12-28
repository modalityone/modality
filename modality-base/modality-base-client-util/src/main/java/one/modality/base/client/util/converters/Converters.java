package one.modality.base.client.util.converters;

import javafx.util.StringConverter;
import one.modality.base.shared.domainmodel.formatters.DateFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Bruno Salmon
 */
public final class Converters {

    public static StringConverter<LocalDate> dateFormatterStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return (String) DateFormatter.SINGLETON.formatValue(date);
            }

            @Override
            public LocalDate fromString(String date) {
                return (LocalDate) DateFormatter.SINGLETON.parseValue(date);
            }
        };
    }

    public static String convertLocalDateToTextFieldValue(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static LocalDate convertTextFieldValueToLocalDate(String value) {
        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null;
        }
    }
}
