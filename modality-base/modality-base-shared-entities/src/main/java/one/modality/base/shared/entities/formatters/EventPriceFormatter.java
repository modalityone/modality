package one.modality.base.shared.entities.formatters;

import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.Event;

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
    if (organizationId == null) return null;
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
