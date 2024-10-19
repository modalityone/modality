package one.modality.event.backoffice.activities.program;


import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.collection.Collections;
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
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.client.util.masterslave.ModalitySlaveEditor;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.*;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
public final class ProgramPanel extends ModalitySlaveEditor<Event> implements ButtonFactoryMixin {

    private static final double MAX_WIDTH = 1500;

    private final KnownItemFamily programItemFamily;

    //This parameter will allow us to manage the interaction and behaviour of the Panel that display the details of an event and the event selected
    private final MasterSlaveLinker<Event> masterSlaveEventLinker = new MasterSlaveLinker<>(this);

    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    final EntityStore entityStore = EntityStore.create(dataSourceModel);
    final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private final ObjectProperty<Event> loadedEventProperty = new SimpleObjectProperty<>();

    Site programSite;
    List<Item> languageAudioItems;
    Item videoItem;

    final ObservableList<DayTemplate> workingDayTemplates = FXCollections.observableArrayList();
    private final Map<DayTemplate, DayTemplatePanel> correspondenceBetweenDayTemplateAndDayTemplatePanel = new IdentityHashMap<>();
    final BooleanProperty programGeneratedProperty = new SimpleBooleanProperty();

    private VBox mainVBox;
    final ModalityValidationSupport validationSupport = new ModalityValidationSupport();
    private boolean validationSupportInitialised;

    public ProgramPanel(KnownItemFamily programItemFamily) {
        this.programItemFamily = programItemFamily;
    }

    public Node getPanel() {
        if (mainVBox == null)
            buildUi();
        return mainVBox;
    }

    void startLogic() {
        masterSlaveEventLinker.masterProperty().bindBidirectional(FXEvent.eventProperty());
    }

    @Override
    public void setSlave(Event approvedEntity) {
        super.setSlave(approvedEntity);
        reloadProgramFromSelectedEvent();
    }

    @Override
    public boolean hasChanges() {
        return updateStore.hasChanges();
    }

    // Private implementation

    Event getLoadedEvent() {
        return loadedEventProperty.get();
    }

    void setLoadedEvent(Event loadedEvent) {
        loadedEventProperty.set(loadedEvent);
    }

    private void buildUi() {
        // Building the top line
        Label subtitle = I18nControls.bindI18nProperties(Bootstrap.h4(new Label()), new I18nSubKey("expression: '[Programme] - ' + name + ' (' + dateIntervalFormat(startDate, endDate)+')'", loadedEventProperty), loadedEventProperty);
        subtitle.setWrapText(true);
        TextTheme.createSecondaryTextFacet(subtitle).style();

        Button addTemplateButton = Bootstrap.primaryButton(I18nControls.bindI18nProperties(new Button(), "AddDayTemplate"));
        addTemplateButton.setGraphicTextGap(10);
        addTemplateButton.setPadding(new Insets(20, 200, 20, 0));
        addTemplateButton.setOnAction(event1 -> addNewDayTemplate());

        HBox topLine = new HBox(subtitle, LayoutUtil.createHGrowable(), addTemplateButton);

        // Building the bottom line
        Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.bindI18nProperties(new Button(), ProgramI18nKeys.CancelProgram));
        cancelButton.setOnAction(e -> reloadProgramFromSelectedEvent());

