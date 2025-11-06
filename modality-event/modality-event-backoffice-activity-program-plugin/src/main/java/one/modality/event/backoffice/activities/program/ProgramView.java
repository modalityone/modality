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
 * Main UI view for the Program module in the back-office application.
 *
 * This view provides a comprehensive interface for event program management:
 * <ol>
 *   <li><b>Day Ticket Configuration:</b> Shows current event configuration for teaching/audio day tickets</li>
 *   <li><b>Preliminary Setup:</b> Generate bookable scheduled items for day ticket pricing</li>
 *   <li><b>Template Management:</b> Create, edit, and delete day templates with timelines</li>
 *   <li><b>Program Generation:</b> Generate the complete program from templates</li>
 *   <li><b>Program Locking:</b> Lock/unlock the program to control when templates can be edited</li>
 * </ol>
 *
 * <p><b>UI Layout Structure:</b>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │ [Event Title and Date Range]                                │
 * ├─────────────────────────────────────────────────────────────┤
 * │ [Day Ticket Configuration Status] (teaching/audio enabled)  │
 * ├─────────────────────────────────────────────────────────────┤
 * │ [Preliminary Bookable Items Setup] (if not yet generated)   │
 * │   OR                                                         │
 * │ [Preliminary Bookable Items Info] (if already generated)    │
 * ├─────────────────────────────────────────────────────────────┤
 * │ [Day Template 1]  [Day Template 2]  [Day Template 3]...     │
 * │ (Multiple columns, each showing timelines and dates)        │
 * ├─────────────────────────────────────────────────────────────┤
 * │ [+ Add Day Template]                                         │
 * ├─────────────────────────────────────────────────────────────┤
 * │ [Program State] (generated or not yet generated)            │
 * ├─────────────────────────────────────────────────────────────┤
 * │ [Cancel] [Save] [Generate Program / Delete Program]         │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><b>Visibility Rules:</b>
 * <ul>
 *   <li>Recurring item events show a warning and hide all content (not supported)</li>
 *   <li>Generate Program button shows when program is not generated</li>
 *   <li>Delete Program button shows when program is generated (to unlock templates)</li>
 *   <li>Preliminary setup shows only if bookable items haven't been created yet</li>
 *   <li>Save/Cancel buttons are disabled when there are no unsaved changes</li>
 * </ul>
 *
 * <p><b>Master-Slave Pattern:</b>
 * This view acts as a "slave" to the event selector (master). When a different event
 * is selected, {@link #setSlave(Event)} is called to reload all program data for the new event.
 *
 * @author David Hello
 * @author Bruno Salmon
 *
 * @see ProgramModel
 * @see DayTemplateView
 * @see ProgramActivity
 */
final class ProgramView extends ModalitySlaveEditor<Event> implements ButtonFactoryMixin  {

    /**
     * Maximum width for the main UI container.
     * Prevents the UI from becoming too wide on large screens.
     */
    private static final double MAX_WIDTH = 1600;

    /**
     * The business logic model managing program data and operations.
     * All UI actions delegate to this model for data manipulation.
     */
    private final ProgramModel programModel;

    /**
     * Manages the master-slave relationship between event selector and this view.
     * When the selected event changes (master), this view updates accordingly (slave).
     */
    private final MasterSlaveLinker<Event> masterSlaveEventLinker = new MasterSlaveLinker<>(this);

    /**
     * Observable list of day template views for reactive UI updates.
     * Automatically synchronized with programModel.getWorkingDayTemplates().
     * Each view represents one day template with its timelines and assigned dates.
     */
    final ObservableList<DayTemplateView> workingDayTemplateViews = FXCollections.observableArrayList();

    /**
     * Item selector for choosing the teaching item for preliminary bookable scheduled items.
     * Allows user to select which teaching item (e.g., "STTP") to use for day ticket pricing.
     */
    private ButtonSelector<Item> itemSelector;

    /**
     * The root container for the entire UI.
     * Built once in the constructor and returned by {@link #getView()}.
     */
    private final VBox mainVBox;


    /**
     * Constructs the Program view with the given model.
     *
     * Initialization steps:
     * <ol>
     *   <li>Store the program model reference</li>
     *   <li>Build the complete UI structure</li>
     *   <li>Set maximum width constraint</li>
     *   <li>Bind day template views to model for automatic synchronization</li>
     * </ol>
     *
     * The binding ensures that when day templates are added/removed from the model,
     * the corresponding views are automatically created/removed from the UI.
     *
     * @param programModel The business logic model for program management
     */
    ProgramView(ProgramModel programModel) {
        this.programModel = programModel;
        mainVBox = buildUi();
        mainVBox.setMaxWidth(MAX_WIDTH);
        // Automatic conversion: DayTemplateModel → DayTemplateView
        ObservableLists.bindConvertedOptimized(workingDayTemplateViews, programModel.getWorkingDayTemplates(), DayTemplateView::new);
    }

    /**
     * Returns the root UI node for this view.
     *
     * Called by the activity to display this view in the main application area.
     *
     * @return The main VBox container with all UI components
     */
    public Node getView() {
        return mainVBox;
    }

    /**
     * Starts the reactive logic for this view.
     *
     * Establishes bidirectional binding between:
     * <ul>
     *   <li><b>Master:</b> Global event selector (FXEvent.eventProperty)</li>
     *   <li><b>Slave:</b> This view's masterSlaveEventLinker</li>
     * </ul>
     *
     * When the user selects a different event, this view automatically reloads
     * all program data for the new event via {@link #setSlave(Event)}.
     *
     * Called once by {@link ProgramActivity} after view construction.
     */
    void startLogic() {
        masterSlaveEventLinker.masterProperty().bindBidirectional(FXEvent.eventProperty());
    }

    /**
     * Callback invoked when a new event is selected (master-slave pattern).
     *
     * This method is called automatically by the master-slave linker when:
     * <ul>
     *   <li>User selects a different event in the event selector</li>
     *   <li>Application navigates to this view with a new event</li>
     * </ul>
     *
     * It delegates to the model to reload all program data (templates, timelines,
     * scheduled items) for the newly selected event.
     *
     * @param approvedEntity The newly selected event
     */
    @Override
    public void setSlave(Event approvedEntity) {
        super.setSlave(approvedEntity);
        programModel.reloadProgramFromSelectedEvent(approvedEntity);
    }

    /**
     * Synchronizes the selected teaching item from UI to the model.
     *
     * Called whenever the item selector's selection changes.
     * Updates the model with the user's item choice for preliminary bookable scheduled items.
     */
    private void syncItemModelFromUi() {
        programModel.setBookableTeachingItem(itemSelector.getSelectedItem());
    }

    /**
     * Updates visibility of UI components based on whether the event is a recurring item event.
     *
     * <p><b>Recurring Item Events:</b> These are not yet supported by the Program module.
     * When detected:
     * <ul>
     *   <li>Show warning message explaining that recurring events are not supported</li>
     *   <li>Hide all main content (templates, buttons, etc.)</li>
     * </ul>
     *
     * <p><b>Regular Events:</b> Normal event program workflow is supported.
     * When detected:
     * <ul>
     *   <li>Hide the warning message</li>
     *   <li>Show all main content for normal program management</li>
     * </ul>
     *
     * This prevents users from attempting to use unsupported features and potentially
     * corrupting data.
     *
     * @param event The event to check
     * @param warningLine The warning UI component to show/hide
     * @param contentBox The main content UI component to show/hide
     */
    private void updateVisibilityBasedOnRecurringItem(Event event, HBox warningLine, VBox contentBox) {
        boolean isRecurringEvent = event != null && event.getType() != null && Boolean.TRUE.equals(event.getType().isRecurring());

        // Show warning for recurring events, hide main content
        warningLine.setVisible(isRecurringEvent);
        warningLine.setManaged(isRecurringEvent);

        // Show main content for non-recurring events, hide warning
        contentBox.setVisible(!isRecurringEvent);
        contentBox.setManaged(!isRecurringEvent);
    }

    /**
     * Checks if there are unsaved changes in the program.
     *
     * Used by the framework to:
     * <ul>
     *   <li>Enable/disable Save and Cancel buttons</li>
     *   <li>Warn users when navigating away with unsaved changes</li>
     *   <li>Prevent data loss from accidental navigation</li>
     * </ul>
     *
     * @return true if there are unsaved changes, false otherwise
     */
    @Override
    public boolean hasChanges() {
        return programModel.getUpdateStore().hasChanges();
    }

    // Private implementation

    /**
     * Builds the complete UI structure for the Program view.
     *
     * This method creates a complex, reactive UI with multiple sections:
     *
     * <p><b>1. Top Line (Event Header):</b>
     * <ul>
     *   <li>Event name and date range</li>
     *   <li>Centered, wrapped text for long event names</li>
     *   <li>Reactive binding to loadedEventProperty</li>
     * </ul>
     *
     * <p><b>2. Day Ticket Configuration Status:</b>
     * <ul>
     *   <li>Shows teaching day ticket status (enabled/disabled)</li>
     *   <li>Shows audio recording day ticket status (enabled/disabled)</li>
     *   <li>Green alert if both enabled, red alert if either disabled</li>
     *   <li>Updates reactively when event changes</li>
     * </ul>
     *
     * <p><b>3. Recurring Item Warning:</b>
     * <ul>
     *   <li>Red danger alert for recurring item events</li>
     *   <li>Explains that recurring events are not yet supported</li>
     *   <li>Visibility controlled by updateVisibilityBasedOnRecurringItem()</li>
     * </ul>
     *
     * <p><b>4. Preliminary Bookable Items Section:</b>
     * <ul>
     *   <li>Visible only if preliminary items haven't been generated yet</li>
     *   <li>Item selector for choosing teaching item (e.g., "STTP")</li>
     *   <li>Button to generate preliminary bookable scheduled items</li>
     *   <li>After generation, shows info about created items</li>
     * </ul>
     *
     * <p><b>5. Day Template Columns:</b>
     * <ul>
     *   <li>ColumnsPane with automatic layout (min 500px per column)</li>
     *   <li>Each column shows one day template with timelines and dates</li>
     *   <li>Reactive binding to workingDayTemplateViews</li>
     *   <li>Responsive gap sizing based on container width</li>
     * </ul>
     *
     * <p><b>6. Add Template Button:</b>
     * <ul>
     *   <li>Centered button to create new day templates</li>
     *   <li>Calls programModel.addNewDayTemplate()</li>
     * </ul>
     *
     * <p><b>7. Program State Alert:</b>
     * <ul>
     *   <li>Yellow warning if program already generated (templates locked)</li>
     *   <li>Blue info if program not yet generated (templates editable)</li>
     *   <li>Updates reactively based on programGeneratedProperty</li>
     * </ul>
     *
     * <p><b>8. Bottom Action Buttons:</b>
     * <ul>
     *   <li><b>Cancel:</b> Reverts all unsaved changes</li>
     *   <li><b>Save:</b> Validates and saves all changes to database</li>
     *   <li><b>Generate Program:</b> Transforms templates into scheduled items (shows when not generated)</li>
     *   <li><b>Delete Program:</b> Unlocks templates by deleting scheduled items (shows when generated)</li>
     *   <li>Save/Cancel disabled when no changes exist</li>
     *   <li>Generate Program disabled when there are unsaved changes (forces save first)</li>
     *   <li>Entire bottom section hidden until preliminary items are generated</li>
     * </ul>
     *
     * <p><b>Reactive Bindings:</b>
     * The UI uses extensive property bindings for automatic updates:
     * <ul>
     *   <li>Event changes trigger full UI refresh</li>
     *   <li>Program generated state toggles button visibility</li>
     *   <li>Day ticket configuration updates alert colors</li>
     *   <li>Preliminary items toggle section visibility</li>
     *   <li>Unsaved changes enable/disable Save and Cancel</li>
     * </ul>
     *
     * @return The root VBox containing all UI components
     */
    private VBox buildUi() {
        // ========== 1. Top Line (Event Header) ==========
        ObjectProperty<Event> loadedEventProperty = programModel.loadedEventProperty();
        Label subtitle = Bootstrap.strong(Bootstrap.h4(I18nEntities.newExpressionLabel(loadedEventProperty,
            "'[" + ProgramI18nKeys.Programme + "] - ' + name + ' (' + dateIntervalFormat(startDate, endDate) +')'")));
        subtitle.setWrapText(true);

        HBox topLine = new HBox(subtitle);
        topLine.setAlignment(Pos.CENTER);
        topLine.setPadding(new Insets(20, 0, 20, 0));

        // ========== 2. Day Ticket Configuration Status ==========
        Label dayTicketConfigLabel = Bootstrap.strong(new Label());
        dayTicketConfigLabel.setWrapText(true);
        dayTicketConfigLabel.setMaxWidth(750);

        VBox dayTicketConfigBox = new VBox(dayTicketConfigLabel);
        dayTicketConfigBox.setAlignment(Pos.CENTER);
        dayTicketConfigBox.setMaxWidth(MAX_WIDTH);

        // Reactive update: Change alert color based on day ticket configuration
        FXProperties.runNowAndOnPropertyChange(event -> {
            if (event != null) {
                boolean teachingEnabled = event.isTeachingsDayTicket();
                boolean audioEnabled = event.isAudioRecordingsDayTicket();

                String teachingStatus = teachingEnabled ? "✓ ENABLED" : "✗ DISABLED";
                String audioStatus = audioEnabled ? "✓ ENABLED" : "✗ DISABLED";
                I18nControls.bindI18nProperties(dayTicketConfigLabel, ProgramI18nKeys.DayTicketConfiguration, teachingStatus, audioStatus);

                // Apply danger alert if either is disabled, success alert if both enabled
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

        // ========== 3. Recurring Item Warning ==========
        Label recurringItemWarningLabel = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.RecurringItemEventNotSupported));
        recurringItemWarningLabel.setWrapText(true);
        recurringItemWarningLabel.setMaxWidth(800);

        VBox recurringItemWarningBox = Bootstrap.alertDanger(new VBox(recurringItemWarningLabel));
        recurringItemWarningBox.setAlignment(Pos.CENTER);
        recurringItemWarningBox.setMaxWidth(MAX_WIDTH);

        HBox recurringItemWarningLine = new HBox(recurringItemWarningBox);
        recurringItemWarningLine.setAlignment(Pos.CENTER);
        recurringItemWarningLine.setPadding(new Insets(20, 0, 20, 0));

        // ========== 8. Bottom Action Buttons ==========
        Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.newButton(ProgramI18nKeys.CancelProgram));
        cancelButton.setOnAction(e -> programModel.cancelChanges());

        Button saveButton = Bootstrap.largeSuccessButton(I18nControls.newButton(ProgramI18nKeys.SaveProgram));
        saveButton.setOnAction(e -> programModel.saveChanges(saveButton, cancelButton));

        // Disable Save/Cancel when no changes exist
        UpdateStore updateStore = programModel.getUpdateStore();
        BooleanExpression hasChangesProperty = EntityBindings.hasChangesProperty(updateStore);
        BooleanBinding hasNoChangesProperty = hasChangesProperty.not();
        saveButton.disableProperty().bind(hasNoChangesProperty);
        cancelButton.disableProperty().bind(hasNoChangesProperty);

        // Generate Program / Delete Program buttons (mutually exclusive visibility)
        Button generateProgramButton = Bootstrap.largePrimaryButton(I18nControls.newButton(ProgramI18nKeys.GenerateProgram));
        generateProgramButton.setOnAction(e -> ModalityDialog.showConfirmationDialog(ProgramI18nKeys.ProgramGenerationConfirmation, programModel::generateProgram));

        Button deleteProgramButton = Bootstrap.largePrimaryButton(I18nControls.newButton(ProgramI18nKeys.DeleteProgram));
        deleteProgramButton.setOnAction(e -> ModalityDialog.showConfirmationDialog(ProgramI18nKeys.DeleteProgramConfirmation, programModel::deleteProgram));

        // Toggle visibility: Generate shows when not generated, Delete shows when generated
        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();
        generateProgramButton.visibleProperty().bind(programGeneratedProperty.not());
        deleteProgramButton.visibleProperty().bind(programGeneratedProperty);
        Layouts.bindAllManagedToVisibleProperty(generateProgramButton, deleteProgramButton);

        // Disable Generate Program button when there are unsaved changes (force save first)
        generateProgramButton.disableProperty().bind(hasChangesProperty);

        HBox bottomLine = new HBox(cancelButton, saveButton, generateProgramButton, deleteProgramButton);
        bottomLine.setAlignment(Pos.BASELINE_CENTER);
        bottomLine.setSpacing(100);

        // ========== 5. Day Template Columns ==========
        ColumnsPane templateDayColumnsPane = new ColumnsPane();
        templateDayColumnsPane.setMinColumnWidth(500);
        // Responsive gap sizing: 2% of width, max 50px
        templateDayColumnsPane.hgapProperty().bind(templateDayColumnsPane.widthProperty().map(w -> Math.min(50, 0.02 * w.doubleValue())));
        templateDayColumnsPane.vgapProperty().bind(templateDayColumnsPane.hgapProperty());
        templateDayColumnsPane.setPadding(new Insets(50, 0, 20, 0));
        templateDayColumnsPane.setAlignment(Pos.TOP_CENTER);
        // Reactive binding: DayTemplateView list → UI panels
        ObservableLists.bindConvertedOptimized(templateDayColumnsPane.getChildren(), workingDayTemplateViews, DayTemplateView::getPanel);

        // ========== 6. Add Template Button ==========
        Button addTemplateButton = Bootstrap.primaryButton(I18nControls.newButton(ProgramI18nKeys.AddDayTemplate));
        addTemplateButton.setGraphicTextGap(10);
        addTemplateButton.setOnAction(e -> programModel.addNewDayTemplate());
        HBox addTemplateButtonBox = new HBox(addTemplateButton);
        addTemplateButtonBox.setAlignment(Pos.CENTER);
        addTemplateButtonBox.setPadding(new Insets(0, 0, 30, 0));

        // ========== 4. Preliminary Bookable Items Section ==========
        BooleanProperty dayTicketPreliminaryScheduledItemProperty = programModel.getDayTicketPreliminaryScheduledItemProperty();

        // Section 4a: Generate Preliminary Items (shows when NOT yet generated)
        Label dayTicketTeachingAndAudioScheduledItemGenerationLabel = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.DayTicketTeachingsAndAudioScheduledItemNotGenerated));
        dayTicketTeachingAndAudioScheduledItemGenerationLabel.setWrapText(true);
        dayTicketTeachingAndAudioScheduledItemGenerationLabel.setMaxWidth(750);

        Label chooseAnItemLabel = I18nControls.newLabel(ProgramI18nKeys.ChooseAnItemForTheTeachingBookableScheduledItem);
        chooseAnItemLabel.setWrapText(true);

        // Item selector: Choose teaching item for preliminary scheduled items (e.g., "STTP")
        itemSelector = new EntityButtonSelector<Item>( // language=JSON5
            "{class: 'Item', alias: 's', where: 'family.code=`teach`', orderBy :'name'}",
            this, FXMainFrameDialogArea::getDialogArea, programModel.getEntityStore().getDataSourceModel())
            .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o));
        Button itemSelectorButton = itemSelector.getButton();
        itemSelectorButton.setMinWidth(250);
        // Sync selected item to model whenever it changes
        FXProperties.runOnPropertyChange(this::syncItemModelFromUi, itemSelector.selectedItemProperty());

        Button generatePreliminaryBookableSIButton = Bootstrap.primaryButton(I18nControls.newButton(ProgramI18nKeys.GeneratePreliminaryBookableSI));

        // Generate preliminary items with validation and error handling
        generatePreliminaryBookableSIButton.setOnAction(e -> {
            // Validation: Ensure item is selected before generating
            if(itemSelector.getSelectedItem()==null) {
                DialogContent dialogContent = new DialogContent().setContentText(I18n.getI18nText(ProgramI18nKeys.PleaseSelectAnItem)).setOk();
                DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, FXMainFrameDialogArea.getDialogArea());
                DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
                return;
            }
                // Show spinner during async operation
                AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    programModel.generatePreliminaryBookableSI()
                        .inUiThread()
                        .onFailure(error -> {
                            // Show error dialog and rollback changes
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

        // Show this section only when preliminary items NOT yet generated
        generatePreliminaryBookableScheduledItemVBox.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty.not());
        generatePreliminaryBookableScheduledItemVBox.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty.not());

        // Section 4b: Preliminary Items Info (shows when already generated)
        Label dayTicketTeachingAndAudioScheduledItemInfoLabel = Bootstrap.strong(new Label());
        dayTicketTeachingAndAudioScheduledItemInfoLabel.setWrapText(true);
        dayTicketTeachingAndAudioScheduledItemInfoLabel.setMaxWidth(750);

        // Reactive update: Reload data when preliminary items are generated or event changes
        FXProperties.runNowAndOnPropertiesChange(dayTicketTeachingsAndAudioScheduledItemGenerated -> {
            if(FXEvent.getEvent()!=null) {
                programModel.reloadProgramFromSelectedEvent(FXEvent.getEvent())
                    .inUiThread()
                    .onSuccess(result -> {
                        String teachingItemName = "Not loaded";
                        String languageItemNames = "Not loaded";
                        if (programModel.getTeachingsBookableScheduledItems() != null) {
                            // Extract distinct teaching item names
                            teachingItemName = programModel.getTeachingsBookableScheduledItems().stream()
                                .map(scheduledItem -> scheduledItem.getItem().getName())
                                .distinct()
                                .collect(Collectors.joining(", "));
                            // Extract distinct audio language item names
                            languageItemNames = programModel.getAudioRecordingsBookableScheduledItems().stream()
                                .map(scheduledItem -> scheduledItem.getItem().getName())
                                .distinct()
                                .collect(Collectors.joining(", "));
                        }
                        I18nControls.bindI18nProperties(dayTicketTeachingAndAudioScheduledItemInfoLabel, ProgramI18nKeys.DayTicketTeachingsAndAudioScheduledItemInfos, teachingItemName, languageItemNames);
                    });
            }
           },dayTicketPreliminaryScheduledItemProperty,FXEvent.eventProperty());

        VBox dayTicketSuccessInfoVBox = Bootstrap.alertInfo(new VBox(dayTicketTeachingAndAudioScheduledItemInfoLabel));
        dayTicketSuccessInfoVBox.setAlignment(Pos.CENTER);
        dayTicketSuccessInfoVBox.setMaxWidth(800);
        // Show this section only when preliminary items already generated
        dayTicketSuccessInfoVBox.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty);
        dayTicketSuccessInfoVBox.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty);

        // Container for both preliminary items sections (only one visible at a time)
        HBox dayTicketScheduledItemInfoHBox = new HBox(generatePreliminaryBookableScheduledItemVBox, dayTicketSuccessInfoVBox);
        dayTicketScheduledItemInfoHBox.setAlignment(Pos.CENTER);
        dayTicketScheduledItemInfoHBox.setPadding(new Insets(60, 0, 30, 0));

        // ========== 7. Program State Alert ==========
        Label eventStateLabel = Bootstrap.strong(new Label());
        eventStateLabel.setWrapText(true);
        eventStateLabel.setMaxWidth(550);

        VBox eventStateBox = new VBox(eventStateLabel);
        eventStateBox.setAlignment(Pos.CENTER);
        eventStateBox.setMaxWidth(600);

        // Reactive update: Change message and alert color based on program state
        FXProperties.runNowAndOnPropertyChange(programGenerated -> {
            I18nControls.bindI18nProperties(eventStateLabel, programGenerated ? ProgramI18nKeys.ScheduledItemsAlreadyGenerated : ProgramI18nKeys.ScheduledItemsNotYetGenerated);
            // Apply different alert style based on state
            eventStateBox.getStyleClass().clear();
            if (programGenerated) {
                Bootstrap.alertWarning(eventStateBox); // Yellow warning: Templates locked
            } else {
                Bootstrap.alertInfo(eventStateBox); // Blue info: Templates editable
            }
        }, programGeneratedProperty);

        // Hide bottom buttons until preliminary items are generated
        bottomLine.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty);
        bottomLine.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty);

        HBox eventStateLine = new HBox(eventStateBox);
        eventStateLine.setAlignment(Pos.CENTER);
        eventStateLine.setPadding(new Insets(0, 0, 30, 0));

        // ========== Main Layout Assembly ==========
        // Main content container (everything except topLine and recurring warning)
        VBox mainContentBox = new VBox(
            dayTicketConfigLine,
            dayTicketScheduledItemInfoHBox,
            templateDayColumnsPane,
            addTemplateButtonBox,
            eventStateLine,
            bottomLine
        );

        // Reactive visibility: Control visibility based on recurringItem field
        FXProperties.runNowAndOnPropertyChange(event ->
            updateVisibilityBasedOnRecurringItem(event, recurringItemWarningLine, mainContentBox),
            loadedEventProperty);

        // Final layout: Top line + warning + main content
        return new VBox(
            topLine,
            recurringItemWarningLine,
            mainContentBox
        );
    }
}

