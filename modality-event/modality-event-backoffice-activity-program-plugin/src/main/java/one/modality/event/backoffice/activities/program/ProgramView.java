package one.modality.event.backoffice.activities.program;


import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.OptimizedObservableListWrapper;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.client.util.masterslave.ModalitySlaveEditor;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Item;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.client.event.fx.FXEvent;

import java.util.stream.Collectors;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class ProgramView extends ModalitySlaveEditor<Event> implements ButtonFactoryMixin  {

    private static final double MAX_WIDTH = 1600;

    private final ProgramModel programModel;

    //This parameter will allow us to manage the interaction and behaviour of the Panel that display the details of an event and the event selected
    private final MasterSlaveLinker<Event> masterSlaveEventLinker = new MasterSlaveLinker<>(this);
    final ObservableList<DayTemplateView> workingDayTemplateViews = FXCollections.observableArrayList();

    private ButtonSelector<Item> itemSelector;

    private final VBox mainVBox;


    ProgramView(ProgramModel programModel) {
        this.programModel = programModel;
        mainVBox = buildUi();
        mainVBox.setMaxWidth(MAX_WIDTH);
        ObservableLists.bindConvertedOptimized(workingDayTemplateViews, programModel.getWorkingDayTemplates(), DayTemplateView::new);
    }

    public Node getView() {
        return mainVBox;
    }

    void startLogic() {
        masterSlaveEventLinker.masterProperty().bindBidirectional(FXEvent.eventProperty());
    }

    @Override
    public void setSlave(Event approvedEntity) {
        super.setSlave(approvedEntity);
        programModel.reloadProgramFromSelectedEvent(approvedEntity);
    }

    private void syncItemModelFromUi() {programModel.setBookableTeachingItem(itemSelector.getSelectedItem());
    }


    @Override
    public boolean hasChanges() {
        return programModel.getUpdateStore().hasChanges();
    }

    // Private implementation

    private VBox buildUi() {
        // Building the top line
        ObjectProperty<Event> loadedEventProperty = programModel.loadedEventProperty();
        Label subtitle = Bootstrap.strong(Bootstrap.h4(I18nEntities.newExpressionLabel(loadedEventProperty,
            "'[" + ProgramI18nKeys.Programme + "] - ' + name + ' (' + dateIntervalFormat(startDate, endDate) +')'")));
        subtitle.setWrapText(true);

        HBox topLine = new HBox(subtitle);
        topLine.setAlignment(Pos.CENTER);
        topLine.setPadding(new Insets(20, 0, 20, 0));

        // Building the day ticket configuration warning
        Label dayTicketConfigLabel = Bootstrap.strong(new Label());
        dayTicketConfigLabel.setWrapText(true);
        dayTicketConfigLabel.setMaxWidth(750);

        VBox dayTicketConfigBox = new VBox(dayTicketConfigLabel);
        dayTicketConfigBox.setAlignment(Pos.CENTER);
        dayTicketConfigBox.setMaxWidth(MAX_WIDTH);

        FXProperties.runNowAndOnPropertyChange(event -> {
            if (event != null) {
                boolean teachingEnabled = event.isTeachingsDayTicket();
                boolean audioEnabled = event.isAudioRecordingsDayTicket();

                String teachingStatus = teachingEnabled ? "✓ ENABLED" : "✗ DISABLED";
                String audioStatus = audioEnabled ? "✓ ENABLED" : "✗ DISABLED";
                I18nControls.bindI18nProperties(dayTicketConfigLabel, ProgramI18nKeys.DayTicketConfiguration, teachingStatus, audioStatus);

                // Apply danger alert if either is disabled, info alert if both enabled
                dayTicketConfigBox.getStyleClass().clear();
                if (!teachingEnabled || !audioEnabled) {
                    Bootstrap.alertDanger(dayTicketConfigBox);
                } else {
                    Bootstrap.alertSuccess(dayTicketConfigBox);
                }
            }
        }, loadedEventProperty);

        HBox dayTicketConfigLine = new HBox(dayTicketConfigBox);
        dayTicketConfigLine.setAlignment(Pos.CENTER);
        dayTicketConfigLine.setPadding(new Insets(0, 0, 20, 0));

        // Building the recurring item blocking message
        Label recurringItemWarningLabel = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.RecurringItemEventNotSupported));
        recurringItemWarningLabel.setWrapText(true);
        recurringItemWarningLabel.setMaxWidth(800);

        VBox recurringItemWarningBox = Bootstrap.alertDanger(new VBox(recurringItemWarningLabel));
        recurringItemWarningBox.setAlignment(Pos.CENTER);
        recurringItemWarningBox.setMaxWidth(MAX_WIDTH);

        HBox recurringItemWarningLine = new HBox(recurringItemWarningBox);
        recurringItemWarningLine.setAlignment(Pos.CENTER);
        recurringItemWarningLine.setPadding(new Insets(20, 0, 20, 0));

        // Building the bottom line
        Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.newButton(ProgramI18nKeys.CancelProgram));
        cancelButton.setOnAction(e -> programModel.cancelChanges());

        Button saveButton = Bootstrap.largeSuccessButton(I18nControls.newButton(ProgramI18nKeys.SaveProgram));
        saveButton.setOnAction(e -> programModel.saveChanges(saveButton, cancelButton));

        UpdateStore updateStore = programModel.getUpdateStore();
        BooleanExpression hasChangesProperty = EntityBindings.hasChangesProperty(updateStore);
        BooleanBinding hasNoChangesProperty = hasChangesProperty.not();
        saveButton.disableProperty().bind(hasNoChangesProperty);
        cancelButton.disableProperty().bind(hasNoChangesProperty);

        Button generateProgramButton = Bootstrap.largePrimaryButton(I18nControls.newButton(ProgramI18nKeys.GenerateProgram));
        generateProgramButton.setOnAction(e -> ModalityDialog.showConfirmationDialog(ProgramI18nKeys.ProgramGenerationConfirmation, programModel::generateProgram));

        Button deleteProgramButton = Bootstrap.largePrimaryButton(I18nControls.newButton(ProgramI18nKeys.DeleteProgram));
        deleteProgramButton.setOnAction(e -> ModalityDialog.showConfirmationDialog(ProgramI18nKeys.DeleteProgramConfirmation, programModel::deleteProgram));

        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();
        generateProgramButton.visibleProperty().bind(programGeneratedProperty.not());
        deleteProgramButton.visibleProperty().bind(programGeneratedProperty);
        Layouts.bindAllManagedToVisibleProperty(generateProgramButton, deleteProgramButton);

        HBox bottomLine = new HBox(cancelButton, saveButton, generateProgramButton, deleteProgramButton);

        bottomLine.setAlignment(Pos.BASELINE_CENTER);
        bottomLine.setSpacing(100);

        // Building the template days
        ColumnsPane templateDayColumnsPane = new ColumnsPane();
        templateDayColumnsPane.setMinColumnWidth(500);
        templateDayColumnsPane.hgapProperty().bind(templateDayColumnsPane.widthProperty().map(w -> Math.min(50, 0.02 * w.doubleValue())));
        templateDayColumnsPane.vgapProperty().bind(templateDayColumnsPane.hgapProperty());
        templateDayColumnsPane.setPadding(new Insets(50, 0, 20, 0));
        templateDayColumnsPane.setAlignment(Pos.TOP_CENTER);
        ObservableLists.bindConvertedOptimized(templateDayColumnsPane.getChildren(), workingDayTemplateViews, DayTemplateView::getPanel);

        // Building the add template button section
        Button addTemplateButton = Bootstrap.primaryButton(I18nControls.newButton(ProgramI18nKeys.AddDayTemplate));
        addTemplateButton.setGraphicTextGap(10);
        addTemplateButton.setOnAction(e -> programModel.addNewDayTemplate());
        HBox addTemplateButtonBox = new HBox(addTemplateButton);
        addTemplateButtonBox.setAlignment(Pos.CENTER);
        addTemplateButtonBox.setPadding(new Insets(0, 0, 30, 0));

        BooleanProperty dayTicketPreliminaryScheduledItemProperty = programModel.getDayTicketPreliminaryScheduledItemProperty();

        Label dayTicketTeachingAndAudioScheduledItemGenerationLabel = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.DayTicketTeachingsAndAudioScheduledItemNotGenerated));
        dayTicketTeachingAndAudioScheduledItemGenerationLabel.setWrapText(true);
        dayTicketTeachingAndAudioScheduledItemGenerationLabel.setMaxWidth(750);

        Label chooseAnItemLabel = I18nControls.newLabel(ProgramI18nKeys.ChooseAnItemForTheTeachingBookableScheduledItem);
        chooseAnItemLabel.setWrapText(true);

        itemSelector = new EntityButtonSelector<Item>( // language=JSON5
            "{class: 'Item', alias: 's', where: 'family.code=`teach`', orderBy :'name'}",
            this, FXMainFrameDialogArea::getDialogArea, programModel.getEntityStore().getDataSourceModel())
            .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o));
        Button itemSelectorButton = itemSelector.getButton();
        itemSelectorButton.setMinWidth(250);
        FXProperties.runOnPropertyChange(this::syncItemModelFromUi, itemSelector.selectedItemProperty());

        Button generatePreliminaryBookableSIButton = Bootstrap.primaryButton(I18nControls.newButton(ProgramI18nKeys.GeneratePreliminaryBookableSI));

        generatePreliminaryBookableSIButton.setOnAction(e -> {
            if(itemSelector.getSelectedItem()==null) {
                DialogContent dialogContent = new DialogContent().setContentText(I18n.getI18nText(ProgramI18nKeys.PleaseSelectAnItem)).setOk();
                DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, FXMainFrameDialogArea.getDialogArea());
                DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
                return;
            }
                AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    programModel.generatePreliminaryBookableSI()
                        .inUiThread()
                        .onFailure(error -> {
                            DialogContent dialogContent = new DialogContent().setContentText(error.getMessage()).setOk();
                            DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, FXMainFrameDialogArea.getDialogArea());
                            DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
                            updateStore.cancelChanges();
                        })
                        .onSuccess(success-> programModel.getDayTicketPreliminaryScheduledItemProperty().setValue(true))
                    , generatePreliminaryBookableSIButton);});

        HBox itemAndButtonHBox = new HBox(20,chooseAnItemLabel,itemSelectorButton,generatePreliminaryBookableSIButton);
        itemAndButtonHBox.setAlignment(Pos.BASELINE_CENTER);
        itemAndButtonHBox.setPadding(new Insets(20,0,0,0));
        VBox generatePreliminaryBookableScheduledItemVBox = Bootstrap.alertInfo(new VBox(10, dayTicketTeachingAndAudioScheduledItemGenerationLabel,itemAndButtonHBox));
        generatePreliminaryBookableScheduledItemVBox.setAlignment(Pos.CENTER);
        generatePreliminaryBookableScheduledItemVBox.setMaxWidth(800);

        generatePreliminaryBookableScheduledItemVBox.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty.not());
        generatePreliminaryBookableScheduledItemVBox.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty.not());

        Label dayTicketTeachingAndAudioScheduledItemInfoLabel = Bootstrap.strong(new Label());
        dayTicketTeachingAndAudioScheduledItemInfoLabel.setWrapText(true);
        dayTicketTeachingAndAudioScheduledItemInfoLabel.setMaxWidth(750);

        FXProperties.runNowAndOnPropertiesChange(dayTicketTeachingsAndAudioScheduledItemGenerated -> {
            if(FXEvent.getEvent()!=null) {
                programModel.reloadProgramFromSelectedEvent(FXEvent.getEvent());
            }
            //We delay a bit so the request has the time to execute so the program is reloaded.
            //TODO improve to wait for the reload to be finished rather than waiting 300ms
            UiScheduler.scheduleDelay(300, ()-> {
                String teachingItemName = "Not loaded";
                String languageItemNames = "Not loaded";
                if (programModel.getTeachingsBookableScheduledItems() != null) {
                    teachingItemName = programModel.getTeachingsBookableScheduledItems().stream()
                        .map(scheduledItem -> scheduledItem.getItem().getName())
                        .distinct()
                        .collect(Collectors.joining(", "));
                    languageItemNames = programModel.getAudioRecordingsBookableScheduledItems().stream()
                        .map(scheduledItem -> scheduledItem.getItem().getName())
                        .distinct()
                        .collect(Collectors.joining(", "));
                }
                I18nControls.bindI18nProperties(dayTicketTeachingAndAudioScheduledItemInfoLabel, ProgramI18nKeys.DayTicketTeachingsAndAudioScheduledItemInfos, teachingItemName, languageItemNames);
            });
           },dayTicketPreliminaryScheduledItemProperty,FXEvent.eventProperty());

        VBox dayTicketSuccessInfoVBox = Bootstrap.alertInfo(new VBox(dayTicketTeachingAndAudioScheduledItemInfoLabel));
        dayTicketSuccessInfoVBox.setAlignment(Pos.CENTER);
        dayTicketSuccessInfoVBox.setMaxWidth(800);
        dayTicketSuccessInfoVBox.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty);
        dayTicketSuccessInfoVBox.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty);

        HBox dayTicketScheduledItemInfoHBox = new HBox(generatePreliminaryBookableScheduledItemVBox, dayTicketSuccessInfoVBox);
        dayTicketScheduledItemInfoHBox.setAlignment(Pos.CENTER);
        dayTicketScheduledItemInfoHBox.setPadding(new Insets(60, 0, 30, 0));

        // Building the event state line
        Label eventStateLabel = Bootstrap.strong(new Label());
        eventStateLabel.setWrapText(true);
        eventStateLabel.setMaxWidth(550);

        VBox eventStateBox = new VBox(eventStateLabel);
        eventStateBox.setAlignment(Pos.CENTER);
        eventStateBox.setMaxWidth(600);

        FXProperties.runNowAndOnPropertyChange(programGenerated -> {
            I18nControls.bindI18nProperties(eventStateLabel, programGenerated ? ProgramI18nKeys.ScheduledItemsAlreadyGenerated : ProgramI18nKeys.ScheduledItemsNotYetGenerated);
            // Apply different alert style based on state
            eventStateBox.getStyleClass().clear();
            if (programGenerated) {
                Bootstrap.alertWarning(eventStateBox);
            } else {
                Bootstrap.alertInfo(eventStateBox);
            }
        }, programGeneratedProperty);

        bottomLine.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty);
        bottomLine.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty);

        HBox eventStateLine = new HBox(eventStateBox);
        eventStateLine.setAlignment(Pos.CENTER);
        eventStateLine.setPadding(new Insets(0, 0, 30, 0));

        // Main content container (everything except topLine and recurring warning)
        VBox mainContentBox = new VBox(
            dayTicketConfigLine,
            dayTicketScheduledItemInfoHBox,
            templateDayColumnsPane,
            addTemplateButtonBox,
            eventStateLine,
            bottomLine
        );

        // Control visibility based on recurringItem field
        FXProperties.runNowAndOnPropertyChange(event -> {
            if (event != null && event.getType() != null) {
                Boolean isRecurring = event.getType().isRecurring();
                if (Boolean.TRUE.equals(isRecurring)) {
                    // Show warning, hide main content for recurring item events
                    recurringItemWarningLine.setVisible(true);
                    recurringItemWarningLine.setManaged(true);
                    mainContentBox.setVisible(false);
                    mainContentBox.setManaged(false);
                } else {
                    // Hide warning, show main content for non-recurring events
                    recurringItemWarningLine.setVisible(false);
                    recurringItemWarningLine.setManaged(false);
                    mainContentBox.setVisible(true);
                    mainContentBox.setManaged(true);
                }
            } else {
                // Default: hide warning, show main content
                recurringItemWarningLine.setVisible(false);
                recurringItemWarningLine.setManaged(false);
                mainContentBox.setVisible(true);
                mainContentBox.setManaged(true);
            }
        }, loadedEventProperty);

        return new VBox(
            topLine,
            recurringItemWarningLine,
            mainContentBox
        );
    }
}

