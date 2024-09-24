package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class VODView {
    private final MediasActivity activity;
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private final ModalityValidationSupport validationSupport = new ModalityValidationSupport();
    private final boolean[] validationSupportInitialised = {false};
    private final ObservableList<LocalDate> teachingsDates = FXCollections.observableArrayList();
    private final ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase = FXCollections.observableArrayList();
    private final ObservableList<Media> recordingsMediasReadFromDatabase = FXCollections.observableArrayList();
    private final ObservableList<ScheduledItem>  scheduledItemsLinkedToTeachingScheduledItemsReadFromDatabase = FXCollections.observableArrayList();
   //TEMPORARY FOR TESTING TODO private final String itemFamilyTypeCode = "VOD";
    private final String itemFamilyTypeCode = "record";
    private TextField contentExpirationDateTextField;
    private TextField contentExpirationTimeTextField;
    private TextField videoAvailableAfterTextField;


    public VODView(MediasActivity activity) {
        this.activity = activity;
    }


    public void startLogic() {

    }

    public Node buildContainer() {
        BorderPane mainFrame = new BorderPane();
        mainFrame.setPadding(new Insets(0,0,30,0));
        Label title = I18nControls.bindI18nProperties(new Label(), "VODTitle");
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add(Bootstrap.H2);
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);

        /////////////////
        VBox masterSettings = new VBox();
        masterSettings.setPadding(new Insets(20));

        Label masterLabel = I18nControls.bindI18nProperties(new Label(), "MasterSettings");
        masterLabel.getStyleClass().add(Bootstrap.STRONG);
        masterLabel.setPadding(new Insets(20,0,0,0));
        masterSettings.getChildren().add(masterLabel);

        Label availableUntilLabel = I18nControls.bindI18nProperties(new Label(), "AvailableUntil");
        availableUntilLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        availableUntilLabel.setPadding(new Insets(15,0,0,0));
        masterSettings.getChildren().add(availableUntilLabel);

        Label availableUntilCommentLabel = I18nControls.bindI18nProperties(new Label(), "AvailableUntilComment");
        availableUntilCommentLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        availableUntilCommentLabel.getStyleClass().add(Bootstrap.SMALL);
        availableUntilCommentLabel.setPadding(new Insets(0,0,10,0));

        masterSettings.getChildren().add(availableUntilCommentLabel);

        contentExpirationDateTextField = new TextField();
        contentExpirationDateTextField.setPromptText("Format: 25-09-2025");
        validationSupport.addDateValidation(contentExpirationDateTextField, "dd-MM-yyyy", contentExpirationDateTextField,I18n.getI18nText("ValidationTimeFormatIncorrect"));

        masterSettings.getChildren().add(contentExpirationDateTextField);

        contentExpirationTimeTextField = new TextField();
        contentExpirationTimeTextField.setPromptText("Format: 14:25");
        validationSupport.addDateValidation(contentExpirationTimeTextField, "HH:mm", contentExpirationTimeTextField,I18n.getI18nText("ValidationTimeFormatIncorrect"));
        VBox.setMargin(contentExpirationTimeTextField, new Insets(10, 0, 10, 0));
        masterSettings.getChildren().add(contentExpirationTimeTextField);

        //TODO: here add the timezone selector when it's readuy


        Label vodAvailableAfterLive = I18nControls.bindI18nProperties(new Label(), "VODAvailableAfter");
        vodAvailableAfterLive.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        vodAvailableAfterLive.setPadding(new Insets(15,0,0,0));
        masterSettings.getChildren().add(vodAvailableAfterLive);

        Label vodAvailableAfterLiveComment = I18nControls.bindI18nProperties(new Label(), "VODAvailableAfterComment");
        vodAvailableAfterLiveComment.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
        vodAvailableAfterLiveComment.getStyleClass().add(Bootstrap.SMALL);
        masterSettings.getChildren().add(vodAvailableAfterLiveComment);

        videoAvailableAfterTextField = new TextField();
        videoAvailableAfterTextField.setPromptText("Format: 90");
        validationSupport.addIntegerValidation(videoAvailableAfterTextField, videoAvailableAfterTextField,I18n.getI18nText("ValidationIntegerIncorrect"));
        VBox.setMargin(videoAvailableAfterTextField, new Insets(10, 0, 10, 0));
        masterSettings.getChildren().add(videoAvailableAfterTextField);

        //SAVE BUTTON
        Button saveButton = Bootstrap.successButton(I18nControls.bindI18nProperties(new Button(), "Save"));
        saveButton.disableProperty().bind(updateStore.hasChangesProperty().not());
        saveButton.setOnAction(e -> {
            if (!validationSupportInitialised[0]) {
                FXProperties.runNowAndOnPropertiesChange(() -> {
                    if (I18n.getDictionary() != null) {
                        validationSupport.reset();
                    }
                }, I18n.dictionaryProperty());
                validationSupportInitialised[0] = true;
            }

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

        BorderPane.setAlignment(mainFrame,Pos.CENTER);
        BorderPane.setAlignment(mainLayout,Pos.CENTER);

        return ControlUtil.createVerticalScrollPaneWithPadding(10, mainFrame);
    }

    private BorderPane buildIndividualLinksContainer() {
        //TODO: when we know what will be the Item for VOD, we can remove perhaps the first request and make the necessary changes
        BorderPane container = new BorderPane();

        // Initialize the list that will hold the current item
        final ArrayList<Item> currentItem = new ArrayList<>();

        entityStore.executeQueryBatch(
                new EntityStoreQuery("select distinct name,family.code from Item where organization=? and family.code = '" + itemFamilyTypeCode + "' order by name",
                    new Object[] { FXEvent.getEvent().getOrganization() }),
                new EntityStoreQuery("select name, date, timeline.startTime, timeline.endTime, item.name, event, site from ScheduledItem where event= ? and item.family.code = 'teach' order by date",
                    new Object[] { FXEvent.getEvent() }),
                new EntityStoreQuery("select name, parent, date, event, site, expirationDate,available from ScheduledItem where parent.event= ? and item.family.code = '"+itemFamilyTypeCode+"' and parent.item.family.code = 'teach' order by date",
                    new Object[] { FXEvent.getEvent() }),
                new EntityStoreQuery("select url, scheduledItem.parent, scheduledItem.item, scheduledItem.date, published from Media where scheduledItem.event= ? and scheduledItem.item.family.code = '" + itemFamilyTypeCode + "'",
                    new Object[] { FXEvent.getEvent() })
            ).onFailure(Console::log)
            .onSuccess(entityList -> Platform.runLater(() -> {
                //TODO: when we know which Item we use for VOD, we change the code bellow
                EntityList<Item> VODItems = entityList[0];
                currentItem.add(VODItems.get(0));
                EntityList<ScheduledItem> teachingSIList = entityList[1];
                EntityList<ScheduledItem> childSIList = entityList[2];
                EntityList<Media> mediaList = entityList[3];

                // Update lists with data from the database
                teachingsScheduledItemsReadFromDatabase.setAll(teachingSIList);
                scheduledItemsLinkedToTeachingScheduledItemsReadFromDatabase.setAll(childSIList);
                recordingsMediasReadFromDatabase.setAll(mediaList);
                teachingsDates.setAll(teachingSIList.stream().map(EntityHasLocalDate::getDate).distinct().collect(Collectors.toList()));

                // Instantiate the MediaLinksForVODManagement with data
                MediaLinksForVODManagement languageLinkManagement = new MediaLinksForVODManagement(
                    VODItems.get(0), entityStore, teachingsDates, teachingsScheduledItemsReadFromDatabase,scheduledItemsLinkedToTeachingScheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase);

                // Now that the data is ready, update the container
                container.setCenter(languageLinkManagement.getContainer());
            }));

        // Return the placeholder container, which will be updated later
        return container;
    }

    public void setActive(boolean b) {
        activeProperty.set(b);
    }
}





