package one.modality.booking.client.workingbooking;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.com.bus.call.BusCallService;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import one.modality.base.client.message.receiver.ModalityEntityMessageReceiver;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.document.service.SubmitDocumentChangesResult;
import one.modality.ecommerce.document.service.buscall.DocumentServiceBusAddresses;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
final class EventQueueNotification {

    private static final Map<Object /* eventPk */, EventQueueNotification> NOTIFICATIONS = new HashMap<>();
    private static final Map<Object /* queueToken */, Consumer<SubmitDocumentChangesResult>> ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS = new HashMap<>();

    static {
        BusCallService.registerBusCallEndpoint(
            DocumentServiceBusAddresses.SUBMIT_DOCUMENT_CHANGES_CLIENT_PUSH_ADDRESS,
            (SubmitDocumentChangesResult result) -> {
                Consumer<SubmitDocumentChangesResult> handler = ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS.remove(result.queueToken());
                if (handler != null) {
                    handler.accept(result);
                    return "OK";
                }
                Console.log("🪣 No handler found for queue token: " + result.queueToken());
                return "KO";
            }
        );
    }

    private final ObjectProperty<EventQueueProgress> progressProperty = new SimpleObjectProperty<>();

    EventQueueNotification(Event event) {
        // The document service is publishing the progress of the event queue through the "queueProgress" virtual field
        // of the event. So we listen to it to receive updates on the event queue progress.
        ModalityEntityMessageReceiver.getFrontOfficeEntityMessageReceiver().listenEntityChanges(event.getStore());
        StringProperty queueProgressProperty = EntityBindings.getStringFieldProperty(event, Event.queueProgress);
        // The server pushes it using the following String format: processedRequests/totalRequests
        // So we transform it into an EventQueueProgress object and set it to the progressProperty
        FXProperties.runOnPropertyChange(queueProgress -> {
            EventQueueProgress progress = null;
            if (queueProgress != null) {
                int slashIndex = queueProgress.indexOf('/');
                if (slashIndex != -1) {
                    try {
                        int processed = Integer.parseInt(queueProgress.substring(0, slashIndex));
                        int total = Integer.parseInt(queueProgress.substring(slashIndex + 1));
                        progress = new EventQueueProgress(processed, total);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            this.progressProperty.set(progress);
        }, queueProgressProperty);
        //
    }

    EventQueueProgress getProgress() {
        return progressProperty.get();
    }

    ObjectProperty<EventQueueProgress> progressProperty() {
        return progressProperty;
    }

    static void setEnqueuedBookingFinalResultHandler(Object queueToken, Consumer<SubmitDocumentChangesResult> handler) {
        ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS.put(queueToken, handler);
    }

    static EventQueueNotification create(Event event) {
        return NOTIFICATIONS.computeIfAbsent(event.getPrimaryKey(), ignored -> new EventQueueNotification(event));
    }

}
