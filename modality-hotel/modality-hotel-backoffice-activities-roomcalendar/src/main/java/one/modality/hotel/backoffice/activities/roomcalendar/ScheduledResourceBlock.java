package one.modality.hotel.backoffice.activities.roomcalendar;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.ScheduledResource;

/**
 * @author Bruno Salmon
 */
final class ScheduledResourceBlock {
    private final Entity resourceConfiguration;
    private final int available;

    public ScheduledResourceBlock(ScheduledResource sr) {
        resourceConfiguration = sr.getForeignEntity("configuration");
        int booked = sr.getIntegerFieldValue("booked");
        int max = sr.getIntegerFieldValue("max");
        available = max - booked;
    }

    public Entity getResourceConfiguration() {
        return resourceConfiguration;
    }

    public String getResourceName() {
        return (String) resourceConfiguration.evaluate("name");
    }

    public int getAvailable() {
        return available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduledResourceBlock that = (ScheduledResourceBlock) o;

        if (available != that.available) return false;
        return resourceConfiguration == that.resourceConfiguration;
    }

    @Override
    public int hashCode() {
        int result = resourceConfiguration.hashCode();
        result = 31 * result + available;
        return result;
    }
}
