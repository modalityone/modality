package one.modality.base.shared.entities;

/**
 * @author Bruno Salmon
 */
public enum CleaningState {

    DIRTY,
    TO_INSPECT,
    READY;

    public static CleaningState of(String state) {
        return state == null ? null : valueOf(state);
    }
}
