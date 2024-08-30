package one.modality.ecommerce.payment.server.gateway;

/**
 * @author Bruno Salmon
 */
public class GatewayCompletePaymentArgument {

    private final boolean live;
    private final String accessToken;
    private final String payload;

    public GatewayCompletePaymentArgument(boolean live, String accessToken, String payload) {
        this.live = live;
        this.accessToken = accessToken;
        this.payload = payload;
    }

    public boolean isLive() {
        return live;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getPayload() {
        return payload;
    }
}
