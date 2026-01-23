package one.modality.booking.client.workingbooking;

import dev.webfx.platform.console.Console;
import dev.webfx.stack.com.bus.call.BusCallService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesResult;
import one.modality.ecommerce.document.service.buscall.DocumentServiceBusAddresses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
public class EventQueueFinalResultNotification {

    static final List<SubmitDocumentChangesResult> DEBUG_FINAL_RESULT_HANDLERS = new ArrayList<>();
    private static final Map<Object /* queueToken */, Consumer<SubmitDocumentChangesResult>> ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS = new HashMap<>();

    static {
        BusCallService.registerBusCallEndpoint(
            DocumentServiceBusAddresses.SUBMIT_DOCUMENT_CHANGES_FINAL_CLIENT_PUSH_ADDRESS,
            (SubmitDocumentChangesResult finalResult) -> {
                if (EventQueueProgressNotification.DEBUG_ADDITIONAL_UI_ONLY_QUEUE_SIZE > 0) {
                    DEBUG_FINAL_RESULT_HANDLERS.add(finalResult);
                    return "OK";
                }
                boolean notified = notifyFinalResult(finalResult);
                return notified ? "OK" : "KO";
            }
        );
    }

    public static void setEnqueuedBookingFinalResultHandler(Object queueToken, Consumer<SubmitDocumentChangesResult> handler) {
        ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS.put(queueToken, handler);
    }

    public static void removeEnqueuedBookingFinalResultHandler(Object queueToken) {
        ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS.remove(queueToken);
    }

    static boolean notifyFinalResult(SubmitDocumentChangesResult finalResult) {
        Consumer<SubmitDocumentChangesResult> handler = ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS.remove(finalResult.queueToken());
        if (handler != null) {
            handler.accept(finalResult);
            return true;
        }
        Console.log("🪣 No handler found for queue token: " + finalResult.queueToken());
        return false;
    }

    static void notifyAllFinalResults() {
        DEBUG_FINAL_RESULT_HANDLERS.forEach(EventQueueFinalResultNotification::notifyFinalResult);
        DEBUG_FINAL_RESULT_HANDLERS.clear();
    }
}
