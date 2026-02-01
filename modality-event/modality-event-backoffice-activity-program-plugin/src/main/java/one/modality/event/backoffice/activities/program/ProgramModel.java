package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.util.OptimizedObservableListWrapper;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.knownitems.KnownItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core business logic model for the Program module.
 * This model manages the complete program lifecycle for an event:
 * <ol>
 *   <li><b>Template Design:</b> Create/edit day templates with teaching sessions</li>
 *   <li><b>Date Assignment:</b> Assign templates to specific dates</li>
 *   <li><b>Program Generation:</b> Transform templates into actual scheduled items</li>
 *   <li><b>Program Management:</b> Lock/unlock, save, cancel changes</li>
 * </ol>
 *
 * <p><b>Key Concepts:</b>
 * <ul>
 *   <li><b>Day Templates:</b> Reusable schedule patterns (e.g., "Morning Session")</li>
 *   <li><b>Timelines:</b> Teaching sessions within a template (item, start/end time)</li>
 *   <li><b>Event Timelines:</b> Shared timeline definitions for the entire event</li>
 *   <li><b>Scheduled Items:</b> Actual bookable sessions generated for each date</li>
 *   <li><b>Day Tickets:</b> Optional all-day booking configuration</li>
 * </ul>
 *
 * <p><b>Program Generation Flow:</b>
 * <pre>
 * 1. User creates day templates:
 *    - "Morning Session": Heart Jewel 9:00-12:00
 *    - "Evening Session": Modern Buddhism 19:00-21:00
 *
 * 2. User assigns dates to templates:
 *    - Morning Session → Jan 15, Jan 22, Jan 29
 *    - Evening Session → Jan 15, Jan 22, Jan 29
 *
 * 3. User clicks "Generate Program":
 *    - Creates event timelines (Heart Jewel 9:00-12:00, Modern Buddhism 19:00-21:00)
 *    - Creates 6 teaching scheduled items (3 dates × 2 sessions)
 *    - Creates 18 audio scheduled items (3 dates × 2 sessions × 3 languages)
 *    - Creates 6 video scheduled items (3 dates × 2 sessions)
 *    - Links all items according to day ticket configuration
 *
 * 4. Program is "locked" (cannot edit templates until unlocked)
 * </pre>
 *
 * <p><b>Day Ticket Architecture:</b>
 * The model handles three day ticket configurations:
 * <ul>
 *   <li><b>Teaching Day Ticket (STTP):</b> One bookable item per day covers all teachings + audio/video</li>
 *   <li><b>Audio Day Ticket (Festivals):</b> Separate bookable items per language per day</li>
 *   <li><b>No Day Ticket:</b> Each teaching session is independently bookable</li>
 * </ul>
 *
 * <p><b>State Management:</b>
 * <ul>
 *   <li><b>Initial State:</b> Loaded from database when event is selected</li>
 *   <li><b>Working State:</b> User's editable copy with unsaved changes</li>
 *   <li><b>Generated State:</b> Program locked, templates linked to event timelines</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see ProgramView
 * @see DayTemplateModel
 * @see ProgramActivity
 */
final class ProgramModel {

    /**
     * The item family for program items (typically TEACHING).
     * Used to filter items when querying the database.
     */
    private final KnownItemFamily programItemFamily;

    /**
     * Entity store for reading data from the database.
     * Provides a local cache of entities with lazy loading support.
     */
    private final EntityStore entityStore;

    /**
     * Update store for managing changes before committing to database.
     * Tracks inserted, updated, and deleted entities for batch submission.
     */
    private final UpdateStore updateStore;

    /**
     * Observable property holding the currently loaded event.
     * Updates trigger UI refresh when a different event is selected.
     */
    private final ObjectProperty<Event> loadedEventProperty = new SimpleObjectProperty<>();

    /**
     * The main site where program sessions take place.
     * Assigned to all generated scheduled items.
     */
    private Site programSite;

    /**
     * List of available audio recording language items (e.g., English, French, Spanish).
     * One audio scheduled item is created per language for each teaching session.
     */
    private List<Item> languageAudioItems;

    /**
     * The video recording item.
     * Used to create video scheduled items when video is offered.
     */
    private Item videoItem;

    /**
     * Pre-created bookable teaching scheduled items for day ticket configuration.
     * Created during event setup, one per day for the entire event duration.
     * Program scheduled items link to these when teaching day ticket is enabled.
     */
    private List<ScheduledItem> teachingsBookableScheduledItems;

    /**
     * Pre-created bookable audio recording scheduled items for day ticket configuration.
     * Created during event setup, one per language per day.
     * Program audio items link to these when audio day ticket is enabled.
     */
    private List<ScheduledItem> audioRecordingsBookableScheduledItems;

    /**
     * Flag indicating whether the program has been generated (locked).
     * When true, templates cannot be edited until the program is unlocked.
     */
    private final BooleanProperty programGeneratedProperty = new SimpleBooleanProperty();

    /**
     * Flag indicating whether day ticket preliminary scheduled items exist.
     * True if either teaching or audio recording bookable items have been created.
     * Affects UI visibility of day ticket configuration options.
     */
    private final BooleanProperty dayTicketPreliminaryScheduledItemProperty = new SimpleBooleanProperty();

    /**
     * Snapshot of day templates loaded from database.
     * Used to revert changes when user cancels edits.
     */
    private final List<DayTemplate> initialWorkingDayTemplates = new ArrayList<>();

    /**
     * Working copy of day templates that may have unsaved changes.
     * Modified as user creates/edits/deletes templates.
     */
    private final ObservableList<DayTemplate> currentDayTemplates = new OptimizedObservableListWrapper<>();

    /**
     * Observable list of day template models for reactive UI updates.
     * Automatically synchronized with currentDayTemplates via binding.
     */
    private final ObservableList<DayTemplateModel> dayTemplateModels = FXCollections.observableArrayList();

