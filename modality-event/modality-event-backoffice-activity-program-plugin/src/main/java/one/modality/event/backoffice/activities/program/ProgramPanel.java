package one.modality.event.backoffice.activities.program;


import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.extras.util.masterslave.SlaveEditor;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.util.masterslave.ModalitySlaveEditor;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.*;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public final class ProgramPanel implements ButtonFactoryMixin {

    private static final double MAX_WIDTH = 1500;

    private final KnownItemFamily itemFamily;
    private final VBox mainVBox = new VBox();

    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    final UpdateStore updateStore = UpdateStore.createAbove(entityStore);

    final ObservableList<DayTemplate> workingDayTemplates = FXCollections.observableArrayList();
    private final ObservableList<Item> workingItems = FXCollections.observableArrayList();
    private List<DayTemplate> dayTemplatesReadFromDatabase = new ArrayList<>();
    private List<Item> itemsReadFromDatabase = new ArrayList<>();
    final ModalityValidationSupport validationSupport = new ModalityValidationSupport();
    private boolean validationSupportInitialised = false;

    private Event currentEditedEvent;
    private ColumnsPane templateDayColumnsPane;
    Site currentSite;
    private final Label festivalDescriptionLabel = new Label();
    final Map<DayTemplate, DayTemplatePanel> correspondenceBetweenDayTemplateAndDayTemplatePanel = new IdentityHashMap<>();
    EntityList<Item> audioLanguages;
    Item videoItem;
    private ListChangeListener<DayTemplate> dayTemplateListChangeListener;
    private final Button saveButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), ProgramI18nKeys.SaveProgram));
    private final Button generateProgramButton = Bootstrap.largePrimaryButton(I18nControls.bindI18nProperties(new Button(), ProgramI18nKeys.GenerateProgram));
    private final Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.bindI18nProperties(new Button(), ProgramI18nKeys.CancelProgram));
    final BooleanProperty areScheduledItemBeenGeneratedProperty = new SimpleBooleanProperty(false);
    private final StringProperty eventStateDescriptionStringProperty = new SimpleStringProperty();
    private final StringProperty generateButtonLabelStringProperty = new SimpleStringProperty();

    public ProgramPanel(KnownItemFamily itemFamily) {
        this.itemFamily = itemFamily;

        eventStateDescriptionStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.ScheduledItemsNotYetGenerated));
        generateButtonLabelStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.GenerateProgram));

        areScheduledItemBeenGeneratedProperty.addListener((observable, oldValue, generated) -> {
            if (generated) {
                eventStateDescriptionStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.ScheduledItemsAlreadyGenerated));
                generateButtonLabelStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.DeleteProgram));
            } else {
                eventStateDescriptionStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.ScheduledItemsNotYetGenerated));
                generateButtonLabelStringProperty.setValue(I18n.getI18nText(ProgramI18nKeys.GenerateProgram));
            }
        });
    }

    public Node getPanel() {
        //The main container is build by drawContainer, who is called by displayEventDetails.
        return mainVBox;
    }

    void startLogic() {
        masterSlaveEventLinker.masterProperty().bindBidirectional(FXEvent.eventProperty());
    }

    // Private implementation

    private boolean validateForm() {
        if (!validationSupportInitialised) {
            initFormValidation();
            validationSupportInitialised = true;
        }
        return validationSupport.isValid();
    }

    private void drawUIContainer() {
        areScheduledItemBeenGeneratedProperty.setValue(false);

        Button generateProgramButton = Bootstrap.largePrimaryButton(new Button());
        generateProgramButton.textProperty().bind(generateButtonLabelStringProperty);

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
                        DayTemplatePanel dayTemplatePanel = correspondenceBetweenDayTemplateAndDayTemplatePanel.get(removedItem);
                        if (dayTemplatePanel == null) {
                            correspondenceBetweenDayTemplateAndDayTemplatePanel.get(removedItem);
                        }
                        templateDayColumnsPane.getChildren().remove(dayTemplatePanel.getPanel());
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
        mainVBox.setMaxWidth(MAX_WIDTH);
    }


    private void addNewDayTemplate() {
        DayTemplate dayTemplate = updateStore.insertEntity(DayTemplate.class);
        dayTemplate.setEvent(currentEditedEvent);
        workingDayTemplates.add(dayTemplate);
    }

    private void generateProgram() {
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        List<Timeline> newlyCreatedEventTimelines = new ArrayList<>();
        //Here, we take all the template timelines, and create the event timelines needed
        //We create an event timeline for all template timelines having distinct element on {item, startTime, endTime}
        workingDayTemplates.forEach(currentDayTemplate -> {
            DayTemplatePanel dayTemplatePanel = correspondenceBetweenDayTemplateAndDayTemplatePanel.get(currentDayTemplate);

            dayTemplatePanel.workingTemplateTimelines.forEach(currentTemplateTimeline -> {
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
                for (LocalDate date : dayTemplatePanel.datePicker.getSelectedDates()) {
                    ScheduledItem teachingScheduledItem = dayTemplatePanel.addTeachingsScheduledItemsForDateAndTimeline(date, currentTemplateTimeline.getName(), eventTimeLine, updateStore);
                    if (currentTemplateTimeline.isAudioOffered()) {
                        dayTemplatePanel.addAudioScheduledItemsForDate(date, teachingScheduledItem, updateStore);
                    }
                    if (currentTemplateTimeline.isVideoOffered()) {
                        dayTemplatePanel.addVideoScheduledItemsForDate(date, teachingScheduledItem);
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
        entityStore.<ScheduledItem>executeQuery(
                new EntityStoreQuery("select id, item.family.code from ScheduledItem si where event=? order by name", new Object[]{ currentEditedEvent }))
            .onFailure(Console::log)
            .onSuccess(scheduledItems -> Platform.runLater(() -> {
                EntityList<ScheduledItem> scheduledItemsList = scheduledItems;
                //First we delete all the audios and videos scheduledItem
                scheduledItemsList.forEach(currentScheduledItem -> {
                    String scheduledItemFamilyCode = currentScheduledItem.getItem().getFamily().getCode();
                    if(scheduledItemFamilyCode.equals(KnownItemFamily.AUDIO_RECORDING.getCode()) || scheduledItemFamilyCode.equals(KnownItemFamily.VIDEO.getCode()))
                        localUpdateStore.deleteEntity(currentScheduledItem);
                    });

                scheduledItemsList.forEach(currentScheduledItem -> {
                    String scheduledItemFamilyCode = currentScheduledItem.getItem().getFamily().getCode();
                    if(scheduledItemFamilyCode.equals(KnownItemFamily.TEACHING.getCode()))
                        localUpdateStore.deleteEntity(currentScheduledItem);
                });

                //then we put the reference to the event timeline to null on the template timeline
                workingDayTemplates.forEach(currentDayTemplate -> {
                    DayTemplatePanel dayTemplatePanel = correspondenceBetweenDayTemplateAndDayTemplatePanel.get(currentDayTemplate);
                    dayTemplatePanel.workingTemplateTimelines.forEach(currentTemplateTimeline -> {
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

    private void displayEventDetails(Event e) {
        Console.log("Display Event Called");
        currentEditedEvent = e;

        //First we reset everything
        resetUpdateStoreAndOtherComponents();

        e.onExpressionLoaded("livestreamUrl,vodExpirationDate,audioExpirationDate")
            .onFailure((Console::log));

        //We execute the query in batch, otherwise we can have synchronisation problem between the different threads
        entityStore.executeQueryBatch(
                new EntityStoreQuery("select name, event, dates from DayTemplate si where event=? order by name", new Object[]{ e }),
                new EntityStoreQuery("select distinct name from Item where organization=? and family.code=?", new Object[]{ e.getOrganization(), itemFamily.getCode() }),
                new EntityStoreQuery("select name from Site where event=? and main limit 1", new Object[]{ e }),
                new EntityStoreQuery("select distinct name from Item where organization=? and family.code = ? and not deprecated order by name ",
                    new Object[]{ FXOrganization.getOrganization(), KnownItemFamily.AUDIO_RECORDING.getCode() }),
                new EntityStoreQuery("select distinct name from Item where organization=? and family.code = ? and not deprecated order by name ",
                    new Object[]{ FXOrganization.getOrganization(), KnownItemFamily.VIDEO.getCode() }))
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                EntityList<DayTemplate> dayTemplateList = entityLists[0];
                EntityList<Item> itemList = entityLists[1];
                EntityList<Site> siteList = entityLists[2];
                audioLanguages = entityLists[3];
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

    private final SlaveEditor<Event> eventDetailsSlaveEditor = new ModalitySlaveEditor<>() {
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
    private final MasterSlaveLinker<Event> masterSlaveEventLinker = new MasterSlaveLinker<>(eventDetailsSlaveEditor);

    private void initFormValidation() {
        if (!validationSupportInitialised) {
            FXProperties.runNowAndOnPropertiesChange(() -> {
                if (I18n.getDictionary() != null) {
                    validationSupport.reset();
                    workingDayTemplates.forEach(wt -> {
                        DayTemplatePanel dtm = correspondenceBetweenDayTemplateAndDayTemplatePanel.get(wt);
                        dtm.initFormValidation();
                    });
                }
            }, I18n.dictionaryProperty());
            validationSupportInitialised = true;
        }
    }

    private BorderPane drawDayTemplate(DayTemplate dayTemplate) {
        DayTemplatePanel dayTemplatePanel = new DayTemplatePanel(dayTemplate, this);
        correspondenceBetweenDayTemplateAndDayTemplatePanel.put(dayTemplate, dayTemplatePanel);
        return dayTemplatePanel.getPanel();
    }

    /**
     * This method is used to reset the different components in this class
     */
    private void resetUpdateStoreAndOtherComponents() {
        validationSupport.reset();
        mainVBox.getChildren().clear();
        if (dayTemplateListChangeListener != null)
            workingDayTemplates.removeListener(dayTemplateListChangeListener);
        workingDayTemplates.clear();
        if (templateDayColumnsPane != null)
            templateDayColumnsPane.getChildren().clear();
        dayTemplatesReadFromDatabase.clear();
        itemsReadFromDatabase.clear();
        validationSupportInitialised = false;
        correspondenceBetweenDayTemplateAndDayTemplatePanel.clear();
        updateStore.cancelChanges();
    }

}

