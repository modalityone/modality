package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class CompletePaymentArgument {

    private final Object paymentPrimaryKey; // PK of the generated payment in the database (MoneyTransfer in Modality)
    private final boolean live;
    private final String gatewayName;
    private final String gatewayCompletePaymentPayload;

    public CompletePaymentArgument(Object paymentPrimaryKey, boolean live, String gatewayName, String gatewayCompletePaymentPayload) {
        this.paymentPrimaryKey = paymentPrimaryKey;
        this.live = live;
        this.gatewayName = gatewayName;
        this.gatewayCompletePaymentPayload = gatewayCompletePaymentPayload;
    }

    public Object getPaymentPrimaryKey() {
        return paymentPrimaryKey;
    }

    public boolean isLive() {
        return live;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public String getGatewayCompletePaymentPayload() {
        return gatewayCompletePaymentPayload;
    }
}