    {
        // Automatic conversion: DayTemplate entities → DayTemplateModel wrappers
        ObservableLists.bindConvertedOptimized(dayTemplateModels, currentDayTemplates, dayTemplate -> new DayTemplateModel(dayTemplate, this));

        // Listen for changes to templates and their timelines to update canGenerateProgram property
        dayTemplateModels.addListener((javafx.collections.ListChangeListener<DayTemplateModel>) change -> {
            while (change.next()) {
                // Listen to added templates' timeline changes
                if (change.wasAdded()) {
                    for (DayTemplateModel model : change.getAddedSubList()) {
                        model.getWorkingDayTemplateTimelines().addListener(
                            (javafx.collections.ListChangeListener<DayTemplateTimelineModel>) c -> updateCanGenerateProgramProperty());
                    }
                }
            }
            updateCanGenerateProgramProperty();
        });
    }

    /**
     * Property tracking whether the program can be generated.
     * Updated reactively when templates or timelines change.
     */
    private final javafx.beans.property.BooleanProperty canGenerateProgramProperty =
        new javafx.beans.property.SimpleBooleanProperty(false);

    /**
     * Updates the canGenerateProgram property based on current state.
     */
    private void updateCanGenerateProgramProperty() {
        canGenerateProgramProperty.set(canGenerateProgram());
    }

    /**
     * Form validation support for all program fields.
     * Aggregates validation from all day templates and their timelines.
     */
    private final ValidationSupport validationSupport = new ValidationSupport();

    /**
     * The bookable teaching item selected for day ticket preliminary scheduled items.
     * Used when generating teaching day ticket configuration.
     */
    private Item bookableTeachingItem;

    /**
     * The program session item (hardcoded to KnownItem.PROGRAM_SESSION).
     * This item is used for all timeline sessions in the program.
     */
    private Item sessionProgramItem;


    /**
     * Constructs a new ProgramModel for managing event programs.
     * Initializes entity stores for database operations and sets the program item family.
     *
     * @param programItemFamily The item family for program items (typically TEACHING)
     * @param dataSourceModel   The data source configuration for database access
     */
    ProgramModel(KnownItemFamily programItemFamily, DataSourceModel dataSourceModel) {
        this.programItemFamily = programItemFamily;
        entityStore = EntityStore.create(dataSourceModel);
        updateStore = UpdateStore.createAbove(entityStore);
    }

    /**
     * Returns the entity store for reading data.
     */
    EntityStore getEntityStore() {
        return entityStore;
    }

    /**
     * Returns the update store for managing changes.
     */
    UpdateStore getUpdateStore() {
        return updateStore;
    }

    /**
     * Returns the currently loaded event.
     */
    Event getLoadedEvent() {
        return loadedEventProperty.get();
    }

    /**
     * Sets the currently loaded event.
     */
    void setLoadedEvent(Event loadedEvent) {
        loadedEventProperty.set(loadedEvent);
    }

    /**
     * Returns the observable property for the loaded event.
     */
    ObjectProperty<Event> loadedEventProperty() {
        return loadedEventProperty;
    }

    /**
     * Returns the main program site.
     */
    Site getProgramSite() {
        return programSite;
    }

    /**
     * Returns the list of available audio language items.
     */
    List<Item> getLanguageAudioItems() {
        return languageAudioItems;
    }

    /**
     * Returns the pre-created bookable teaching scheduled items for day ticket.
     */
    List<ScheduledItem> getTeachingsBookableScheduledItems() {
        return teachingsBookableScheduledItems;
    }

    /**
     * Returns the pre-created bookable audio scheduled items for day ticket.
     */
    List<ScheduledItem> getAudioRecordingsBookableScheduledItems() {
        return audioRecordingsBookableScheduledItems;
    }

    /**
     * Returns the property indicating day ticket preliminary items exist.
     */
    public BooleanProperty getDayTicketPreliminaryScheduledItemProperty() {
        return dayTicketPreliminaryScheduledItemProperty;
    }

    /**
     * Returns the video recording item.
     */
    Item getVideoItem() {
        return videoItem;
    }

    Item getSessionProgramItem() {
        return sessionProgramItem;
    }

    /**
     * Returns the property indicating whether the program is generated.
     */
    BooleanProperty programGeneratedProperty() {
        return programGeneratedProperty;
    }

    /**
     * Returns the working copy of day templates.
     */
    ObservableList<DayTemplate> getCurrentDayTemplates() {
        return currentDayTemplates;
    }

    /**
     * Returns the observable list of day template models.
     */
    public ObservableList<DayTemplateModel> getWorkingDayTemplates() {
        return dayTemplateModels;
    }

    /**
     * Returns the validation support for the entire program.
     */
    ValidationSupport getValidationSupport() {
        return validationSupport;
    }

