package one.modality.booking.client.workingbooking;

/**
 * @author Bruno Salmon
 */
public record EventQueueProgress(
    int processedRequests,
    int totalRequests
) {
}
