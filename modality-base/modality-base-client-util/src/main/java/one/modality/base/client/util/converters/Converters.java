package one.modality.base.client.util.converters;

import javafx.util.StringConverter;
import one.modality.base.shared.domainmodel.formatters.DateFormatter;

import java.time.LocalDate;

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

}