    /**
     * Reloads all program data from the database for the selected event.
     * This method executes a batch of 6 database queries to load all necessary data:
     * <ol>
     *   <li>Day templates for the event</li>
     *   <li>Main program site</li>
     *   <li>Teaching, audio, and video items</li>
     *   <li>Bookable scheduled items (for day ticket configuration)</li>
     *   <li>Event day ticket configuration flags</li>
     *   <li>Available audio language items</li>
     * </ol>
     * <p>
     * After loading, the method:
     * <ul>
     *   <li>Updates all model state with loaded data</li>
     *   <li>Reloads timelines for each day template</li>
     *   <li>Resets UI to match database state</li>
     *   <li>Checks if day ticket items exist</li>
     * </ul>
     *
     * @param selectedEvent The event to load program data for
     * @return Future that completes when all data is loaded and UI is updated
     */
    Future<?> reloadProgramFromSelectedEvent(Event selectedEvent) {
        // Execute queries in batch to avoid synchronization issues between threads
        return entityStore.executeQueryBatch(
                        // Index 0: day templates
                        new EntityStoreQuery("select name, event.(livestreamUrl,vodExpirationDate,audioExpirationDate, shortDescription, shortDescriptionLabel.(en,fr,de,es,pt,zhs,zht,el,vi)), dates from DayTemplate dt where event=$1 order by name", selectedEvent),
                        // Index 1: program site (singleton list)
                        new EntityStoreQuery("select name from Site where event=$1 and main limit 1", selectedEvent),
                        // Index 2: items for this program item family + audio recording + video
                        new EntityStoreQuery("select name,family.code, deprecated from Item where organization=$1 and family.code in ($2, $3, $4)",
                                selectedEvent.getOrganization(), programItemFamily.getCode(), KnownItemFamily.AUDIO_RECORDING.getCode(), KnownItemFamily.VIDEO.getCode()),
                        // Index 3: bookableScheduledItem for this event (teachings + optional audio), created during the event setup.
                        new EntityStoreQuery("select item, date, timeline, programScheduledItem, bookableScheduledItem from ScheduledItem si where event=$1 and bookableScheduledItem=si", selectedEvent),
                        // Index 4: we load some fields from the Event table that are not yet loaded. We don't need to look for the result, the result will be loaded automatically in `selectedEvent` because it has the same id.
                        new EntityStoreQuery("select teachingsDayTicket, audioRecordingsDayTicket, type.recurringItem from Event where id=$1", selectedEvent),
                        // Index 5: available audio languages
                        new EntityStoreQuery("select distinct name, code from item  where family.code = $1 and organization = $2 and not deprecated order by name",
                                KnownItemFamily.AUDIO_RECORDING.getCode(), FXEvent.getEvent().getOrganization()),
                        // Index 6: Check if program scheduled items exist (NOT bookable items themselves)
                new EntityStoreQuery("select id from ScheduledItem si where event=$1 and item.family.code=$2 and bookableScheduledItem!=si limit 1",
                    selectedEvent, KnownItemFamily.TEACHING.getCode()),
                        new EntityStoreQuery("select name, code from item  where code = $1",
                                KnownItem.PROGRAM_SESSION.getCode()))
                .onFailure(Console::error)
                .inUiThread()
                .onSuccess(entityLists -> {
                    // Extract query results
                    EntityList<DayTemplate> dayTemplates = entityLists[0];
                    EntityList<Site> sites = entityLists[1];
                    EntityList<Item> items = entityLists[2];
                    EntityList<ScheduledItem> bookableScheduledItems = entityLists[3];
                    EntityList<ScheduledItem> programScheduledItems = entityLists[6];
                    EntityList<Item> sessionProgramItems = entityLists[7];

                    // Update model state from query results
                    programSite = Collections.first(sites);
                    sessionProgramItem = Collections.first(sessionProgramItems);

                    //TODO: for now, we look for all language available. Change this to a list of language that is setup as the event creation.
                    languageAudioItems = Collections.filter(items, item -> KnownItemFamily.AUDIO_RECORDING.getCode().equals(item.getFamily().getCode()) && !item.isDeprecated());
                    videoItem = Collections.findFirst(items, item -> KnownItemFamily.VIDEO.getCode().equals(item.getFamily().getCode()));
                    teachingsBookableScheduledItems = Collections.filter(bookableScheduledItems, scheduledItem -> KnownItemFamily.TEACHING.getCode().equals(scheduledItem.getItem().getFamily().getCode()));
                    audioRecordingsBookableScheduledItems = Collections.filter(bookableScheduledItems, scheduledItem -> KnownItemFamily.AUDIO_RECORDING.getCode().equals(scheduledItem.getItem().getFamily().getCode()));
                    dayTicketPreliminaryScheduledItemProperty.setValue(!teachingsBookableScheduledItems.isEmpty() || !audioRecordingsBookableScheduledItems.isEmpty());
                    Collections.setAll(initialWorkingDayTemplates, dayTemplates.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                    setLoadedEvent(entityStore.copyEntity(selectedEvent));

                    // Reload timelines for each day template
                    for (DayTemplateModel dayTemplateModel : dayTemplateModels) {
                        dayTemplateModel.reloadTimelinesFromDatabase();
                    }
                    resetModelAndUiToInitial(!programScheduledItems.isEmpty());
                });
    }

    /**
     * Resets all models and UI to the initial database state.
     * This method:
     * <ul>
     *   <li>Clears all validation errors</li>
     *   <li>Cancels all unsaved changes in the update store</li>
     *   <li>Reverts day templates to database state</li>
     *   <li>Sets program generated flag based on actual database state</li>
     *   <li>Resets each day template model and its UI</li>
     * </ul>
     * <p>
     * Called when:
     * <ul>
     *   <li>User clicks "Cancel" button</li>
     *   <li>Data is reloaded from database</li>
     *   <li>Event is changed</li>
     * </ul>
     *
     * @param isProgramGenerated true if program scheduled items exist in database, false otherwise
     */
    private void resetModelAndUiToInitial(boolean isProgramGenerated) {
        validationSupport.clear();
        updateStore.cancelChanges();
        // Re-add entities to UpdateStore after cancelChanges to ensure future changes are tracked
        currentDayTemplates.setAll(initialWorkingDayTemplates.stream()
                .map(updateStore::updateEntity)
                .collect(Collectors.toList()));
        programGeneratedProperty.setValue(isProgramGenerated);
        dayTemplateModels.forEach(DayTemplateModel::resetModelAndUiToInitial);
        updateCanGenerateProgramProperty();
    }

    /**
     * Resets all models and UI to initial state without changing program generated flag.
     * Used when canceling changes where program state hasn't changed.
     */
    private void resetModelAndUiToInitial() {
        validationSupport.clear();
        updateStore.cancelChanges();
        // Re-add entities to UpdateStore after cancelChanges to ensure future changes are tracked
        currentDayTemplates.setAll(initialWorkingDayTemplates.stream()
                .map(updateStore::updateEntity)
                .collect(Collectors.toList()));
        // Don't change programGeneratedProperty - keep current state
        dayTemplateModels.forEach(DayTemplateModel::resetModelAndUiToInitial);
    }

    /**
     * Adds a new empty day template to the program.
     * Creates a new DayTemplate entity linked to the current event.
     * The template is initially unnamed and has no timelines or dates.
     */
    void addNewDayTemplate() {
        DayTemplate dayTemplate = updateStore.insertEntity(DayTemplate.class);
        dayTemplate.setEvent(getLoadedEvent());
        currentDayTemplates.add(dayTemplate);
    }

    /**
     * Deletes a day template and all its timeline entries.
     * This operation:
     * <ul>
     *   <li>Removes the template from the working list</li>
     *   <li>Removes all timeline entries linked to the template</li>
     *   <li>Marks the template for deletion in the update store</li>
     * </ul>
     * <p>
     * The actual database deletion occurs when changes are saved.
     *
     * @param dayTemplateModel The model of the template to delete
     */
    void deleteDayTemplate(DayTemplateModel dayTemplateModel) {
        DayTemplate dayTemplate = dayTemplateModel.getDayTemplate();
        currentDayTemplates.remove(dayTemplate);
        dayTemplateModel.removeTemplateTimeLineLinkedToDayTemplate();
        updateStore.deleteEntity(dayTemplate);
    }

    /**
     * Generates the complete program from day templates.
     * This is the main program generation operation that:
     * <ol>
     *   <li>Creates event timelines for unique (item, startTime, endTime) combinations</li>
     *   <li>Links template timelines to event timelines</li>
     *   <li>Creates teaching scheduled items for each date</li>
     *   <li>Creates audio scheduled items (one per language per teaching)</li>
     *   <li>Creates video scheduled items (one per teaching if video offered)</li>
     *   <li>Links all items according to day ticket configuration</li>
     * </ol>
     * <p>
     * Uses a local UpdateStore to batch all changes before submitting to database.
     * After generation, the program is "locked" (templates cannot be edited).
     *
     * @return Future that completes when program is generated and saved to database
     * @see DayTemplateModel#generateProgram
     */
    boolean canGenerateProgram() {
        if (dayTemplateModels.isEmpty()) {
            return false;
        }
        // Check that at least one template has at least one timeline
        return dayTemplateModels.stream()
                .anyMatch(template -> !template.getWorkingDayTemplateTimelines().isEmpty());
    }

    /**
     * Generates the complete program from day templates.
     * Should only be called after {@link #canGenerateProgram()} returns true.
     */
    Future<?> generateProgram() {
        UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);
        List<Timeline> newlyCreatedEventTimelines = new ArrayList<>();
        // Take all template timelines and create event timelines for unique (item, startTime, endTime)
        dayTemplateModels.forEach(dayTemplateView ->
                dayTemplateView.generateProgram(newlyCreatedEventTimelines, localUpdateStore));
        return submitUpdateStoreChanges(localUpdateStore);
    }

