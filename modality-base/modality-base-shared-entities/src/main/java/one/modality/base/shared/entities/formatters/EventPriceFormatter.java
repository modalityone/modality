package one.modality.base.shared.entities.formatters;

import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public class EventPriceFormatter extends PriceFormatter {

    public EventPriceFormatter(Event event) {
        super(getEventCurrencySymbol(event));
    }

    public static String getEventCurrencyCode(Event event) {
        // Temporary hardcoded
        if (Entities.samePrimaryKey(event, 1647)) // Fall Festival 2025 - Arizona
            return "USD";
        Object organizationPk = Entities.getPrimaryKey(event.getOrganizationId());
        if (organizationPk == null)
            return null;
        boolean isKMCF = Numbers.toInteger(organizationPk) == 2;
        return isKMCF ? "EUR" : "GBP";
    }

    public static String getEventCurrencySymbol(Event event) {
        // Temporary hardcoded
        if ("USD".equals(getEventCurrencyCode(event)))
            return "$ ";
        if ("EUR".equals(getEventCurrencyCode(event)))
            return " €";
        return "£ ";
    }

    public static String formatWithCurrency(Object value, Event event) {
        return formatWithCurrency(value, getEventCurrencySymbol(event));
    }

    public static String format(Object value, Event event, boolean withCurrency) {
        return formatWithCurrency(value, withCurrency ? getEventCurrencySymbol(event) : "");
    }
}
