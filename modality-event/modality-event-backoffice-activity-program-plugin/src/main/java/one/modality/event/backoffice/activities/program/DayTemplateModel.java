package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.util.OptimizedObservableListWrapper;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class DayTemplateModel {

    private final ProgramModel programModel;
    private final DayTemplate dayTemplate;

    private ObservableList<LocalDate> selectedDates;
    private Runnable syncUiFromModelRunnable;
    private Runnable initFormValidationRunnable;

    private final List<Timeline> initialTemplateTimelines = new ArrayList<>();
    private final ObservableList<Timeline> currentTemplateTimelines = new OptimizedObservableListWrapper<>();
    private final ObservableList<DayTemplateTimelineModel> dayTemplateTimelineModels = FXCollections.observableArrayList();
    {
        ObservableLists.bindConverted(dayTemplateTimelineModels, currentTemplateTimelines, timeline -> new DayTemplateTimelineModel(timeline, this));
    }

    DayTemplateModel(DayTemplate dayTemplate, ProgramModel programModel) {
        this.programModel = programModel;
        this.dayTemplate = dayTemplate;
        startLogic();
    }

    public DayTemplate getDayTemplate() {
        return dayTemplate;
    }

    public void setSelectedDates(ObservableList<LocalDate> selectedDates) {
        this.selectedDates = selectedDates;
    }

    public void setSyncUiFromModelRunnable(Runnable syncUiFromModelRunnable) {
        this.syncUiFromModelRunnable = syncUiFromModelRunnable;
    }

    public void setInitFormValidationRunnable(Runnable initFormValidationRunnable) {
        this.initFormValidationRunnable = initFormValidationRunnable;
    }

    public ObservableList<DayTemplateTimelineModel> getWorkingDayTemplateTimelines() {
        return dayTemplateTimelineModels;
    }

    private Event getEvent() {
        return dayTemplate.getEvent();
    }

    ModalityValidationSupport getValidationSupport() {
        return programModel.getValidationSupport();
    }

    private Site getSite() {
        return programModel.getProgramSite();
    }

    private Item getVideoItem() {
        return programModel.getVideoItem();
    }

    private UpdateStore getUpdateStore() {
        return programModel.getUpdateStore();
    }

    private void startLogic() {
        //We read the value of the database for the child elements only if the dayTemplate is already existing in the database (ie not in cache)
        if (!dayTemplate.getId().isNew()) {
            programModel.getEntityStore().<Timeline>executeQuery(
                    "select item, dayTemplate, startTime, endTime, videoOffered, audioOffered, name, site, eventTimeline from Timeline where dayTemplate=? order by startTime"
                    , dayTemplate
                )
                .onFailure(Console::log)
                .onSuccess(timelines -> Platform.runLater(() -> {
                    Collections.setAll(initialTemplateTimelines, timelines.stream().map(getUpdateStore()::updateEntity).collect(Collectors.toList()));
                    resetModelAndUiToInitial();
                }));
        }
    }

    void resetModelAndUiToInitial() {
        currentTemplateTimelines.setAll(initialTemplateTimelines);
        dayTemplateTimelineModels.forEach(DayTemplateTimelineModel::resetModelAndUiToInitial);
        //Here we test if the scheduled item have already been generated, ie if at least one of the workingTemplateTimelines got an eventTimeLine not null
        boolean hasEventTimeline = currentTemplateTimelines.stream()
            .anyMatch(timeline -> timeline.getEventTimelineId() != null);
        if (hasEventTimeline)
            programModel.setProgramGenerated();
        syncUiFromModel();
    }

    private void syncUiFromModel() {
        syncUiFromModelRunnable.run();
    }


    void initFormValidation() {
        initFormValidationRunnable.run();
    }

    void duplicate() {
        DayTemplate duplicateDayTemplate = getUpdateStore().insertEntity(DayTemplate.class);
        duplicateDayTemplate.setName(dayTemplate.getName() + " - copy");
        duplicateDayTemplate.setEvent(dayTemplate.getEvent());
        programModel.getCurrentDayTemplates().add(duplicateDayTemplate);
        DayTemplateModel newWTP = Collections.last(programModel.getWorkingDayTemplates());
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
            newWTP.currentTemplateTimelines.add(newTimeline);
        }
    }

    void addTemplateTimeline() {
        Timeline newTimeLine = getUpdateStore().insertEntity(Timeline.class);
        newTimeLine.setAudioOffered(false);
        newTimeLine.setVideoOffered(false);
        newTimeLine.setDayTemplate(dayTemplate);
        newTimeLine.setSite(getSite());
        currentTemplateTimelines.add(newTimeLine);
    }

    ScheduledItem addTeachingsScheduledItemsForDateAndTimeline(LocalDate date, String name, Timeline timeline, UpdateStore currentUpdateStore) {
        ScheduledItem teachingScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
        teachingScheduledItem.setEvent(getEvent());
        teachingScheduledItem.setSite(getSite());
        teachingScheduledItem.setDate(date);
        teachingScheduledItem.setName(name);
        teachingScheduledItem.setTimeLine(timeline);
        teachingScheduledItem.setItem(timeline.getItem());
        return teachingScheduledItem;
    }

    void deleteDayTemplate() {
        programModel.deleteDayTemplate(this);
    }

    void deleteTimelines(UpdateStore updateStore) {
        currentTemplateTimelines.forEach(currentTemplateTimeline -> {
            Timeline templateTimelineToUpdate = updateStore.updateEntity(currentTemplateTimeline);
            templateTimelineToUpdate.setEventTimeline(null);
            Timeline eventTimeLine = currentTemplateTimeline.getEventTimeline();
            updateStore.deleteEntity(eventTimeLine);
        });
    }

    void removeTemplateTimeLineLinkedToDayTemplate() {
        currentTemplateTimelines.removeIf(timeline -> (timeline.getDayTemplate() == dayTemplate));
    }

    void removeTemplateTimeLine(Timeline timeline) {
        getUpdateStore().deleteEntity(timeline);
        currentTemplateTimelines.remove(timeline);
    }

    void generateProgram(List<Timeline> newlyCreatedEventTimelines, UpdateStore updateStore) {
        currentTemplateTimelines.forEach(templateTimeline -> {
            LocalTime startTime = templateTimeline.getStartTime();
            LocalTime endTime = templateTimeline.getEndTime();
            Item item = templateTimeline.getItem();
            Timeline templateTimelineToEdit = updateStore.updateEntity(templateTimeline);
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
                eventTimeLine.setEvent(getEvent());
                eventTimeLine.setSite(templateTimeline.getSite());
                eventTimeLine.setItem(item);
                eventTimeLine.setStartTime(startTime);
                eventTimeLine.setEndTime(endTime);
                eventTimeLine.setItemFamily(templateTimeline.getItemFamily());
                newlyCreatedEventTimelines.add(eventTimeLine);
            }
            templateTimelineToEdit.setEventTimeline(eventTimeLine);
            //Now, we create the associated scheduledItem
            for (LocalDate date : selectedDates) {
                ScheduledItem teachingScheduledItem = addTeachingsScheduledItemsForDateAndTimeline(date, templateTimeline.getName(), eventTimeLine, updateStore);
                if (templateTimeline.isAudioOffered()) {
                    addAudioScheduledItemsForDate(date, teachingScheduledItem, updateStore);
                }
                if (templateTimeline.isVideoOffered()) {
                    addVideoScheduledItemsForDate(date, teachingScheduledItem,updateStore);
                }
            }
        });
    }

    void addAudioScheduledItemsForDate(LocalDate date, ScheduledItem parentTeachingScheduledItem, UpdateStore currentUpdateStore) {
        //Here we add for each language not deprecated the scheduledItemAssociated to the date and parent scheduledItem*
        programModel.getLanguageAudioItems().forEach(languageItem -> {
            ScheduledItem audioScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
            audioScheduledItem.setEvent(getEvent());
            audioScheduledItem.setSite(getSite());
            audioScheduledItem.setDate(date);
            audioScheduledItem.setProgramScheduledItem(parentTeachingScheduledItem);
            audioScheduledItem.setItem(languageItem);
        });
    }

    void addVideoScheduledItemsForDate(LocalDate date, ScheduledItem parentTeachingScheduledItem, UpdateStore currentUpdateStore) {
        ScheduledItem videoScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
        videoScheduledItem.setEvent(getEvent());
        videoScheduledItem.setSite(getSite());
        videoScheduledItem.setItem(getVideoItem());
        videoScheduledItem.setDate(date);
        videoScheduledItem.setProgramScheduledItem(parentTeachingScheduledItem);
    }
}
