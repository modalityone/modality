package one.modality.base.shared.entities;

/**
 * @author Bruno Salmon
 */
public enum EventState {
    DRAFT,
    ADVERTISABLE,
    TESTABLE,
    TESTING,
    OPENABLE,
    OPEN,
    ON_HOLD,
    RESTRICTED,
    CLOSED,
    RECONCILIED,
    FINALISED,
    ARCHIVED;

    static EventState of(String state) {
        return state == null ? null : valueOf(state);
    }
}