    /**
     * Saves all program changes to the database.
     * This method:
     * <ol>
     *   <li>Validates all form fields across all day templates</li>
     *   <li>Shows a spinner on the save button during save operation</li>
     *   <li>Submits all changes to database</li>
     *   <li>Reloads program data from database on success</li>
     * </ol>
     * <p>
     * If validation fails, the save operation is aborted and validation errors are displayed.
     *
     * @param saveButton The save button (spinner displayed here during operation)
     */
    void saveChanges(Button saveButton) {
        if (validateForm()) {
            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    submitUpdateStoreChangesAndReload(updateStore),
                    saveButton);
        }
    }

    /**
     * Cancels all unsaved changes and reverts to database state.
     * Calls {@link #resetModelAndUiToInitial()} to discard all edits.
     */
    void cancelChanges() {
        resetModelAndUiToInitial();
    }

    /**
     * Deletes the entire generated program (unlocks templates for editing).
     * This "Unlock Program" operation:
     * <ol>
     *   <li>Deletes all audio and video scheduled items</li>
     *   <li>Deletes all teaching scheduled items</li>
     *   <li>Breaks links between template timelines and event timelines</li>
     *   <li>Deletes all event timelines</li>
     * </ol>
     * <p>
     * After deletion, templates can be edited again and the program can be regenerated.
     *
     * <p><b>Important:</b> This only works if there are no bookings yet. Attempting to
     * delete a program with existing bookings will fail with a foreign key constraint error.
     *
     * @return Future that completes when program is deleted and data is reloaded
     */
    Future<?> deleteProgram() {
        UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);

        // Query all scheduled items for this event
        return entityStore.<ScheduledItem>executeQuery(
                "select id, item.family.code from ScheduledItem si where event=$1 order by name", getLoadedEvent()
        ).compose(scheduledItems -> {
            // First delete audio and video items (children of teaching items)
            scheduledItems.forEach(currentScheduledItem -> {
                String scheduledItemFamilyCode = currentScheduledItem.getItem().getFamily().getCode();
                if (scheduledItemFamilyCode.equals(KnownItemFamily.AUDIO_RECORDING.getCode()) || scheduledItemFamilyCode.equals(KnownItemFamily.VIDEO.getCode()))
                    localUpdateStore.deleteEntity(currentScheduledItem);
            });

            // Then delete teaching items
            scheduledItems.forEach(currentScheduledItem -> {
                String code = currentScheduledItem.getItem().getFamily().getCode();
                if (code.equals(KnownItemFamily.TEACHING.getCode()))
                    localUpdateStore.deleteEntity(currentScheduledItem);
            });

            // Finally, remove event timeline links and delete event timelines
            dayTemplateModels.forEach(dayTemplateView -> dayTemplateView.deleteTimelines(localUpdateStore));
            return submitUpdateStoreChanges(localUpdateStore);
        });
    }

    /**
     * Submits changes from a local update store and reloads program data.
     * This method is used for operations that use a local UpdateStore (like {@link #generateProgram()}
     * and {@link #deleteProgram()}) to batch changes before submission.
     *
     * <p>Process flow:
     * <ol>
     *   <li>Submit all changes to database</li>
     *   <li>If successful: Reload program data and reset UI to match database</li>
     *   <li>If failed: Show error dialog with failure message</li>
     * </ol>
     * <p>
     * The reload ensures that all auto-generated IDs and server-side changes are reflected in the UI.
     *
     * @param updateStore The local update store containing batched changes
     * @return Future that completes when changes are submitted and data is reloaded
     */
    Future<?> submitUpdateStoreChanges(UpdateStore updateStore) {
        return updateStore.submitChanges()
                .inUiThread()
                .onFailure(x -> {
                    DialogContent dialog = DialogContent.createConfirmationDialog("Error", "Operation failed", x.getMessage());
                    dialog.setOk();
                    DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
                    dialog.getPrimaryButton().setOnAction(a -> dialog.getDialogCallback().closeDialog());
                    Console.error(x);
                })
                .onSuccess(x -> reloadProgramFromSelectedEvent(FXEvent.getEvent()));
        // Note: reloadProgramFromSelectedEvent() already calls resetModelAndUiToInitial() in its success handler
    }

    /**
     * Submits changes from the main update store and reloads program data.
     * This method is used for user-initiated save operations (like {@link #saveChanges(Button)})
     * that modify the main {@link #updateStore}.
     *
     * <p>Difference from {@link #submitUpdateStoreChanges(UpdateStore)}:
     * <ul>
     *   <li>This method only reloads data, doesn't call resetModelAndUiToInitial()</li>
     *   <li>Used when changes are already in the main update store (not a local one)</li>
     * </ul>
     *
     * <p>Process flow:
     * <ol>
     *   <li>Submit all changes to database</li>
     *   <li>If successful: Reload program data from database</li>
     *   <li>If failed: Show error dialog with failure message</li>
     * </ol>
     *
     * @param updateStore The update store containing changes (typically {@link #updateStore})
     * @return Future that completes when changes are submitted and data is reloaded
     */
    Future<?> submitUpdateStoreChangesAndReload(UpdateStore updateStore) {
        return updateStore.submitChanges()
                .inUiThread()
                .onFailure(x -> {
                    DialogContent dialog = DialogContent.createConfirmationDialog("Error", "Operation failed", x.getMessage());
                    dialog.setOk();
                    DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
                    dialog.getPrimaryButton().setOnAction(a -> dialog.getDialogCallback().closeDialog());
                    Console.error(x);
                })
                .onSuccess(x -> reloadProgramFromSelectedEvent(FXEvent.getEvent()));
    }

    /**
     * Validates all form fields across the entire program.
     * This method checks:
     * <ul>
     *   <li>Day template names (required, unique)</li>
     *   <li>Timeline names (required)</li>
     *   <li>Timeline times (valid LocalTime format, start before end)</li>
     *   <li>Assigned dates (at least one date per template)</li>
     * </ul>
     * <p>
     * If validation hasn't been initialized yet, this method initializes it first.
     *
     * @return true if all validation rules pass, false otherwise
     * @see DayTemplateModel#initFormValidation()
     */
    private boolean validateForm() {
        checkValidationInitialized();
        return validationSupport.isValid();
    }

    /**
     * Ensures validation support is properly initialized.
     * Validation is initialized lazily on first use (not when data is loaded).
     * This prevents showing validation errors before the user starts editing.
     * If validation support is empty, this triggers initialization across all day templates.
     */
    private void checkValidationInitialized() {
        if (validationSupport.isEmpty()) {
            resetValidation();
        }
    }

    /**
     * Resets and reinitializes validation for the entire program.
     * This method:
     * <ol>
     *   <li>Clears all existing validation rules and error states</li>
     *   <li>Delegates to each day template model to re-add its validation rules</li>
     * </ol>
     * <p>
     * Called when:
     * <ul>
     *   <li>Validation needs to be initialized for the first time</li>
     *   <li>Day templates are modified (added/removed)</li>
     *   <li>Form state is reset</li>
     * </ul>
     */
    private void resetValidation() {
        validationSupport.clear();
        dayTemplateModels.forEach(DayTemplateModel::initFormValidation);
    }

    /**
     * Generates preliminary bookable scheduled items for day ticket configuration.
     * This creates the foundation for day ticket pricing by generating:
     * <ol>
     *   <li><b>Teaching day tickets:</b> One bookable scheduled item per day (for all teachings)</li>
     *   <li><b>Audio day tickets:</b> One bookable scheduled item per language per day</li>
     * </ol>
     *
     * <p><b>Day Ticket Concept:</b>
     * Instead of booking individual sessions, customers can book a "day ticket" that covers:
     * <ul>
     *   <li>All teaching sessions on that day (teaching day ticket)</li>
     *   <li>All audio recordings in a specific language on that day (audio day ticket)</li>
     * </ul>
     *
     * <p><b>Example:</b> If an event runs Jan 15-17 with 3 audio languages (EN, FR, ES):
     * <pre>
     * Teaching items created: 3 (one per day)
     *   - Jan 15: Teaching Day Ticket
     *   - Jan 16: Teaching Day Ticket
     *   - Jan 17: Teaching Day Ticket
     *
     * Audio items created: 9 (3 languages × 3 days)
     *   - Jan 15: EN Audio Day Ticket, FR Audio Day Ticket, ES Audio Day Ticket
     *   - Jan 16: EN Audio Day Ticket, FR Audio Day Ticket, ES Audio Day Ticket
     *   - Jan 17: EN Audio Day Ticket, FR Audio Day Ticket, ES Audio Day Ticket
     * </pre>
     *
     * <p><b>Configuration side effects:</b>
     * <ul>
     *   <li>Sets event.kbs3 = true (enables KBS3 features)</li>
     *   <li>Sets event.audioRecordingsDayTicket = true (required for correct program generation)</li>
     * </ul>
     * <p>
     * These preliminary items are later linked to actual program scheduled items during
     * {@link #generateProgram()} based on the day ticket configuration.
     *
     * @return Future that completes when all preliminary items are created and saved
     * @see #getTeachingsBookableScheduledItems()
     * @see #getAudioRecordingsBookableScheduledItems()
     */
    public Future<?> generatePreliminaryBookableSI() {
        // First we add all the teaching scheduledItem: one per day
        Event currentSelectedEvent = updateStore.updateEntity(FXEvent.getEvent());
        currentSelectedEvent.setKbs3(true);
        // Important: audioRecordingsDayTicket must be true for correct program generation
        currentSelectedEvent.setAudioRecordingsDayTicket(true);
        LocalDate startDate = currentSelectedEvent.getStartDate();
        LocalDate endDate = currentSelectedEvent.getEndDate();

        // Create one teaching day ticket per day
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            ScheduledItem teachingScheduledItem = updateStore.insertEntity(ScheduledItem.class);
            teachingScheduledItem.setEvent(currentSelectedEvent);
            teachingScheduledItem.setDate(date);
            teachingScheduledItem.setSite(programSite);
            teachingScheduledItem.setItem(bookableTeachingItem);
        }

        // Then we add the audio recording scheduledItem: one per day and one per language
        for (Item currentItem : languageAudioItems) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                ScheduledItem audioScheduledItem = updateStore.insertEntity(ScheduledItem.class);
                audioScheduledItem.setEvent(currentSelectedEvent);
                audioScheduledItem.setDate(date);
                audioScheduledItem.setSite(programSite);
                audioScheduledItem.setItem(currentItem);
            }
        }
        return updateStore.submitChanges();
    }

    /**
     * Sets the teaching item to use when generating preliminary bookable scheduled items.
     * This item will be assigned to all teaching day ticket scheduled items created by
     * {@link #generatePreliminaryBookableSI()}.
     *
     * <p>Typically called from the UI when the user selects which teaching item should be
     * used for day ticket pricing.
     *
     * @param selectedItem The teaching item to use for day tickets (e.g., "STTP")
     * @see #generatePreliminaryBookableSI()
     */
    public void setBookableTeachingItem(Item selectedItem) {
        bookableTeachingItem = selectedItem;
    }

    /**
     * Helper class to hold counts for a scheduled item.
     */
    static class ScheduledItemCounts {
        final java.util.Map<Object, Integer> attendanceCounts = new java.util.HashMap<>();
        final java.util.Map<Object, Boolean> audioOffered = new java.util.HashMap<>(); // ScheduledItem exists
        final java.util.Map<Object, Integer> audioCounts = new java.util.HashMap<>();   // Media uploaded
        final java.util.Map<Object, Boolean> videoOffered = new java.util.HashMap<>(); // ScheduledItem exists
        final java.util.Map<Object, Integer> videoCounts = new java.util.HashMap<>();   // Media uploaded
    }

    /**
     * OPTIMIZED: Loads attendance and media counts for a list of scheduled items.
     * Uses only 5 queries total (instead of N*3 queries) by consolidating with IN clauses.
     * For all scheduled items, queries:
     * <ul>
     *   <li>All attendance records (from bookableScheduledItems)</li>
     *   <li>Audio ScheduledItems (to check if audio is offered)</li>
     *   <li>Audio Media records (count of actual uploaded recordings)</li>
     *   <li>Video ScheduledItems (to check if video is offered)</li>
     *   <li>Video Media records (count of actual uploaded recordings)</li>
     * </ul>
     * Then groups/counts results in Java code.
     *
     * @param scheduledItems List of scheduled items to load counts for
     * @return Future containing the counts, mapped by scheduled item ID
     */
    Future<ScheduledItemCounts> loadScheduledItemCounts(EntityList<ScheduledItem> scheduledItems) {
        if (scheduledItems.isEmpty()) {
            return Future.succeededFuture(new ScheduledItemCounts());
        }

        // Collect all unique bookable and scheduled item IDs
        java.util.Set<Object> uniqueBookableItemIds = new java.util.LinkedHashSet<>();
        java.util.Map<Object, Object> scheduledItemToBookableId = new java.util.HashMap<>();
        List<Object> scheduledItemIds = new ArrayList<>();

        for (ScheduledItem scheduledItem : scheduledItems) {
            ScheduledItem si = scheduledItem;
            Object scheduledItemId = si.getPrimaryKey();
            scheduledItemIds.add(scheduledItemId);

            ScheduledItem bookableItem = si.getBookableScheduledItem();
            if (bookableItem != null) {
                Object bookableId = bookableItem.getPrimaryKey();
                uniqueBookableItemIds.add(bookableId);
                scheduledItemToBookableId.put(scheduledItemId, bookableId);
            }
        }

        List<EntityStoreQuery> queries = new ArrayList<>();

        // Query 1: Load ALL attendance records for all bookable items at once
        if (!uniqueBookableItemIds.isEmpty()) {
            List<Object> bookableIdList = new ArrayList<>(uniqueBookableItemIds);
            String placeholders = String.join(",", java.util.Collections.nCopies(bookableIdList.size(), "?"));
            queries.add(new EntityStoreQuery(
                "select id, scheduledItem from Attendance where scheduledItem in (" + placeholders + ")",
                bookableIdList.toArray()
            ));
        }

        // Query 2: Load ALL audio ScheduledItems to check if audio is offered
        String scheduledPlaceholders = String.join(",", java.util.Collections.nCopies(scheduledItemIds.size(), "?"));
        Object[] audioSIParams = new Object[scheduledItemIds.size() + 1];
        for (int i = 0; i < scheduledItemIds.size(); i++) {
            audioSIParams[i] = scheduledItemIds.get(i);
        }
        audioSIParams[scheduledItemIds.size()] = KnownItemFamily.AUDIO_RECORDING.getCode();

        queries.add(new EntityStoreQuery(
            "select id, programScheduledItem from ScheduledItem where programScheduledItem in (" + scheduledPlaceholders + ") and item.family.code=?",
            audioSIParams
        ));

        // Query 3: Load ALL audio Media records for upload counts
        // We query Media where scheduledItem.programScheduledItem points to our teaching sessions
        // and scheduledItem is an audio ScheduledItem
        Object[] audioMediaParams = new Object[scheduledItemIds.size() + 1];
        for (int i = 0; i < scheduledItemIds.size(); i++) {
            audioMediaParams[i] = scheduledItemIds.get(i);
        }
        audioMediaParams[scheduledItemIds.size()] = KnownItemFamily.AUDIO_RECORDING.getCode();

        queries.add(new EntityStoreQuery(
            "select id, scheduledItem.programScheduledItem from Media where scheduledItem.programScheduledItem in (" + scheduledPlaceholders + ") and scheduledItem.item.family.code=?",
            audioMediaParams
        ));

        // Query 4: Load ALL video ScheduledItems to check if video is offered
        Object[] videoSIParams = new Object[scheduledItemIds.size() + 1];
        for (int i = 0; i < scheduledItemIds.size(); i++) {
            videoSIParams[i] = scheduledItemIds.get(i);
        }
        videoSIParams[scheduledItemIds.size()] = KnownItemFamily.VIDEO.getCode();

        queries.add(new EntityStoreQuery(
            "select id, programScheduledItem from ScheduledItem where programScheduledItem in (" + scheduledPlaceholders + ") and item.family.code=?",
            videoSIParams
        ));

        // Query 5: Load ALL video Media records for upload counts
        // We query Media where scheduledItem.programScheduledItem points to our teaching sessions
        // and scheduledItem is a video ScheduledItem
        Object[] videoMediaParams = new Object[scheduledItemIds.size() + 1];
        for (int i = 0; i < scheduledItemIds.size(); i++) {
            videoMediaParams[i] = scheduledItemIds.get(i);
        }
        videoMediaParams[scheduledItemIds.size()] = KnownItemFamily.VIDEO.getCode();

        queries.add(new EntityStoreQuery(
            "select id, scheduledItem.programScheduledItem from Media where scheduledItem.programScheduledItem in (" + scheduledPlaceholders + ") and scheduledItem.item.family.code=?",
            videoMediaParams
        ));

        // Execute batch and process results
        return entityStore.executeQueryBatch(queries.toArray(new EntityStoreQuery[0]))
            .map(results -> {
                ScheduledItemCounts counts = new ScheduledItemCounts();
                int resultIndex = 0;

                // Process attendance results - group by scheduledItem and count
                if (!uniqueBookableItemIds.isEmpty()) {
                    EntityList attendanceResults = results[resultIndex++];
                    java.util.Map<Object, Integer> attendanceCountMap = new java.util.HashMap<>();

                    for (Object attendanceResult : attendanceResults) {
                        Attendance attendance = (Attendance) attendanceResult;
                        Object bookableId = attendance.getScheduledItem().getPrimaryKey();
                        attendanceCountMap.put(bookableId, attendanceCountMap.getOrDefault(bookableId, 0) + 1);
                    }
                    counts.attendanceCounts.putAll(attendanceCountMap);
                }

                // Process audio ScheduledItem results - track which sessions have audio offered
                EntityList audioSIResults = results[resultIndex++];
                for (Object audioSIResult : audioSIResults) {
                    ScheduledItem audioItem = (ScheduledItem) audioSIResult;
                    ScheduledItem programItem = audioItem.getProgramScheduledItem();
                    if (programItem != null) {
                        counts.audioOffered.put(programItem.getPrimaryKey(), true);
                    }
                }

                // Process audio Media results - count actual uploaded files
                EntityList<Media> audioMediaResults = results[resultIndex++];
                java.util.Map<Object, Integer> audioCountMap = new java.util.HashMap<>();

                for (Media audioMedia : audioMediaResults) {
                    // Get the teaching scheduledItem ID via: Media -> audio ScheduledItem -> programScheduledItem
                    ScheduledItem audioScheduledItem = audioMedia.getScheduledItem();
                    if (audioScheduledItem != null && audioScheduledItem.getProgramScheduledItem() != null) {
                        Object programItemId = audioScheduledItem.getProgramScheduledItem().getPrimaryKey();
                        audioCountMap.put(programItemId, audioCountMap.getOrDefault(programItemId, 0) + 1);
                    }
                }
                counts.audioCounts.putAll(audioCountMap);

                // Process video ScheduledItem results - track which sessions have video offered
                EntityList<ScheduledItem> videoSIResults = results[resultIndex++];
                for (ScheduledItem videoItem : videoSIResults) {
                    ScheduledItem programItem = videoItem.getProgramScheduledItem();
                    if (programItem != null) {
                        counts.videoOffered.put(programItem.getPrimaryKey(), true);
                    }
                }

                // Process video Media results - count actual uploaded files
                EntityList<Media> videoMediaResults = results[resultIndex];
                java.util.Map<Object, Integer> videoCountMap = new java.util.HashMap<>();

                for (Media videoMedia : videoMediaResults) {
                    // Get the teaching scheduledItem ID via: Media -> video ScheduledItem -> programScheduledItem
                    ScheduledItem videoScheduledItem = videoMedia.getScheduledItem();
                    if (videoScheduledItem != null && videoScheduledItem.getProgramScheduledItem() != null) {
                        Object programItemId = videoScheduledItem.getProgramScheduledItem().getPrimaryKey();
                        videoCountMap.put(programItemId, videoCountMap.getOrDefault(programItemId, 0) + 1);
                    }
                }
                counts.videoCounts.putAll(videoCountMap);

                return counts;
            });
    }

    /**
     * Creates audio and video child scheduledItems for a program scheduled item.
     *
     * @param programScheduledItem The parent program scheduled item
     * @param audioOffered Whether to create audio child scheduledItems
     * @param videoOffered Whether to create video child scheduledItem
     * @param updateStore The update store to use for creation
     */
    void createMediaChildScheduledItems(ScheduledItem programScheduledItem, boolean audioOffered,
                                        boolean videoOffered, UpdateStore updateStore) {
        Event event = programScheduledItem.getEvent();
        Site site = programScheduledItem.getSite();
        LocalDate date = programScheduledItem.getDate();
        ScheduledItem bookableScheduledItem = programScheduledItem.getBookableScheduledItem();

        // Create audio child scheduledItems if audio is offered
        if (audioOffered) {
            languageAudioItems.forEach(languageItem -> {
                ScheduledItem audioScheduledItem = updateStore.insertEntity(ScheduledItem.class);
                audioScheduledItem.setEvent(event);
                audioScheduledItem.setSite(site);
                audioScheduledItem.setDate(date);
                audioScheduledItem.setProgramScheduledItem(programScheduledItem);
                audioScheduledItem.setItem(languageItem);

                // Link to bookable audio scheduled item
                if (event.isAudioRecordingsDayTicket()) {
                    ScheduledItem audioBookableScheduledItem = findBookableScheduledItemByDate(
                        audioRecordingsBookableScheduledItems, date, languageItem);
                    audioScheduledItem.setBookableScheduledItem(audioBookableScheduledItem);
                }
                // TODO: manage when it's not an audio recording day ticket
            });
        }

        // Create video child scheduledItem if video is offered
        if (videoOffered) {
            ScheduledItem videoScheduledItem = updateStore.insertEntity(ScheduledItem.class);
            videoScheduledItem.setEvent(event);
            videoScheduledItem.setSite(site);
            videoScheduledItem.setDate(date);
            videoScheduledItem.setProgramScheduledItem(programScheduledItem);
            videoScheduledItem.setItem(videoItem);

            // Link to bookable teaching scheduled item
            if (event.isTeachingsDayTicket()) {
                videoScheduledItem.setBookableScheduledItem(bookableScheduledItem);
            }
        }
    }

    /**
     * Updates audio and video child scheduledItems based on checkbox states.
     * If audio/video is now enabled but wasn't before, creates child scheduledItems.
     * If audio/video is now disabled but was enabled before, deletes child scheduledItems.
     *
     * @param programScheduledItem The parent program scheduled item
     * @param audioOffered Whether audio should be offered
     * @param videoOffered Whether video should be offered
     * @param updateStore The update store to use
     * @return Future that completes when updates are done
     */
    Future<Void> updateMediaChildScheduledItems(ScheduledItem programScheduledItem, boolean audioOffered,
                                                 boolean videoOffered, UpdateStore updateStore) {
        // Query current state
        return entityStore.executeQueryBatch(
            new EntityStoreQuery(
                "select id from ScheduledItem where programScheduledItem=$1 and item.family.code=$2",
                programScheduledItem, KnownItemFamily.AUDIO_RECORDING.getCode()
            ),
            new EntityStoreQuery(
                "select id from ScheduledItem where programScheduledItem=$1 and item.family.code=$2",
                programScheduledItem, KnownItemFamily.VIDEO.getCode()
            )
        ).map(results -> {
            EntityList<ScheduledItem> audioChildren = results[0];
            EntityList<ScheduledItem> videoChildren = results[1];
            boolean hasAudio = !audioChildren.isEmpty();
            boolean hasVideo = !videoChildren.isEmpty();

            // Handle audio
            if (audioOffered && !hasAudio) {
                // Create audio child scheduledItems
                createAudioChildScheduledItems(programScheduledItem, updateStore);
            } else if (!audioOffered && hasAudio) {
                // Delete audio child scheduledItems
                for (ScheduledItem audioChild : audioChildren) {
                    updateStore.deleteEntity(audioChild);
                }
            }

            // Handle video
            if (videoOffered && !hasVideo) {
                // Create video child scheduledItem
                createVideoChildScheduledItem(programScheduledItem, updateStore);
            } else if (!videoOffered && hasVideo) {
                // Delete video child scheduledItems
                for (ScheduledItem videoChild : videoChildren) {
                    updateStore.deleteEntity(videoChild);
                }
            }

            return null;
        });
    }

    /**
     * Creates only audio child scheduledItems for a program scheduled item.
     */
    private void createAudioChildScheduledItems(ScheduledItem programScheduledItem, UpdateStore updateStore) {
        // Get event from programScheduledItem, or fall back to current event if not loaded
        Event event = programScheduledItem.getEvent();
        if (event == null) {
            event = FXEvent.getEvent(); // Fall back to current event
        }
        // Get site from programScheduledItem, or fall back to program site if not loaded
        Site site = programScheduledItem.getSite();
        if (site == null) {
            site = getProgramSite(); // Fall back to program site
        }
        LocalDate date = programScheduledItem.getDate();

        Event finalEvent = event; // For lambda capture
        Site finalSite = site; // For lambda capture
        languageAudioItems.forEach(languageItem -> {
            ScheduledItem audioScheduledItem = updateStore.insertEntity(ScheduledItem.class);
            audioScheduledItem.setEvent(finalEvent);
            audioScheduledItem.setSite(finalSite);
            audioScheduledItem.setDate(date);
            audioScheduledItem.setProgramScheduledItem(programScheduledItem);
            audioScheduledItem.setItem(languageItem);

            if (finalEvent != null && finalEvent.isAudioRecordingsDayTicket()) {
                ScheduledItem audioBookableScheduledItem = findBookableScheduledItemByDate(
                    audioRecordingsBookableScheduledItems, date, languageItem);
                audioScheduledItem.setBookableScheduledItem(audioBookableScheduledItem);
            }
        });
    }

    /**
     * Creates only a video child scheduledItem for a program scheduled item.
     */
    private void createVideoChildScheduledItem(ScheduledItem programScheduledItem, UpdateStore updateStore) {
        // Get event from programScheduledItem, or fall back to current event if not loaded
        Event event = programScheduledItem.getEvent();
        if (event == null) {
            event = FXEvent.getEvent(); // Fall back to current event
        }
        // Get site from programScheduledItem, or fall back to program site if not loaded
        Site site = programScheduledItem.getSite();
        if (site == null) {
            site = getProgramSite(); // Fall back to program site
        }
        LocalDate date = programScheduledItem.getDate();
        ScheduledItem bookableScheduledItem = programScheduledItem.getBookableScheduledItem();

        ScheduledItem videoScheduledItem = updateStore.insertEntity(ScheduledItem.class);
        videoScheduledItem.setEvent(event);
        videoScheduledItem.setSite(site);
        videoScheduledItem.setDate(date);
        videoScheduledItem.setProgramScheduledItem(programScheduledItem);
        videoScheduledItem.setItem(videoItem);

        if (event != null && event.isTeachingsDayTicket()) {
            videoScheduledItem.setBookableScheduledItem(bookableScheduledItem);
        }
    }

    /**
     * Finds a bookable scheduled item for a specific date and item.
     */
    private static ScheduledItem findBookableScheduledItemByDate(List<ScheduledItem> bookableScheduledItems,
                                                                  LocalDate date, Item item) {
        return bookableScheduledItems.stream()
            .filter(si -> si.getDate().equals(date))
            .filter(si -> item == null || si.getItem().equals(item))
            .findFirst()
            .orElse(null);
    }
}
