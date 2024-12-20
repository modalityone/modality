package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.visual.controls.grid.SkinnedVisualGrid;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.client.event.fx.FXEvent;

import java.time.format.DateTimeFormatter;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

final class RecurringEventAttendanceView {

    private static final String EVENT_COLUMNS = "[" +
            "{expression: 'state', label: 'Status', renderer: 'eventStateRenderer'}," +
            "{expression: 'name', label: 'Name'}," +
            "{expression: 'type', label: 'TypeOfEvent'}," +
            "{expression: '(select site.name from Timeline where event=e limit 1)', label: 'Location'}," +
            "{expression: 'dateIntervalFormat(startDate, endDate)', label: 'Dates'}" +
            "]";

    private static final String BOOKINGS_COLUMNS = "[" + // Note: the starting class is DocumentLine
           "{expression: 'document.(`#` + ref + ` ` + person_name)'}," +
           "{expression: 'document', textAlign: 'center', foreignColumns: 'this', renderer: 'confirmRenderer', prefWidth: 70}" +
           "]";

    private final RecurringEventsActivity activity;
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final VisualGrid eventTable = new VisualGrid();
    private final ColumnsPane attendancePane = new ColumnsPane(20, 20);
    private ReactiveObjectsMapper<ScheduledItem, Node> scheduledItemObjectMapper;

    RecurringEventAttendanceView(RecurringEventsActivity activity) {
        this.activity = activity;
    }

    Node buildContainer() {
        Label currentEventLabel = Bootstrap.h3(I18nControls.newLabel(RecurringEventsI18nKeys.CurrentClasses));
        currentEventLabel.setPadding(new Insets(0, 0, 20, 0));
        TextTheme.createSecondaryTextFacet(currentEventLabel).style();

        attendancePane.setMinColumnWidth(300);
        attendancePane.setPadding(new Insets(60, 0, 0, 0));
        attendancePane.setId("attendanceFlowPane");
        eventTable.setFullHeight(true);

        VBox mainVBox = new VBox(currentEventLabel, eventTable, attendancePane);
        return ControlUtil.createVerticalScrollPaneWithPadding(10, new BorderPane(mainVBox));
    }

    void setActive(boolean active) {
        activeProperty.set(active);
    }

    void startLogic() {
        RecurringEventRenderers.registerRenderers();

        ReactiveVisualMapper<Event> eventVisualMapper = ReactiveVisualMapper.<Event>createPushReactiveChain(activity)
                .always("{class: 'Event', alias: 'e', where: 'type.recurringItem != null and kbs3'}")
                .ifNotNullOtherwiseEmpty(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o))
                .setEntityColumns(EVENT_COLUMNS)
                .visualizeResultInto(eventTable.visualResultProperty())
                .setVisualSelectionProperty(eventTable.visualSelectionProperty())
                .bindActivePropertyTo(Bindings.and(activity.activeProperty(), activeProperty))
                .start();
        eventVisualMapper.requestedSelectedEntityProperty().bindBidirectional(FXEvent.eventProperty());

        scheduledItemObjectMapper = ReactiveObjectsMapper.<ScheduledItem, Node>createPushReactiveChain(activity)
                .always("{class: 'ScheduledItem', alias: 's', fields: 'date', where: 'event.(type.recurringItem != null and kbs3)', orderBy: 'date'}")
                .ifNotNullOtherwiseEmpty(FXEvent.eventProperty(), event -> where("event=?", event))
                .ifInstanceOf(FXGanttSelection.ganttSelectedObjectProperty(), ScheduledItem.class, si -> where("s = ?", si))
                .setIndividualEntityToObjectMapperFactory(IndividualScheduledItemToBoxMapper::new)
                .storeMappedObjectsInto(attendancePane.getChildren())
                .bindActivePropertyTo(Bindings.and(activity.activeProperty(), activeProperty))
                .start();
    }

    class IndividualScheduledItemToBoxMapper implements IndividualEntityToObjectMapper<ScheduledItem, Node> {

        private final ObjectProperty<ScheduledItem> scheduledItemObjectProperty = new SimpleObjectProperty<>();
        private final Label dateLabel = new Label();
        private final BorderPane boxesContainer = new BorderPane();

        IndividualScheduledItemToBoxMapper(ScheduledItem scheduledItem) {
            dateLabel.setMinHeight(30);
            dateLabel.setTextFill(Color.WHITE);
            dateLabel.setBackground(Background.fill(Color.rgb(0, 150, 214)));
            dateLabel.setAlignment(Pos.CENTER);
            dateLabel.setFont(Font.font(null, FontWeight.BOLD, 16));
            boxesContainer.setTop(LayoutUtil.setMaxWidthToInfinite(dateLabel));

            VisualGrid linesGrid = new SkinnedVisualGrid();
            linesGrid.setHeaderVisible(false);
            linesGrid.setFullHeight(true);
            boxesContainer.setCenter(linesGrid);
            BorderPane.setAlignment(linesGrid, Pos.TOP_CENTER);

            Label totalCountLabel = I18nControls.newLabel("Total {0}" /* ??? */, linesGrid.rowCountProperty());
            totalCountLabel.setPadding(new Insets(5,0,0,0));
            totalCountLabel.getStyleClass().add("booking-total-count");
            boxesContainer.setBottom(totalCountLabel);

            ReactiveVisualMapper.<DocumentLine>createPushReactiveChain(activity)
                    .setActiveParent(scheduledItemObjectMapper)
                    .always("{class: 'DocumentLine', alias: 'dl', fields: 'document.confirmed', orderBy: 'document.person_lastName,document.person_firstName,document.ref'}")
                    .setEntityColumns(BOOKINGS_COLUMNS)
                    .ifNotNullOtherwiseEmpty(scheduledItemObjectProperty, si -> where("exists(select Attendance a where a.documentLine=dl and a.date=? and a.scheduledItem=?)", si.getDate(), si))
                    .visualizeResultInto(linesGrid.visualResultProperty())
                    .start();

            onEntityChangedOrReplaced(scheduledItem);
        }

        @Override
        public Pane getMappedObject() {
            return boxesContainer;
        }

        public void onEntityChangedOrReplaced(ScheduledItem scheduledItem) {
            scheduledItemObjectProperty.set(scheduledItem);
            dateLabel.setText(scheduledItem.getDate().format(DateTimeFormatter.ofPattern("MMMM dd")));
        }

        @Override
        public void onEntityRemoved(ScheduledItem entity) { }
    }
}


