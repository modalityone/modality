package one.modality.event.backoffice.activities.program;


import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
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
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.client.util.masterslave.ModalitySlaveEditor;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Item;
import one.modality.event.client.event.fx.FXEvent;

/**
 * Main UI view for the Program module in the back-office application.
 * This view provides a comprehensive interface for event program management:
 * <ol>
 *   <li><b>Preliminary Setup:</b> Generate bookable scheduled items for day ticket pricing</li>
 *   <li><b>Template Management:</b> Create, edit, and delete day templates with timelines</li>
 *   <li><b>Program Generation:</b> Generate the complete program from templates</li>
 *   <li><b>Program Locking:</b> Lock/unlock the program to control when templates can be edited</li>
 * </ol>
 *
 * <p><b>UI Layout Structure:</b>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │ [Program & Timeline] (Title)                                │
 * ├─────────────────────────────────────────────────────────────┤
 * │ [Event Name and Date Range]                                 │
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
    private final ButtonSelector<Item> itemSelector;

    /**
     * Step 1 view component (Preliminary Bookable Items Configuration).
     */
    private final ProgramStep1View step1View;

    /**
     * Step 2 view component (Day Template Management).
     */
    private final ProgramStep2View step2View;

    /**
     * Step 3 view component (Program Validation & Finalization).
     */
    private final ProgramStep3View step3View;

    /**
     * The root container for the entire UI.
     * Built once in the constructor and returned by {@link #getView()}.
     */
    private final VBox mainVBox;


    /**
     * Constructs the Program view with the given model.
     * Initialization steps:
     * <ol>
     *   <li>Store the program model reference</li>
     *   <li>Create step view components</li>
     *   <li>Build the complete UI structure</li>
     *   <li>Set maximum width constraint</li>
     *   <li>Bind day template views to model for automatic synchronization</li>
     *   <li>Bind item selector from step 1 view</li>
     * </ol>
     *
     * The binding ensures that when day templates are added/removed from the model,
     * the corresponding views are automatically created/removed from the UI.
     *
     * @param programModel The business logic model for program management
     */
    ProgramView(ProgramModel programModel) {
        this.programModel = programModel;
        // Automatic conversion: DayTemplateModel → DayTemplateView
        ObservableLists.bindConvertedOptimized(workingDayTemplateViews, programModel.getWorkingDayTemplates(), DayTemplateView::new);
        // Create step view components
        step1View = new ProgramStep1View(programModel, this);
        step2View = new ProgramStep2View(programModel, workingDayTemplateViews);
        step3View = new ProgramStep3View(programModel);
        // Build UI
        mainVBox = buildUi();
        mainVBox.setMaxWidth(MAX_WIDTH);
        // Bind item selector from step 1 view for syncItemModelFromUi
        itemSelector = step1View.getItemSelector();
        FXProperties.runOnPropertyChange(this::syncItemModelFromUi, itemSelector.selectedItemProperty());
    }

    /**
     * Returns the root UI node for this view.
     * Called by the activity to display this view in the main application area.
     *
     * @return The main VBox container with all UI components
     */
    public Node getView() {
        return mainVBox;
    }

    /**
     * Starts the reactive logic for this view.
     * Establishes bidirectional binding between:
     * <ul>
     *   <li><b>Master:</b> Global event selector (FXEvent.eventProperty)</li>
     *   <li><b>Slave:</b> This view's masterSlaveEventLinker</li>
     * </ul>
     *
     * When the user selects a different event, this view automatically reloads
     * all program data for the new event via {@link #setSlave(Event)}.
     * Called once by {@link ProgramActivity} after view construction.
     */
    void startLogic() {
        masterSlaveEventLinker.masterProperty().bindBidirectional(FXEvent.eventProperty());
    }

    /**
     * Callback invoked when a new event is selected (master-slave pattern).
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
        step3View.setEvent(approvedEntity);
    }

    /**
     * Synchronizes the selected teaching item from UI to the model.
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

    /**
     * Builds the complete UI structure for the Program view.
     *
     * <p><b>Main Components:</b>
     * <ol>
     *   <li><b>Header:</b> Program title ("Program & Timeline")</li>
     *   <li><b>Event Info:</b> Event name and date range</li>
     *   <li><b>Recurring Warning:</b> Alert for unsupported recurring events</li>
     *   <li><b>Step 1 View:</b> Preliminary bookable items configuration (from ProgramStep1View)</li>
     *   <li><b>Step 2 View:</b> Day template management (from ProgramStep2View)</li>
     *   <li><b>Step 3 View:</b> Program validation and finalization (from ProgramStep3View)</li>
     *   <li><b>Bottom Buttons:</b> Save, Generate/Delete Program actions</li>
     * </ol>
     *
     * <p><b>Bottom Action Buttons:</b>
     * <ul>
     *   <li><b>Save:</b> Validates and saves all changes to database</li>
     *   <li><b>Generate Program:</b> Transforms templates into scheduled items (shows when not generated)</li>
     *   <li><b>Delete Program:</b> Unlocks templates by deleting scheduled items (shows when generated)</li>
     *   <li>Save disabled when no changes exist</li>
     *   <li>Generate Program disabled when there are unsaved changes (forces save first)</li>
     *   <li>Entire bottom section hidden until preliminary items are generated</li>
     * </ul>
     *
     * @return The root VBox containing all UI components
     */
    private VBox buildUi() {
        // ========== 1. Header ==========
        Label title = Bootstrap.h2(Bootstrap.textPrimary(I18nControls.newLabel(ProgramI18nKeys.ProgramTitle)));
        //title.setContentDisplay(ContentDisplay.TOP);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);

        VBox headerBox = new VBox(title);
        headerBox.setAlignment(Pos.CENTER);

        // ========== 2. Event Info Line ==========
        ObjectProperty<Event> loadedEventProperty = programModel.loadedEventProperty();
        Label subtitle = Bootstrap.strong(Bootstrap.h4(I18nEntities.newExpressionLabel(loadedEventProperty,
            "'[" + ProgramI18nKeys.Programme + "] - ' + name + ' (' + dateIntervalFormat(startDate, endDate) +')'")));
        subtitle.setWrapText(true);

        HBox topLine = new HBox(subtitle);
        topLine.setAlignment(Pos.CENTER);
        topLine.setPadding(new Insets(0, 0, 20, 0));

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

        // ========== Preliminary Bookable Items Section - Property Declarations ==========
        BooleanProperty dayTicketPreliminaryScheduledItemProperty = programModel.getDayTicketPreliminaryScheduledItemProperty();
        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();

        // ========== 8. Bottom Action Buttons ==========
        Button saveButton = Bootstrap.largeSuccessButton(I18nControls.newButton(ProgramI18nKeys.SaveDraft));
        saveButton.setOnAction(e -> programModel.saveChanges(saveButton));

        // Disable Save when no changes exist OR when there are no day templates
        UpdateStore updateStore = programModel.getUpdateStore();
        BooleanExpression hasChangesProperty = EntityBindings.hasChangesProperty(updateStore);
        BooleanBinding hasNoChangesProperty = hasChangesProperty.not();
        BooleanBinding noTemplatesBinding = javafx.beans.binding.Bindings.isEmpty(workingDayTemplateViews);
        BooleanBinding shouldDisableSave = hasNoChangesProperty.or(noTemplatesBinding);
        saveButton.disableProperty().bind(shouldDisableSave);

        // Generate Program / Delete Program buttons (mutually exclusive visibility)
        Button generateProgramButton = Bootstrap.largePrimaryButton(I18nControls.newButton(ProgramI18nKeys.ValidateProgram));
        generateProgramButton.setOnAction(e -> ModalityDialog.showConfirmationDialog(ProgramI18nKeys.ProgramGenerationConfirmation, programModel::generateProgram));

        Button deleteProgramButton = Bootstrap.largePrimaryButton(I18nControls.newButton(ProgramI18nKeys.DeleteProgram));
        deleteProgramButton.setOnAction(e -> ModalityDialog.showConfirmationDialog(ProgramI18nKeys.DeleteProgramConfirmation, programModel::deleteProgram));

        // Toggle visibility: Generate shows when not generated, Delete shows when generated
        generateProgramButton.visibleProperty().bind(programGeneratedProperty.not());
        deleteProgramButton.visibleProperty().bind(programGeneratedProperty);
        Layouts.bindAllManagedToVisibleProperty(generateProgramButton, deleteProgramButton);

        // Disable Generate Program button when there are unsaved changes (force save first)
        generateProgramButton.disableProperty().bind(hasChangesProperty);

        Label saveHintLabel = Bootstrap.small(I18nControls.newLabel(ProgramI18nKeys.SaveBeforeValidatingHint));
        saveHintLabel.getStyleClass().add("text-muted");
        saveHintLabel.setMaxWidth(400);
        saveHintLabel.setWrapText(true);
        saveHintLabel.setAlignment(Pos.CENTER);
        saveHintLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        saveHintLabel.visibleProperty().bind(hasChangesProperty);
        saveHintLabel.managedProperty().bind(hasChangesProperty);

        HBox bottomLine = new HBox(saveButton, generateProgramButton, deleteProgramButton);
        bottomLine.setAlignment(Pos.BASELINE_CENTER);
        bottomLine.setSpacing(100);

        VBox bottomContainer = new VBox(8, saveHintLabel, bottomLine);
        bottomContainer.setAlignment(Pos.CENTER);

        // Hide bottom buttons until preliminary items are generated
        bottomContainer.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty);
        bottomContainer.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty);

        // ========== Step Views ==========
        // Step 1 - Preliminary Bookable Items Configuration
        VBox mainContentBox = getVBox(bottomContainer);

        // Reactive visibility: Control visibility based on recurringItem field
        FXProperties.runNowAndOnPropertyChange(event ->
            updateVisibilityBasedOnRecurringItem(event, recurringItemWarningLine, mainContentBox),
            loadedEventProperty);

        // Final layout: Header + event info line + warning + main content
        return new VBox(
            headerBox,
            topLine,
            recurringItemWarningLine,
            mainContentBox
        );
    }

    private VBox getVBox(VBox bottomContainer) {
        Node step1Node = step1View.getView();

        // Step 2 - Day Template Management (complete)
        Node step2Node = step2View.getView();

        // Step 3 - Program Validation & Finalization
        Node step3Node = step3View.getView();

        // ========== Main Layout Assembly ==========
        // Main content container (everything except topLine and recurring warning)
        VBox mainContentBox = new VBox(
            step1Node,
            step2Node,
            step3Node,
                bottomContainer
        );
        mainContentBox.setAlignment(Pos.TOP_CENTER);
        mainContentBox.setFillWidth(true); // Ensure children get full width
        return mainContentBox;
    }
}
