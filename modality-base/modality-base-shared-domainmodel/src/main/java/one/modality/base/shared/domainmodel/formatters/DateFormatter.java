package one.modality.base.shared.domainmodel.formatters;

import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.type.Type;
import dev.webfx.platform.util.Dates;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.domainmodel.formatter.ValueFormatter;
import dev.webfx.stack.orm.domainmodel.formatter.ValueParser;
import javafx.util.StringConverter;

import java.time.LocalDate;


/**
 * @author Bruno Salmon
 */
public final class DateFormatter implements ValueFormatter, ValueParser {

    public static final DateFormatter SINGLETON = new DateFormatter();

    private DateFormatter() {
    }

    @Override
    public Type getFormattedValueType() {
        return PrimType.STRING;
    }

    @Override
    public Object formatValue(Object value) {
        return Dates.format(value, "dd/MM/yyyy");
    }

    @Override
    public Object parseValue(Object value) {
        String text = Strings.toSafeString(value);
        if (text.isEmpty())
            return null;
        int p;
        int dayOfMonth = Integer.parseInt(text.substring(0, p = text.indexOf('/')));
        int month = Integer.parseInt(text.substring(p + 1, p = text.indexOf('/', p + 1)));
        int year = Integer.parseInt(text.substring(p + 1, p + 5));
        return LocalDate.of(year, month, dayOfMonth);
    }

    public StringConverter<LocalDate> toStringConverter() {
        return new StringConverter<LocalDate/*GWT*/>() {
            @Override
            public String toString(LocalDate date) {
                return (String) formatValue(date);
            }

            @Override
            public LocalDate fromString(String date) {
                return (LocalDate) parseValue(date);
            }
        };
    }
}
