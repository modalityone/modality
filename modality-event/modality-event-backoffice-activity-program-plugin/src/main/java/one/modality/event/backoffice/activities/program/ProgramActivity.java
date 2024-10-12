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
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.dialog.DialogUtil;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
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
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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
    private ColumnsPane templateDayColumnsPane;
    private EventState previousEventState;
    private Site currentSite;
    private final Label festivalDescriptionLabel = new Label();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final Map<DayTemplate, DayTemplateManagement> correspondenceBetweenDayTemplateAndDayTemplateManagement = new IdentityHashMap<>();
    private String familyItemCode = "";
    private final static String FAMILY_ITEM_CODE_TEACHING = "teach";
    private EntityList<Item> audioLanguages;
    private Item videoItem;
    private final ScrollPane mainContainer;
    private final BorderPane mainFrame;
    private ListChangeListener<DayTemplate> dayTemplateListChangeListener;
    Button saveButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), ProgramI18nKeys.SaveProgram));
    Button generateProgramButton = Bootstrap.largePrimaryButton(I18nControls.bindI18nProperties(new Button(), ProgramI18nKeys.GenerateProgram));
    Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.bindI18nProperties(new Button(), ProgramI18nKeys.CancelProgram));
    private BooleanProperty areScheduledItemBeenGeneratedProperty = new SimpleBooleanProperty(false);
    private StringProperty eventStateDescriptionStringProperty = new SimpleStringProperty();
    private StringProperty generateButtonLabelStringProperty = new SimpleStringProperty();

    public ProgramActivity() {
        mainFrame = new BorderPane();
        mainFrame.setPadding(new Insets(0, 0, 30, 0));
        mainContainer = ControlUtil.createVerticalScrollPane(mainFrame);

        eventStateDescriptionStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.ScheduledItemsNotYetGenerated));
        generateButtonLabelStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.GenerateProgram));

        areScheduledItemBeenGeneratedProperty.addListener((observable, oldValue, newValue) ->
        {
            if (newValue) {
                eventStateDescriptionStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.ScheduledItemsAlreadyGenerated));
                generateButtonLabelStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.DeleteProgram));
            } else {
                eventStateDescriptionStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.ScheduledItemsNotYetGenerated));
                generateButtonLabelStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.GenerateProgram));
            }
        });
    }

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
        //The main container is build by drawContainer, who is called by displayEventDetails.
        return mainContainer;
    }

    private void drawUIContainer() {
        Label title = I18nControls.bindI18nProperties(new Label(), "ProgramTitle");
        title.setContentDisplay(ContentDisplay.TOP);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        title.getStyleClass().add(Bootstrap.H2);
        TextTheme.createPrimaryTextFacet(title).style();

        areScheduledItemBeenGeneratedProperty.setValue(false);

        Button generateProgramButton = Bootstrap.largePrimaryButton(new Button());
        generateProgramButton.textProperty().bind(generateButtonLabelStringProperty);

        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);
        VBox mainVBox = new VBox();
        mainFrame.setCenter(mainVBox);

        int width = 1500;
        HBox firstLine = new HBox();
        firstLine.setAlignment(Pos.CENTER_LEFT);
        Label subtitle = I18nControls.bindI18nProperties(new Label(), new I18nSubKey("expression: '[Programme] - ' + name + ' (' + dateIntervalFormat(startDate, endDate)+')'", currentEditedEvent));
        subtitle.getStyleClass().add(Bootstrap.H4);
        TextTheme.createSecondaryTextFacet(subtitle).style();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addTemplateButton = Bootstrap.primaryButton(I18nControls.bindI18nProperties(new Button(), "AddDayTemplate"));
        addTemplateButton.setGraphicTextGap(10);
        addTemplateButton.setPadding(new Insets(20, 200, 20, 0));
        addTemplateButton.setOnAction(event -> addNewDayTemplate());

        firstLine.getChildren().setAll(subtitle, festivalDescriptionLabel, spacer, addTemplateButton);

        HBox lastLine = new HBox();
        lastLine.setAlignment(Pos.BASELINE_CENTER);
        lastLine.setSpacing(100);
        cancelButton.setOnAction(e -> {
            resetUpdateStoreAndOtherComponents();
            displayEventDetails(currentEditedEvent);
        });
        saveButton.disableProperty().bind(updateStore.hasChangesProperty().not());
        cancelButton.disableProperty().bind(updateStore.hasChangesProperty().not());

        saveButton.setOnAction(event -> {
            if (validateForm()) {
                OperationUtil.turnOnButtonsWaitModeDuringExecution(
                    updateStore.submitChanges()
                        .onFailure(x-> {
                            DialogContent dialog = DialogContent.createConfirmationDialog("Error","Operation failed", x.getMessage());
                            dialog.setOk();
                            Platform.runLater(()-> {
                                DialogBuilderUtil.showModalNodeInGoldLayout(dialog,FXMainFrameDialogArea.getDialogArea());
                                dialog.getPrimaryButton().setOnAction(a->dialog.getDialogCallback().closeDialog());
                            });
                            Console.log(x);
                        })
                        .onSuccess(x -> Platform.runLater(() -> {
                            displayEventDetails(currentEditedEvent);
                            OperationUtil.turnOffButtonsWaitMode(saveButton, cancelButton);
                        })),
                    saveButton, cancelButton
                );
            }
        });

        generateProgramButton.setOnAction(e -> {
            Text titleConfirmationText = I18n.bindI18nProperties(new Text(), "AreYouSure");
            Bootstrap.textSuccess(Bootstrap.strong(Bootstrap.h3(titleConfirmationText)));
            BorderPane dialog = new BorderPane();
            dialog.setTop(titleConfirmationText);
            BorderPane.setAlignment(titleConfirmationText, Pos.CENTER);
            Text confirmationText;
            if (areScheduledItemBeenGeneratedProperty.getValue()) {
                confirmationText = I18n.bindI18nProperties(new Text(), ProgramI18nKeys.DeleteProgramConfirmation);
            } else {
                confirmationText = I18n.bindI18nProperties(new Text(), ProgramI18nKeys.ProgramGenerationConfirmation);
            }
            dialog.setCenter(confirmationText);
            BorderPane.setAlignment(confirmationText, Pos.CENTER);
            BorderPane.setMargin(confirmationText, new Insets(30, 0, 30, 0));
            Button okGenerateButton = Bootstrap.largeDangerButton(I18nControls.bindI18nProperties(new Button(), "Confirm"));
            Button cancelActionButton = Bootstrap.largeSecondaryButton(I18nControls.bindI18nProperties(new Button(), "Cancel"));

            HBox buttonsHBox = new HBox(cancelActionButton, okGenerateButton);
            buttonsHBox.setAlignment(Pos.CENTER);
            buttonsHBox.setSpacing(30);
            dialog.setBottom(buttonsHBox);
            BorderPane.setAlignment(buttonsHBox, Pos.CENTER);
            DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
            okGenerateButton.setOnAction(l -> {
                if (areScheduledItemBeenGeneratedProperty.getValue()) {
                    deleteProgram();
                } else {
                    generateProgram();
                }
                dialogCallback.closeDialog();
            });
            cancelActionButton.setOnAction(l -> dialogCallback.closeDialog());
        });

        generateProgramButton.disableProperty().bind(updateStore.hasChangesProperty());
        lastLine.getChildren().setAll(cancelButton, saveButton, generateProgramButton);
        templateDayColumnsPane = new ColumnsPane();
        templateDayColumnsPane.setMaxColumnCount(2);
        templateDayColumnsPane.setHgap(100);
        templateDayColumnsPane.setVgap(50);
        templateDayColumnsPane.setPadding(new Insets(50, 0, 50, 0));
        templateDayColumnsPane.setAlignment(Pos.TOP_CENTER);

        //We add a listener on the workingDayTemplates to create a new panel or remove it according to the list changes.
        dayTemplateListChangeListener = change -> {
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
                        if (dayTemplateManagement == null) {
                            correspondenceBetweenDayTemplateAndDayTemplateManagement.get(removedItem);
                        }
                        templateDayColumnsPane.getChildren().remove(dayTemplateManagement.getContainer());
                    }
                }
            }
        };
        // Add the listener to the observable list
        workingDayTemplates.addListener(dayTemplateListChangeListener);
        Label eventStateLabel = new Label();
        eventStateLabel.getStyleClass().addAll(Bootstrap.H4, Bootstrap.TEXT_SECONDARY);
        eventStateLabel.textProperty().bind(eventStateDescriptionStringProperty);
        HBox eventStateLine = new HBox(eventStateLabel);
        eventStateLine.setAlignment(Pos.CENTER);
        eventStateLine.setPadding(new Insets(0, 0, 30, 0));
        mainVBox.getChildren().setAll(firstLine, templateDayColumnsPane, eventStateLine, lastLine);
        mainVBox.setMaxWidth(width);
        mainContainer.setContent(mainFrame);
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

    private void generateProgram() {
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        List<Timeline> newlyCreatedEventTimelines = new ArrayList<Timeline>();
        //Here, we take all the template timelines, and create the event timelines needed
        //We create an event timeline for all template timelines having distinct element on {item, startTime, endTime}
        workingDayTemplates.forEach(currentDayTemplate -> {
            DayTemplateManagement dayTemplateManagement = correspondenceBetweenDayTemplateAndDayTemplateManagement.get(currentDayTemplate);

            dayTemplateManagement.workingTemplateTimelines.forEach(currentTemplateTimeline -> {
                LocalTime startTime = currentTemplateTimeline.getStartTime();
                LocalTime endTime = currentTemplateTimeline.getEndTime();
                Item item = currentTemplateTimeline.getItem();
                Timeline templateTimelineToEdit = updateStore.updateEntity(currentTemplateTimeline);
                Timeline eventTimeLine = newlyCreatedEventTimelines.stream()
                    .filter(timeline ->
                        timeline.getStartTime().equals(startTime) &&
                            timeline.getEndTime().equals(endTime) &&
                            timeline.getItem().equals(item)
                    )
                    .findFirst()
                    .orElse(null);
                if (eventTimeLine == null) {
                    //Here we create an event timeline
                    eventTimeLine = updateStore.insertEntity(Timeline.class);
                    eventTimeLine.setEvent(currentEditedEvent);
                    eventTimeLine.setSite(currentTemplateTimeline.getSite());
                    eventTimeLine.setItem(item);
                    eventTimeLine.setStartTime(startTime);
                    eventTimeLine.setEndTime(endTime);
                    eventTimeLine.setItemFamily(currentTemplateTimeline.getItemFamily());
                    newlyCreatedEventTimelines.add(eventTimeLine);

                }
                templateTimelineToEdit.setEventTimeline(eventTimeLine);
                //Now, we create the associated scheduledItem
                for (LocalDate date : dayTemplateManagement.datePicker.getSelectedDates()) {
                    ScheduledItem teachingScheduledItem = dayTemplateManagement.addTeachingsScheduledItemsForDateAndTimeline(date, currentTemplateTimeline.getName(), eventTimeLine, updateStore);
                    if (currentTemplateTimeline.isAudioOffered()) {
                        dayTemplateManagement.addAudioScheduledItemsForDate(date, teachingScheduledItem, updateStore);
                    }
                    if (currentTemplateTimeline.isVideoOffered()) {
                        dayTemplateManagement.addVideoScheduledItemsForDate(date, teachingScheduledItem, updateStore);
                    }
                }
            });
        });
        OperationUtil.turnOnButtonsWaitModeDuringExecution(
            updateStore.submitChanges()
                .onFailure(x-> {
                    DialogContent dialog = DialogContent.createConfirmationDialog("Error","Operation failed", x.getMessage());
                    dialog.setOk();
                    Platform.runLater(()-> {
                        DialogBuilderUtil.showModalNodeInGoldLayout(dialog,FXMainFrameDialogArea.getDialogArea());
                        dialog.getPrimaryButton().setOnAction(a->dialog.getDialogCallback().closeDialog());
                    });
                    Console.log(x);
                })
                .onSuccess(x -> Platform.runLater(() -> {
                    displayEventDetails(currentEditedEvent);
                    OperationUtil.turnOffButtonsWaitMode(generateProgramButton);
                })),
            generateProgramButton
        );
    }

    private void deleteProgram() {
        UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);

        //Here we look for the teachings, audio and video scheduled Item related to this timeline and delete them
        entityStore.executeQueryBatch(
                new EntityStoreQuery("select id, item.family.code from ScheduledItem si where event=? order by name", new Object[]{currentEditedEvent}))
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                EntityList<ScheduledItem> scheduledItemsList = entityLists[0];
                //First we delete all the audios and videos scheduledItem
                scheduledItemsList.forEach(currentScheduledItem -> {
                    if(currentScheduledItem.getItem().getFamily().getCode().equals(KnownItemFamily.AUDIO_RECORDING.getCode()) || currentScheduledItem.getItem().getFamily().getCode().equals(KnownItemFamily.VIDEO.getCode()))
                        localUpdateStore.deleteEntity(currentScheduledItem);
                    });

                scheduledItemsList.forEach(currentScheduledItem -> {
                    if(currentScheduledItem.getItem().getFamily().getCode().equals(KnownItemFamily.TEACHING.getCode()))
                        localUpdateStore.deleteEntity(currentScheduledItem);
                });

                //then we put the reference to the event timeline to null on the template timeline
                workingDayTemplates.forEach(currentDayTemplate -> {
                    DayTemplateManagement dayTemplateManagement = correspondenceBetweenDayTemplateAndDayTemplateManagement.get(currentDayTemplate);
                    dayTemplateManagement.workingTemplateTimelines.forEach(currentTemplateTimeline -> {
                        Timeline templateTimelineToUpdate = localUpdateStore.updateEntity(currentTemplateTimeline);
                        templateTimelineToUpdate.setEventTimeline(null);
                        Timeline eventTimeLine = currentTemplateTimeline.getEventTimeline();
                        localUpdateStore.deleteEntity(eventTimeLine);
                    });
                });
                OperationUtil.turnOnButtonsWaitModeDuringExecution(
                    localUpdateStore.submitChanges()
                        .onFailure(x-> {
                            DialogContent dialog = DialogContent.createConfirmationDialog("Error","Operation failed", x.getMessage());
                            dialog.setOk();
                            Platform.runLater(()-> {
                                DialogBuilderUtil.showModalNodeInGoldLayout(dialog,FXMainFrameDialogArea.getDialogArea());
                                dialog.getPrimaryButton().setOnAction(a->dialog.getDialogCallback().closeDialog());
                            });
                            Console.log(x);
                        })
                        .onSuccess(x -> Platform.runLater(() -> {
                            displayEventDetails(currentEditedEvent);
                            OperationUtil.turnOffButtonsWaitMode(generateProgramButton);
                        })),
                    generateProgramButton
                );
            }));
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
                new EntityStoreQuery("select name, event, dates from DayTemplate si where event=? order by name", new Object[]{e})
                , new EntityStoreQuery("select distinct name from Item where organization=? and family.code=?", new Object[]{e.getOrganization(), familyItemCode})
                , new EntityStoreQuery("select name from Site where event=? and main limit 1", new Object[]{e}),
                new EntityStoreQuery("select distinct name from Item where organization=? and family.code = ? and not deprecated order by name ",
                    new Object[]{FXOrganization.getOrganization(), KnownItemFamily.AUDIO_RECORDING.getCode()}),
                new EntityStoreQuery("select distinct name from Item where organization=? and family.code = ? and not deprecated order by name ",
                    new Object[]{FXOrganization.getOrganization(), KnownItemFamily.VIDEO.getCode()}))
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
        if (!validationSupportInitialised) {
            FXProperties.runNowAndOnPropertiesChange(() -> {
                if (I18n.getDictionary() != null) {
                    validationSupport.reset();
                    workingDayTemplates.forEach(wt -> {
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
        correspondenceBetweenDayTemplateAndDayTemplateManagement.put(dayTemplate, dayTemplateManagement);
        return dayTemplateManagement.getContainer();
    }

    /**
     * This method is used to reset the different components in this class
     */
    private void resetUpdateStoreAndOtherComponents() {
        validationSupport.reset();
        mainFrame.getChildren().clear();
        if (dayTemplateListChangeListener != null)
            workingDayTemplates.removeListener(dayTemplateListChangeListener);
        workingDayTemplates.clear();
        if (templateDayColumnsPane != null)
            templateDayColumnsPane.getChildren().clear();
        dayTemplatesReadFromDatabase.clear();
        itemsReadFromDatabase.clear();
        validationSupportInitialised = false;
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

    private class DayTemplateManagement {
        BorderPane mainContainer = new BorderPane();
        TextField templateNameTextField;
        Line verticalLine;
        DayTemplate dayTemplate;
        private final ObservableList<Timeline> workingTemplateTimelines = FXCollections.observableArrayList();

        DatePicker datePicker;
        VBox listOfSelectedDatesVBox;

        public DayTemplateManagement(DayTemplate dayTemplate) {
            this.dayTemplate = dayTemplate;
            VBox timelinesContainer = new VBox();

            ObservableLists.bindConverted(timelinesContainer.getChildren(), workingTemplateTimelines, this::drawSelectedDatesAsLine);

            //We read the value of the database for the child elements only if the dayTemplate is already existing in the database (ie not in cache)
            if (!dayTemplate.getId().isNew()) {
                entityStore.executeQueryBatch(
                        new EntityStoreQuery("select item, dayTemplate, startTime, endTime, videoOffered, audioOffered, name,  site, eventTimeline from Timeline where dayTemplate=? order by startTime",
                            new Object[]{dayTemplate})
                    )
                    .onFailure(Console::log)
                    .onSuccess(entityList -> Platform.runLater(() -> {
                        //timelinesReadFromDatabase =
                        workingTemplateTimelines.setAll(entityList[0]);
                        if (dayTemplate.getDates() != null) {
                            datePicker.setSelectedDates(DatesToStringConversion.getDateList(dayTemplate.getDates()));
                        }
                        //Here we test if the schedueled item have already been generated, ie if at least one of the workingTemplateTimelines got an eventTimeLine not null
                        boolean hasEventTimeline = workingTemplateTimelines.stream()
                            .anyMatch(timeline -> timeline.getEventTimeline() != null);
                        if (hasEventTimeline) {
                            areScheduledItemBeenGeneratedProperty.setValue(true);
                        } else {
                            areScheduledItemBeenGeneratedProperty.setValue(false);
                        }
                    }));
            } else {
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
                for (Timeline timelineItem : workingTemplateTimelines) {
                    Timeline newTimeline = updateStore.insertEntity(Timeline.class);
                    newTimeline.setItem(timelineItem.getItem());
                    newTimeline.setStartTime(timelineItem.getStartTime());
                    newTimeline.setEndTime(timelineItem.getEndTime());
                    newTimeline.setAudioOffered(timelineItem.isAudioOffered());
                    newTimeline.setVideoOffered(timelineItem.isVideoOffered());
                    newTimeline.setSite(timelineItem.getSite());
                    newTimeline.setName(timelineItem.getName());
                    newTimeline.setDayTemplate(duplicateDayTemplate);
                    newDTM.workingTemplateTimelines.add(newTimeline);
                }
            });
            duplicateButton.setCursor(Cursor.HAND);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.SOMETIMES);

            templateNameTextField = new TextField();
            templateNameTextField.setMinWidth(350);
            Separator topSeparator = new Separator();
            topSeparator.setPadding(new Insets(10, 0, 10, 0));
            templateNameTextField.setPromptText("Name this template");
            templateNameTextField.setText(dayTemplate.getName());
            templateNameTextField.setOnMouseExited(e -> dayTemplate.setName(templateNameTextField.getText()));
            topLine.getChildren().setAll(templateNameTextField, spacer, duplicateButton);

            verticalLine = new Line();
            verticalLine.setStartY(0);
            verticalLine.setEndY(180);
            verticalLine.setStroke(Color.LIGHTGRAY);

            mainContainer.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
                BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));
            mainContainer.setPadding(new Insets(10, 10, 10, 10));
            mainContainer.setTop(new VBox(topLine, topSeparator));

            //****************************  CENTER ******************************************//
            BorderPane centerBorderPane = new BorderPane();
            centerBorderPane.setTop(timelinesContainer);
            SVGPath plusButton = SvgIcons.createPlusPath();
            //TODO change when a generic function has been created
            plusButton.setFill(Color.web("#0096D6"));
            MonoPane buttonContainer = new MonoPane(plusButton);
            buttonContainer.setOnMouseClicked(e -> addTemplateTimeline());
            buttonContainer.setCursor(Cursor.HAND);
            buttonContainer.setPadding(new Insets(10, 0, 0, 0));
            BorderPane.setAlignment(buttonContainer, Pos.CENTER_LEFT);
            centerBorderPane.setCenter(buttonContainer);
            BorderPane.setAlignment(buttonContainer, Pos.TOP_LEFT);

            Label deleteDayTemplate = I18nControls.bindI18nProperties(new Label(), "DeleteDayTemplate");
            deleteDayTemplate.setPadding(new Insets(10, 0, 5, 0));
            deleteDayTemplate.getStyleClass().add(Bootstrap.SMALL);
            deleteDayTemplate.getStyleClass().add(Bootstrap.TEXT_DANGER);
            deleteDayTemplate.setOnMouseClicked(e -> removeDayTemplate(dayTemplate));
            deleteDayTemplate.setCursor(Cursor.HAND);
            deleteDayTemplate.setPadding(new Insets(30, 0, 0, 0));

            Separator separator = new Separator();
            separator.setPadding(new Insets(10, 0, 10, 0));

            VBox bottomVBox = new VBox(deleteDayTemplate, separator);
            centerBorderPane.setBottom(bottomVBox);
            BorderPane.setAlignment(bottomVBox, Pos.BOTTOM_LEFT);
            centerBorderPane.setMaxHeight(Region.USE_PREF_SIZE);

            mainContainer.setCenter(centerBorderPane);
            mainContainer.setMaxHeight(Region.USE_PREF_SIZE);

            //****************************  BOTTOM ******************************************//
            BorderPane bottomBorderPane = new BorderPane();
            bottomBorderPane.setMaxWidth(600);
            Label assignDateLabel = I18nControls.bindI18nProperties(new Label(), "AssignDay");
            TextTheme.createPrimaryTextFacet(assignDateLabel).style();
            assignDateLabel.getStyleClass().add(Bootstrap.SMALL);
            assignDateLabel.setPadding(new Insets(5, 0, 10, 0));
            datePicker = new DatePicker(new DatePickerOptions()
                .setMultipleSelectionAllowed(true)
                .setPastDatesSelectionAllowed(false)
                .setApplyBorderStyle(false)
                .setApplyMaxSize(false)
                .setSortSelectedDates(true)
            );
            LocalDate eventStartDate = currentEditedEvent.getStartDate();
            datePicker.setDisplayedYearMonth(YearMonth.of(eventStartDate.getYear(), eventStartDate.getMonth()));

            listOfSelectedDatesVBox = new VBox();
            listOfSelectedDatesVBox.setAlignment(Pos.CENTER);
            ScrollPane listOfSelectedDatesVBoxScrollPane = new ScrollPane(listOfSelectedDatesVBox);
            listOfSelectedDatesVBoxScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            listOfSelectedDatesVBoxScrollPane.setMaxHeight(150);
            listOfSelectedDatesVBoxScrollPane.setMinWidth(120);

            bottomBorderPane.setTop(assignDateLabel);
            BorderPane.setAlignment(assignDateLabel, Pos.CENTER);
            bottomBorderPane.setCenter(datePicker.getView());
            BorderPane.setAlignment(datePicker.getView(), Pos.CENTER);
            Separator verticalSeparator = new Separator(Orientation.VERTICAL);
            verticalSeparator.setPadding(new Insets(0, 0, 0, 40));
            HBox listOfDatesHBox = new HBox(verticalSeparator, listOfSelectedDatesVBoxScrollPane);
            listOfDatesHBox.setSpacing(40);
            listOfDatesHBox.setAlignment(Pos.CENTER);
            bottomBorderPane.setRight(listOfDatesHBox);
            BorderPane.setAlignment(listOfDatesHBox, Pos.CENTER);

            mainContainer.setBottom(bottomBorderPane);
            BorderPane.setAlignment(bottomBorderPane, Pos.CENTER);

            //We define the behaviour when we add or remove a date
            datePicker.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        // Handle added dates
                        for (LocalDate date : change.getAddedSubList()) {
                            dayTemplate.setDates(DatesToStringConversion.addDate(dayTemplate.getDates(), date));
                        }
                    }
                    if (change.wasRemoved()) {
                        // Handle removed dates
                        for (LocalDate date : change.getRemoved()) {
                            dayTemplate.setDates(DatesToStringConversion.removeDate(dayTemplate.getDates(), date));
                        }
                    }
                }
            });

            ObservableLists.bindConverted(listOfSelectedDatesVBox.getChildren(), datePicker.getSelectedDates(), this::drawSelectedDatesAsLine);

            initFormValidation();
        }

        private BorderPane drawSelectedDatesAsLine(LocalDate currentDate) {
            SVGPath trashDate = SvgIcons.createTrashSVGPath();
            trashDate.setTranslateY(2);
            Text currentDateValue = new Text(currentDate.format(DateTimeFormatter.ofPattern("MMM dd")));
            trashDate.setOnMouseClicked(event -> datePicker.getSelectedDates().remove(currentDate));
            ShapeTheme.createSecondaryShapeFacet(trashDate).style();
            BorderPane currentLineBorderPane = new BorderPane();
            BorderPane.setMargin(currentDateValue, new Insets(0, 20, 0, 10));
            currentLineBorderPane.setLeft(trashDate);
            currentLineBorderPane.setCenter(currentDateValue);
            currentLineBorderPane.setPadding(new Insets(0, 0, 3, 0));
            listOfSelectedDatesVBox.getChildren().add(currentLineBorderPane);
            return currentLineBorderPane;
        }


        private void initFormValidation() {
            validationSupport.addRequiredInput(templateNameTextField);
        }


        private void addTemplateTimeline() {
            Timeline newTimeLine = updateStore.insertEntity(Timeline.class);
            newTimeLine.setAudioOffered(false);
            newTimeLine.setVideoOffered(false);
            newTimeLine.setDayTemplate(dayTemplate);
            newTimeLine.setSite(currentSite);
            workingTemplateTimelines.add(newTimeLine);
        }


        private ScheduledItem addTeachingsScheduledItemsForDateAndTimeline(LocalDate date, String name, Timeline timeline, UpdateStore currentUpdateStore) {
            ScheduledItem teachingScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
            teachingScheduledItem.setEvent(currentEditedEvent);
            teachingScheduledItem.setSite(currentSite);
            teachingScheduledItem.setDate(date);
            teachingScheduledItem.setName(name);
            teachingScheduledItem.setTimeLine(timeline);
            teachingScheduledItem.setItem(timeline.getItem());
            return teachingScheduledItem;
        }

        private void addAudioScheduledItemsForDate(LocalDate date, ScheduledItem parentTeachingScheduledItem, UpdateStore currentUpdateStore) {
            //Here we add for each language not deprecated the scheduledItemAssociated to the date and parent scheduledItem*
            audioLanguages.forEach(languageItem -> {
                ScheduledItem audioScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
                audioScheduledItem.setEvent(currentEditedEvent);
                audioScheduledItem.setSite(currentSite);
                audioScheduledItem.setDate(date);
                audioScheduledItem.setParent(parentTeachingScheduledItem);
                audioScheduledItem.setItem(languageItem);
            });
        }

        private void addVideoScheduledItemsForDate(LocalDate date, ScheduledItem parentTeachingScheduledItem, UpdateStore currentUpdateStore) {
            ScheduledItem videoScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
            videoScheduledItem.setEvent(currentEditedEvent);
            videoScheduledItem.setSite(currentSite);
            videoScheduledItem.setDate(date);
            videoScheduledItem.setParent(parentTeachingScheduledItem);
            videoScheduledItem.setItem(videoItem);
        }


        private void removeDayTemplate(DayTemplate dayTemplate) {
            workingDayTemplates.remove(dayTemplate);
            removeTemplateTimeLineLinkedToDayTemplate(dayTemplate);
            updateStore.deleteEntity(dayTemplate);
            correspondenceBetweenDayTemplateAndDayTemplateManagement.remove(dayTemplate);
        }

        private void removeTemplateTimeLineLinkedToDayTemplate(DayTemplate dayTemplate) {
            workingTemplateTimelines.removeIf(timeline -> (timeline.getDayTemplate() == dayTemplate));
        }

        private void removeTemplateTimeLine(Timeline timeline) {
            updateStore.deleteEntity(timeline);
            workingTemplateTimelines.remove(timeline);
        }


        private HBox drawSelectedDatesAsLine(Timeline timeline) {
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
            });
            validationSupport.addValidationRule(FXProperties.compute(itemSelector.selectedItemProperty(), s1 -> itemSelector.getSelectedItem() != null), itemSelector.getButton(), I18n.getI18nText("ItemSelectedShouldntBeNull"));

            MonoPane selectorPane = new MonoPane(itemSelector.getButton());
            TextField fromTextField = new TextField();
            fromTextField.setMaxWidth(60);
            fromTextField.setPromptText("8:46");
            validationSupport.addValidationRule(FXProperties.compute(fromTextField.textProperty(), s1 -> isLocalTimeTextValid(fromTextField.getText())), fromTextField, I18n.getI18nText("ValidationTimeFormatIncorrect"));

            if (timeline.getStartTime() != null)
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
            if (timeline.getEndTime() != null)
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
            if (timeline.getName() != null)
                nameTextField.setText(timeline.getName());
            nameTextField.textProperty().addListener(obs -> timeline.setName(nameTextField.getText()));

            SVGPath audioAvailableIcon = SvgIcons.createSoundIconPath();
            audioAvailableIcon.setFill(Color.GREEN);
            SVGPath audioUnavailable = SvgIcons.createSoundIconInactivePath();
            audioUnavailable.setFill(Color.RED);
            MonoPane audioMonoPane = new MonoPane();
            int iconWith = 30;
            audioMonoPane.setMinWidth(iconWith);
            audioMonoPane.setAlignment(Pos.CENTER);
            audioMonoPane.setCursor(Cursor.HAND);
            if (timeline.isAudioOffered() != null) {
                if (timeline.isAudioOffered())
                    audioMonoPane.getChildren().setAll(audioAvailableIcon);
                else
                    audioMonoPane.getChildren().setAll(audioUnavailable);
            }
            audioMonoPane.setOnMouseClicked(e -> {
                timeline.setAudioOffered(!timeline.isAudioOffered());
                if (timeline.isAudioOffered()) {
                    audioMonoPane.getChildren().setAll(audioAvailableIcon);
                    //We add the recording scheduledItem for those date and associated timeline
//                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
//                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
//                            addAudioScheduledItemsForDate(date,teachingScheduledItem);
//                        }
//                    }));
                } else {
                    audioMonoPane.getChildren().setAll(audioUnavailable);
                    //We remove the recording scheduledItem for those date and associated timeline
//                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
//                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
//                            removeAudioScheduledItem(teachingScheduledItem);
//                        }
//                    }));
                }
            });

            SVGPath videoAvailableIcon = SvgIcons.createVideoIconPath();
            videoAvailableIcon.setFill(Color.GREEN);
            SVGPath videoUnavailableIcon = SvgIcons.createVideoIconInactivePath();
            videoUnavailableIcon.setFill(Color.RED);

            MonoPane videoMonoPane = new MonoPane();
            videoMonoPane.setCursor(Cursor.HAND);
            if (timeline.isVideoOffered() != null) {
                if (timeline.isVideoOffered())
                    videoMonoPane.getChildren().setAll(videoAvailableIcon);
                else
                    videoMonoPane.getChildren().setAll(videoUnavailableIcon);
            }

            videoMonoPane.setOnMouseClicked(e -> {
                timeline.setVideoOffered(!timeline.isVideoOffered());
                if (timeline.isVideoOffered()) {
                    videoMonoPane.getChildren().setAll(videoAvailableIcon);
                    //We add the recording scheduledItem for those date and associated timeline
//                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
//                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
//                            addVideoScheduledItemsForDate(date,teachingScheduledItem);
//                        }
//                    }));
                } else {
                    videoMonoPane.getChildren().setAll(videoUnavailableIcon);
                    //We remove the recording scheduledItem for those date and associated timeline
//                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
//                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
//                            removeVideoScheduledItem(teachingScheduledItem);
//                        }
//                    }));
                }
            });

            SVGPath trashImage = SvgIcons.createTrashSVGPath();
            MonoPane trashContainer = new MonoPane(trashImage);
            trashContainer.setCursor(Cursor.HAND);
            trashContainer.setOnMouseClicked(event -> removeTemplateTimeLine(timeline));
            ShapeTheme.createSecondaryShapeFacet(trashImage).style();

            HBox afterItemSelectorLine = new HBox(fromTextField, toLabel, untilTextField, nameTextField, audioMonoPane, videoMonoPane, trashContainer);
            afterItemSelectorLine.setAlignment(Pos.CENTER_RIGHT);
            afterItemSelectorLine.setSpacing(10);
            selectorPane.setPadding(new Insets(0, 20, 0, 0));
            HBox line = new HBox(selectorPane, afterItemSelectorLine);
            line.setAlignment(Pos.CENTER_LEFT);
            line.setPadding(new Insets(5, 0, 5, 0));
            return line;
        }

        public BorderPane getContainer() {
            return mainContainer;
        }
    }
}

