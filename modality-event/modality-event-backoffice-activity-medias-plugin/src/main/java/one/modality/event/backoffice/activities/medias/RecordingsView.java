package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class RecordingsView {
    private final MediasActivity activity;

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
        title.getStyleClass().add("title");
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);

        /////////////////
        VBox masterSettings = new VBox(10);
        masterSettings.setPadding(new Insets(20));

        Label masterLabel = new Label("Master settings");
        CheckBox contentAvailableCheck = new CheckBox("Content available Until");
        TextField daysTextField = new TextField();
        daysTextField.setPromptText("Days");
        CheckBox availableOfflineCheck = new CheckBox("Available offline");

        Label languageLabel = new Label("Select the language before entering the links");
        ListView<String> languagesList = new ListView<>();
        languagesList.getItems().addAll("English", "Spanish", "French", "Portuguese", "Italian",
            "German", "Chinese", "Cantonese", "Russian", "Israeli");
        languagesList.setPrefHeight(200);

        masterSettings.getChildren().addAll(masterLabel, contentAvailableCheck, daysTextField,
            availableOfflineCheck, languageLabel, languagesList);

        // Main Section (Recordings Section)
        VBox recordingsSection = new VBox(10);
        recordingsSection.setPadding(new Insets(20));

        Label recordingsLabel = new Label("Recordings");

        // Create a VBox to hold the date entries
        VBox dateEntries = new VBox(10);
        for (int i = 1; i <= 7; i++) {
            HBox dayEntry = createDayEntry("Day " + i, "Links not entered");
            dateEntries.getChildren().add(dayEntry);
        }

        // Example Expanded Section for Day 7 (you can toggle visibility of these dynamically)
        VBox expandedSection = new VBox(10);
        expandedSection.setPadding(new Insets(10));
        expandedSection.setStyle("-fx-background-color: #E8E8E8;");

        Label sessionLabel = new Label("Meditation session");
        TextField meditationLink = new TextField();
        meditationLink.setPromptText("Link");

        Label teaching1Label = new Label("Teaching 1");
        TextField teaching1Link = new TextField();
        teaching1Link.setPromptText("Link");

        Label teaching2Label = new Label("Teaching 2");
        TextField teaching2Link = new TextField();
        teaching2Link.setPromptText("Link");

        Label prayersLabel = new Label("Chanted prayers");
        TextField prayersLink = new TextField();
        prayersLink.setPromptText("Link");

        CheckBox offlineAvailable = new CheckBox("Available offline");
        HBox contentAvailableBox = new HBox(10, new CheckBox(), new Label("Content available for"), new TextField("Days"));
        Button confirmButton = new Button("Confirm");

        expandedSection.getChildren().addAll(sessionLabel, meditationLink, teaching1Label, teaching1Link,
            teaching2Label, teaching2Link, prayersLabel, prayersLink,
            offlineAvailable, contentAvailableBox, confirmButton);

        recordingsSection.getChildren().addAll(recordingsLabel, dateEntries, expandedSection);

        // Layout container (HBox)
        HBox mainLayout = new HBox(10, masterSettings, recordingsSection);
        BorderPane.setAlignment(mainLayout,Pos.CENTER);
        /////////////////
        mainFrame.setCenter(mainLayout);

        return ControlUtil.createVerticalScrollPaneWithPadding(10, mainFrame);
    }


    private HBox createDayEntry(String day, String status) {
        HBox dayEntry = new HBox(10);
        dayEntry.setPadding(new Insets(5));
        dayEntry.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC;");

        Label dayLabel = new Label(day);
        TextField introText = new TextField("Introduction");
        introText.setPrefWidth(200);

        Label statusLabel = new Label(status);
        Button editButton = new Button("+");

        dayEntry.getChildren().addAll(new Label("Date"), dayLabel, introText, statusLabel, editButton);
        return dayEntry;
    }
    public void setActive(boolean b) {
    }
}