        Button saveButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), ProgramI18nKeys.SaveProgram));
        saveButton.setOnAction(e -> {
            if (validateForm()) {
                submitUpdateStoreChanges(updateStore, saveButton, cancelButton);
            }
        });

        BooleanBinding hasNoChangesProperty = updateStore.hasChangesProperty().not();
        saveButton.disableProperty().bind(hasNoChangesProperty);
        cancelButton.disableProperty().bind(hasNoChangesProperty);


        Button generateProgramButton = Bootstrap.largePrimaryButton(I18nControls.bindI18nProperties(new Button(), ProgramI18nKeys.GenerateProgram));
        generateProgramButton.setOnAction(e -> ModalityDialog.showConfirmationDialog(ProgramI18nKeys.ProgramGenerationConfirmation, () -> generateProgram(generateProgramButton)));

        Button deleteProgramButton = Bootstrap.largePrimaryButton(I18nControls.bindI18nProperties(new Button(), ProgramI18nKeys.DeleteProgram));
        deleteProgramButton.setOnAction(e -> ModalityDialog.showConfirmationDialog(ProgramI18nKeys.DeleteProgramConfirmation, () -> deleteProgram(deleteProgramButton)));

        generateProgramButton.disableProperty().bind(updateStore.hasChangesProperty());
        generateProgramButton.visibleProperty().bind(programGeneratedProperty.not());
        deleteProgramButton.visibleProperty().bind(programGeneratedProperty);
        LayoutUtil.setAllUnmanagedWhenInvisible(generateProgramButton, deleteProgramButton);

        HBox bottomLine = new HBox(cancelButton, saveButton, generateProgramButton, deleteProgramButton);

        bottomLine.setAlignment(Pos.BASELINE_CENTER);
        bottomLine.setSpacing(100);

        // Building the template days
        ColumnsPane templateDayColumnsPane = new ColumnsPane();
        templateDayColumnsPane.setMaxColumnCount(2);
        templateDayColumnsPane.setHgap(100);
        templateDayColumnsPane.setVgap(50);
        templateDayColumnsPane.setPadding(new Insets(50, 0, 50, 0));
        templateDayColumnsPane.setAlignment(Pos.TOP_CENTER);

        //We add a listener on the workingDayTemplates to create a new panel or remove it according to the list changes.
        // Handle added elements
        // Handle removed elements
        // Find and remove the corresponding UI element
        // Add the listener to the observable list
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
                        DayTemplatePanel dayTemplatePanel = correspondenceBetweenDayTemplateAndDayTemplatePanel.get(removedItem);
                        if (dayTemplatePanel == null) {
                            correspondenceBetweenDayTemplateAndDayTemplatePanel.get(removedItem);
                        }
                        templateDayColumnsPane.getChildren().remove(dayTemplatePanel.getPanel());
                    }
                }
            }
        });

        // Building the event state line
        Label eventStateLabel = Bootstrap.h4(Bootstrap.textSecondary(new Label()));
        FXProperties.runNowAndOnPropertiesChange(() -> {
            I18nControls.bindI18nProperties(eventStateLabel, programGeneratedProperty.get() ? ProgramI18nKeys.ScheduledItemsAlreadyGenerated : ProgramI18nKeys.ScheduledItemsNotYetGenerated);
        }, programGeneratedProperty);

        HBox eventStateLine = new HBox(eventStateLabel);
        eventStateLine.setAlignment(Pos.CENTER);
        eventStateLine.setPadding(new Insets(0, 0, 30, 0));

        mainVBox = new VBox(
            topLine,
            templateDayColumnsPane,
            eventStateLine,
            bottomLine
        );
        mainVBox.setMaxWidth(MAX_WIDTH);
    }

    private void reloadProgramFromSelectedEvent() {
        Event selectedEvent = getSlave();

        //First we reset everything
        resetStoresAndOtherComponents();

        //We execute the query in batch, otherwise we can have synchronisation problem between the different threads
        entityStore.executeQueryBatch(
                // Index 0: day templates
                new EntityStoreQuery("select name, event.(livestreamUrl,vodExpirationDate,audioExpirationDate), dates from DayTemplate si where event=? order by name", new Object[]{selectedEvent}),
                // Index 1: program site (singleton list)
                new EntityStoreQuery("select name from Site where event=? and main limit 1", new Object[]{ selectedEvent }),
                // Index 2: items for this program item family + audio recording + video
                new EntityStoreQuery("select name,family.code from Item where organization=? and family.code in (?,?,?)",
                    new Object[]{ selectedEvent.getOrganization(), programItemFamily.getCode(), KnownItemFamily.AUDIO_RECORDING.getCode(), KnownItemFamily.VIDEO.getCode() })
            )
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                // Extracting the different entity lists from the query batch result
                EntityList<DayTemplate> dayTemplates = entityLists[0];
                EntityList<Site> sites = entityLists[1];
                EntityList<Item> items = entityLists[2];

                programSite = Collections.first(sites);
                languageAudioItems = Collections.filter(items, item -> KnownItemFamily.AUDIO_RECORDING.getCode().equals(item.getFamily().getCode()));
                videoItem =   Collections.findFirst(items, item -> KnownItemFamily.VIDEO.getCode().equals(item.getFamily().getCode()));
                setLoadedEvent(entityStore.copyEntity(selectedEvent));
                workingDayTemplates.setAll(dayTemplates.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
            }));
    }

    private void addNewDayTemplate() {
        DayTemplate dayTemplate = updateStore.insertEntity(DayTemplate.class);
        dayTemplate.setEvent(getLoadedEvent());
        workingDayTemplates.add(dayTemplate);
    }

    void deleteDayTemplate(DayTemplate dayTemplate) {
        DayTemplatePanel dayTemplatePanel = correspondenceBetweenDayTemplateAndDayTemplatePanel.remove(dayTemplate);
        workingDayTemplates.remove(dayTemplate);
        if (dayTemplatePanel != null)
            dayTemplatePanel.removeTemplateTimeLineLinkedToDayTemplate(dayTemplate);
        updateStore.deleteEntity(dayTemplate);
    }

    private void generateProgram(Button generateProgramButton) {
        UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);
        List<Timeline> newlyCreatedEventTimelines = new ArrayList<>();
        //Here, we take all the template timelines, and create the event timelines needed
        //We create an event timeline for all template timelines having distinct element on {item, startTime, endTime}
        workingDayTemplates.forEach(currentDayTemplate -> {
            DayTemplatePanel dayTemplatePanel = correspondenceBetweenDayTemplateAndDayTemplatePanel.get(currentDayTemplate);

            dayTemplatePanel.workingTemplateTimelines.forEach(currentTemplateTimeline -> {
                LocalTime startTime = currentTemplateTimeline.getStartTime();
                LocalTime endTime = currentTemplateTimeline.getEndTime();
                Item item = currentTemplateTimeline.getItem();
                Timeline templateTimelineToEdit = localUpdateStore.updateEntity(currentTemplateTimeline);
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
                    eventTimeLine = localUpdateStore.insertEntity(Timeline.class);
                    eventTimeLine.setEvent(getLoadedEvent());
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
                    ScheduledItem teachingScheduledItem = dayTemplatePanel.addTeachingsScheduledItemsForDateAndTimeline(date, currentTemplateTimeline.getName(), eventTimeLine, localUpdateStore);
                    if (currentTemplateTimeline.isAudioOffered()) {
                        dayTemplatePanel.addAudioScheduledItemsForDate(date, teachingScheduledItem, localUpdateStore);
                    }
                    if (currentTemplateTimeline.isVideoOffered()) {
                        dayTemplatePanel.addVideoScheduledItemsForDate(date, teachingScheduledItem);
                    }
                }
            });
        });
        submitUpdateStoreChanges(localUpdateStore, generateProgramButton);
    }

    private void deleteProgram(Button deleteProgramButton) {
        UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);

        //Here we look for the teachings, audio and video scheduled Item related to this timeline and delete them
        entityStore.<ScheduledItem>executeQuery(
                new EntityStoreQuery("select id, item.family.code from ScheduledItem si where event=? order by name", new Object[]{ getLoadedEvent() }))
            .onFailure(Console::log)
            .onSuccess(scheduledItems -> Platform.runLater(() -> {
                //First we delete all the audios and videos scheduledItem
                scheduledItems.forEach(currentScheduledItem -> {
                    String scheduledItemFamilyCode = currentScheduledItem.getItem().getFamily().getCode();
                    if(scheduledItemFamilyCode.equals(KnownItemFamily.AUDIO_RECORDING.getCode()) || scheduledItemFamilyCode.equals(KnownItemFamily.VIDEO.getCode()))
                        localUpdateStore.deleteEntity(currentScheduledItem);
                    });

                scheduledItems.forEach(currentScheduledItem -> {
                    String code = currentScheduledItem.getItem().getFamily().getCode();
                    if(code.equals(KnownItemFamily.TEACHING.getCode()))
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
                submitUpdateStoreChanges(localUpdateStore, deleteProgramButton);
            }));
    }

    private void submitUpdateStoreChanges(UpdateStore updateStore, Labeled... buttons) {
        OperationUtil.turnOnButtonsWaitModeDuringExecution(
            updateStore.submitChanges()
                .onFailure(x-> {
                    DialogContent dialog = DialogContent.createConfirmationDialog("Error","Operation failed", x.getMessage());
                    dialog.setOk();
                    Platform.runLater(()-> {
                        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
                        dialog.getPrimaryButton().setOnAction(a->dialog.getDialogCallback().closeDialog());
                    });
                    Console.log(x);
                })
                .onSuccess(x -> Platform.runLater(() -> {
                    reloadProgramFromSelectedEvent();
                    OperationUtil.turnOffButtonsWaitMode(buttons);
                })),
            buttons
        );
    }

    private boolean validateForm() {
        if (!validationSupportInitialised) {
            initFormValidation();
            validationSupportInitialised = true;
        }
        return validationSupport.isValid();
    }

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

    DayTemplatePanel getOrCreateDayTemplatePanel(DayTemplate dayTemplate) {
        DayTemplatePanel dayTemplatePanel = correspondenceBetweenDayTemplateAndDayTemplatePanel.get(dayTemplate);
        if (dayTemplatePanel == null)
            correspondenceBetweenDayTemplateAndDayTemplatePanel.put(dayTemplate, dayTemplatePanel = new DayTemplatePanel(dayTemplate, this));
        return dayTemplatePanel;
    }

    private BorderPane drawDayTemplate(DayTemplate dayTemplate) {
        return getOrCreateDayTemplatePanel(dayTemplate).getPanel();
    }

    /**
     * This method is used to reset the different components in this class
     */
    private void resetStoresAndOtherComponents() {
        validationSupport.reset();
        validationSupportInitialised = false;
        workingDayTemplates.clear();
        entityStore.clear();
        updateStore.clear();
        programGeneratedProperty.setValue(false);
    }

}

