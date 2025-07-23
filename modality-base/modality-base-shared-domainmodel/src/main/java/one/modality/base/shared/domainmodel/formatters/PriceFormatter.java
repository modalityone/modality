package one.modality.base.shared.domainmodel.formatters;

import dev.webfx.stack.orm.domainmodel.formatter.ValueFormatter;
import dev.webfx.stack.orm.domainmodel.formatter.ValueParser;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.type.Type;
import dev.webfx.platform.util.Numbers;

/**
 * @author Bruno Salmon
 */
public class PriceFormatter implements ValueFormatter, ValueParser {

    public static final PriceFormatter INSTANCE = new PriceFormatter();

    private final String currencySymbol;

    public PriceFormatter() {
        this(null);
    }

    public PriceFormatter(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    @Override
    public Type getFormattedValueType() {
        return PrimType.STRING;
    }

    @Override
    public Object formatValue(Object value) {
        return currencySymbol != null ? formatWithCurrency(value, currencySymbol) : format(value, true);
    }

    public Object format(Object value, boolean show00cents) {
        if (!Numbers.isNumber(value))
            return value;
        String cents = value.toString();
        switch (cents.length()) {
            case 1 : return show00cents || !cents.equals("0") ? "0.0" + cents : "0";
            case 2 : return "0." + cents;
            default: return cents.substring(0, cents.length() - 2) + (!show00cents && cents.endsWith("00") ? "" : "." + cents.substring(cents.length() - 2));
        }
    }

    @Override
    public Object parseValue(Object value) {
        if (value == null || "".equals(value))
            return null;
        return Numbers.toShortestNumber(Numbers.toFloat(value) * 100);
    }

    public static String formatWithoutCurrency(Object value) {
        return formatWithoutCurrency(value, true);
    }

    public static String formatWithoutCurrency(Object value, boolean show00cents) {
        return formatWithCurrency(value, null);
    }

    public static String formatWithCurrency(Object value, String currencySymbol) {
        return formatWithCurrency(value, currencySymbol, false);
    }

    public static String formatWithCurrency(Object value, String currencySymbol, boolean show00cents) {
        String price = (String) INSTANCE.format(value, show00cents);
        return currencySymbol == null || currencySymbol.isEmpty() ? price : currencySymbol.startsWith(" ") ? price + currencySymbol : currencySymbol + price;
    }
}
