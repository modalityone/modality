package one.modality.event.backoffice.activities.program;


import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.pickers.DatePicker;
import dev.webfx.extras.time.pickers.DatePickerOptions;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.*;
import one.modality.event.client.event.fx.FXEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public class ProgramActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final BorderPane container = new BorderPane();

    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private final ObservableList<DayTemplate> workingDayTemplates = FXCollections.observableArrayList();
    private final ObservableList<Item> workingItems = FXCollections.observableArrayList();

    private final BooleanExpression isWorkingDaysTemplateEmpty = ObservableLists.isEmpty(workingDayTemplates);
    private List<DayTemplate> dayTemplatesReadFromDatabase = new ArrayList<>();
    private List<Item> itemsReadFromDatabase = new ArrayList<>();
    private final ModalityValidationSupport validationSupport = new ModalityValidationSupport();
    private boolean validationSupportInitialised = false;
    private Event currentEditedEvent;
    private Event currentSelectedEvent;
    private Event currentObservedEvent;
    private Site eventSite;
    private Switch advertisedSwitch;
    private Switch registrationOpenSwitch;
    private BooleanBinding updateStoreHasChanged;
    private Button saveButton;
    private Button cancelButton;
    private ColumnsPane templateDayColumnsPane;
    private EventState previousEventState;
    private Site currentSite;


    @Override
    public Node buildUi() {
        BorderPane mainFrame = new BorderPane();
        mainFrame.setPadding(new Insets(0,0,30,0));
        Label title = I18nControls.bindI18nProperties(new Label(), "ProgramTitle");
        title.setContentDisplay(ContentDisplay.TOP);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        title.getStyleClass().add(Bootstrap.H2);
        TextTheme.createPrimaryTextFacet(title).style();

        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);
        VBox mainVBox = new VBox();
        mainFrame.setCenter(mainVBox);
        HBox firstLine = new HBox();
        firstLine.setAlignment(Pos.BASELINE_LEFT);
        Label subtitle = I18nControls.bindI18nProperties(new Label(), "Program");
        subtitle.getStyleClass().add(Bootstrap.H4);
        //TextTheme.createSecondaryTextFacet(subtitle).style();
        subtitle.setPadding(new Insets(20,0,20,200));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        Button addTemplateButton = Bootstrap.primaryButton(I18nControls.bindI18nProperties(new Button(), "AddDayTemplate"));
        addTemplateButton.setGraphicTextGap(10);
        addTemplateButton.setPadding(new Insets(20,200,20,0));
        addTemplateButton.setOnAction(event -> addNewDayTemplate());

        firstLine.getChildren().setAll(subtitle,spacer,addTemplateButton);

        HBox lastLine = new HBox();
        lastLine.setAlignment(Pos.BASELINE_CENTER);
        lastLine.setSpacing(100);
        saveButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "Save"));
        saveButton.setOnAction(e->updateStore.submitChanges());
        cancelButton = Bootstrap.largeSecondaryButton(I18nControls.bindI18nProperties(new Button(),"Cancel"));
        cancelButton.setOnAction(e->resetUpdateStoreAndOtherComponents());
        lastLine.getChildren().setAll(cancelButton,saveButton);
        templateDayColumnsPane = new ColumnsPane();
        templateDayColumnsPane.setMaxColumnCount(2);
        templateDayColumnsPane.setHgap(100);
        templateDayColumnsPane.setVgap(50);
        templateDayColumnsPane.setPadding(new Insets(50,0,50,0));


        ObservableLists.bindConverted(templateDayColumnsPane.getChildren(),workingDayTemplates,this::drawDayTemplate);
        mainVBox.getChildren().setAll(firstLine,templateDayColumnsPane,lastLine);
        mainVBox.setMaxWidth(800);

        displayEventDetails(FXEvent.getEvent());

        return ControlUtil.createVerticalScrollPaneWithPadding(10, mainFrame);
    }

    private void addNewDayTemplate() {
        DayTemplate dayTemplate = updateStore.insertEntity(DayTemplate.class);
        dayTemplate.setEvent(FXEvent.getEvent());
        workingDayTemplates.add(dayTemplate);
    }

    public void onResume() {
        super.onResume();
        FXEventSelector.showEventSelector();
    }

    @Override
    public void onPause() {
        FXEventSelector.resetToDefault();
        super.onPause();
    }

    private void displayEventDetails(Event e) {
        Console.log("Display Event Called");
        currentSelectedEvent = e;
        //Event e can be null if for example we select on the gantt graph an event that is not a recurring event
        if (e == null) {
            templateDayColumnsPane.setVisible(false);
            templateDayColumnsPane.setManaged(false);
            return;
        }
        //First we reset everything
        resetUpdateStoreAndOtherComponents();
        previousEventState = e.getState();
        templateDayColumnsPane.setVisible(true);
        templateDayColumnsPane.setManaged(true);

        //We execute the query in batch, otherwise we can have synchronisation problem between the different threads
        entityStore.executeQueryBatch(
            new EntityStoreQuery("select name from DayTemplate si where event=?", new Object[] { e})
            , new EntityStoreQuery("select distinct Item where exists(select ScheduledItem where event=? and item.family.code='teach')", new Object[] { e})
            ,new EntityStoreQuery("select name from Site where event=? and main limit 1", new Object[] { e}))
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                EntityList<DayTemplate> dayTemplateList = entityLists[0];
                EntityList<Item> itemList = entityLists[1];
                EntityList<Site> siteList = entityLists[2];

                // we test if the selectedEvent==e, because, if a user click very fast from en event to another, there
                // can be a sync pb between the result of the request from the database and the code executed
                if (currentSelectedEvent == e) {

                    // We take the selected date from the database, and transform the result in a list of LocalDate,
                    // that we pass to the datePicker, so they appear selected in the calendar
                    dayTemplatesReadFromDatabase = dayTemplateList;
                    itemsReadFromDatabase = itemList;
                    currentSite = siteList.get(0);

                        //Then we get the timeline and event, there should be just one timeline per recurring event
                    eventSite = currentSelectedEvent.getVenue();
                    //We add the event and timeline to the updateStore, so they will be modified when changed
                    currentEditedEvent = updateStore.updateEntity(e);


                    workingDayTemplates.setAll(dayTemplatesReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                    workingItems.setAll(itemsReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                  //  sortWorkingScheduledItemsByDate();
                    DayTemplate dt = null;
                    if(!workingDayTemplates.isEmpty()) dt = workingDayTemplates.get(0);
                    //In case the scheduledItem has no startTime and endTime, we position this value from the timeline
                    workingDayTemplates.forEach(dayTemplate -> {

                    });

                    boolean isAdvertised;
                    if(currentEditedEvent.isAdvertised()==null) isAdvertised = false;
                    else isAdvertised = currentEditedEvent.isAdvertised();
                   // advertisedSwitch.setSelected(isAdvertised);
                    //registrationOpenSwitch.setSelected(currentEditedEvent.getState()==EventState.OPEN);
                    //We try to load the image from cloudinary if it exists
                }
                saveButton.disableProperty().bind(updateStore.hasChangesProperty().not());
                cancelButton.disableProperty().bind(updateStore.hasChangesProperty().not());
                currentObservedEvent=currentEditedEvent;
            }));
    }

   protected void startLogic() {
   }


    private BorderPane drawDayTemplate(DayTemplate dayTemplate) {
        DayTemplateManagement dayTemplateManagement = new DayTemplateManagement(dayTemplate);
        return dayTemplateManagement.getContainer();
    }
    /**
     * This method is used to reset the different components in this class
     */
    private void resetUpdateStoreAndOtherComponents() {
        validationSupport.reset();
        currentObservedEvent = null;
        validationSupportInitialised = false;
        workingDayTemplates.clear();
        updateStore.cancelChanges();
    }

    private class DayTemplateManagement  {
        BorderPane mainContainer = new BorderPane();
        TextField templateNameTextField;
        Label selectEachDayLabel = I18nControls.bindI18nProperties(new Label(),"SelectTheDays");
        Line verticalLine;
        DayTemplate dayTemplate;
        private final ObservableList<Timeline> workingTimelines = FXCollections.observableArrayList();
        private final ObservableList<ScheduledItem> workingScheduledItems = FXCollections.observableArrayList();

        private final BooleanExpression isWorkingDaysTemplateEmpty = ObservableLists.isEmpty(workingDayTemplates);
        private List<Timeline> timelinesReadFromDatabase = new ArrayList<>();
        private VBox timelinesContainer = new VBox();

        DatePicker datePicker;

        public DayTemplateManagement(DayTemplate dayTemplate) {
            this.dayTemplate = dayTemplate;
            //****************************  TOP ******************************************//
            HBox topLine = new HBox();

            Label duplicateButton = I18nControls.bindI18nProperties(new Label(), "DuplicateIcon");
            duplicateButton.setOnMouseClicked(e -> {
            });
            duplicateButton.setCursor(Cursor.HAND);


            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.SOMETIMES);

            templateNameTextField = new TextField();
            Separator topSeparator = new Separator();
            topSeparator.setPadding(new Insets(10,0,10,0));
            templateNameTextField.setPromptText("Name this template");
            templateNameTextField.setOnMouseExited(e->dayTemplate.setName(templateNameTextField.getText()));
            topLine.getChildren().setAll(templateNameTextField,spacer, duplicateButton);

            verticalLine = new Line();
            verticalLine.setStartY(0);
            verticalLine.setEndY(180);
            verticalLine.setStroke(Color.LIGHTGRAY);

            mainContainer.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
                BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));
            mainContainer.setPadding(new Insets(10,10,10,10));

            mainContainer.setTop(new VBox(topLine,topSeparator));

            //****************************  CENTER ******************************************//
            BorderPane centerBorderPane = new BorderPane();
            centerBorderPane.setTop(timelinesContainer);
            SVGPath plusButton = SvgIcons.createPlusPath();
            plusButton.setFill(Color.web("#0096D6"));
            MonoPane buttonContainer = new MonoPane(plusButton);
            buttonContainer.setOnMouseClicked(e -> {
                Timeline newTimeLine = updateStore.insertEntity(Timeline.class);
                newTimeLine.setDayTemplate(dayTemplate);
                newTimeLine.setEvent(FXEvent.getEvent());
                newTimeLine.setSite(currentSite);
                workingTimelines.add(newTimeLine);
            });
            buttonContainer.setCursor(Cursor.HAND);
            BorderPane.setAlignment(buttonContainer,Pos.CENTER_LEFT);
            centerBorderPane.setCenter(buttonContainer);

            Separator separator = new Separator();
            separator.setPadding(new Insets(10,0,10,0));
            centerBorderPane.setBottom(separator);

            mainContainer.setCenter(centerBorderPane);
            //****************************  BOTTOM ******************************************//
            BorderPane bottomBorderPane = new BorderPane();
            Label assignDateLabel =  I18nControls.bindI18nProperties(new Label(),"AssignDay");
            TextTheme.createPrimaryTextFacet(assignDateLabel).style();
            assignDateLabel.getStyleClass().add(Bootstrap.SMALL);
            assignDateLabel.setPadding(new Insets(5,0,10,0));
            datePicker = new DatePicker(new DatePickerOptions()
                .setMultipleSelectionAllowed(true)
                .setPastDatesSelectionAllowed(false)
                .setApplyBorderStyle(false)
                .setApplyMaxSize(false)
                .setSortSelectedDates(true)
            );
            Label deleteDayTemplate =  I18nControls.bindI18nProperties(new Label(),"DeleteDayTemplate");
            deleteDayTemplate.setPadding(new Insets(10,0,5,0));
            deleteDayTemplate.getStyleClass().add(Bootstrap.SMALL);
            deleteDayTemplate.getStyleClass().add(Bootstrap.TEXT_DANGER);

            bottomBorderPane.setTop(assignDateLabel);
            BorderPane.setAlignment(assignDateLabel,Pos.CENTER);
            bottomBorderPane.setCenter(datePicker.getView());
            BorderPane.setAlignment(datePicker.getView(),Pos.CENTER);
            bottomBorderPane.setBottom(deleteDayTemplate);
            BorderPane.setAlignment(deleteDayTemplate,Pos.BOTTOM_RIGHT);
            mainContainer.setBottom(bottomBorderPane);
            ObservableLists.bindConverted(timelinesContainer.getChildren(),workingTimelines,this::drawTimeline);
        }

        private HBox drawTimeline(Timeline timeline) {
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().addAll("Option 1", "Option 2", "Option 3");
            comboBox.setValue("Option 1");
            return new HBox(comboBox);
        }


        public BorderPane getContainer() {return mainContainer;}
        public DatePicker getDatePicker() {
            return datePicker;
        }
    }
}

