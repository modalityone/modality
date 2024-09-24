package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.webtext.HtmlTextEditor;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.util.stream.Collectors;

import static dev.webfx.extras.webtext.HtmlTextEditor.Mode.BASIC;


public class LiveStreamingView {
    private final MediasActivity activity;
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final ObservableList<LocalDate> teachingsDates = FXCollections.observableArrayList();
    private final ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase = FXCollections.observableArrayList();
    private final ObservableList<Media> recordingsMediasReadFromDatabase = FXCollections.observableArrayList();
    private final String itemFamilyTypeCode = "record";

    public LiveStreamingView(MediasActivity activity) {
        this.activity = activity;
    }


    public void startLogic() {
    }

    public Node buildContainer() {
        BorderPane mainFrame = new BorderPane();
        mainFrame.setPadding(new Insets(0,0,30,0));
        Label title = I18nControls.bindI18nProperties(new Label(), "LiveStreamingTitle");
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add(Bootstrap.H2);
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);

        int maxWith = 1000;
        VBox mainVBox = new VBox();
        mainVBox.setSpacing(20);
        mainVBox.setAlignment(Pos.CENTER);
        mainVBox.setMaxWidth(maxWith);

        HtmlTextEditor liveMessageHTMLEditor = new HtmlTextEditor();
        liveMessageHTMLEditor.setMode(BASIC);
        liveMessageHTMLEditor.setPrefHeight(270);

        Label liveMessageLabel = I18nControls.bindI18nProperties(new Label(), "LiveInfoMessage");
        liveMessageLabel.setTextFill(Color.WHITE);
        liveMessageLabel.getStyleClass().add(Bootstrap.STRONG);

        TextField titleTextField = new TextField();
        titleTextField.setPromptText(I18n.getI18nText("Title"));
        titleTextField.setMinWidth(700);

        Region spacer = new Region();
        HBox.setHgrow(spacer,Priority.ALWAYS);
        Button publishMessageButton = Bootstrap.successButton(new Button(I18n.getI18nText("PublishMessage")));
        publishMessageButton.setOnAction(event -> {
            //TODO: Here we call a translation service to translate in the different language, and we display on the front end user the message
        });

        HBox firstLine = new HBox(titleTextField,spacer,publishMessageButton);
        firstLine.setAlignment(Pos.CENTER_LEFT);

        VBox liveMessageLabelVBox = new VBox();
        liveMessageLabelVBox.setSpacing(10);
        liveMessageLabelVBox.setAlignment(Pos.CENTER_LEFT);
        liveMessageLabelVBox.getChildren().addAll(liveMessageLabel,firstLine,liveMessageHTMLEditor);
        liveMessageLabelVBox.setPadding(new Insets(40));

        MonoPane liveMessageContainer = new MonoPane(liveMessageLabelVBox);

        liveMessageContainer.setBackground(new Background(new BackgroundFill(
            Color.web("0096D6"), new CornerRadii(10), Insets.EMPTY // Match CornerRadii of border
        )));


        mainVBox.getChildren().add(liveMessageContainer);

        HtmlTextEditor explanationHTMLEditor = new HtmlTextEditor();
        explanationHTMLEditor.setMode(HtmlTextEditor.Mode.STANDARD);
        explanationHTMLEditor.setPrefHeight(450);
        Label explanationLabel = I18nControls.bindI18nProperties(new Label(), "ExplanationText");
       // HBox explanationHBox = new HBox(explanationLabel,explanationHTMLEditor);
        mainVBox.getChildren().addAll(new HBox(explanationLabel),explanationHTMLEditor);

        Label areWeUsingIndividualLinksForEachSessionLabel = I18nControls.bindI18nProperties(new Label(), "AreWeUsingIndividualLinksForEachSession");
        Switch areWeUsingIndividualLinksForEachSessionSwitch = new Switch();

        HBox individualLinksHBox = new HBox();
        individualLinksHBox.setSpacing(10);
        individualLinksHBox.getChildren().addAll(areWeUsingIndividualLinksForEachSessionLabel,areWeUsingIndividualLinksForEachSessionSwitch);
        mainVBox.getChildren().add(individualLinksHBox);

        HBox globalLinkLine = new HBox();
        globalLinkLine.setAlignment(Pos.CENTER_LEFT);
        Label liveStreamGlobalLink = I18nControls.bindI18nProperties(new Label(), "LiveStreamGlobalLink");
        liveStreamGlobalLink.setPadding(new Insets(0,10,0,0));
        // liveStreamGlobalLink.setMinWidth(labelMinWith);
        globalLinkLine.getChildren().add(liveStreamGlobalLink);
        TextField globalLinkTextField = new TextField();
        globalLinkTextField.setMinWidth(600);
        globalLinkLine.getChildren().add(globalLinkTextField);

        BorderPane individualSettingsHBox = buildIndividualSettingsContainer();

        //The bindings of the visibility and manage property to the switch
        individualSettingsHBox.visibleProperty().bind(areWeUsingIndividualLinksForEachSessionSwitch.selectedProperty());
        individualSettingsHBox.managedProperty().bind(areWeUsingIndividualLinksForEachSessionSwitch.selectedProperty());
        globalLinkLine.visibleProperty().bind(areWeUsingIndividualLinksForEachSessionSwitch.selectedProperty().not());
        globalLinkLine.managedProperty().bind(areWeUsingIndividualLinksForEachSessionSwitch.selectedProperty().not());


        mainVBox.getChildren().addAll(globalLinkLine,individualSettingsHBox);

        mainFrame.setCenter(mainVBox);
        BorderPane.setAlignment(mainVBox,Pos.CENTER);

        return ControlUtil.createVerticalScrollPaneWithPadding(10, mainFrame);
    }

    private BorderPane buildIndividualSettingsContainer() {
        entityStore.executeQueryBatch(
                new EntityStoreQuery("select name, date, timeline.startTime, timeline.endTime, item.name, event, site from ScheduledItem where event= ? and item.family.code = 'teach' order by date", new Object[] { FXEvent.getEvent()}),
                new EntityStoreQuery("select url, scheduledItem.parent, scheduledItem.item, scheduledItem.date, published from Media where scheduledItem.event= ? and scheduledItem.item.family.code = '"+itemFamilyTypeCode+"'", new Object[] { FXEvent.getEvent()}))
            .onFailure(Console::log)
            .onSuccess(entityList -> Platform.runLater(() -> {
                EntityList<ScheduledItem> siList = entityList[0];
                EntityList<Media> mediaList = entityList[1];
                //We have two lists of scheduled items, the teachings and the recordings (we suppose that for each recording ScheduledItem, we have a media associated in the database
                teachingsScheduledItemsReadFromDatabase.setAll(siList);
                recordingsMediasReadFromDatabase.setAll(mediaList);
                teachingsDates.setAll(siList.stream().map(EntityHasLocalDate::getDate).distinct().collect(Collectors.toList()));
            }));
                MediaLinksManagement languageLinkManagement = new MediaLinksForLiveStreamingManagement(entityStore,teachingsDates,teachingsScheduledItemsReadFromDatabase,recordingsMediasReadFromDatabase);
                return languageLinkManagement.getContainer();
    }

    public void setActive(boolean b) {
        activeProperty.set(b);
    }
}





