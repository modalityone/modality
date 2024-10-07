package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.webtext.HtmlTextEditor;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
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
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;

import static dev.webfx.extras.webtext.HtmlTextEditor.Mode.BASIC;


public class LiveStreamingView {
    private final MediasActivity activity;
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final ObservableList<LocalDate> teachingsDates = FXCollections.observableArrayList();
    private final ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase = FXCollections.observableArrayList();
    private final ObservableList<Media> recordingsMediasReadFromDatabase = FXCollections.observableArrayList();
    private Event currentEditedEvent;
    public LiveStreamingView(MediasActivity activity) {
        this.activity = activity;
    }


    public void startLogic() {
    }

    public Node buildContainer() {
        BorderPane mainFrame = new BorderPane();
        mainFrame.setPadding(new Insets(0,0,30,0));
        Label title = I18nControls.bindI18nProperties(new Label(), MediasI18nKeys.LiveStreamingTitle);
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

        Label liveMessageLabel = I18nControls.bindI18nProperties(new Label(), MediasI18nKeys.LiveInfoMessage);
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


        mainVBox.getChildren().addAll(globalLinkLine);

        mainFrame.setCenter(mainVBox);
        BorderPane.setAlignment(mainVBox,Pos.CENTER);

        return ControlUtil.createVerticalScrollPaneWithPadding(10, mainFrame);
    }

    public void setActive(boolean b) {
        activeProperty.set(b);
    }

}





