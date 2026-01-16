package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.util.OptimizedObservableListWrapper;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model for managing a single day template and its timelines.
 * A day template represents a reusable schedule pattern for teaching sessions during an event.
 * For example, a "Morning Session" template might define teaching sessions from 9:00-12:00,
 * which can then be applied to multiple dates (e.g., all Saturdays during the event).
 *
 * <p><b>Core Concepts:</b>
 * <ul>
 *   <li><b>Day Template:</b> A named schedule pattern with multiple timeline entries</li>
 *   <li><b>Timelines:</b> Individual teaching sessions within the template (item, start/end time, audio/video)</li>
 *   <li><b>Selected Dates:</b> The specific dates this template applies to</li>
 *   <li><b>Scheduled Items:</b> Actual bookable sessions generated from templates and dates</li>
 * </ul>
 *
 * <p><b>Program Generation Process:</b>
 * When the user generates the program, this model creates scheduled items by combining
 * each timeline with each selected date. For example:
 * <pre>
 * Template: "Morning Session" with dates [Jan 15, Jan 22]
 *   Timeline 1: "Heart Jewel" 9:00-12:00, audio=true, video=false
 *   Timeline 2: "Modern Buddhism" 14:00-17:00, audio=true, video=true
 *
 * Generated Scheduled Items:
 *   - Jan 15, 9:00-12:00: Heart Jewel (teaching)
 *   - Jan 15, 9:00-12:00: Heart Jewel Audio - English (audio recording)
 *   - Jan 15, 9:00-12:00: Heart Jewel Audio - French (audio recording)
 *   - Jan 22, 9:00-12:00: Heart Jewel (teaching)
 *   - ... (and so on for all combinations)
 * </pre>
 *
 * <p><b>Day Ticket Support:</b>
 * This model handles special "day ticket" configurations where bookable scheduled items
 * are created for entire days rather than individual sessions. See documentation in
 * {@link #addTeachingsScheduledItemsForDateAndTimeline} for details.
 *
 * <p><b>State Management:</b>
 * <ul>
 *   <li><b>Initial State:</b> Loaded from database when event is selected</li>
 *   <li><b>Current State:</b> User's working copy with unsaved changes</li>
 *   <li><b>Reset:</b> Discards changes and reverts to initial state</li>
 *   <li><b>Save:</b> Commits changes to database via UpdateStore</li>
 * </ul>
 *
 * @author Bruno Salmon
 *
 * @see DayTemplateView
 * @see DayTemplateTimelineModel
 * @see ProgramModel
 */
final class DayTemplateModel {

    /**
     * Reference to the parent ProgramModel managing all day templates.
     * Provides access to shared resources like validation, site, items, and event configuration.
     */
    private final ProgramModel programModel;

    /**
     * The DayTemplate entity being managed by this model.
     * Contains the template name and references to its timeline entries.
     */
    private final DayTemplate dayTemplate;

    /**
     * Observable list of dates selected for this template.
     * When the program is generated, scheduled items are created for each date.
     */
    private ObservableList<LocalDate> selectedDates;

    /**
     * Callback to synchronize the UI when the model state changes.
     * Invoked after loading from database or resetting changes.
     */
    private Runnable syncUiFromModelRunnable;

    /**
     * Callback to initialize form validation rules.
     * Called once during view setup to register validation rules.
     */
    private Runnable initFormValidationRunnable;

    /**
     * Snapshot of initial timeline state loaded from the database.
     * Used to revert changes when the user cancels edits.
     */
    private final List<Timeline> initialTemplateTimelines = new ArrayList<>();

    /**
     * Working copy of timelines that may have unsaved changes.
     * Modified as the user edits teaching sessions in the UI.
     */
    private final ObservableList<Timeline> currentTemplateTimelines = new OptimizedObservableListWrapper<>();

    /**
     * Observable list of timeline models for reactive UI updates.
     * Automatically synchronized with currentTemplateTimelines via binding.
     * Each model wraps one Timeline entity and provides UI interaction.
     */
    private final ObservableList<DayTemplateTimelineModel> dayTemplateTimelineModels = FXCollections.observableArrayList();
    {
        // Automatic conversion: Timeline entities → DayTemplateTimelineModel wrappers
        ObservableLists.bindConvertedOptimized(dayTemplateTimelineModels, currentTemplateTimelines, timeline -> new DayTemplateTimelineModel(timeline, this));
    }

    /**
     * Constructs a new DayTemplateModel for managing a day template.
     * Automatically loads timeline data from the database if the template exists.
     *
     * @param dayTemplate The DayTemplate entity to manage
     * @param programModel The parent ProgramModel containing all templates
     */
    DayTemplateModel(DayTemplate dayTemplate, ProgramModel programModel) {
        this.programModel = programModel;
        this.dayTemplate = dayTemplate;
        reloadTimelinesFromDatabase();
    }

    /**
     * Returns the DayTemplate entity managed by this model.
     *
     * @return The DayTemplate entity
     */
    public DayTemplate getDayTemplate() {
        return dayTemplate;
    }

    /**
     * Sets the observable list of selected dates for this template.
     *
     * @param selectedDates Observable list of dates to apply this template to
     */
    public void setSelectedDates(ObservableList<LocalDate> selectedDates) {
        this.selectedDates = selectedDates;
    }

    /**
     * Sets the callback to synchronize the UI from the model.
     *
     * @param syncUiFromModelRunnable Callback invoked when model state changes
     */
    public void setSyncUiFromModelRunnable(Runnable syncUiFromModelRunnable) {
        this.syncUiFromModelRunnable = syncUiFromModelRunnable;
    }

    /**
     * Sets the callback to initialize form validation.
     *
     * @param initFormValidationRunnable Callback to set up validation rules
     */
    public void setInitFormValidationRunnable(Runnable initFormValidationRunnable) {
        this.initFormValidationRunnable = initFormValidationRunnable;
    }

    /**
     * Returns the observable list of timeline models for UI binding.
     *
     * @return Observable list of DayTemplateTimelineModel instances
     */
    public ObservableList<DayTemplateTimelineModel> getWorkingDayTemplateTimelines() {
        return dayTemplateTimelineModels;
    }

    /**
     * Returns the event associated with this day template.
     *
     * @return The Event entity
     */
    private Event getEvent() {
        return dayTemplate.getEvent();
    }

    /**
     * Returns the validation support instance from the parent model.
     *
     * @return ValidationSupport for form field validation
     */
    ValidationSupport getValidationSupport() {
        return programModel.getValidationSupport();
    }

    /**
     * Returns the main site for this program.
     *
     * @return The Site entity where sessions take place
     */
    private Site getSite() {
        return programModel.getProgramSite();
    }

    /**
     * Returns the video item used for video recordings.
     *
     * @return The Item entity representing video recordings
     */
    private Item getVideoItem() {
        return programModel.getVideoItem();
    }

    /**
     * Returns the UpdateStore for managing entity changes.
     *
     * @return UpdateStore instance for database operations
     */
    private UpdateStore getUpdateStore() {
        return programModel.getUpdateStore();
    }

    /**
     * Reloads timeline data from the database.
     * Queries the database for all timelines associated with this day template,
     * ordered by start time. The loaded timelines become the initial state for
     * change tracking and reset operations.
     * This method is called:
     * <ul>
     *   <li>During model construction</li>
     *   <li>When switching to a different event</li>
     * </ul>
     *
     * Note: Only executes if the template already exists in the database (not new).
     */
    public void reloadTimelinesFromDatabase() {
        // Only query database if template exists (not newly created in UI)
        if (!dayTemplate.getId().isNew()) {
            programModel.getEntityStore().<Timeline>executeQuery(
                    "select item, dayTemplate, startTime, endTime, videoOffered, audioOffered, name, site, eventTimeline from Timeline where dayTemplate=? order by startTime"
                    , dayTemplate
                )
                .onFailure(Console::log)
                .inUiThread()
                .onSuccess(timelines -> {
                    // Convert to UpdateStore entities for change tracking
                    Collections.setAll(initialTemplateTimelines, timelines.stream().map(getUpdateStore()::updateEntity).collect(Collectors.toList()));
                    resetModelAndUiToInitial();
                });
        }
    }

    /**
     * Resets the model and UI to the initial state loaded from the database.
     * This method:
     * <ul>
     *   <li>Reverts all timeline changes to the database state</li>
     *   <li>Updates all timeline model UI components</li>
     *   <li>Syncs the dates UI</li>
     * </ul>
     *
     * Called when:
     * <ul>
     *   <li>User clicks "Cancel" to discard changes</li>
     *   <li>Data is reloaded from database</li>
     *   <li>Event is changed</li>
     * </ul>
     *
     * <p><b>Note:</b> The program generated status is now managed centrally by ProgramModel
     * based on actual database queries for program scheduled items, not by checking eventTimelineId values.
     */
    void resetModelAndUiToInitial() {
        // Revert working copy to initial database state
        // Re-add entities to UpdateStore to ensure future changes are tracked after cancelChanges()
        currentTemplateTimelines.setAll(initialTemplateTimelines.stream()
                .map(getUpdateStore()::updateEntity)
                .collect(Collectors.toList()));
        // Reset each timeline's UI fields
        dayTemplateTimelineModels.forEach(DayTemplateTimelineModel::resetModelAndUiToInitial);

        syncUiFromModel();
    }

    /**
     * Invokes the registered UI sync callback.
     * Updates the dates display to match the model state.
     */
    private void syncUiFromModel() {
        syncUiFromModelRunnable.run();
    }

    /**
     * Initializes form validation rules.
     * Delegates to the registered validation initialization callback.
     */
    void initFormValidation() {
        initFormValidationRunnable.run();
    }

    /**
     * Duplicates this day template with all its timelines.
     * Creates a new day template named "{original name} - copy" with identical
     * timeline entries. The duplicate is added to the program model and becomes
     * immediately editable.
     *
     * <p>Duplicated properties:
     * <ul>
     *   <li>Template name (with " - copy" suffix)</li>
     *   <li>All timeline entries (item, times, audio/video flags)</li>
     *   <li>Event and site associations</li>
     * </ul>
     *
     * Note: Selected dates are NOT copied - the duplicate starts with no dates.
     */
    void duplicate() {
        // Create new day template entity
        DayTemplate duplicateDayTemplate = getUpdateStore().insertEntity(DayTemplate.class);
        duplicateDayTemplate.setName(dayTemplate.getName() + " - copy");
        duplicateDayTemplate.setEvent(dayTemplate.getEvent());
        programModel.getCurrentDayTemplates().add(duplicateDayTemplate);

        // Get the newly created model wrapper
        DayTemplateModel newWTP = Collections.last(programModel.getWorkingDayTemplates());

        // Duplicate all timeline entries
        for (Timeline timelineItem : currentTemplateTimelines) {
            Timeline newTimeline = getUpdateStore().insertEntity(Timeline.class);
            newTimeline.setItem(timelineItem.getItem());
            newTimeline.setStartTime(timelineItem.getStartTime());
            newTimeline.setEndTime(timelineItem.getEndTime());
            newTimeline.setAudioOffered(timelineItem.isAudioOffered());
            newTimeline.setVideoOffered(timelineItem.isVideoOffered());
            newTimeline.setSite(timelineItem.getSite());
            newTimeline.setName(timelineItem.getName());
            newTimeline.setDayTemplate(duplicateDayTemplate);
            assert newWTP != null;
            newWTP.currentTemplateTimelines.add(newTimeline);
        }
    }

    /**
     * Adds a new empty timeline to this day template.
     * Creates a new Timeline entity with default settings:
     * <ul>
     *   <li>Audio offered: false</li>
     *   <li>Video offered: false</li>
     *   <li>Site: Program's main site</li>
     *   <li>Linked to this day template</li>
     * </ul>
     *
     * The user must fill in the item, start time, and end time.
     */
    void addTemplateTimeline() {
        Timeline newTimeLine = getUpdateStore().insertEntity(Timeline.class);
        newTimeLine.setAudioOffered(false);
        newTimeLine.setVideoOffered(false);
        newTimeLine.setDayTemplate(dayTemplate);
        newTimeLine.setSite(getSite());
        currentTemplateTimelines.add(newTimeLine);
    }

    /**
     * Finds a bookable scheduled item for a specific date and optionally item.
     * Helper method used in day ticket configuration to locate pre-created bookable
     * scheduled items that program scheduled items should link to.
     *
     * @param bookableScheduledItems List of bookable scheduled items to search
     * @param date The date to match
     * @param item The item to match (null to match any item for that date)
     * @return The matching bookable scheduled item, or null if not found
     */
    private ScheduledItem findBookableScheduledItemByDate(List<ScheduledItem> bookableScheduledItems, LocalDate date, Item item) {
        return bookableScheduledItems.stream()
            .filter(scheduledItem -> scheduledItem.getDate().equals(date))
            .filter(scheduledItem -> item == null || scheduledItem.getItem().equals(item))
            .findFirst()
            .orElse(null);
    }

    /**
     * Creates a teaching scheduled item for a specific date and timeline.
     * This is a core method in the program generation process. It creates one teaching
     * scheduled item (representing a teaching session) and handles day ticket linking.
     *
     * <p><b>Day Ticket Behavior:</b>
     * Day tickets allow attendees to book an entire day rather than individual sessions.
     * When enabled, this method links the created teaching scheduled item to a pre-existing
     * "bookable" scheduled item that was created during event setup.
     *
     * <p><b>Two Day Ticket Scenarios:</b>
     * <ul>
     *   <li><b>Teachings Day Ticket (STTP):</b> Bookable item covers all teachings for the day.
     *       Audio/video items also link to the same teaching bookable item.</li>
     *   <li><b>No Day Ticket:</b> Each teaching session is independently bookable.</li>
     * </ul>
     *
     * <p>See project documentation in doc/ directory for detailed day ticket architecture.
     *
     * @param date The date for this scheduled item
     * @param name Custom name for this session (optional, from timeline.name)
     * @param timeline The timeline (teaching session definition) to create item from
     * @param currentUpdateStore UpdateStore for creating entities
     * @return The created teaching scheduled item
     */
    ScheduledItem addTeachingsScheduledItemsForDateAndTimeline(LocalDate date, String name, Timeline timeline, UpdateStore currentUpdateStore) {
        // Create the teaching scheduled item
        ScheduledItem teachingScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
        teachingScheduledItem.setEvent(getEvent());
        teachingScheduledItem.setSite(getSite());
        teachingScheduledItem.setDate(date);
        teachingScheduledItem.setName(name);
        teachingScheduledItem.setTimeline(timeline);
        teachingScheduledItem.setItem(timeline.getItem());

        // Link to bookable scheduled item if day ticket is enabled
        if(getEvent().isTeachingsDayTicket()) {
            // Find the pre-created bookable teaching item for this date
            ScheduledItem bookableScheduleItem = findBookableScheduledItemByDate(
                programModel.getTeachingsBookableScheduledItems(), date, null);
            teachingScheduledItem.setBookableScheduledItem(bookableScheduleItem);
        }

        return teachingScheduledItem;
    }

    /**
     * Deletes this day template and all its timeline entries.
     * Delegates to the parent model which handles cascading deletion.
     */
    void deleteDayTemplate() {
        programModel.deleteDayTemplate(this);
    }

    /**
     * Deletes all generated event timelines associated with this template.
     * This is part of the "Unlock Program" operation. It:
     * <ul>
     *   <li>Breaks the link between template timelines and event timelines</li>
     *   <li>Deletes the event timeline entities</li>
     *   <li>Allows the user to edit the template again</li>
     * </ul>
     *
     * Note: This only works if there are no bookings yet. Scheduled items
     * referencing these timelines must be deleted first.
     *
     * @param updateStore UpdateStore to use for the deletion
     */
    void deleteTimelines(UpdateStore updateStore) {
        currentTemplateTimelines.forEach(currentTemplateTimeline -> {
            // Update the template timeline to remove event timeline link
            Timeline templateTimelineToUpdate = updateStore.updateEntity(currentTemplateTimeline);
            templateTimelineToUpdate.setEventTimeline(null);
            // Delete the generated event timeline
            Timeline eventTimeLine = currentTemplateTimeline.getEventTimeline();
            updateStore.deleteEntity(eventTimeLine);
        });
    }

    /**
     * Removes all timelines that belong to this day template.
     * Used during day template deletion to clean up timeline lists.
     */
    void removeTemplateTimeLineLinkedToDayTemplate() {
        currentTemplateTimelines.removeIf(timeline -> timeline.getDayTemplate() != null &&
            timeline.getDayTemplate().getId().equals(dayTemplate.getId()));
    }

    /**
     * Removes a specific timeline from this day template.
     * Marks the timeline for deletion in the UpdateStore and removes it from
     * the current timelines list. The actual database deletion occurs on save.
     *
     * @param timeline The timeline to remove
     */
    void removeTemplateTimeLine(Timeline timeline) {
        getUpdateStore().deleteEntity(timeline);
        currentTemplateTimelines.remove(timeline);
    }

    /**
     * Duplicates a specific timeline entry.
     * Creates a new timeline with the same name, audio/video flags, but leaves
     * start and end times empty for the user to fill in.
     *
     * @param sourceTimeline The timeline to duplicate
     */
    void duplicateTimeline(Timeline sourceTimeline) {
        // Create new timeline entity
        Timeline duplicateTimeline = getUpdateStore().insertEntity(Timeline.class);

        // Copy only name and audio/video settings
        duplicateTimeline.setItem(sourceTimeline.getItem());
        duplicateTimeline.setName(sourceTimeline.getName());
        duplicateTimeline.setAudioOffered(sourceTimeline.isAudioOffered());
        duplicateTimeline.setVideoOffered(sourceTimeline.isVideoOffered());
        duplicateTimeline.setSite(sourceTimeline.getSite());
        duplicateTimeline.setDayTemplate(dayTemplate);
        // Leave start and end times null (user must fill them in)

        // Insert after the source timeline
        int index = currentTemplateTimelines.indexOf(sourceTimeline);
        if (index >= 0) {
            currentTemplateTimelines.add(index + 1, duplicateTimeline);
        } else {
            currentTemplateTimelines.add(duplicateTimeline);
        }
    }

    /**
     * Generates the actual program by creating event timelines and scheduled items.
     * This is the core program generation algorithm. It transforms the template (design-time)
     * into the actual event program (runtime) by creating:
     * <ol>
     *   <li><b>Event Timelines:</b> Shared timeline definitions for the entire event</li>
     *   <li><b>Scheduled Items:</b> Actual bookable sessions for each date</li>
     *   <li><b>Audio/Video Items:</b> Recording options linked to teaching sessions</li>
     * </ol>
     *
     * <p><b>Process for Each Template Timeline:</b>
     * <ol>
     *   <li>Find or create a matching event timeline (reused across dates with same item/times)</li>
     *   <li>Link the template timeline to the event timeline</li>
     *   <li>For each selected date:
     *     <ul>
     *       <li>Create teaching scheduled item</li>
     *       <li>Create audio scheduled items (one per language) if audio offered</li>
     *       <li>Create video scheduled item if video offered</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><b>Example:</b>
     * <pre>
     * Template: "Morning Session" with dates [Jan 15, Jan 22]
     *   Timeline: "Heart Jewel" 9:00-12:00, audio=true (English, French)
     *
     * Generated:
     *   Event Timeline: "Heart Jewel" 9:00-12:00
     *   Scheduled Items:
     *     - Jan 15, 9:00: Heart Jewel (teaching)
     *     - Jan 15, 9:00: Heart Jewel Audio - English
     *     - Jan 15, 9:00: Heart Jewel Audio - French
     *     - Jan 22, 9:00: Heart Jewel (teaching)
     *     - Jan 22, 9:00: Heart Jewel Audio - English
     *     - Jan 22, 9:00: Heart Jewel Audio - French
     * </pre>
     *
     * @param newlyCreatedEventTimelines Shared list to track created event timelines (for reuse)
     * @param updateStore UpdateStore to use for creating entities
     */
    void generateProgram(List<Timeline> newlyCreatedEventTimelines, UpdateStore updateStore) {
        currentTemplateTimelines.forEach(templateTimeline -> {
            LocalTime startTime = templateTimeline.getStartTime();
            LocalTime endTime = templateTimeline.getEndTime();
            Item item = templateTimeline.getItem();

            // Prepare template timeline for update
            Timeline templateTimelineToEdit = updateStore.updateEntity(templateTimeline);

            // Try to find an existing event timeline with same item and times
            // (Event timelines are shared across dates to avoid duplication)
            Timeline eventTimeLine = newlyCreatedEventTimelines.stream()
                .filter(timeline ->
                    timeline.getStartTime().equals(startTime) &&
                    timeline.getEndTime().equals(endTime) &&
                    timeline.getItem().equals(item)
                )
                .findFirst()
                .orElse(null);

            if (eventTimeLine == null) {
                // Create new event timeline (will be reused for all dates with this item/times)
                eventTimeLine = updateStore.insertEntity(Timeline.class);
                eventTimeLine.setEvent(getEvent());
                eventTimeLine.setSite(templateTimeline.getSite());
                eventTimeLine.setItem(item);
                eventTimeLine.setStartTime(startTime);
                eventTimeLine.setEndTime(endTime);
                eventTimeLine.setItemFamily(templateTimeline.getItemFamily());
                newlyCreatedEventTimelines.add(eventTimeLine); // Track for reuse
            }

            // Link template timeline to event timeline (marks program as generated)
            templateTimelineToEdit.setEventTimeline(eventTimeLine);

            // Create scheduled items for each selected date
            for (LocalDate date : selectedDates) {
                // Create teaching scheduled item (handles day ticket linking)
                ScheduledItem teachingScheduledItem = addTeachingsScheduledItemsForDateAndTimeline(
                    date, templateTimeline.getName(), eventTimeLine, updateStore);

                // Create audio scheduled items if audio recordings are offered
                if (templateTimeline.isAudioOffered()) {
                    addAudioScheduledItemsForDate(date, teachingScheduledItem, updateStore);
                }

                // Create video scheduled item if video recording is offered
                if (templateTimeline.isVideoOffered()) {
                    addVideoScheduledItemsForDate(date, teachingScheduledItem, updateStore);
                }
            }
        });
    }

    /**
     * Creates audio recording scheduled items for all available languages.
     * This method creates one audio scheduled item per language (English, French, etc.) for
     * a specific teaching session. Audio items are linked to their parent teaching item.
     *
     * <p><b>Day Ticket Scenarios:</b>
     * <ol>
     *   <li><b>Audio Recordings Day Ticket (Festivals):</b>
     *       Each language audio item links to its own language-specific bookable item.
     *       Example: "English Audio Day Ticket", "French Audio Day Ticket"</li>
     *
     *   <li><b>Teachings Day Ticket (STTP):</b>
     *       Audio items link to the teaching day ticket (not separate audio tickets).
     *       Attendees who book the teaching day ticket also get audio recordings.</li>
     *
     *   <li><b>No Day Ticket:</b>
     *       Audio items are independently bookable (no linking required).</li>
     * </ol>
     *
     * <p>See project documentation in doc/ directory for detailed day ticket architecture.
     *
     * @param date The date for these audio items
     * @param teachingScheduledItem The parent teaching session these recordings belong to
     * @param currentUpdateStore UpdateStore for creating entities
     */
    void addAudioScheduledItemsForDate(LocalDate date, ScheduledItem teachingScheduledItem,UpdateStore currentUpdateStore) {
        // Create one audio scheduled item per language (e.g., English, French, Spanish)
        programModel.getLanguageAudioItems().forEach(languageItem -> {
            ScheduledItem audioScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
            audioScheduledItem.setEvent(getEvent());
            audioScheduledItem.setSite(getSite());
            audioScheduledItem.setDate(date);
            audioScheduledItem.setProgramScheduledItem(teachingScheduledItem); // Link to parent teaching
            audioScheduledItem.setItem(languageItem); // e.g., "Heart Jewel Audio - English"

            if(getEvent().isAudioRecordingsDayTicket()) {
                // SCENARIO 1: Festival with separate audio day tickets per language
                // Link to language-specific bookable audio item
                ScheduledItem bookableScheduleItem = findBookableScheduledItemByDate(
                    programModel.getAudioRecordingsBookableScheduledItems(), date, languageItem);
                audioScheduledItem.setBookableScheduledItem(bookableScheduleItem);
            } else if(getEvent().isTeachingsDayTicket()) {
                // SCENARIO 2: STTP with teaching day ticket (includes audio)
                // Link to teaching day ticket (attendees get audio as part of teaching booking)
                ScheduledItem bookableScheduleItem = findBookableScheduledItemByDate(
                    programModel.getTeachingsBookableScheduledItems(), date, null);
                audioScheduledItem.setBookableScheduledItem(bookableScheduleItem);
            }
            // SCENARIO 3: No day ticket - audio items are independently bookable (no link needed)
        });
    }

    /**
     * Creates a video recording scheduled item for a teaching session.
     * Creates a single video scheduled item linked to the parent teaching session.
     * Unlike audio (which has multiple languages), there's only one video item.
     *
     * <p><b>Day Ticket Scenario:</b>
     * <ul>
     *   <li><b>Teachings Day Ticket:</b> Video item links to the teaching day ticket.
     *       Attendees who book the teaching day ticket also get video access.</li>
     *   <li><b>No Day Ticket:</b> Video item is independently bookable.</li>
     * </ul>
     *
     * @param date The date for this video item
     * @param teachingScheduledItem The parent teaching session this recording belongs to
     * @param currentUpdateStore UpdateStore for creating entities
     */
    void addVideoScheduledItemsForDate(LocalDate date,  ScheduledItem teachingScheduledItem, UpdateStore currentUpdateStore) {
        ScheduledItem videoScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
        videoScheduledItem.setEvent(getEvent());
        videoScheduledItem.setSite(getSite());
        videoScheduledItem.setItem(getVideoItem()); // Single video item for all languages
        videoScheduledItem.setDate(date);
        videoScheduledItem.setProgramScheduledItem(teachingScheduledItem); // Link to parent teaching

        if(getEvent().isTeachingsDayTicket()) {
            // Link to teaching day ticket (attendees get video as part of teaching booking)
            ScheduledItem bookableScheduleItem = findBookableScheduledItemByDate(
                programModel.getTeachingsBookableScheduledItems(), date, null);
            videoScheduledItem.setBookableScheduledItem(bookableScheduleItem);
        }
        // If no day ticket: video item is independently bookable (no link needed)
    }

    /**
     * Returns the parent ProgramModel managing all day templates.
     *
     * @return The ProgramModel instance
     */
    ProgramModel getProgramModel() {
        return programModel;
    }
}
