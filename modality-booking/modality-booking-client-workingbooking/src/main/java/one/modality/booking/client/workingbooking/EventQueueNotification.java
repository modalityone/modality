package one.modality.booking.client.workingbooking;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.com.bus.call.BusCallService;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import one.modality.base.client.message.receiver.ModalityEntityMessageReceiver;
import one.modality.base.shared.entities.Event;
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
public final class EventQueueNotification {

    private static final int DEBUG_ADDITIONAL_QUEUE_SIZE = 0; // Can be changed to 100 for ex for debugging
    private static final long DEBUG_ADDITIONAL_BOOKING_PERIODIC_MILLIS = 1000L;
    private static final List<SubmitDocumentChangesResult> DEBUG_FINAL_RESULT_HANDLERS = new ArrayList<>();

    private static final Map<Object /* eventPk */, EventQueueNotification> NOTIFICATIONS = new HashMap<>();
    private static final Map<Object /* queueToken */, Consumer<SubmitDocumentChangesResult>> ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS = new HashMap<>();

    static {
        BusCallService.registerBusCallEndpoint(
            DocumentServiceBusAddresses.SUBMIT_DOCUMENT_CHANGES_FINAL_CLIENT_PUSH_ADDRESS,
            (SubmitDocumentChangesResult finalResult) -> {
                if (DEBUG_ADDITIONAL_QUEUE_SIZE > 0) {
                    DEBUG_FINAL_RESULT_HANDLERS.add(finalResult);
                    return "OK";
                }
                boolean notified = notifyFinalResult(finalResult);
                return notified ? "OK" : "KO";
            }
        );
    }

    private final ObjectProperty<EventQueueProgress> progressProperty = new SimpleObjectProperty<>();

    private EventQueueNotification(Event event) {
        // The document service is publishing the progress of the event queue through the "queueProgress" virtual field
        // of the event. So we listen to it to receive updates on the event queue progress.
        ModalityEntityMessageReceiver.getFrontOfficeEntityMessageReceiver().listenEntityChanges(event.getStore());
        StringProperty queueProgressProperty = EntityBindings.getStringFieldProperty(event, Event.queueProgress);
        // The server pushes it using the following String format: processedRequests/totalRequests
        // So we transform it into an EventQueueProgress object and set it to the progressProperty
        FXProperties.runOnPropertyChange(queueProgress -> {
            if (queueProgress != null) {
                int slashIndex = queueProgress.indexOf('/');
                if (slashIndex != -1) {
                    try {
                        int processed = Integer.parseInt(queueProgress.substring(0, slashIndex));
                        int total = Integer.parseInt(queueProgress.substring(slashIndex + 1));
                        if (DEBUG_ADDITIONAL_QUEUE_SIZE > 0 && processed >= total) {
                            int[] additionalProcessed = {0};
                            UiScheduler.schedulePeriodic(DEBUG_ADDITIONAL_BOOKING_PERIODIC_MILLIS, scheduled -> {
                                progressProperty.set(new EventQueueProgress(processed + ++additionalProcessed[0], total + DEBUG_ADDITIONAL_QUEUE_SIZE));
                                if (additionalProcessed[0] >= DEBUG_ADDITIONAL_QUEUE_SIZE) {
                                    scheduled.cancel();
                                    DEBUG_FINAL_RESULT_HANDLERS.forEach(EventQueueNotification::notifyFinalResult);
                                    DEBUG_FINAL_RESULT_HANDLERS.clear();
                                }
                            });
                        }
                        UiScheduler.runInUiThread(() -> progressProperty.set(new EventQueueProgress(processed, total + DEBUG_ADDITIONAL_QUEUE_SIZE)));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }, queueProgressProperty);
    }

    public EventQueueProgress getProgress() {
        return progressProperty.get();
    }

    public ObjectProperty<EventQueueProgress> progressProperty() {
        return progressProperty;
    }

    public static void setEnqueuedBookingFinalResultHandler(Object queueToken, Consumer<SubmitDocumentChangesResult> handler) {
        ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS.put(queueToken, handler);
    }

    public static void removeEnqueuedBookingFinalResultHandler(Object queueToken) {
        ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS.remove(queueToken);
    }

    public static EventQueueNotification getOrCreate(Event event) {
        return NOTIFICATIONS.computeIfAbsent(event.getPrimaryKey(), ignored -> new EventQueueNotification(event));
    }

    private static boolean notifyFinalResult(SubmitDocumentChangesResult finalResult) {
        Consumer<SubmitDocumentChangesResult> handler = ENQUEUED_BOOKING_FINAL_RESULT_HANDLERS.remove(finalResult.queueToken());
        if (handler != null) {
            handler.accept(finalResult);
            return true;
        }
        Console.log("🪣 No handler found for queue token: " + finalResult.queueToken());
        return false;
    }

}
