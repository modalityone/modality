package one.modality.event.frontoffice.activities.booking.process.event.slides;

/**
 * Convenient enum for identifying event types from the database. The primary keys are temporary hardcoded (will be
 * configurable later). Also, some type (like STTP) are specific to NKT (will be generalised later for Modality).
 *
 * @author Bruno Salmon
 */
public enum KnownEventType {

    GP_CLASSES(47),
    STTP(48);

    private final int typeId;

    KnownEventType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }
}
