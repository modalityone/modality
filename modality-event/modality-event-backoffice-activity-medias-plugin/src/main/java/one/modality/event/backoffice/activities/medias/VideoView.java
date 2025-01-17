package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.extras.util.masterslave.SlaveEditor;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.ui.validation.ValidationSupport;
import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
import java.util.Objects;
import java.util.stream.Collectors;


public class VideoView {
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private final ValidationSupport validationSupport = new ValidationSupport();
    private final ObservableList<LocalDate> teachingsDates = FXCollections.observableArrayList();
    private final ObservableList<ScheduledItem> vodScheduledItemsReadFromDatabase = FXCollections.observableArrayList();
    private final ObservableList<Media> recordingsMediasReadFromDatabase = FXCollections.observableArrayList();
    private TextField contentExpirationDateTextField;
    private TextField contentExpirationTimeTextField;
    private TextField livestreamGlobalLinkTextField;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private Event currentEditedEvent;
    private final ScrollPane mainContainer;
    private final BorderPane mainFrame;
    ObservableList<BooleanExpression> listOfUpdateStoresHasChangedProperty = FXCollections.observableArrayList();
    // Create the BooleanBinding that represents the AND condition of all properties

    public VideoView() {
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

    public void addUpdateStoreHasChangesProperty(BooleanExpression booleanProperty) {
        listOfUpdateStoresHasChangedProperty.add(booleanProperty);
    }

    public void drawContainer() {
        Label title = I18nControls.newLabel(MediasI18nKeys.VideoSettingsTitle);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add(Bootstrap.H2);
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);

        /////////////////
        VBox masterSettings = new VBox();
        masterSettings.setPadding(new Insets(20));

        Label masterLabel = I18nControls.newLabel(MediasI18nKeys.MasterSettings);
        masterLabel.getStyleClass().add(Bootstrap.STRONG);
        masterLabel.setPadding(new Insets(20, 0, 0, 0));
        masterSettings.getChildren().add(masterLabel);

        Event currentEvent = updateStore.updateEntity(FXEvent.getEvent());

        Label liveStreamGlobalLink = I18nControls.newLabel(MediasI18nKeys.LiveStreamGlobalLink);
        liveStreamGlobalLink.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        liveStreamGlobalLink.setPadding(new Insets(15, 0, 0, 0));
        masterSettings.getChildren().add(liveStreamGlobalLink);

        Label liveStreamGlobalComment = I18nControls.newLabel(MediasI18nKeys.LiveStreamGlobalLinkComment);
        liveStreamGlobalComment.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        liveStreamGlobalComment.getStyleClass().add(Bootstrap.SMALL);
        masterSettings.getChildren().add(liveStreamGlobalComment);

        livestreamGlobalLinkTextField = new TextField();
        livestreamGlobalLinkTextField.setPromptText("Ex: https://player.castr.com/live_14831a60190211efb48523dddcde7908");
        validationSupport.addUrlOrEmptyValidation(livestreamGlobalLinkTextField, I18n.i18nTextProperty(MediasI18nKeys.MalformedUrl));
        if (currentEvent.getLivestreamUrl() != null) {
            livestreamGlobalLinkTextField.setText(currentEvent.getLivestreamUrl());
        }
        livestreamGlobalLinkTextField.textProperty().addListener(observable -> {
            if (Objects.equals(livestreamGlobalLinkTextField.getText(), "")) {
                currentEvent.setLivestreamUrl(null);
            } else {
                currentEvent.setLivestreamUrl(livestreamGlobalLinkTextField.getText());
            }
        });
        VBox.setMargin(livestreamGlobalLinkTextField, new Insets(10, 0, 10, 0));
        masterSettings.getChildren().add(livestreamGlobalLinkTextField);


        Label availableUntilLabel = I18nControls.newLabel(MediasI18nKeys.AvailableUntil);
        availableUntilLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        availableUntilLabel.setPadding(new Insets(15, 0, 0, 0));
        masterSettings.getChildren().add(availableUntilLabel);

        Label availableUntilCommentLabel = I18nControls.newLabel(MediasI18nKeys.AvailableUntilComment);
        availableUntilCommentLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        availableUntilCommentLabel.getStyleClass().add(Bootstrap.SMALL);
        availableUntilCommentLabel.setPadding(new Insets(0, 0, 10, 0));

        masterSettings.getChildren().add(availableUntilCommentLabel);

        contentExpirationDateTextField = new TextField();
        contentExpirationDateTextField.setPromptText("Format: 25-09-2025");
        if (currentEvent.getVodExpirationDate() != null) {
            contentExpirationDateTextField.setText(currentEvent.getVodExpirationDate().format(dateFormatter));
        }
        validationSupport.addDateOrEmptyValidation(contentExpirationDateTextField, "dd-MM-yyyy", contentExpirationDateTextField, I18n.i18nTextProperty("ValidationTimeFormatIncorrect")); // ???

        masterSettings.getChildren().add(contentExpirationDateTextField);

        contentExpirationTimeTextField = new TextField();
        contentExpirationTimeTextField.setPromptText("Format: 14:25");
        validationSupport.addDateOrEmptyValidation(contentExpirationTimeTextField, "HH:mm", contentExpirationTimeTextField, I18n.i18nTextProperty("ValidationTimeFormatIncorrect")); // ???
        if (currentEvent.getVodExpirationDate() != null) {
            contentExpirationTimeTextField.setText(currentEvent.getVodExpirationDate().format(timeFormatter));
        }
        VBox.setMargin(contentExpirationTimeTextField, new Insets(10, 0, 10, 0));
        masterSettings.getChildren().add(contentExpirationTimeTextField);

        contentExpirationTimeTextField.textProperty().addListener(observable -> updateVodExpirationDate(currentEvent));
        contentExpirationDateTextField.textProperty().addListener(observable -> updateVodExpirationDate(currentEvent));

        Label vodAvailableAfterLive = I18nControls.newLabel(MediasI18nKeys.VODAvailableAfter);
        vodAvailableAfterLive.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        vodAvailableAfterLive.setPadding(new Insets(15, 0, 0, 0));
        masterSettings.getChildren().add(vodAvailableAfterLive);

        Label vodAvailableAfterLiveComment = I18nControls.newLabel(MediasI18nKeys.VODAvailableAfterComment);
        vodAvailableAfterLiveComment.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        vodAvailableAfterLiveComment.getStyleClass().add(Bootstrap.SMALL);
        masterSettings.getChildren().add(vodAvailableAfterLiveComment);

        TextField videoAvailableAfterTextField = new TextField();
        videoAvailableAfterTextField.setPromptText("Format: 90");
        // validationSupport.addIntegerValidation(videoAvailableAfterTextField, I18n.getI18nText("ValidationIntegerIncorrect"));
        VBox.setMargin(videoAvailableAfterTextField, new Insets(10, 0, 30, 0));
        masterSettings.getChildren().add(videoAvailableAfterTextField);


        //SAVE BUTTON
        Button saveButton = Bootstrap.largeSuccessButton(I18nControls.newButton(ModalityI18nKeys.Save));
        BooleanExpression hasChangesProperty = EntityBindings.hasChangesProperty(updateStore);
        saveButton.disableProperty().bind(hasChangesProperty.not());
        addUpdateStoreHasChangesProperty(hasChangesProperty);

        saveButton.setOnAction(e -> {
            if (validationSupport.isValid()) {
                updateStore.submitChanges()
                    .onFailure(Console::log)
                    //TODO : display a message to say the data has been saved
                    .onSuccess(Console::log);
            }
        });

        masterSettings.getChildren().add(saveButton);

        BorderPane individualSettingsHBox = buildIndividualLinksContainer();
        individualSettingsHBox.setPadding(new Insets(20));
        individualSettingsHBox.setMinWidth(1000);

        // Layout container (HBox)
        Separator VSeparator = new Separator();
        VSeparator.setOrientation(Orientation.VERTICAL);
        //  VSeparator.setPadding(new Insets(30));
        HBox mainLayout = new HBox(60, masterSettings, VSeparator, individualSettingsHBox);

        mainFrame.setCenter(mainLayout);
        individualSettingsHBox.setMaxWidth(800);

        BorderPane.setAlignment(mainFrame, Pos.CENTER);
        BorderPane.setAlignment(mainLayout, Pos.CENTER);
    }

