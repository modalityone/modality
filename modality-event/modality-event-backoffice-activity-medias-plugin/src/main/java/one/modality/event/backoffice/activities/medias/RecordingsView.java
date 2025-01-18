package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.extras.util.masterslave.SlaveEditor;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.*;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.ui.validation.ValidationSupport;
import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.client.util.masterslave.ModalitySlaveEditor;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class RecordingsView {

    private Event currentEditedEvent;
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private final ValidationSupport validationSupport = new ValidationSupport();
    private TextField contentExpirationDate;
    private final ObservableList<Item> workingItems = FXCollections.observableArrayList();
    private final Map<String, MediaLinksManagement> correspondenceBetweenLanguageAndLanguageLinkManagement = new HashMap<>();
    private final ObservableList<LocalDate> teachingsDates = FXCollections.observableArrayList();
    private final ObservableList<ScheduledItem> audioScheduledItemsReadFromDatabase = FXCollections.observableArrayList();
    private final ObservableList<Media> recordingsMediasReadFromDatabase = FXCollections.observableArrayList();
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    ObservableList<BooleanExpression> listOfUpdateStoresHasChangedProperty = FXCollections.observableArrayList();
    private final ScrollPane mainContainer;
    private final BorderPane mainFrame;
    String lastLanguageSelected = "";

    public RecordingsView() {
        mainFrame = new BorderPane();
        mainFrame.setPadding(new Insets(0, 0, 30, 0));
        mainContainer = ControlUtil.createVerticalScrollPane(mainFrame);
    }


    public void startLogic() {
        //we initialise the current Edited event to the current event. This will be update later when we change the event selected with the masterSlaveEventLinker
        currentEditedEvent = FXEvent.getEvent();
        masterSlaveEventLinker.masterProperty().bindBidirectional(FXEvent.eventProperty());
        displayEventDetails(currentEditedEvent);
    }

    public Node buildContainer() {
        //The main container is build by drawContainer, who is called by displayEventDetails.
        return mainContainer;
    }

    public void drawContainer() {
        mainFrame.setPadding(new Insets(0, 0, 30, 0));

        Label title = I18nControls.newLabel(MediasI18nKeys.RecordingsTitle);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add(Bootstrap.H2);
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);

        if(currentEditedEvent.getRepeatedEvent()!=null && currentEditedEvent.isRepeatAudio()) {
            Label seeRepeatableEventLabel = I18nControls.newLabel(MediasI18nKeys.AudioConfigurationDoneInRepeatableEvent, currentEditedEvent.getRepeatedEvent().getName(), Entities.getPrimaryKey(currentEditedEvent.getRepeatedEventId()).toString());
            seeRepeatableEventLabel.setPadding(new Insets(200,0,0,0));
            mainFrame.setCenter(seeRepeatableEventLabel);
            return;
        }
        /////////////////
        VBox masterSettings = new VBox();
        masterSettings.setPadding(new Insets(20));

        Label masterLabel = I18nControls.newLabel(MediasI18nKeys.MasterSettings);
        masterLabel.getStyleClass().add(Bootstrap.STRONG);
        masterLabel.setPadding(new Insets(20, 0, 0, 0));
        masterSettings.getChildren().add(masterLabel);

        Label availableUntilLabel = I18nControls.newLabel(MediasI18nKeys.AvailableUntil);
        availableUntilLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        availableUntilLabel.setPadding(new Insets(15, 0, 0, 0));
        masterSettings.getChildren().add(availableUntilLabel);

        Label availableUntilCommentLabel = I18nControls.newLabel(MediasI18nKeys.AvailableUntilComment);
        availableUntilCommentLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        availableUntilCommentLabel.getStyleClass().add(Bootstrap.SMALL);
        availableUntilCommentLabel.setPadding(new Insets(0, 0, 5, 0));

        masterSettings.getChildren().add(availableUntilCommentLabel);
        Event event = updateStore.updateEntity(currentEditedEvent);
        contentExpirationDate = new TextField();
        contentExpirationDate.setPromptText("Format: 25-09-2028");
        validationSupport.addDateOrEmptyValidation(contentExpirationDate, "dd-MM-yyyy", contentExpirationDate, I18n.i18nTextProperty("ValidationTimeFormatIncorrect")); // ???
        //TODO how to load the expiration date in the event
        if (event.getAudioExpirationDate() != null) {
            contentExpirationDate.setText(event.getAudioExpirationDate().format(dateFormatter));
        }
        contentExpirationDate.textProperty().addListener(observable -> {
            try {
                if (Strings.isEmpty(contentExpirationDate.getText())) {
                    event.setAudioExpirationDate(null);
                }
                LocalDate date = LocalDate.parse(contentExpirationDate.getText(), dateFormatter);
                LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(0, 0));
                event.setAudioExpirationDate(dateTime);
            } catch (DateTimeParseException e) {
            }
        });


        masterSettings.getChildren().add(contentExpirationDate);

        Switch contentAvailableOfflineSwitch = new Switch();

        Label availableOfflineLabel = I18nControls.newLabel(MediasI18nKeys.AvailableOffline);
        availableOfflineLabel.setPadding(new Insets(0, 0, 0, 10));
        TextTheme.createSecondaryTextFacet(availableOfflineLabel).style();
        HBox offlineManagementHBox = new HBox(contentAvailableOfflineSwitch, availableOfflineLabel);
        offlineManagementHBox.setPadding(new Insets(20, 0, 0, 0));

        offlineManagementHBox.setAlignment(Pos.CENTER_LEFT);
        //TODO: implement the offLine access management
        // masterSettings.getChildren().add(offlineManagementHBox);

        //SAVE BUTTON
        Button saveButton = Bootstrap.successButton(I18nControls.newButton(ModalityI18nKeys.Save));
        VBox.setMargin(saveButton, new Insets(20, 0, 0, 0));
        BooleanExpression hasChangesProperty = EntityBindings.hasChangesProperty(updateStore);
        addUpdateStoreHasChangesProperty(hasChangesProperty);
        saveButton.disableProperty().bind(hasChangesProperty.not());
        saveButton.setOnAction(e -> {
            if (validationSupport.isValid()) {
                updateStore.submitChanges()
                    .onFailure(Console::log)
                    //TODO : display a message to say the data has been saved
                    .onSuccess(Console::log);
            }
        });

        masterSettings.getChildren().add(saveButton);


        /* The language section */
        /* **********************/
        Label languageLabel = I18nControls.newLabel(MediasI18nKeys.SelectLanguage);
        languageLabel.getStyleClass().add(Bootstrap.STRONG);
        languageLabel.setPadding(new Insets(30, 0, 10, 0));
        masterSettings.getChildren().add(languageLabel);

        entityStore.executeQueryBatch(
                new EntityStoreQuery("select distinct item.name, item.code from ScheduledItem  where programScheduledItem.event= ? and item.family.code = ? and programScheduledItem.item.family.code = ? order by item.name",
                    new Object[]{currentEditedEvent, KnownItemFamily.AUDIO_RECORDING.getCode(), KnownItemFamily.TEACHING.getCode()}),
                new EntityStoreQuery("select name, date, programScheduledItem.(startTime, endTime), item.code, programScheduledItem.timeline.startTime, published, programScheduledItem.name, programScheduledItem.timeline.endTime,programScheduledItem.timeline.audioOffered, event, site, expirationDate, available from ScheduledItem where programScheduledItem.event= ? and item.family.code = ? and programScheduledItem.item.family.code = ? order by date",
                    new Object[]{currentEditedEvent, KnownItemFamily.AUDIO_RECORDING.getCode(), KnownItemFamily.TEACHING.getCode()}),
                new EntityStoreQuery("select url, scheduledItem.programScheduledItem, scheduledItem.item, scheduledItem.item.code ,scheduledItem.date, scheduledItem.published, durationMillis from Media where scheduledItem.event= ? and scheduledItem.item.family.code = ?", new Object[]{currentEditedEvent, KnownItemFamily.AUDIO_RECORDING.getCode()}))
            .onFailure(Console::log)
            .onSuccess(entityList -> Platform.runLater(() -> {
                EntityList<ScheduledItem> itemList = entityList[0];
                EntityList<ScheduledItem> siList = entityList[1];
                EntityList<Media> mediaList = entityList[2];
                if (siList.isEmpty()) {
                    Console.log("No recording offered for this event");
                } else {
                    //We have two lists of scheduled items, the teachings and the recordings (we suppose that for each recording ScheduledItem, we have a media associated in the database
                    audioScheduledItemsReadFromDatabase.setAll(siList);
                    recordingsMediasReadFromDatabase.setAll(mediaList);
                    teachingsDates.setAll(siList.stream().map(EntityHasLocalDate::getDate).distinct().collect(Collectors.toList()));
                    workingItems.setAll(itemList.stream()
                        .map(ScheduledItem::getItem) // Map ScheduledItem to Item
                        .collect(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(Item::getCode)))) // Ensure distinct items by code
                    );

                    VBox languageListVBox = new VBox();
                    languageListVBox.setSpacing(10);
                    workingItems.forEach(currentItem -> {
                        HBox currentLanguageHBox = new HBox();
                        Label currentLanguageLabel = new Label(currentItem.getName());
                        MediaLinksManagement newLanguageLinksManagement = correspondenceBetweenLanguageAndLanguageLinkManagement.get(currentItem.getName());
                        TextTheme.createPrimaryTextFacet(currentLanguageLabel).style();
                        currentLanguageHBox.getChildren().add(currentLanguageLabel);
                        currentLanguageLabel.setOnMouseClicked(e -> {
                            MediaLinksManagement oldLanguage = correspondenceBetweenLanguageAndLanguageLinkManagement.get(lastLanguageSelected);
                            lastLanguageSelected = currentItem.getName();
                            if (oldLanguage != null) oldLanguage.setVisible(false);
                            newLanguageLinksManagement.setVisible(true);
                        });
                        currentLanguageLabel.setCursor(Cursor.HAND);

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);
                        currentLanguageHBox.getChildren().add(spacer);

                        //Here we display the percentage of what has been entered.
                        // To do this, we filter the list of scheduledItem per language and date, and we do the same for the media,
                        // and we compare the number
                        StringProperty cssProperty = new SimpleStringProperty();
                        Label percentageLabel = new Label();
                        IntegerProperty percentageProperty = new SimpleIntegerProperty() {
                            @Override
                            protected void invalidated() {
                                percentageLabel.setText(get() + "%");
                            }
                        };
                        percentageLabel.setPadding(new Insets(0, 0, 0, 40));
                        currentLanguageHBox.getChildren().add(percentageLabel);

                        FXProperties.runOnPropertyChange((obs, oldClass, newClass) -> {
                            percentageLabel.getStyleClass().removeAll(oldClass); // Remove the old class
                            percentageLabel.getStyleClass().add(newClass);       // Add the new class
                        }, cssProperty);

                        ObservableLists.runOnListChange(() -> updatePercentageProperty(percentageProperty, cssProperty, currentItem)
                        , newLanguageLinksManagement.getRecordingsMediasReadFromDatabase());
                        //We call it manually the first time
                        updatePercentageProperty(percentageProperty, cssProperty, currentItem);
                        languageListVBox.getChildren().add(currentLanguageHBox);
                    });

                    masterSettings.getChildren().add(languageListVBox);
                }
            }));


        // Main Section (Recordings Section)
        VBox recordingsSection = new VBox(10);
        recordingsSection.setPadding(new Insets(20));

        ObservableLists.bindConverted(recordingsSection.getChildren(), workingItems, this::drawLanguageBox);

        // Layout container (HBox)
        Separator VSeparator = new Separator();
        VSeparator.setOrientation(Orientation.VERTICAL);
        VSeparator.setPadding(new Insets(30));
        HBox mainLayout = new HBox(10, masterSettings, VSeparator, recordingsSection);
        BorderPane.setAlignment(mainLayout, Pos.CENTER);
        /////////////////
        mainFrame.setCenter(mainLayout);
    }

    public void addUpdateStoreHasChangesProperty(BooleanExpression booleanProperty) {
        listOfUpdateStoresHasChangedProperty.add(booleanProperty);
    }

    public void updatePercentageProperty(IntegerProperty percentageProperty, StringProperty cssProperty, Item currentItem) {
        long numberOfTeachingForThisLanguage = audioScheduledItemsReadFromDatabase.stream().filter(scheduledItem -> scheduledItem.getItem().equals(currentItem))
            .count();
        long numberOfMediaForThisLanguage = recordingsMediasReadFromDatabase.stream()
            .filter(media -> media.getScheduledItem().getItem().equals(currentItem))
            .count();

        // Calculate percentage, handling division by zero
        percentageProperty.set((int) (numberOfTeachingForThisLanguage == 0 ? 0 :
            (100.0 * numberOfMediaForThisLanguage / numberOfTeachingForThisLanguage)));

        if (percentageProperty.get() < 100) {
            cssProperty.setValue(Bootstrap.TEXT_DANGER);
        } else {
            cssProperty.setValue(Bootstrap.TEXT_SUCCESS);
        }
    }

    private Node drawLanguageBox(Item item) {
        MediaLinksManagement languageLinkManagement = new MediaLinksForAudioRecordingsManagement(item, entityStore, teachingsDates, audioScheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase, this);
        correspondenceBetweenLanguageAndLanguageLinkManagement.put(item.getName(), languageLinkManagement);
        languageLinkManagement.setVisible(false);
        return languageLinkManagement.getContainer();
    }

    public void setActive(boolean b) {
        activeProperty.set(b);
    }

    private final SlaveEditor<Event> eventDetailsSlaveEditor = new ModalitySlaveEditor<>() {
        /**
         * This method is called by the master controller when we change the event we're editing
         *
         * @param approvedEntity the approved Entity
         */
        @Override
        public void setSlave(Event approvedEntity) {
            currentEditedEvent = approvedEntity;
            resetUpdateStoreAndOtherComponents();
            displayEventDetails(currentEditedEvent);
        }

        @Override
        public Event getSlave() {
            return currentEditedEvent;
        }

        @Override
        public boolean hasChanges() {
            if (listOfUpdateStoresHasChangedProperty.isEmpty()) {
                return false;
            }
            return listOfUpdateStoresHasChangedProperty.stream()
                .anyMatch(BooleanExpression::getValue);
        }
    };

    private void resetUpdateStoreAndOtherComponents() {
        updateStore.cancelChanges();
        listOfUpdateStoresHasChangedProperty.clear();
    }

    private void displayEventDetails(Event e) {
        e.onExpressionLoaded("organization,audioExpirationDate, repeatedEvent, repeatAudio, repeatVideo")
            .onSuccess(ignored -> Platform.runLater(this::drawContainer))
            .onFailure((Console::log));

    }

    //This parameter will allow us to manage the interaction and behaviour of the Panel that display the details of an event and the event selected
    final private MasterSlaveLinker<Event> masterSlaveEventLinker = new MasterSlaveLinker<>(eventDetailsSlaveEditor);
}





