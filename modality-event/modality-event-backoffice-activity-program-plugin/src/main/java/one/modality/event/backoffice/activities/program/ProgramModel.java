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
 *
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
     * Constructs a new ProgramModel for managing event programs.
     * Initializes entity stores for database operations and sets the program item family.
     *
     * @param programItemFamily The item family for program items (typically TEACHING)
     * @param dataSourceModel The data source configuration for database access
     */
    ProgramModel(KnownItemFamily programItemFamily, DataSourceModel dataSourceModel) {
        this.programItemFamily = programItemFamily;
        entityStore = EntityStore.create(dataSourceModel);
        updateStore = UpdateStore.createAbove(entityStore);
    }

    /** Returns the entity store for reading data. */
    EntityStore getEntityStore() {
        return entityStore;
    }

    /** Returns the update store for managing changes. */
    UpdateStore getUpdateStore() {
        return updateStore;
    }

    /** Returns the currently loaded event. */
    Event getLoadedEvent() {
        return loadedEventProperty.get();
    }

    /** Sets the currently loaded event. */
    void setLoadedEvent(Event loadedEvent) {
        loadedEventProperty.set(loadedEvent);
    }

    /** Returns the observable property for the loaded event. */
    ObjectProperty<Event> loadedEventProperty() {
        return loadedEventProperty;
    }

    /** Returns the main program site. */
    Site getProgramSite() {
        return programSite;
    }

    /** Returns the list of available audio language items. */
    List<Item> getLanguageAudioItems() {
        return languageAudioItems;
    }

    /** Returns the pre-created bookable teaching scheduled items for day ticket. */
    List<ScheduledItem> getTeachingsBookableScheduledItems() {
        return teachingsBookableScheduledItems;
    }

    /** Returns the pre-created bookable audio scheduled items for day ticket. */
    List<ScheduledItem> getAudioRecordingsBookableScheduledItems() {
        return audioRecordingsBookableScheduledItems;
    }

    /** Returns the property indicating day ticket preliminary items exist. */
    public BooleanProperty getDayTicketPreliminaryScheduledItemProperty() {
        return dayTicketPreliminaryScheduledItemProperty;
    }

    /** Returns the video recording item. */
    Item getVideoItem() {
        return videoItem;
    }

    /** Returns the property indicating whether the program is generated. */
    BooleanProperty programGeneratedProperty() {
        return programGeneratedProperty;
    }

    /** Returns the working copy of day templates. */
    ObservableList<DayTemplate> getCurrentDayTemplates() {
        return currentDayTemplates;
    }

    /** Returns the observable list of day template models. */
    public ObservableList<DayTemplateModel> getWorkingDayTemplates() {
        return dayTemplateModels;
    }

    /** Returns the validation support for the entire program. */
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
     *
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
                new EntityStoreQuery("select name, event.(livestreamUrl,vodExpirationDate,audioExpirationDate), dates from DayTemplate dt where event=? order by name", selectedEvent),
                // Index 1: program site (singleton list)
                new EntityStoreQuery("select name from Site where event=? and main limit 1", selectedEvent),
                // Index 2: items for this program item family + audio recording + video
                new EntityStoreQuery("select name,family.code, deprecated from Item where organization=? and family.code in (?,?,?)",
                    selectedEvent.getOrganization(), programItemFamily.getCode(), KnownItemFamily.AUDIO_RECORDING.getCode(), KnownItemFamily.VIDEO.getCode()),
                // Index 3: bookableScheduledItem for this event (teachings + optional audio), created during the event setup.
                new EntityStoreQuery("select item, date, timeline, programScheduledItem, bookableScheduledItem from ScheduledItem si where event=? and bookableScheduledItem=si", selectedEvent),
                // Index 4: we load some fields from the Event table that are not yet loaded. We don't need to look for the result, the result will be loaded automatically in `selectedEvent` because it has the same id.
                new EntityStoreQuery("select teachingsDayTicket, audioRecordingsDayTicket, type.recurringItem from Event where id=?", selectedEvent),
                // Index 5: available audio languages
                new EntityStoreQuery("select distinct name, code from item  where family.code = ? and organization = ? and not deprecated order by name",
                    KnownItemFamily.AUDIO_RECORDING.getCode(), FXEvent.getEvent().getOrganization()),
                // Index 6: Check if program scheduled items exist (NOT bookable items themselves)
                new EntityStoreQuery("select id from ScheduledItem si where event=? and item.family.code=? and bookableScheduledItem!=si limit 1",
                    selectedEvent, KnownItemFamily.TEACHING.getCode()))
            .onFailure(Console::log)
            .inUiThread()
            .onSuccess(entityLists -> {
                // Extract query results
                EntityList<DayTemplate> dayTemplates = entityLists[0];
                EntityList<Site> sites = entityLists[1];
                EntityList<Item> items = entityLists[2];
                EntityList<ScheduledItem> bookableScheduledItems = entityLists[3];
                EntityList<ScheduledItem> programScheduledItems = entityLists[6];

                // Update model state from query results
                programSite = Collections.first(sites);
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
     *
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
        currentDayTemplates.setAll(initialWorkingDayTemplates);
        programGeneratedProperty.setValue(isProgramGenerated);
        dayTemplateModels.forEach(DayTemplateModel::resetModelAndUiToInitial);
    }

    /**
     * Resets all models and UI to initial state without changing program generated flag.
     * Used when canceling changes where program state hasn't changed.
     */
    private void resetModelAndUiToInitial() {
        validationSupport.clear();
        updateStore.cancelChanges();
        currentDayTemplates.setAll(initialWorkingDayTemplates);
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
     *
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
     *
     * Uses a local UpdateStore to batch all changes before submitting to database.
     * After generation, the program is "locked" (templates cannot be edited).
     *
     * @return Future that completes when program is generated and saved to database
     * @see DayTemplateModel#generateProgram
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
     *
     * If validation fails, the save operation is aborted and validation errors are displayed.
     *
     * @param saveButton The save button (spinner displayed here during operation)
     * @param cancelButton The cancel button (disabled during operation)
     */
    void saveChanges(Button saveButton, Button cancelButton) {
        if (validateForm()) {
            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                submitUpdateStoreChangesAndReload(updateStore),
                saveButton, cancelButton);
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
     *
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
            "select id, item.family.code from ScheduledItem si where event=? order by name", getLoadedEvent()
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
     *
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
                Console.log(x);
            })
            .onSuccess(x -> reloadProgramFromSelectedEvent(FXEvent.getEvent()));
            // Note: reloadProgramFromSelectedEvent() already calls resetModelAndUiToInitial() in its success handler
    }

    /**
     * Submits changes from the main update store and reloads program data.
     * This method is used for user-initiated save operations (like {@link #saveChanges(Button, Button)})
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
                Console.log(x);
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
     *
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
     *
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
     *
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
}
