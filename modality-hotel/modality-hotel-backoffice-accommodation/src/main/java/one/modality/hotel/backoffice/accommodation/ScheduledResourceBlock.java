package one.modality.hotel.backoffice.accommodation;

import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;

/**
 * @author Bruno Salmon
 */
public final class ScheduledResourceBlock implements AccommodationBlock {
    private final ResourceConfiguration resourceConfiguration;
    private final boolean available;
    private final boolean online;
    private final int remaining;

    public ScheduledResourceBlock(ScheduledResource sr) {
        resourceConfiguration = sr.getResourceConfiguration();
        available = sr.isAvailable();
        online = sr.isOnline();
        int max = sr.getMax();
        // The "booked" field is an extra computed fields added by the ReactiveEntitiesMapper in RoomCalendarGanttCanvas
        int booked = sr.getIntegerFieldValue("booked");
        remaining = max - booked;
    }

    @Override
    public ResourceConfiguration getRoomConfiguration() {
        return resourceConfiguration;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isOnline() {
        return online;
    }

    public int getRemaining() {
        return remaining;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduledResourceBlock that = (ScheduledResourceBlock) o;

        if (available != that.available) return false;
        if (online != that.online) return false;
        if (remaining != that.remaining) return false;
        return resourceConfiguration.equals(that.resourceConfiguration);
    }

    @Override
    public int hashCode() {
        int result = resourceConfiguration.hashCode();
        result = 31 * result + (available ? 1 : 0);
        result = 31 * result + (online ? 1 : 0);
        result = 31 * result + remaining;
        return result;
    }
}
