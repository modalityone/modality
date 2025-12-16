package one.modality.booking.frontoffice.bookingpage;

/**
 * @author Bruno Salmon
 */
public final class PriceFormatter {

    public static String getCurrencySymbol() {
        return "£";
    }

    public static String formatPriceWithCurrencyNoDecimals(int priceInCents) {
        return one.modality.base.shared.domainmodel.formatters.PriceFormatter.formatWithCurrency(priceInCents, getCurrencySymbol());
    }

    public static String formatPriceWithCurrencyWithDecimals(int priceInCents) {
        return one.modality.base.shared.domainmodel.formatters.PriceFormatter.formatWithCurrency(priceInCents, getCurrencySymbol(), true);
    }

    public static String formatPriceNoCurrencyWithDecimals(int priceInCents) {
        return one.modality.base.shared.domainmodel.formatters.PriceFormatter.formatWithoutCurrency(priceInCents);
    }

    public static String formatPriceNoCurrencyNoDecimals(int amount) {
        return one.modality.base.shared.domainmodel.formatters.PriceFormatter.formatWithoutCurrency(amount, false);
    }

    public static int parsePrice(String price) {
        return doublePriceToCentsPrice(Double.parseDouble(price.replace(getCurrencySymbol(), "")));
    }

    public static int doublePriceToCentsPrice(double doublePrice) {
        return (int) (doublePrice * 100);
    }

    public static double centsPriceToDoublePrice(int priceInCents) {
        return ((double) priceInCents) / 100;
    }


}