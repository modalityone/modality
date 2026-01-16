package one.modality.booking.frontoffice.bookingpage;

/**
 * @deprecated Use {@link one.modality.booking.frontoffice.bookingpage.components.price.UnifiedPriceDisplay}
 * for event-aware price formatting that properly detects the currency from the event's organization.
 * This class uses a hardcoded £ currency symbol which is incorrect for non-GBP events.
 *
 * @author Bruno Salmon
 */
@Deprecated
public final class PriceFormatter {

    /**
     * @deprecated Hardcoded to £. Use EventPriceFormatter or UnifiedPriceDisplay for correct currency.
     */
    @Deprecated
    public static String getCurrencySymbol() {
        return "£";
    }

    /**
     * @deprecated Use {@link one.modality.booking.frontoffice.bookingpage.components.price.UnifiedPriceDisplay#formatPrice(int)}
     */
    @Deprecated
    public static String formatPriceWithCurrencyNoDecimals(int priceInCents) {
        return one.modality.base.shared.domainmodel.formatters.PriceFormatter.formatWithCurrency(priceInCents, getCurrencySymbol());
    }

    /**
     * @deprecated Use {@link one.modality.booking.frontoffice.bookingpage.components.price.UnifiedPriceDisplay#formatPrice(int)}
     */
    @Deprecated
    public static String formatPriceWithCurrencyWithDecimals(int priceInCents) {
        return one.modality.base.shared.domainmodel.formatters.PriceFormatter.formatWithCurrency(priceInCents, getCurrencySymbol(), true);
    }

    /**
     * @deprecated Use base PriceFormatter.formatWithoutCurrency directly if needed
     */
    @Deprecated
    public static String formatPriceNoCurrencyWithDecimals(int priceInCents) {
        return one.modality.base.shared.domainmodel.formatters.PriceFormatter.formatWithoutCurrency(priceInCents);
    }

    /**
     * @deprecated Use base PriceFormatter.formatWithoutCurrency directly if needed
     */
    @Deprecated
    public static String formatPriceNoCurrencyNoDecimals(int amount) {
        return one.modality.base.shared.domainmodel.formatters.PriceFormatter.formatWithoutCurrency(amount, false);
    }

    /**
     * @deprecated Parsing assumes £ currency. Consider using event-aware parsing.
     */
    @Deprecated
    public static int parsePrice(String price) {
        return doublePriceToCentsPrice(Double.parseDouble(price.replace(getCurrencySymbol(), "")));
    }

    /**
     * @deprecated Use base PriceFormatter for price conversion utilities
     */
    @Deprecated
    public static int doublePriceToCentsPrice(double doublePrice) {
        return (int) (doublePrice * 100);
    }

    /**
     * @deprecated Use base PriceFormatter for price conversion utilities
     */
    @Deprecated
    public static double centsPriceToDoublePrice(int priceInCents) {
        return ((double) priceInCents) / 100;
    }


}