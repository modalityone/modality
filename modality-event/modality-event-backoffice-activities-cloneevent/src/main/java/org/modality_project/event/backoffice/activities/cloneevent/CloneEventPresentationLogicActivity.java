package org.modality_project.event.backoffice.activities.cloneevent;

import org.modality_project.base.client.activity.eventdependent.EventDependentPresentationLogicActivity;
import org.modality_project.base.shared.entities.Event;
import org.modality_project.ecommerce.backoffice.operations.routes.bookings.RouteToBookingsRequest;
import dev.webfx.kit.util.properties.Properties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.db.submit.SubmitService;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class CloneEventPresentationLogicActivity extends EventDependentPresentationLogicActivity<CloneEventPresentationModel> {

    public CloneEventPresentationLogicActivity() {
        super(CloneEventPresentationModel::new);
    }

    @Override
    protected void startLogic(CloneEventPresentationModel pm) {
        // Load and display fees groups now but also on event change
        Properties.runNowAndOnPropertiesChange(() -> {
            pm.setName(null);
            pm.setDate(null);
            onEventOptions().onSuccess(options -> {
                Event event = getEvent();
                pm.setName(event.getName());
                pm.setDate(event.getStartDate());
            });
        }, pm.eventIdProperty());

        pm.setOnSubmit(event -> {
            LocalDate startDate = pm.getDate();
            SubmitService.executeSubmit(SubmitArgument.builder()
                    .setStatement("select copy_event(?,?,?)")
                    .setParameters(getEventId(), pm.getName(), startDate)
                    .setReturnGeneratedKeys(true)
                    .setDataSourceId(getDataSourceId())
                    .build())
                    .onSuccess(result ->
                        UiScheduler.runInUiThread(() ->
                            new RouteToBookingsRequest(result.getGeneratedKeys()[0], getHistory()).execute()
                    ));
        });
    }
}
