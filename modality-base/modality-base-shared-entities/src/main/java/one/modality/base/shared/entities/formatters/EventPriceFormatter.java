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
        // Handle null event - default to GBP
        if (event == null)
            return "GBP";

        Object organizationPk = Entities.getPrimaryKey(event.getOrganizationId());
        if (organizationPk != null) {
            int orgId = Numbers.toInteger(organizationPk);
            // KMCF (organization 2) uses EUR
            if (orgId == 2) {
                return "EUR";
            }
            // KMCNY (organization 187) uses USD
            if (orgId == 187) {
                return "USD";
            }
        }

        // Default to GBP (UK)
        return "GBP";
    }

    public static String getEventCurrencySymbol(Event event) {
        String currencyCode = getEventCurrencyCode(event);
        switch (currencyCode) {
            case "USD":
                return "$ ";
            case "EUR":
                return " €";
            default:
                return "£ ";
        }
    }

    public static String formatWithCurrency(Object value, Event event) {
        return formatWithCurrency(value, getEventCurrencySymbol(event));
    }

    public static String format(Object value, Event event, boolean withCurrency) {
        return formatWithCurrency(value, withCurrency ? getEventCurrencySymbol(event) : "");
    }
}
