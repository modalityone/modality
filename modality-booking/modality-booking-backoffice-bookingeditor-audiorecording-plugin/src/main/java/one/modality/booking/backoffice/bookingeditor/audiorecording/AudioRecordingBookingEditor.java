package one.modality.booking.backoffice.bookingeditor.audiorecording;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.FlowPane;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.backoffice.bookingeditor.family.FamilyBookingEditorBase;
import one.modality.booking.client.workingbooking.WorkingBooking;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
final class AudioRecordingBookingEditor extends FamilyBookingEditorBase {

    private final Map<Item, List<ScheduledItem>> audioRecordingItemsToScheduledItemsMap;
    private final Map<Item, CheckBox> audioRecordingItemsToCheckBoxMap= new LinkedHashMap<>();

    AudioRecordingBookingEditor(WorkingBooking workingBooking) {
        super(workingBooking, KnownItemFamily.AUDIO_RECORDING);
        audioRecordingItemsToScheduledItemsMap = workingBooking
            .getPolicyAggregate()
            .audioRecordingItemsToScheduledItemsMap();
        // Final subclasses should call this method:
        initiateUiAndSyncFromWorkingBooking();
    }

    @Override
    protected void initiateUiAndSyncFromWorkingBooking() {
        List<ScheduledItem> alreadyBookedAudioRecordingScheduledItems = getAlreadyBookedFamilyScheduledItems();
        for (Map.Entry<Item, List<ScheduledItem>> entry : audioRecordingItemsToScheduledItemsMap.entrySet()) {
            CheckBox languageCheckBox = I18nEntities.newExpressionCheckBox(entry.getKey(), "i18n(this)");
            audioRecordingItemsToCheckBoxMap.put(entry.getKey(), languageCheckBox);
            languageCheckBox.setSelected(entry.getValue().stream().anyMatch(alreadyBookedAudioRecordingScheduledItems::contains));
        }
        audioRecordingItemsToCheckBoxMap.values().forEach(languageCheckBox ->
            FXProperties.runNowAndOnPropertyChange(this::syncWorkingBookingFromUi, languageCheckBox.selectedProperty())
        );
    }

    @Override
    public void syncWorkingBookingFromUi() {
        List<ScheduledItem> bookedTeachingScheduledItems = getBookedFamilyScheduledItems(KnownItemFamily.TEACHING);
        for (Map.Entry<Item, List<ScheduledItem>> entry : audioRecordingItemsToScheduledItemsMap.entrySet()) {
            CheckBox languageCheckBox = audioRecordingItemsToCheckBoxMap.get(entry.getKey());
            List<ScheduledItem> policyLanguageScheduledItems = entry.getValue();
            if (languageCheckBox.isSelected()) {
                // Restricting the dates to the booked teachings
                List<ScheduledItem> restrictedLanguageScheduledItems = Collections.filter(policyLanguageScheduledItems,
                    audioScheduledItem -> bookedTeachingScheduledItems.stream()
                        .anyMatch(teachingScheduledItem -> Objects.equals(teachingScheduledItem.getDate(), audioScheduledItem.getDate())));
                workingBooking.bookScheduledItems(restrictedLanguageScheduledItems, false);
            } else
                workingBooking.unbookScheduledItems(policyLanguageScheduledItems);
        }
    }

    @Override
    public Node buildUi() {
        FlowPane flowPane = new FlowPane(20, 15);
        flowPane.getChildren().addAll(audioRecordingItemsToCheckBoxMap.values());
        return flowPane;
    }
}
