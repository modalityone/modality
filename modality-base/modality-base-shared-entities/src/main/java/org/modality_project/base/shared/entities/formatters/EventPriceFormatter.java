package org.modality_project.base.shared.entities.formatters;

import org.modality_project.base.shared.domainmodel.formatters.PriceFormatter;
import org.modality_project.base.shared.entities.Event;
import dev.webfx.stack.framework.shared.orm.entity.EntityId;
import dev.webfx.platform.util.Numbers;

/**
 * @author Bruno Salmon
 */
public class EventPriceFormatter extends PriceFormatter {

    public EventPriceFormatter(Event event) {
        super(getEventCurrencySymbol(event));
    }

    public static String getEventCurrencySymbol(Event event) {
        // Temporary hardcoded
        EntityId organizationId = event == null ? null : event.getOrganizationId();
        if (organizationId == null)
            return null;
        boolean isKMCF = Numbers.toInteger(organizationId.getPrimaryKey()) == 2;
        return isKMCF ? " €" : "£";
    }

    public static String formatWithCurrency(Object value, Event event) {
        return formatWithCurrency(value, getEventCurrencySymbol(event));
    }

    public static String format(Object value, Event event, boolean withCurrency) {
        return formatWithCurrency(value, withCurrency ? getEventCurrencySymbol(event) : "");
    }
}
