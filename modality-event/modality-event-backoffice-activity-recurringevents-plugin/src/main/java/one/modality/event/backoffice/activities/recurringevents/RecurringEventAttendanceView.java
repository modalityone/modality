package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class RecurringEventAttendanceView {

    private final RecurringEventsActivity activity;
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final VisualGrid eventTable = new VisualGrid();
    private static final String EVENT_COLUMNS = "[" +
            "{expression: 'state', label: 'Status', renderer: 'eventStateRenderer'}," +
            "{expression: 'name', label: 'Name'}," +
            "{expression: 'type', label: 'TypeOfEvent'}," +
            "{expression: 'location', label: 'Location'}," +
            "{expression: 'dateIntervalFormat(startDate, endDate)', label: 'Dates'}" +
            "]";

    private final FlowPane attendanceFlowPane = new FlowPane();
    private final ObjectProperty<Event> selectedEventProperty = new SimpleObjectProperty<>();
    private ReactiveObjectsMapper<ScheduledItem, Node> scheduledItemObjectMapper;

    public RecurringEventAttendanceView(RecurringEventsActivity activity) {
        this.activity = activity;
    }

    public Node buildContainer() {
        BorderPane mainFrame = new BorderPane();

        Label currentEventLabel = new Label();
        I18nControls.bindI18nProperties(currentEventLabel,"CurrentClasses");
        currentEventLabel.setPadding(new Insets(0,0,20,0));
        TextTheme.createSecondaryTextFacet(currentEventLabel).style();
        currentEventLabel.getStyleClass().add("title3");

        attendanceFlowPane.setPadding(new Insets(60,0,0,0));
        attendanceFlowPane.setId("attendanceFlowPane");
        eventTable.setFullHeight(true);

        VBox mainVBox = new VBox(currentEventLabel, eventTable, attendanceFlowPane);
        mainFrame.setCenter(mainVBox);
        return ControlUtil.createVerticalScrollPaneWithPadding(10, mainFrame);
    }

    void setActive(boolean active) {
        activeProperty.set(active);
    }

    public void startLogic() {
        EventRenderers.registerRenderers();

        ReactiveVisualMapper<Event> eventVisualMapper = ReactiveVisualMapper.<Event>createPushReactiveChain(activity)
                .always("{class: 'Event', alias: 'e', fields: 'name, openingDate, description, type.recurringItem,(select site.name from Timeline where event=e limit 1) as location'}")
                .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o))
                .always(DqlStatement.where("type.recurringItem!=null and kbs3"))
                .setEntityColumns(EVENT_COLUMNS)
                .setVisualSelectionProperty(eventTable.visualSelectionProperty())
                .visualizeResultInto(eventTable.visualResultProperty())
                .bindActivePropertyTo(Bindings.and(activity.activeProperty(), activeProperty))
                .start();

        scheduledItemObjectMapper = ReactiveObjectsMapper.<ScheduledItem, Node>createPushReactiveChain(activity)
                .always("{class: 'ScheduledItem', alias: 's', fields: 'date,startTime,endTime', where: 'event.type.recurringItem!=null and event.kbs3=true', orderBy: 'date'}")
                .ifNotNullOtherwiseEmpty(selectedEventProperty, event -> where("event=?", event))
                .setIndividualEntityToObjectMapperFactory(IndividualScheduledItemToBoxMapper::new)
                .storeMappedObjectsInto(attendanceFlowPane.getChildren())
                .bindActivePropertyTo(Bindings.and(activity.activeProperty(), activeProperty))
                .start();

        eventVisualMapper.requestedSelectedEntityProperty().bindBidirectional(FXEvent.eventProperty());
        selectedEventProperty.bindBidirectional(FXEvent.eventProperty());
    }

    class IndividualScheduledItemToBoxMapper implements IndividualEntityToObjectMapper<ScheduledItem, Node> {
        private final ObjectProperty<ScheduledItem> scheduledItemObjectProperty = new SimpleObjectProperty<>();
        private final Label label = new Label();
        private final GridPane peopleGrid = new GridPane();
        Label totalCountLabel = new Label();
        private final ObservableList<DocumentLine> observableDocumentList;

        final BorderPane boxesContainer = new BorderPane();

        IndividualScheduledItemToBoxMapper(ScheduledItem scheduledItem) {
            peopleGrid.setBorder(Border.EMPTY);
            peopleGrid.setBackground(Background.fill(Color.WHITE));
            boxesContainer.setMinWidth(300);
            boxesContainer.setPadding(new Insets(0, 50, 50, 0));
            label.setMinHeight(30);
            label.setTextFill(Color.WHITE);
            label.setBackground(Background.fill(Color.rgb(0, 150, 214)));
            label.setAlignment(Pos.CENTER);
            label.setFont(Font.font(null, FontWeight.BOLD, 16));
            VBox.setMargin(label, new Insets(2));
            VBox.setMargin(peopleGrid, new Insets(0, 5, 5, 5));
            VBox.setVgrow(peopleGrid, Priority.ALWAYS);
            VBox box = new VBox(LayoutUtil.setMaxWidthToInfinite(label), peopleGrid, totalCountLabel);
            box.setMinWidth(150);
            //box.setEffect(BOX_SHADOW_EFFECT);
            boxesContainer.setTop(label);
            boxesContainer.setCenter(peopleGrid);
            BorderPane.setAlignment(peopleGrid, Pos.TOP_CENTER);
            ColumnConstraints col1 = new ColumnConstraints(230);
            ColumnConstraints col2 = new ColumnConstraints(70);
            peopleGrid.getColumnConstraints().addAll(col1, col2);
            peopleGrid.setBorder(Border.stroke(Color.GRAY));
            attendanceFlowPane.getChildren().add(boxesContainer);
            totalCountLabel.setPadding(new Insets(5,0,0,0));
            totalCountLabel.getStyleClass().add("booking-total-count");
            boxesContainer.setBottom(totalCountLabel);

            onEntityChangedOrReplaced(scheduledItem);
            // Visualizing people booked for that site item resource configuration into a visual grid (inside the box)
            // Each person (row in the grid) is represented by a DocumentLine allocated to that site item resource configuration

            ReactiveEntitiesMapper<DocumentLine> peopleMapper = ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(activity)
                    .setActiveParent(scheduledItemObjectMapper)
                    .always("{class: 'DocumentLine', alias: 'dl', fields: 'document.(person_name, confirmed,ref)',orderBy: 'document.person_lastName'}")
                    .ifNotNullOtherwiseEmpty(scheduledItemObjectProperty, si -> where("exists(select Attendance a where a.documentLine=dl and a.date=? and a.scheduledItem=?)", si.getDate(), si))
                    .start();
            observableDocumentList = peopleMapper.getObservableEntities();
            observableDocumentList.addListener((InvalidationListener) observable -> {
                peopleGrid.getChildren().clear();
                observableDocumentList.forEach(dl-> {
                    Label name = new Label("#"+dl.getDocument().getRef() + " " + dl.getDocument().getFullName());
                    name.setPadding(new Insets(5,0,5,5));
                    name.getStyleClass().add("booking-name");
                    String text;
                    Label state = new Label();
                    if(dl.getDocument().isConfirmed()) {
                        text = I18n.getI18nText("BookingConfirmed");
                        state.getStyleClass().add("booking-status-confirmed");
                    }
                    else {
                        text = I18n.getI18nText("BookingUnconfirmed");
                        state.getStyleClass().add("booking-status-unconfirmed");
                    }
                    state.setText(text);
                    TextTheme.createSecondaryTextFacet(name);
                    state.setPadding(new Insets(5,0,5,0));
                    int row = peopleGrid.getRowCount();
                    peopleGrid.add(name, 0, row);
                    peopleGrid.add(state, 1, row);
                    GridPane.setHalignment(state, HPos.CENTER);
                });
                totalCountLabel.setText(I18n.getI18nText("Total") + " " + observableDocumentList.size());
            });
        }

        @Override
        public Pane getMappedObject() {
            return boxesContainer;
        }

        public void onEntityChangedOrReplaced(ScheduledItem scheduledItem) {
            scheduledItemObjectProperty.set(scheduledItem);
            LocalDate date = scheduledItem.evaluate("date");
            label.setText(date.format(DateTimeFormatter.ofPattern("MMMM dd")));
        }

        @Override
        public void onEntityRemoved(ScheduledItem entity) {
        }
    }
}


