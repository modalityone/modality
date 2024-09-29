package one.modality.event.backoffice.activities.program;


import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.pickers.DatePicker;
import dev.webfx.extras.time.pickers.DatePickerOptions;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.extras.util.masterslave.SlaveEditor;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.util.masterslave.ModalitySlaveEditor;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.*;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public class ProgramActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private final ObservableList<DayTemplate> workingDayTemplates = FXCollections.observableArrayList();
    private final ObservableList<Item> workingItems = FXCollections.observableArrayList();

    private List<DayTemplate> dayTemplatesReadFromDatabase = new ArrayList<>();
    private List<Item> itemsReadFromDatabase = new ArrayList<>();
    private final ModalityValidationSupport validationSupport = new ModalityValidationSupport();
    private boolean validationSupportInitialised = false;

    private Event currentEditedEvent;
    private Button saveButton;
    private Button cancelButton;
    private ColumnsPane templateDayColumnsPane;
    private EventState previousEventState;
    private Site currentSite;
    private final Label festivalDescriptionLabel = new Label();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final Map<DayTemplate, DayTemplateManagement> correspondenceBetweenDayTemplateAndDayTemplateManagement = new IdentityHashMap<>();
    private String familyItemCode = "";
    private final static String FAMILY_ITEM_CODE_TEACHING = "teach";
    private EntityList<Item> audioLanguages;
    private Item videoItem;
    private ScrollPane mainContainer;


    public void setFamilyItemCode(String code) {
        familyItemCode = code;
    }

    public boolean validateForm() {
        if (!validationSupportInitialised) {
            initFormValidation();
            validationSupportInitialised = true;
        }
        return validationSupport.isValid();
    }

    public Node buildUi() {
        drawUIContainer();
        return mainContainer;
    }

    private void drawUIContainer() {
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

        int width = 1500;
        HBox firstLine = new HBox();
        firstLine.setAlignment(Pos.CENTER_LEFT);
        Label subtitle = I18nControls.bindI18nProperties(new Label(),  new I18nSubKey("expression: '[Programme] - ' + name + ' (' + dateIntervalFormat(startDate, endDate)+')'", currentEditedEvent));
        subtitle.getStyleClass().add(Bootstrap.H4);
        TextTheme.createSecondaryTextFacet(subtitle).style();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addTemplateButton = Bootstrap.primaryButton(I18nControls.bindI18nProperties(new Button(), "AddDayTemplate"));
        addTemplateButton.setGraphicTextGap(10);
        addTemplateButton.setPadding(new Insets(20,200,20,0));
        addTemplateButton.setOnAction(event -> addNewDayTemplate());

        firstLine.getChildren().setAll(subtitle,festivalDescriptionLabel,spacer,addTemplateButton);

        HBox lastLine = new HBox();
        lastLine.setAlignment(Pos.BASELINE_CENTER);
        lastLine.setSpacing(100);
        saveButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "Save"));
        saveButton.setOnAction(event -> {
            if (validateForm()) {
                submitUpdateStoreChanges();
            }
        });
        cancelButton = Bootstrap.largeSecondaryButton(I18nControls.bindI18nProperties(new Button(),"Cancel"));
        cancelButton.setOnAction(e-> {
            resetUpdateStoreAndOtherComponents();
            displayEventDetails(currentEditedEvent);
        });
        saveButton.disableProperty().bind(updateStore.hasChangesProperty().not());
        cancelButton.disableProperty().bind(updateStore.hasChangesProperty().not());

        lastLine.getChildren().setAll(cancelButton,saveButton);
        templateDayColumnsPane = new ColumnsPane();
        templateDayColumnsPane.setMaxColumnCount(2);
        templateDayColumnsPane.setHgap(100);
        templateDayColumnsPane.setVgap(50);
        templateDayColumnsPane.setPadding(new Insets(50,0,50,0));


        //The BindConverted seems to call drawDayTemplate on all elements, even the one already in the list, and not only on the added one.
