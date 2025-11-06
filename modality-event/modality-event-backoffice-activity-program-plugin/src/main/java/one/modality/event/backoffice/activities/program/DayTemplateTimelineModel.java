package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.validation.ValidationSupport;
import one.modality.base.shared.entities.Timeline;

/**
 * Model for a single timeline item within a day template.
 * This model represents one timeline entry (a teaching session) in a day template.
 * A timeline defines when a teaching session occurs during the day and what item is taught.
 *
 * <p>Each timeline contains:
 * <ul>
 *   <li>Item: The teaching item (e.g., "Heart Jewel", "Modern Buddhism")</li>
 *   <li>Start time: When the session begins (e.g., 9:00)</li>
 *   <li>End time: When the session ends (e.g., 12:00)</li>
 *   <li>Name: Optional custom name for this timeline</li>
 *   <li>Audio/Video offered flags: Whether recordings are available</li>
 *   <li>Site: Where the session takes place</li>
 * </ul>
 *
 * <p>This model acts as a lightweight wrapper around the {@link Timeline} entity,
 * delegating to its parent {@link DayTemplateModel} for validation and deletion operations.
 *
 * <p>Relationship hierarchy:
 * <pre>
 * ProgramModel
 *   └── DayTemplateModel (one day template, e.g., "Morning Session")
 *         └── DayTemplateTimelineModel (one timeline, e.g., "9:00-12:00 Heart Jewel")
 * </pre>
 *
 * @author David Hello
 * @author Bruno Salmon
 *
 * @see DayTemplateModel
 * @see Timeline
 * @see DayTemplateTimelineView
 */
final class DayTemplateTimelineModel {

    /**
     * The underlying Timeline entity managed by this model.
     * Contains the actual data (item, times, flags, etc.).
     */
    private final Timeline timeline;

    /**
     * Reference to the parent day template model.
     * Used for delegating validation and deletion operations.
     */
    private final DayTemplateModel dayTemplateModel;

    /**
     * Callback to synchronize the UI when the model state changes.
     * Set by the associated {@link DayTemplateTimelineView}.
     */
    private Runnable syncUiFromModelRunnable;

    /**
     * Constructs a new DayTemplateTimelineModel wrapping a Timeline entity.
     *
     * @param timeline The Timeline entity to manage
     * @param dayTemplateModel The parent day template model
     */
    DayTemplateTimelineModel(Timeline timeline, DayTemplateModel dayTemplateModel) {
        this.timeline = timeline;
        this.dayTemplateModel = dayTemplateModel;
    }

    /**
     * Returns the underlying Timeline entity managed by this model.
     *
     * @return The Timeline entity containing item, times, and other properties
     */
    public Timeline getTimeline() {
        return timeline;
    }

    /**
     * Returns the validation support instance from the parent model.
     * The validation support is shared across all timelines in the same day template,
     * allowing for coordinated form validation.
     *
     * @return The ValidationSupport instance for this timeline's form fields
     */
    ValidationSupport getValidationSupport() {
        return dayTemplateModel.getValidationSupport();
    }

    /**
     * Sets the callback for synchronizing UI when the model changes.
     * This callback is invoked when the model state is reset to initial values,
     * ensuring the UI reflects the current model state.
     *
     * @param syncUiFromModelRunnable Callback to update UI from model state
     */
    void setSyncUiFromModelRunnable(Runnable syncUiFromModelRunnable) {
        this.syncUiFromModelRunnable = syncUiFromModelRunnable;
    }

    /**
     * Resets the model and UI to their initial state.
     * This is called when the user cancels changes or when data is reloaded from the database.
     * The UI is updated by invoking the registered sync callback.
     */
    void resetModelAndUiToInitial() {
        syncUiFromModelRunnable.run();
    }

    /**
     * Removes this timeline from the parent day template.
     * This operation:
     * <ul>
     *   <li>Removes the timeline from the day template's timeline list</li>
     *   <li>Marks the timeline entity for deletion in the UpdateStore</li>
     *   <li>Updates the UI to reflect the removal</li>
     * </ul>
     *
     * The actual database deletion occurs when the user saves changes.
     */
    void removeTemplateTimeLine() {
        dayTemplateModel.removeTemplateTimeLine(timeline);
    }

}
