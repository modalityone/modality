package one.modality.base.client.messaging;

import dev.webfx.platform.async.Handler;
import dev.webfx.stack.com.bus.BusService;
import dev.webfx.stack.com.bus.Registration;
import dev.webfx.stack.session.state.client.fx.FXConnected;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class ModalityMessaging {

    private static final String FRONT_OFFICE_MESSAGING_ADDRESS = "front-office/messaging";
    private static List<Handler<Object>> FRONT_OFFICE_MESSAGE_HANDLERS;

    public static void publishFrontOfficeMessage(Object messageBody) {
        BusService.bus().publish(FRONT_OFFICE_MESSAGING_ADDRESS, messageBody);
    }

    public static Registration addFrontOfficeMessageBodyHandler(Handler<Object> messageBodyHandler) {
        if (FRONT_OFFICE_MESSAGE_HANDLERS == null) { // Initialization on first call
            FRONT_OFFICE_MESSAGE_HANDLERS = new ArrayList<>();
            // To make this work, the client bus call service must listen server calls! This takes place as soon as the
            // connection to the server is ready, or each time we reconnect to the server:
            FXConnected.runOnEachConnected(() ->
                BusService.bus().register(FRONT_OFFICE_MESSAGING_ADDRESS, message ->
                    FRONT_OFFICE_MESSAGE_HANDLERS.forEach(h -> h.handle(message.body()))
                )
            );

        }
        FRONT_OFFICE_MESSAGE_HANDLERS.add(messageBodyHandler);
        return () -> FRONT_OFFICE_MESSAGE_HANDLERS.remove(messageBodyHandler);
    }
}