//        ObservableLists.bindConverted(templateDayColumnsPane.getChildren(),workingDayTemplates,this::drawDayTemplate);
        //We add a listener on the workingDayTemplates to create a new panel or remove it according to the list changes.
        workingDayTemplates.addListener((ListChangeListener<DayTemplate>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    // Handle added elements
                    for (DayTemplate addedItem : change.getAddedSubList()) {
                        templateDayColumnsPane.getChildren().add(drawDayTemplate(addedItem));
                    }
                }
                if (change.wasRemoved()) {
                    // Handle removed elements
                    for (DayTemplate removedItem : change.getRemoved()) {
                        // Find and remove the corresponding UI element
                        DayTemplateManagement dayTemplateManagement = correspondenceBetweenDayTemplateAndDayTemplateManagement.get(removedItem);
                        if (dayTemplateManagement == null)
                            correspondenceBetweenDayTemplateAndDayTemplateManagement.get(removedItem);
                        templateDayColumnsPane.getChildren().remove(dayTemplateManagement.getContainer());
                    }
                }
            }
        });
        mainVBox.getChildren().setAll(firstLine,templateDayColumnsPane,lastLine);
        mainVBox.setMaxWidth(width);
        mainContainer = ControlUtil.createVerticalScrollPaneWithPadding(10, mainFrame);
    }

    private void submitUpdateStoreChanges() {
            updateStore.submitChanges()
                .onFailure(Console::log)
                .onSuccess(x -> Platform.runLater(() -> displayEventDetails(currentEditedEvent)));
        }

    private void addNewDayTemplate() {
        DayTemplate dayTemplate = updateStore.insertEntity(DayTemplate.class);
        dayTemplate.setEvent(currentEditedEvent);
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
        currentEditedEvent = e;

        //First we reset everything
        resetUpdateStoreAndOtherComponents();
        e.onExpressionLoaded("livestreamUrl,vodExpirationDate,audioExpirationDate")
            .onFailure((Console::log));
        previousEventState = e.getState();

        //We execute the query in batch, otherwise we can have synchronisation problem between the different threads
        entityStore.executeQueryBatch(
              new EntityStoreQuery("select name, event from DayTemplate si where event=? order by name", new Object[] { e })
            , new EntityStoreQuery("select distinct name from Item where organization=? and family.code=?", new Object[] { e.getOrganization(), familyItemCode})
            , new EntityStoreQuery("select name from Site where event=? and main limit 1", new Object[] { e}),
                new EntityStoreQuery("select distinct name from Item where organization=? and family.code = ? and not deprecated order by name ",
                    new Object[]{FXOrganization.getOrganization(), KnownItemFamily.AUDIO_RECORDING.getCode()}),
                new EntityStoreQuery("select distinct name,family.code from Item where organization=? and code = ?",
                    new Object[]{FXOrganization.getOrganization(), KnownItem.VIDEO.getCode()}))
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                EntityList<DayTemplate> dayTemplateList = entityLists[0];
                EntityList<Item> itemList = entityLists[1];
                EntityList<Site> siteList = entityLists[2];
                this.audioLanguages = entityLists[3];
                EntityList<Item> videoItems = entityLists[4];
                videoItem = videoItems.get(0);

                    // We take the selected date from the database, and transform the result in a list of LocalDate,
                    // that we pass to the datePicker, so they appear selected in the calendar
                    dayTemplatesReadFromDatabase = dayTemplateList;
                    itemsReadFromDatabase = itemList;
                    currentSite = siteList.get(0);

                    //We add the event and timeline to the updateStore, so they will be modified when changed
                    currentEditedEvent = updateStore.updateEntity(e);
                    
                    workingDayTemplates.setAll(dayTemplatesReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                    workingItems.setAll(itemsReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));

                    boolean isAdvertised;
                    if(currentEditedEvent.isAdvertised()==null) isAdvertised = false;
                    else isAdvertised = currentEditedEvent.isAdvertised();
                   // advertisedSwitch.setSelected(isAdvertised);
                    //registrationOpenSwitch.setSelected(currentEditedEvent.getState()==EventState.OPEN);
                    //We try to load the image from cloudinary if it exists
            }));
        drawUIContainer();
    }

    private final SlaveEditor<Event> eventDetailsSlaveEditor = new ModalitySlaveEditor<Event>() {
        /**
         * This method is called by the master controller when we change the event we're editing
         *
         * @param approvedEntity the approved Entity
         */
        @Override
        public void setSlave(Event approvedEntity) {
            displayEventDetails(approvedEntity);
            currentEditedEvent = approvedEntity;
        }

        @Override
        public Event getSlave() {
            return currentEditedEvent;
        }

        @Override
        public boolean hasChanges() {
            return updateStore.hasChanges();
        }
    };

    //This parameter will allow us to manage the interaction and behaviour of the Panel that display the details of an event and the event selected
    final private MasterSlaveLinker<Event> masterSlaveEventLinker = new MasterSlaveLinker<>(eventDetailsSlaveEditor);

   protected void startLogic() {
       setFamilyItemCode(FAMILY_ITEM_CODE_TEACHING);
       masterSlaveEventLinker.masterProperty().bindBidirectional(FXEvent.eventProperty());
   }

    private void initFormValidation() {
        if(!validationSupportInitialised) {
            FXProperties.runNowAndOnPropertiesChange(() -> {
                if (I18n.getDictionary() != null) {
                    validationSupport.reset();
                    workingDayTemplates.forEach(wt-> {
                        DayTemplateManagement dtm = correspondenceBetweenDayTemplateAndDayTemplateManagement.get(wt);
                        dtm.initFormValidation();
                    });
                }
            }, I18n.dictionaryProperty());
            validationSupportInitialised = true;
        }
    }

    private BorderPane drawDayTemplate(DayTemplate dayTemplate) {
        DayTemplateManagement dayTemplateManagement = new DayTemplateManagement(dayTemplate);
        correspondenceBetweenDayTemplateAndDayTemplateManagement.put(dayTemplate,dayTemplateManagement);
        return dayTemplateManagement.getContainer();
    }
    /**
     * This method is used to reset the different components in this class
     */
    private void resetUpdateStoreAndOtherComponents() {
        validationSupport.reset();
        validationSupportInitialised = false;
        workingDayTemplates.clear();
        correspondenceBetweenDayTemplateAndDayTemplateManagement.clear();
        updateStore.cancelChanges();
    }
    //TODO: this method is a repetition of a method in ManageRecurringEventView - move it to a shared place
    private static boolean isLocalTimeTextValid(String text) {
        try {
            LocalTime.parse(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private class DayTemplateManagement  {
        BorderPane mainContainer = new BorderPane();
        TextField templateNameTextField;
        Line verticalLine;
        DayTemplate dayTemplate;
        private final ObservableList<Timeline> workingTimelines = FXCollections.observableArrayList();
        private final ObservableList<ScheduledItem> workingTeachingScheduledItems = FXCollections.observableArrayList();
        private final ObservableList<ScheduledItem> workingAudioScheduledItems = FXCollections.observableArrayList();
        private final ObservableList<ScheduledItem> workingVideoScheduledItems = FXCollections.observableArrayList();

        private List<Timeline> timelinesReadFromDatabase = new ArrayList<>();
        private List<ScheduledItem> workingTeachingScheduledItemsReadFromDatabase;
        private List<ScheduledItem> workingAudioScheduledItemsReadFromDatabase;
        private List<ScheduledItem> workingVideoScheduledItemsReadFromDatabase;
        DatePicker datePicker;

        public DayTemplateManagement(DayTemplate dayTemplate) {
            this.dayTemplate = dayTemplate;
            VBox timelinesContainer = new VBox();
            ObservableLists.bindConverted(timelinesContainer.getChildren(),workingTimelines,this::drawTimeline);

            //We read the value of the database for the child elements only if the dayTemplate is already existing
            if(!dayTemplate.getId().isNew()) {
                entityStore.executeQueryBatch(
                        new EntityStoreQuery("select item, dayTemplate, startTime, endTime, videoOffered, audioOffered, name, site from Timeline where dayTemplate=? order by startTime",
                            new Object[]{dayTemplate}),
                        new EntityStoreQuery("select timeline, timeline.dayTemplate, startTime, endTime, date from ScheduledItem where timeline.dayTemplate=? order by startTime", new Object[]{dayTemplate}),
                        new EntityStoreQuery("select name, startTime, endTime, date, parent, item from ScheduledItem where parent.timeline.dayTemplate=? and item.family.code=? order by startTime", new Object[]{dayTemplate, KnownItemFamily.AUDIO_RECORDING.getCode()}),
                        new EntityStoreQuery("select name, startTime, endTime, date, parent, item from ScheduledItem where parent.timeline.dayTemplate=? and item.code=? order by startTime", new Object[]{dayTemplate, KnownItem.VIDEO.getCode()})
                    )
                    .onFailure(Console::log)
                    .onSuccess(entityList -> Platform.runLater(() -> {
                        timelinesReadFromDatabase = entityList[0];
                        workingTeachingScheduledItemsReadFromDatabase = entityList[1];
                        workingAudioScheduledItemsReadFromDatabase = entityList[2];
                        workingVideoScheduledItemsReadFromDatabase = entityList[3];
                        workingTimelines.setAll(timelinesReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                        workingTeachingScheduledItems.setAll(workingTeachingScheduledItemsReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                        workingAudioScheduledItems.setAll(workingAudioScheduledItemsReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                        workingVideoScheduledItems.setAll(workingVideoScheduledItemsReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                        //We remove the duplicates on the date (we have one similar date for each scheduledItem attached to each timeline)
                        Set<LocalDate> dateWithoutDuplicate = workingTeachingScheduledItemsReadFromDatabase.stream().map(ScheduledItem::getDate).collect(Collectors.toSet());
                        datePicker.setSelectedDates(dateWithoutDuplicate);
                    }));
            }
            else {
                //Here we want only the language
            }


                    //****************************  TOP ******************************************//
            HBox topLine = new HBox();

            Label duplicateButton = I18nControls.bindI18nProperties(new Label(), "DuplicateIcon");
            duplicateButton.setOnMouseClicked(e -> {
                DayTemplate duplicateDayTemplate = updateStore.insertEntity(DayTemplate.class);
                duplicateDayTemplate.setName(dayTemplate.getName() + " - copy");
                duplicateDayTemplate.setEvent(dayTemplate.getEvent());
                workingDayTemplates.add(duplicateDayTemplate);
                DayTemplateManagement newDTM = correspondenceBetweenDayTemplateAndDayTemplateManagement.get(duplicateDayTemplate);
                for (Timeline timelineItem : workingTimelines) {
                    Timeline newTimeline = updateStore.insertEntity(Timeline.class);
                    newTimeline.setItem(timelineItem.getItem());
                    newTimeline.setStartTime(timelineItem.getStartTime());
                    newTimeline.setEndTime(timelineItem.getEndTime());
                    newTimeline.setAudioOffered(timelineItem.isAudioOffered());
                    newTimeline.setVideoOffered(timelineItem.isVideoOffered());
                    newTimeline.setSite(timelineItem.getSite());
                    newTimeline.setEvent(timelineItem.getEvent());
                    newTimeline.setName(timelineItem.getName());
                    newTimeline.setDayTemplate(duplicateDayTemplate);
                    newDTM.workingTimelines.add(newTimeline);
                }
            });
            duplicateButton.setCursor(Cursor.HAND);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.SOMETIMES);

            templateNameTextField = new TextField();
            Separator topSeparator = new Separator();
            topSeparator.setPadding(new Insets(10,0,10,0));
            templateNameTextField.setPromptText("Name this template");
            templateNameTextField.setText(dayTemplate.getName());
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
            //TODO change when a generic function has been created
            plusButton.setFill(Color.web("#0096D6"));
            MonoPane buttonContainer = new MonoPane(plusButton);
            buttonContainer.setOnMouseClicked(e -> addTimelineAndLinkedScheduledItem());
            buttonContainer.setCursor(Cursor.HAND);
            buttonContainer.setPadding(new Insets(10,0,0,0));
            BorderPane.setAlignment(buttonContainer,Pos.CENTER_LEFT);
            centerBorderPane.setCenter(buttonContainer);
            BorderPane.setAlignment(buttonContainer,Pos.TOP_LEFT);

            Label deleteDayTemplate =  I18nControls.bindI18nProperties(new Label(),"DeleteDayTemplate");
            deleteDayTemplate.setPadding(new Insets(10,0,5,0));
            deleteDayTemplate.getStyleClass().add(Bootstrap.SMALL);
            deleteDayTemplate.getStyleClass().add(Bootstrap.TEXT_DANGER);
            deleteDayTemplate.setOnMouseClicked(e-> removeDayTemplate(dayTemplate));
            deleteDayTemplate.setCursor(Cursor.HAND);
            deleteDayTemplate.setPadding(new Insets(30,0,0,0));

            Separator separator = new Separator();
            separator.setPadding(new Insets(10,0,10,0));

            VBox bottomVBox = new VBox(deleteDayTemplate,separator);
            centerBorderPane.setBottom(bottomVBox);
            BorderPane.setAlignment(bottomVBox,Pos.BOTTOM_LEFT);

            mainContainer.setCenter(centerBorderPane);
            //****************************  BOTTOM ******************************************//
            BorderPane bottomBorderPane = new BorderPane();
            bottomBorderPane.setMaxWidth(400);
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
            LocalDate eventStartDate = currentEditedEvent.getStartDate();
            datePicker.setDisplayedYearMonth(YearMonth.of(eventStartDate.getYear(), eventStartDate.getMonth()));

            bottomBorderPane.setTop(assignDateLabel);
            BorderPane.setAlignment(assignDateLabel,Pos.CENTER);
            bottomBorderPane.setCenter(datePicker.getView());
            BorderPane.setAlignment(datePicker.getView(),Pos.CENTER);
            mainContainer.setBottom(bottomBorderPane);
            BorderPane.setAlignment(bottomBorderPane,Pos.CENTER);

            //We define the behaviour when we add or remove a date
            datePicker.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        // Handle added dates
                        for (LocalDate date : change.getAddedSubList()) {
                            //If the date is not in the workingScheduledItem, we add it
                            //(It can happen it exists already because it has been added during the initialisation and this code can be called after because of multi-thread)
                            if (workingTeachingScheduledItems.stream().noneMatch(item -> item.getDate().equals(date))) {
                                addTeachingScheduledItemsForDate(date);
                            }
                        }
                    }
                    if (change.wasRemoved()) {
                        // Handle removed dates
                        for (LocalDate date : change.getRemoved()) {
                            removeTeachingScheduledItemsForDate(date);
                        }
                    }
                }
            });
            initFormValidation();
        }

        private void initFormValidation() {
            validationSupport.addRequiredInput(templateNameTextField);
        }

        private void addTimelineAndLinkedScheduledItem() {
            Timeline newTimeLine = updateStore.insertEntity(Timeline.class);
            newTimeLine.setAudioOffered(false);
            newTimeLine.setVideoOffered(false);
            newTimeLine.setDayTemplate(dayTemplate);
            newTimeLine.setEvent(currentEditedEvent);
            newTimeLine.setSite(currentSite);
            workingTimelines.add(newTimeLine);

            for (LocalDate date : datePicker.getSelectedDates()) {
                addTeachingsScheduledItemsForDateAndTimeline(date,newTimeLine);
            }
        }

        private void addTeachingScheduledItemsForDate(LocalDate date) {
            for (Timeline timeline : workingTimelines) {
                ScheduledItem parent = addTeachingsScheduledItemsForDateAndTimeline(date,timeline);
                if(timeline.isAudioOffered()) {
                    addAudioScheduledItemsForDate(date,parent);
                }
                if(timeline.isVideoOffered()) {
                    addVideoScheduledItemsForDate(date,parent);
                }
            }
        }

        private ScheduledItem addTeachingsScheduledItemsForDateAndTimeline(LocalDate date, Timeline timeline) {
            ScheduledItem teachingScheduledItem = updateStore.insertEntity(ScheduledItem.class);
            teachingScheduledItem.setEvent(currentEditedEvent);
            teachingScheduledItem.setSite(currentSite);
            teachingScheduledItem.setDate(date);
            teachingScheduledItem.setTimeLine(timeline);
            teachingScheduledItem.setItem(timeline.getItem());
            workingTeachingScheduledItems.add(teachingScheduledItem);
            return teachingScheduledItem;
        }

        private void addAudioScheduledItemsForDate(LocalDate date, ScheduledItem parentTeachingScheduledItem) {
            //Here we add for each language not deprecated the scheduledItemAssociated to the date and parent scheduledItem*
            audioLanguages.forEach(languageItem-> {
            ScheduledItem audioScheduledItem = updateStore.insertEntity(ScheduledItem.class);
            audioScheduledItem.setEvent(currentEditedEvent);
            audioScheduledItem.setSite(currentSite);
            audioScheduledItem.setDate(date);
            audioScheduledItem.setParent(parentTeachingScheduledItem);
            audioScheduledItem.setItem(languageItem);
            workingAudioScheduledItems.add(audioScheduledItem);
            });
        }

        private void addVideoScheduledItemsForDate(LocalDate date, ScheduledItem parentTeachingScheduledItem) {
            ScheduledItem videoScheduledItem = updateStore.insertEntity(ScheduledItem.class);
            videoScheduledItem.setEvent(currentEditedEvent);
            videoScheduledItem.setSite(currentSite);
            videoScheduledItem.setDate(date);
            videoScheduledItem.setParent(parentTeachingScheduledItem);
            videoScheduledItem.setItem(videoItem);
            workingVideoScheduledItems.add(videoScheduledItem);
        }


        private void removeDayTemplate(DayTemplate dayTemplate) {
            workingDayTemplates.remove(dayTemplate);
            removeTimeLineLinkedToDayTemplate(dayTemplate);
            updateStore.deleteEntity(dayTemplate);
            correspondenceBetweenDayTemplateAndDayTemplateManagement.remove(dayTemplate);
        }

        private void removeTimeLineLinkedToDayTemplate(DayTemplate dayTemplate) {
            workingTimelines.removeIf(timeline -> {
                if (timeline.getDayTemplate() == dayTemplate) {
                    removeScheduledItemsLinkedToTimeline(timeline);
                    return true; // Removes the timeline
                }
                return false; // Keeps the timeline
            });
        }

        private void removeTimeLine(Timeline timeline) {
            workingTimelines.remove(timeline);
            removeScheduledItemsLinkedToTimeline(timeline);
        }

        private void removeScheduledItemsLinkedToTimeline(Timeline timeline) {
            //Here we remove a timeline, and all the scheduledItem associated to this timeline
            workingTeachingScheduledItems.removeIf(teachingScheduledItem -> {
                if (teachingScheduledItem.getTimeline() == timeline) {
                    removeAudioScheduledItem(teachingScheduledItem);
                    removeVideoScheduledItem(teachingScheduledItem);
                    updateStore.deleteEntity(teachingScheduledItem);
                    return true; // Removes the item from the list
                }
                return false;
            });
            updateStore.deleteEntity(timeline);
        }



        private void removeTeachingScheduledItemsForDate(LocalDate date) {
            //Here we remove a timeline, and all the scheduledItem associated to this timeline
            workingTeachingScheduledItems.removeIf(scheduledItem -> {
                if (scheduledItem.getDate() == date) {
                    removeAudioScheduledItem(scheduledItem);
                    removeVideoScheduledItem(scheduledItem);
                    updateStore.deleteEntity(scheduledItem);
                    return true; // Removes the item from the list
                }
                return false; // Keeps the item in the list
            });
        }
        private void removeAudioScheduledItem(ScheduledItem parent) {
            workingAudioScheduledItems.removeIf(audioSI -> {
                if (audioSI.getParent().equals(parent)) {
                    updateStore.deleteEntity(audioSI);
                    return true;
                }
                return false;
            });
        }

        private void removeVideoScheduledItem(ScheduledItem parent) {
            workingVideoScheduledItems.removeIf(videoSI -> {
                if (videoSI.getParent().equals(parent)) {
                    updateStore.deleteEntity(videoSI);
                    return true;
                }
                return false;
            });
        }

        private HBox drawTimeline(Timeline timeline) {
            ButtonSelector itemSelector = new EntityButtonSelector<Item>(
                "{class: 'Item', alias: 's', where: 'family.code=`teach`', orderBy :'name'}",
                ProgramActivity.this, FXMainFrameDialogArea.getDialogArea(), dataSourceModel
            )
                .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o));

            itemSelector.setSelectedItem(timeline.getItem());
            itemSelector.getButton().setMaxWidth(200);
            itemSelector.getButton().setMinWidth(200);

            //When the value is changed, we update the timeline and the teaching scheduledItem associated
            itemSelector.selectedItemProperty().addListener(observable -> {
                timeline.setItem(itemSelector.getSelectedItem());
                workingTeachingScheduledItems.stream().filter(scheduledItem -> scheduledItem.getTimeline().equals(timeline)).forEach(si -> si.setItem(itemSelector.getSelectedItem()));
            });
            validationSupport.addValidationRule(FXProperties.compute(itemSelector.selectedItemProperty(), s1-> itemSelector.getSelectedItem()!=null), itemSelector.getButton(), I18n.getI18nText("ItemSelectedShouldntBeNull"));

            MonoPane selectorPane = new MonoPane(itemSelector.getButton());
            TextField fromTextField = new TextField();
            fromTextField.setMaxWidth(60);
            fromTextField.setPromptText("8:46");
            validationSupport.addValidationRule(FXProperties.compute(fromTextField.textProperty(), s1 -> isLocalTimeTextValid(fromTextField.getText())), fromTextField, I18n.getI18nText("ValidationTimeFormatIncorrect"));

            if(timeline.getStartTime()!=null)
                fromTextField.setText(timeline.getStartTime().format(timeFormatter));
            fromTextField.textProperty().addListener(obs -> {
                if (isLocalTimeTextValid(fromTextField.getText())) {
                    timeline.setStartTime(LocalTime.parse(fromTextField.getText()));
                }
            });

            Label toLabel = I18nControls.bindI18nProperties(new Label(), "To");
            //TextTheme.createSecondaryTextFacet(subtitle).style();

            TextField untilTextField = new TextField();
            untilTextField.setMaxWidth(60);
            untilTextField.setPromptText("13:00");
            if(timeline.getEndTime()!=null)
                untilTextField.setText(timeline.getEndTime().format(timeFormatter));
            untilTextField.textProperty().addListener(obs -> {
                if (isLocalTimeTextValid(untilTextField.getText())) {
                    timeline.setEndTime(LocalTime.parse(untilTextField.getText()));
                }
            });
            validationSupport.addValidationRule(FXProperties.compute(untilTextField.textProperty(), s1 -> isLocalTimeTextValid(untilTextField.getText())), untilTextField, I18n.getI18nText("ValidationTimeFormatIncorrect"));

            TextField nameTextField = new TextField();
            nameTextField.setMaxWidth(150);
            nameTextField.setPromptText(ProgramI18nKeys.NameThisLine);
            if(timeline.getName()!=null)
                nameTextField.setText(timeline.getName());
            nameTextField.textProperty().addListener(obs -> { timeline.setName(nameTextField.getText());});

            SVGPath audioAvailableIcon = SvgIcons.createSoundIconPath();
            audioAvailableIcon.setFill(Color.GREEN);
            SVGPath audioUnavailable = SvgIcons.createSoundIconInactivePath();
            audioUnavailable.setFill(Color.RED);
            MonoPane audioMonoPane = new MonoPane();
            int iconWith = 30;
            audioMonoPane.setMinWidth(iconWith);
            audioMonoPane.setAlignment(Pos.CENTER);
            audioMonoPane.setCursor(Cursor.HAND);
            if(timeline.isAudioOffered()!=null) {
                if(timeline.isAudioOffered())
                    audioMonoPane.getChildren().setAll(audioAvailableIcon);
                else
                    audioMonoPane.getChildren().setAll(audioUnavailable);
            }
            audioMonoPane.setOnMouseClicked(e-> {
                timeline.setAudioOffered(!timeline.isAudioOffered());
                if(timeline.isAudioOffered()) {
                    audioMonoPane.getChildren().setAll(audioAvailableIcon);
                    //We add the recording scheduledItem for those date and associated timeline
                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
                            addAudioScheduledItemsForDate(date,teachingScheduledItem);
                        }
                    }));
                }
                else {
                    audioMonoPane.getChildren().setAll(audioUnavailable);
                    //We remove the recording scheduledItem for those date and associated timeline
                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
                            removeAudioScheduledItem(teachingScheduledItem);
                        }
                    }));
                }
                });

            SVGPath videoAvailableIcon = SvgIcons.createVideoIconPath();
            videoAvailableIcon.setFill(Color.GREEN);
            SVGPath videoUnavailableIcon = SvgIcons.createVideoIconInactivePath();
            videoUnavailableIcon.getStyleClass().add(Bootstrap.TEXT_DANGER);
            MonoPane videoMonoPane = new MonoPane();
            videoMonoPane.setCursor(Cursor.HAND);
            if(timeline.isVideoOffered()!=null) {
                if(timeline.isVideoOffered())
                    videoMonoPane.getChildren().setAll(videoAvailableIcon);
                else
                    videoMonoPane.getChildren().setAll(videoUnavailableIcon);
            }

            videoMonoPane.setOnMouseClicked(e-> {
                timeline.setVideoOffered(!timeline.isVideoOffered());
                if(timeline.isVideoOffered()) {
                    videoMonoPane.getChildren().setAll(videoAvailableIcon);
                    //We add the recording scheduledItem for those date and associated timeline
                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
                            addVideoScheduledItemsForDate(date,teachingScheduledItem);
                        }
                    }));
                }
                else {
                    videoMonoPane.getChildren().setAll(videoUnavailableIcon);
                    //We remove the recording scheduledItem for those date and associated timeline
                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
                            removeVideoScheduledItem(teachingScheduledItem);
                        }
                    }));
                }
            });

            SVGPath trashImage = SvgIcons.createTrashSVGPath();
            MonoPane trashContainer = new MonoPane(trashImage);
            trashContainer.setCursor(Cursor.HAND);
            trashContainer.setOnMouseClicked(event -> removeTimeLine(timeline));
            ShapeTheme.createSecondaryShapeFacet(trashImage).style();

            HBox afterItemSelectorLine = new HBox(fromTextField,toLabel,untilTextField,nameTextField,audioMonoPane,videoMonoPane,trashContainer);
            afterItemSelectorLine.setAlignment(Pos.CENTER_RIGHT);
            afterItemSelectorLine.setSpacing(10);
            selectorPane.setPadding(new Insets(0,20,0,0));
            HBox line = new HBox(selectorPane,afterItemSelectorLine);
            line.setAlignment(Pos.CENTER_LEFT);
            line.setPadding(new Insets(5,0,5,0));
            return line;
        }

        public BorderPane getContainer() {return mainContainer;}


    }
}