    private void updateVodExpirationDate(Event currentEvent) {
        try {
            LocalDate date = LocalDate.parse(contentExpirationDateTextField.getText(), dateFormatter);
            LocalTime time = LocalTime.parse(contentExpirationTimeTextField.getText(), timeFormatter);
            // Combine the date and time to create LocalDateTime
            LocalDateTime availableUntil = LocalDateTime.of(date, time);
            currentEvent.setVodExpirationDate(availableUntil);
        } catch (DateTimeParseException e) {
            if(Objects.equals(contentExpirationDateTextField.getText(), "") && Objects.equals(contentExpirationTimeTextField.getText(), ""))
                currentEvent.setVodExpirationDate(null);
        }
    }

    private BorderPane buildIndividualLinksContainer() {
        BorderPane container = new BorderPane();
        entityStore.executeQueryBatch(
                new EntityStoreQuery("select distinct name,family.code from Item where organization=? and family.code = ? order by name",
                    new Object[]{currentEditedEvent.getOrganization(), KnownItem.VIDEO.getCode()}),
                new EntityStoreQuery("select name, programScheduledItem.(startTime, endTime), date, event, site, expirationDate,available, vodDelayed, published, item, item.code, programScheduledItem.name, programScheduledItem.timeline.startTime, programScheduledItem.timeline.endTime from ScheduledItem where programScheduledItem.event= ? and item.code = ? and programScheduledItem.item.family.code = ? order by date",
                    new Object[]{currentEditedEvent, KnownItem.VIDEO.getCode(), KnownItemFamily.TEACHING.getCode()}),
                new EntityStoreQuery("select url, scheduledItem.item, scheduledItem.date, scheduledItem.vodDelayed, scheduledItem.published, scheduledItem.item.code from Media where scheduledItem.event= ? and scheduledItem.item.code = ?",
                    new Object[]{currentEditedEvent, KnownItem.VIDEO.getCode()})
            ).onFailure(Console::log)
            .onSuccess(entityList -> Platform.runLater(() -> {
                //TODO: when we know which Item we use for VOD, we change the code bellow
                // EntityList<Item> VODItems = entityList[0];
                EntityList<ScheduledItem> videoSIList = entityList[1];
                EntityList<Media> mediaList = entityList[2];

                // Update lists with data from the database
                vodScheduledItemsReadFromDatabase.setAll(videoSIList);
                recordingsMediasReadFromDatabase.setAll(mediaList);
                teachingsDates.setAll(videoSIList.stream().map(EntityHasLocalDate::getDate).distinct().collect(Collectors.toList()));

                // Instantiate the MediaLinksForVODManagement with data
                MediaLinksForVODManagement languageLinkManagement = new MediaLinksForVODManagement(
                    entityStore, teachingsDates, vodScheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase, this);

                // Now that the data is ready, update the container
                container.setCenter(languageLinkManagement.getContainer());
            }));

        // Return the placeholder container, which will be updated later
        return container;
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
        e.onExpressionLoaded("organization,vodExpirationDate,livestreamUrl")
            .onSuccess(ignored -> Platform.runLater(this::drawContainer))
            .onFailure((Console::log));
    }

    //This parameter will allow us to manage the interaction and behaviour of the Panel that display the details of an event and the event selected
    final private MasterSlaveLinker<Event> masterSlaveEventLinker = new MasterSlaveLinker<>(eventDetailsSlaveEditor);
}





