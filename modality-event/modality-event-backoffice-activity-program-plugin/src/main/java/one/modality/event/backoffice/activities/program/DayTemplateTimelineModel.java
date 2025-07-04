package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.validation.ValidationSupport;
import one.modality.base.shared.entities.Timeline;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class DayTemplateTimelineModel {

    private final Timeline timeline;
    private final DayTemplateModel dayTemplateModel;

    private Runnable syncUiFromModelRunnable;

    DayTemplateTimelineModel(Timeline timeline, DayTemplateModel dayTemplateModel) {
        this.timeline = timeline;
        this.dayTemplateModel = dayTemplateModel;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    ValidationSupport getValidationSupport() {
        return dayTemplateModel.getValidationSupport();
    }

    void setSyncUiFromModelRunnable(Runnable syncUiFromModelRunnable) {
        this.syncUiFromModelRunnable = syncUiFromModelRunnable;
    }

    void resetModelAndUiToInitial() {
        syncUiFromModelRunnable.run();
    }

    void removeTemplateTimeLine() {
        dayTemplateModel.removeTemplateTimeLine(timeline);
    }

}
