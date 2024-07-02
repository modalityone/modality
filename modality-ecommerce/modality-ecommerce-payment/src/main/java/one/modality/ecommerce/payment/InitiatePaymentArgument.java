package one.modality.ecommerce.payment;

import dev.webfx.platform.useragent.UserAgent;

/**
 * @author Bruno Salmon
 */
public final class InitiatePaymentArgument {

    private final int amount;
    private final Object documentPrimaryKey;
    // TODO: spread payment over several bookings
    private final boolean seamlessAllowed;

    public InitiatePaymentArgument(int amount, Object documentPrimaryKey) {
        this(amount, documentPrimaryKey, UserAgent.isBrowser());
    }

    public InitiatePaymentArgument(int amount, Object documentPrimaryKey, boolean seamlessAllowed) {
        this.amount = amount;
        this.documentPrimaryKey = documentPrimaryKey;
        this.seamlessAllowed = seamlessAllowed;
    }

    public int getAmount() {
        return amount;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

    public boolean isSeamlessAllowed() {
        return seamlessAllowed;
    }
}
