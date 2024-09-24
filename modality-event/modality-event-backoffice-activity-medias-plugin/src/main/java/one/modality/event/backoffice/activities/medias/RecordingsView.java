package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
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
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class RecordingsView {
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private final ModalityValidationSupport validationSupport = new ModalityValidationSupport();
    private final boolean[] validationSupportInitialised = {false};
    private final MediasActivity activity;
    private Switch contentAvailableOfflineSwitch;
    private TextField contentExpirationDate;
    private final ObservableList<Item> workingItems = FXCollections.observableArrayList();
    private final Map<String, MediaLinksManagement> correspondenceBetweenLanguageAndLanguageLinkManagement = new HashMap<>();
    private final ObservableList<LocalDate> teachingsDates = FXCollections.observableArrayList();
    private final ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase = FXCollections.observableArrayList();
    private final ObservableList<Media> recordingsMediasReadFromDatabase = FXCollections.observableArrayList();
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final String itemFamilyTypeCode = "record";


    String lastLanguageSelected="";

    public RecordingsView(MediasActivity activity) {
        this.activity = activity;
    }


    public void startLogic() {

    }

    public Node buildContainer() {
        BorderPane mainFrame = new BorderPane();
        mainFrame.setPadding(new Insets(0,0,30,0));

        Label title = I18nControls.bindI18nProperties(new Label(), "RecordingsTitle");
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
        availableUntilCommentLabel.setPadding(new Insets(0,0,5,0));

        masterSettings.getChildren().add(availableUntilCommentLabel);

        contentExpirationDate = new TextField();
        contentExpirationDate.setPromptText("Format: 25-09-2025");
        validationSupport.addDateValidation(contentExpirationDate, "dd-MM-yyyy",contentExpirationDate,I18n.getI18nText("ValidationTimeFormatIncorrect"));
        masterSettings.getChildren().add(contentExpirationDate);


        contentAvailableOfflineSwitch = new Switch();

        Label availableOfflineLabel = I18nControls.bindI18nProperties(new Label(), "AvailableOffline");
        availableOfflineLabel.setPadding(new Insets(0,0,0,10));
        TextTheme.createSecondaryTextFacet(availableOfflineLabel).style();
        HBox offlineManagementHBox = new HBox(contentAvailableOfflineSwitch, availableOfflineLabel);
        offlineManagementHBox.setPadding(new Insets(20,0,20,0));
        offlineManagementHBox.setAlignment(Pos.CENTER_LEFT);
        masterSettings.getChildren().add(offlineManagementHBox);

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


        /* The language section */
        /* **********************/
        Label languageLabel = I18nControls.bindI18nProperties(new Label(), "SelectLanguage");
        languageLabel.getStyleClass().add(Bootstrap.STRONG);
        languageLabel.setPadding(new Insets(30,0,10,0));
        masterSettings.getChildren().add(languageLabel);

        entityStore.executeQueryBatch(
             new EntityStoreQuery("select distinct name from Item where organization=? and family.code = 'record' order by name", new Object[] { FXEvent.getEvent().getOrganization()}),
                      new EntityStoreQuery("select name, date, timeline.startTime, timeline.endTime, item.name, event, site from ScheduledItem where event= ? and item.family.code = 'teach' order by date", new Object[] { FXEvent.getEvent()}),
                    new EntityStoreQuery("select url, scheduledItem.parent, scheduledItem.item, scheduledItem.date, published from Media where scheduledItem.event= ? and scheduledItem.item.family.code = '"+itemFamilyTypeCode+"'", new Object[] { FXEvent.getEvent()}))
            .onFailure(Console::log)
            .onSuccess(entityList -> Platform.runLater(() -> {
                    EntityList<Item> itemList = entityList[0];
                    EntityList<ScheduledItem> siList = entityList[1];
                    EntityList<Media> mediaList = entityList[2];
                    workingItems.setAll(itemList);
                    //We have two lists of scheduled items, the teachings and the recordings (we suppose that for each recording ScheduledItem, we have a media associated in the database
                    teachingsScheduledItemsReadFromDatabase.setAll(siList);
                    recordingsMediasReadFromDatabase.setAll(mediaList);

                    teachingsDates.setAll(siList.stream().map(EntityHasLocalDate::getDate).distinct().collect(Collectors.toList()));

                    VBox languageListVBox = new VBox();
                    languageListVBox.setSpacing(10);
                    itemList.forEach(currentItem->{
                        HBox currentLanguageHBox = new HBox();
                        Label currentLanguageLabel = new Label(currentItem.getName());
                        MediaLinksManagement newLanguageLinksManagement = correspondenceBetweenLanguageAndLanguageLinkManagement.get(currentItem.getName());
                        TextTheme.createPrimaryTextFacet(currentLanguageLabel).style();
                        currentLanguageHBox.getChildren().add(currentLanguageLabel);
                        currentLanguageLabel.setOnMouseClicked(event -> {

                            MediaLinksManagement oldLanguage = correspondenceBetweenLanguageAndLanguageLinkManagement.get(lastLanguageSelected);
                            lastLanguageSelected = currentItem.getName();
                            if(oldLanguage!=null) oldLanguage.setVisible(false);
                            newLanguageLinksManagement.setVisible(true);
                        });
                        currentLanguageLabel.setCursor(Cursor.HAND);

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);
                        currentLanguageHBox.getChildren().add(spacer);

                        //Here we display the percentage of what has been entered.
                        // To do this, we filter the list of scheduledItem per language and date, and we do the same for the media,
                        // and we compare the number
                        IntegerProperty percentageProperty = new SimpleIntegerProperty();
                        StringProperty cssProperty = new SimpleStringProperty();
                        Label percentageLabel = new Label();
                        percentageLabel.textProperty().bind(Bindings.format("%d%%", percentageProperty));
                        percentageLabel.setPadding(new Insets(0,0,0,40));
                        currentLanguageHBox.getChildren().add(percentageLabel);

                        cssProperty.addListener((obs, oldClass, newClass) -> {
                            percentageLabel.getStyleClass().removeAll(oldClass); // Remove the old class
                            percentageLabel.getStyleClass().add(newClass);       // Add the new class
                        });

                        newLanguageLinksManagement.getRecordingsMediasReadFromDatabase().addListener((InvalidationListener) observable -> updatePercentageProperty(percentageProperty,cssProperty,currentItem));
                        //We call it manually the first time
                        updatePercentageProperty(percentageProperty,cssProperty,currentItem);
                        languageListVBox.getChildren().add(currentLanguageHBox);
                    });

                    masterSettings.getChildren().add(languageListVBox);
                }));

        // Main Section (Recordings Section)
        VBox recordingsSection = new VBox(10);
        recordingsSection.setPadding(new Insets(20));

        ObservableLists.bindConverted(recordingsSection.getChildren(),workingItems,this::drawLanguageBox);

        // Layout container (HBox)
        Separator VSeparator = new Separator();
        VSeparator.setOrientation(Orientation.VERTICAL);
        VSeparator.setPadding(new Insets(30));
        HBox mainLayout = new HBox(10, masterSettings, VSeparator, recordingsSection);
        BorderPane.setAlignment(mainLayout,Pos.CENTER);
        /////////////////
        mainFrame.setCenter(mainLayout);

        return ControlUtil.createVerticalScrollPaneWithPadding(10, mainFrame);
    }


    public void updatePercentageProperty(IntegerProperty percentageProperty, StringProperty cssProperty,Item currentItem) {
        long numberOfTeachingForThisDayAndLanguage = teachingsScheduledItemsReadFromDatabase.size();
        long numberOfMediaForThisDayAndLanguage = recordingsMediasReadFromDatabase.stream()
            .filter(media -> media.getScheduledItem().getItem().equals(currentItem))
            .count();

        // Calculate percentage, handling division by zero
        percentageProperty.set((int) (numberOfTeachingForThisDayAndLanguage == 0 ? 0 :
            (100.0 * numberOfMediaForThisDayAndLanguage / numberOfTeachingForThisDayAndLanguage)));

        if (percentageProperty.get() < 100) {
            cssProperty.setValue(Bootstrap.TEXT_DANGER );
        } else {
            cssProperty.setValue(Bootstrap.TEXT_SUCCESS);
        }
    }
    private Node drawLanguageBox(Item item) {
        MediaLinksManagement languageLinkManagement = new MediaLinksForRecordingsManagement(item,entityStore,teachingsDates,teachingsScheduledItemsReadFromDatabase,recordingsMediasReadFromDatabase);
        correspondenceBetweenLanguageAndLanguageLinkManagement.put(item.getName(),languageLinkManagement);
        languageLinkManagement.setVisible(false);
        return languageLinkManagement.getContainer();
        }

    public void setActive(boolean b) {
        activeProperty.set(b);
    }

}





