package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.timelayout.util.TimeUtil;
import dev.webfx.extras.timelayout.util.YearWeek;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.text.Text;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilder;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

import static dev.webfx.stack.orm.dql.DqlStatement.fields;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

final class AccommodationActivity extends OrganizationDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();

    @Override
    public AccommodationPresentationModel getPresentationModel() {
        super.getPresentationModel();
        return pm; // eventId and organizationId will then be updated from route
    }

    private ConventionalUiBuilder ui; // Keeping this reference for activity resume

    @Override
    public Node buildUi() {
        return new Text("Accommodation");
    }

    @Override
    public void onResume() {
        super.onResume();
        //ui.onResume(); // activate search text focus on activity resume
        FXGanttVisibility.setGanttVisibility(GanttVisibility.EVENTS);
    }

    @Override
    public void onPause() {
        FXGanttVisibility.setGanttVisibility(GanttVisibility.HIDDEN);
        super.onPause();
    }

    private ReactiveVisualMapper<Document> groupVisualMapper, masterVisualMapper;

    @Override
    protected void startLogic() {
        // Setting up the group mapper that build the content displayed in the group view
        groupVisualMapper = ReactiveVisualMapper.<Document>createGroupReactiveChain(this, pm)
                .always("{class: 'Document', alias: 'd'}")
                // Applying the event condition
                //.ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("event=?", eventId))
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), organizationId -> where("organization=?", organizationId))
                //.ifNotNullOtherwiseEmpty(pm.ganttSelectedObjectProperty(), x -> where("true"))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), LocalDate.class, day -> where("exists(select Attendance where documentLine.document=d and date = ?)", day))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearWeek.class, week -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfWeek(week),   TimeUtil.getLastDayOfWeek(week)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearMonth.class, month -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfMonth(month), TimeUtil.getLastDayOfMonth(month)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Year.class, year -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfYear(year),   TimeUtil.getLastDayOfYear(year)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Event.class, event -> where("event=?", event))
                .start();

        // Setting up the master mapper that build the content displayed in the master view
        /*masterVisualMapper = ReactiveVisualMapper.<Document>createMasterPushReactiveChain(this, pm)
                .always("{class: 'Document', alias: 'd', orderBy: 'ref desc'}")
                // Always loading the fields required for viewing the booking details
                .always(fields(BookingDetailsPanel.REQUIRED_FIELDS))
                // Applying the event condition
                //.ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("event=?", eventId))
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), organizationId -> where("organization=?", organizationId))
                .ifNotNullOtherwiseEmpty(pm.ganttSelectedObjectProperty(), x -> where("true"))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), LocalDate.class, day   -> where("exists(select Attendance where documentLine.document=d and date = ?)", day))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearWeek.class,  week  -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfWeek(week),   TimeUtil.getLastDayOfWeek(week)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), YearMonth.class, month -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfMonth(month), TimeUtil.getLastDayOfMonth(month)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Year.class,      year  -> where("exists(select Attendance where documentLine.document=d and date >= ? and date <= ?)", TimeUtil.getFirstDayOfYear(year),   TimeUtil.getLastDayOfYear(year)))
                .ifInstanceOf(pm.ganttSelectedObjectProperty(), Event.class,     event -> where("event=?", event))
                // Applying the user search
                .ifTrimNotEmpty(pm.searchTextProperty(), s ->
                        Character.isDigit(s.charAt(0)) ? where("ref = ?", Integer.parseInt(s))
                                : s.contains("@") ? where("lower(person_email) like ?", "%" + s.toLowerCase() + "%")
                                : where("person_abcNames like ?", AbcNames.evaluate(s, true)))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .start();*/
    }

    @Override
    protected void refreshDataOnActive() {
        groupVisualMapper.refreshWhenActive();
        masterVisualMapper.refreshWhenActive();
    }

}
