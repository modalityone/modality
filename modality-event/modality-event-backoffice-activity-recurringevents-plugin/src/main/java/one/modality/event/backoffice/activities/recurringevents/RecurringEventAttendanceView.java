package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

public class RecurringEventAttendanceView {
    private final VisualGrid eventTable = new VisualGrid();
    private static final String OPERATION_COLUMNS_EVENT = "[" +
            "{expression: 'name', label: 'Name'}," +
            "{expression: 'type', label: 'TypeOfEvent'}," +
            "{expression: 'dateIntervalFormat(startDate, endDate)', label: 'Dates'}," +
            "{expression: 'organization', label: 'Organisation'},"+
            "{expression: 'organization', label: 'TotalAttendees'}]";
    private final ButtonFactoryMixin mixin;

    public RecurringEventAttendanceView(ButtonFactoryMixin mixin) {
        this.mixin = mixin;
    }

    public Node buildContainer() {
        BorderPane mainFrame = new BorderPane();
        //Displaying The title of the frame
        Label title = new Label();
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add("font-size-35px");

        I18nControls.bindI18nProperties(title,"GPClasses");
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setPadding(new Insets(0,0,30,0));
        mainFrame.setTop(title);

        //Displaying the list of events
        Label currentEventLabel = new Label();
        I18nControls.bindI18nProperties(currentEventLabel,"CurrentClasses");
        currentEventLabel.setPadding(new Insets(0,0,20,0));
        TextTheme.createSecondaryTextFacet(currentEventLabel).style();
        currentEventLabel.getStyleClass().add("font-size-16px");

        VBox mainVBox = new VBox(currentEventLabel,eventTable);
        mainFrame.setCenter(mainVBox);
        ScrollPane scrollPane = new ScrollPane(mainFrame);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        return scrollPane;
    }


    public void startLogic() {
        ReactiveVisualMapper.<Event>createPushReactiveChain(mixin)
                .always("{class: 'Event', alias: 'e', fields: 'name, openingDate, description, type.recurringItem'}")
                .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o))
                .always(DqlStatement.where("type.recurringItem!=null and kbs3"))
                .setEntityColumns(OPERATION_COLUMNS_EVENT)
                .setVisualSelectionProperty(eventTable.visualSelectionProperty())
                .setSelectedEntityHandler(this::displayEventDetails)
                .visualizeResultInto(eventTable.visualResultProperty())
                .start();
 }

    private void displayEventDetails(Event e) {
    }

    public BooleanProperty blocksGroupingProperty() {
        return null;
    }

    public void startLogic(Object mixin)
    {

    }
}
