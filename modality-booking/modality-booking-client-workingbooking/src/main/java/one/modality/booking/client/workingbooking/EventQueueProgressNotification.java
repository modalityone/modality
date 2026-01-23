package one.modality.booking.client.workingbooking;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import one.modality.base.client.message.receiver.ModalityEntityMessageReceiver;
import one.modality.base.shared.entities.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public final class EventQueueProgressNotification {

    static final int DEBUG_ADDITIONAL_UI_ONLY_QUEUE_SIZE = 0; // Can be changed to 100 for ex for debugging
    private static final long DEBUG_ADDITIONAL_UI_ONLY_BOOKING_PERIODIC_MILLIS = 1000L;

    private static final Map<Object /* eventPk */, EventQueueProgressNotification> NOTIFICATIONS = new HashMap<>();

    private final ObjectProperty<EventQueueProgress> progressProperty = new SimpleObjectProperty<>();

    private EventQueueProgressNotification(Event event) {
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
                        if (DEBUG_ADDITIONAL_UI_ONLY_QUEUE_SIZE > 0 && processed >= total) {
                            int[] additionalProcessed = {0};
                            UiScheduler.schedulePeriodic(DEBUG_ADDITIONAL_UI_ONLY_BOOKING_PERIODIC_MILLIS, scheduled -> {
                                progressProperty.set(new EventQueueProgress(processed + ++additionalProcessed[0], total + DEBUG_ADDITIONAL_UI_ONLY_QUEUE_SIZE));
                                if (additionalProcessed[0] >= DEBUG_ADDITIONAL_UI_ONLY_QUEUE_SIZE) {
                                    scheduled.cancel();
                                    EventQueueFinalResultNotification.notifyAllFinalResults();
                                }
                            });
                        }
                        UiScheduler.runInUiThread(() -> progressProperty.set(new EventQueueProgress(processed, total + DEBUG_ADDITIONAL_UI_ONLY_QUEUE_SIZE)));
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

    public static EventQueueProgressNotification getOrCreate(Event event) {
        return NOTIFICATIONS.computeIfAbsent(event.getPrimaryKey(), ignored -> new EventQueueProgressNotification(event));
    }

}
