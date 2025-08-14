package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.operation.OperationUtil;
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
import one.modality.base.shared.entities.markers.EntityHasItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class ProgramModel {

    private final KnownItemFamily programItemFamily;

    private final EntityStore entityStore;
    private final UpdateStore updateStore;

    private final ObjectProperty<Event> loadedEventProperty = new SimpleObjectProperty<>();

    private Site programSite;
    private List<Item> languageAudioItems;
    private Item videoItem;
    private List<ScheduledItem> teachingsBookableScheduledItems;
    private List<ScheduledItem> audioRecordingsBookableScheduledItems;
    private final BooleanProperty programGeneratedProperty = new SimpleBooleanProperty();
    private final BooleanProperty dayTicketPreliminaryScheduledItemProperty = new SimpleBooleanProperty();

    private final List<DayTemplate> initialWorkingDayTemplates = new ArrayList<>();
    private final ObservableList<DayTemplate> currentDayTemplates = new OptimizedObservableListWrapper<>();
    private final ObservableList<DayTemplateModel> dayTemplateModels = FXCollections.observableArrayList();
    {
        ObservableLists.bindConverted(dayTemplateModels, currentDayTemplates, dayTemplate -> new DayTemplateModel(dayTemplate, this));
    }

    private final ValidationSupport validationSupport = new ValidationSupport();
    private Item bookableTeachingItem;

    ProgramModel(KnownItemFamily programItemFamily, DataSourceModel dataSourceModel) {
        this.programItemFamily = programItemFamily;
        entityStore = EntityStore.create(dataSourceModel);
        updateStore = UpdateStore.createAbove(entityStore);
    }

    EntityStore getEntityStore() {
        return entityStore;
    }

    UpdateStore getUpdateStore() {
        return updateStore;
    }

    Event getLoadedEvent() {
        return loadedEventProperty.get();
    }

    void setLoadedEvent(Event loadedEvent) {
        loadedEventProperty.set(loadedEvent);
    }

    ObjectProperty<Event> loadedEventProperty() {
        return loadedEventProperty;
    }

    Site getProgramSite() {
        return programSite;
    }

    List<Item> getLanguageAudioItems() {
        return languageAudioItems;
    }

    List<ScheduledItem> getTeachingsBookableScheduledItems() {
        return teachingsBookableScheduledItems;
    }

    List<ScheduledItem> getAudioRecordingsBookableScheduledItems() {
        return audioRecordingsBookableScheduledItems;
    }


    public BooleanProperty getDayTicketPreliminaryScheduledItemProperty() {
        return dayTicketPreliminaryScheduledItemProperty;
    }
    Item getVideoItem() {
        return videoItem;
    }

    void setProgramGenerated() {
        programGeneratedProperty.set(true);
    }

    BooleanProperty programGeneratedProperty() {
        return programGeneratedProperty;
    }

    ObservableList<DayTemplate> getCurrentDayTemplates() {
        return currentDayTemplates;
    }

    public ObservableList<DayTemplateModel> getWorkingDayTemplates() {
        return dayTemplateModels;
    }

    ValidationSupport getValidationSupport() {
        return validationSupport;
    }

    void reloadProgramFromSelectedEvent(Event selectedEvent) {
        //We execute the query in batch, otherwise we can have synchronisation problem between the different threads
        entityStore.executeQueryBatch(
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
                new EntityStoreQuery("select teachingsDayTicket, audioRecordingsDayTicket from Event where id=?", selectedEvent),
                new EntityStoreQuery("select distinct name, code from item  where family.code = ? and organization = ? and not deprecated order by name",
                    KnownItemFamily.AUDIO_RECORDING.getCode(), FXEvent.getEvent().getOrganization()))
            .onFailure(Console::log)
            .inUiThread()
            .onSuccess(entityLists -> {
                // Extracting the different entity lists from the query batch result
                EntityList<DayTemplate> dayTemplates = entityLists[0];
                EntityList<Site> sites = entityLists[1];
                EntityList<Item> items = entityLists[2];
                EntityList<ScheduledItem> bookableScheduledItems = entityLists[3];

                programSite = Collections.first(sites);
                //TODO: for now, we look for all language available. Change this to a list of language that is setup as the event creation.
                languageAudioItems = Collections.filter(items, item -> KnownItemFamily.AUDIO_RECORDING.getCode().equals(item.getFamily().getCode()) && !item.isDeprecated());
                videoItem = Collections.findFirst(items, item -> KnownItemFamily.VIDEO.getCode().equals(item.getFamily().getCode()));
                teachingsBookableScheduledItems = Collections.filter(bookableScheduledItems, scheduledItem -> KnownItemFamily.TEACHING.getCode().equals(scheduledItem.getItem().getFamily().getCode()));
                audioRecordingsBookableScheduledItems = Collections.filter(bookableScheduledItems, scheduledItem -> KnownItemFamily.AUDIO_RECORDING.getCode().equals(scheduledItem.getItem().getFamily().getCode()));
                dayTicketPreliminaryScheduledItemProperty.setValue(!teachingsBookableScheduledItems.isEmpty() || !audioRecordingsBookableScheduledItems.isEmpty());
                Collections.setAll(initialWorkingDayTemplates, dayTemplates.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                setLoadedEvent(entityStore.copyEntity(selectedEvent));

                for (DayTemplateModel dayTemplateModel : dayTemplateModels) {
                    dayTemplateModel.reloadTimelinesFromDatabase();
                }
                resetModelAndUiToInitial();
            });
    }

    private void resetModelAndUiToInitial() {
        validationSupport.clear();
        updateStore.cancelChanges();
        currentDayTemplates.setAll(initialWorkingDayTemplates);
        programGeneratedProperty.setValue(false); // A priori value. May be set to true in the following loop.
        dayTemplateModels.forEach(DayTemplateModel::resetModelAndUiToInitial);
    }

    void addNewDayTemplate() {
        DayTemplate dayTemplate = updateStore.insertEntity(DayTemplate.class);
        dayTemplate.setEvent(getLoadedEvent());
        currentDayTemplates.add(dayTemplate);
    }

    void deleteDayTemplate(DayTemplateModel dayTemplateModel) {
        DayTemplate dayTemplate = dayTemplateModel.getDayTemplate();
        currentDayTemplates.remove(dayTemplate);
        dayTemplateModel.removeTemplateTimeLineLinkedToDayTemplate();
        updateStore.deleteEntity(dayTemplate);
    }

    Future<?> generateProgram() {
        UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);
        List<Timeline> newlyCreatedEventTimelines = new ArrayList<>();
        //Here, we take all the template timelines, and create the event timelines needed
        //We create an event timeline for all template timelines having distinct element on {item, startTime, endTime}
        dayTemplateModels.forEach(dayTemplateView ->
            dayTemplateView.generateProgram(newlyCreatedEventTimelines, localUpdateStore));
        return submitUpdateStoreChanges(localUpdateStore);
    }

    void saveChanges(Button saveButton, Button cancelButton) {
        if (validateForm()) {
            OperationUtil.turnOnButtonsWaitModeDuringExecution(
                submitUpdateStoreChangesAndReload(updateStore),
                saveButton, cancelButton);
        }
    }

    void cancelChanges() {
        resetModelAndUiToInitial();
    }

    Future<?> deleteProgram() {
        UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);

        //Here we look for the teachings, audio and video scheduled Item related to this timeline and delete them
        return entityStore.<ScheduledItem>executeQuery(
            "select id, item.family.code from ScheduledItem si where event=? order by name", getLoadedEvent()
        ).compose(scheduledItems -> {
            //First we delete all the audios and videos scheduledItem
            scheduledItems.forEach(currentScheduledItem -> {
                String scheduledItemFamilyCode = currentScheduledItem.getItem().getFamily().getCode();
                if (scheduledItemFamilyCode.equals(KnownItemFamily.AUDIO_RECORDING.getCode()) || scheduledItemFamilyCode.equals(KnownItemFamily.VIDEO.getCode()))
                    localUpdateStore.deleteEntity(currentScheduledItem);
            });

            scheduledItems.forEach(currentScheduledItem -> {
                String code = currentScheduledItem.getItem().getFamily().getCode();
                if (code.equals(KnownItemFamily.TEACHING.getCode()))
                    localUpdateStore.deleteEntity(currentScheduledItem);
            });

            //then we put the reference to the event timeline to null on the template timeline
            dayTemplateModels.forEach(dayTemplateView -> dayTemplateView.deleteTimelines(localUpdateStore));
            return submitUpdateStoreChanges(localUpdateStore);
        });
    }

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
            .onSuccess(x -> {
                reloadProgramFromSelectedEvent(FXEvent.getEvent());
                resetModelAndUiToInitial();
            });
    }

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

    private boolean validateForm() {
        checkValidationInitialized();
        return validationSupport.isValid();
    }

    private void checkValidationInitialized() {
        if (validationSupport.isEmpty()) {
            resetValidation();
        }
    }

    private void resetValidation() {
        validationSupport.clear();
        dayTemplateModels.forEach(DayTemplateModel::initFormValidation);
    }

    public Future<?> generatePreliminaryBookableSI() {
        //First we add all the teaching scheduledItem : one per day.
        Event currentSelectedEvent = updateStore.updateEntity(FXEvent.getEvent());
        currentSelectedEvent.setKbs3(true);
        //Important, we need to define audioRecordingDayTicket to true, otherwise the program won't be generated correctly
        currentSelectedEvent.setAudioRecordingsDayTicket(true);
        LocalDate startDate = currentSelectedEvent.getStartDate();
        LocalDate endDate = currentSelectedEvent.getEndDate();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            ScheduledItem teachingScheduledItem = updateStore.insertEntity(ScheduledItem.class);
            teachingScheduledItem.setEvent(currentSelectedEvent);
            teachingScheduledItem.setDate(date);
            teachingScheduledItem.setSite(programSite);
            teachingScheduledItem.setItem(bookableTeachingItem);
        }

        //Then we add the audio recording scheduledItem: one per day and one per language
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

    public Item getTeachingBookableScheduledItem() {
        if(getTeachingsBookableScheduledItems()!=null)
            return getTeachingsBookableScheduledItems().stream()
                .map(EntityHasItem::getItem)
                .distinct()
                .findFirst().get();
        return null;
    }

    public void setBookableTeachingItem(Item selectedItem) {
        bookableTeachingItem = selectedItem;
    }
}
