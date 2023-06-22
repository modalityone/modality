package one.modality.event.frontoffice.activities.app;

import dev.webfx.stack.i18n.I18n;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import one.modality.base.frontoffice.states.GeneralPM;
import one.modality.base.frontoffice.utility.GeneralUtility;

import java.util.ArrayList;

public class BottomButtons extends Application {

    private Button createBottomButton(String key) {
        Button button = GeneralUtility.bindI18N(new Button(), key);

        button.setContentDisplay(ContentDisplay.TOP);

        button.setPrefHeight(100);
        button.setMaxWidth(1000);
        button.setMinWidth(0);
        button.setAlignment(Pos.CENTER);
        button.getProperties().put("button-name", key);

        return button;
    }

    private void setPage(Node selectedButton, ArrayList<Button> buttons) {
        buttons.stream().forEach(c -> c.setOpacity(0.3d));
//        selectedButton.setOpacity(1.0d);
//        switch (selectedButton.getProperties().get("button-name").toString()) {
//            case "Home":
//                GeneralPM.SELECTED_PAGE.set(HomePage.createPage());
//                break;
//            case "Booking":
////                GeneralPM.SELECTED_PAGE.set(BookingHome.createPage());createPage
//                break;
//            case "Alerts":
//                GeneralPM.SELECTED_PAGE.set(AlertsHome.createPage());
//                break;
//            case "Account":
//                //GeneralPM.SELECTED_PAGE.set(AccountSettings.createPage());
//                break;
//        }
    }

    public Node createLanguagesOption() {
        Button lang = new Button(I18n.getLanguage().toString());
        BorderPane bp = new BorderPane();

        bp.setTop(lang);
        bp.bottomProperty().bind(GeneralPM.LANGUAGE_BUTTON_OPTIONS);

        lang.setOnAction(e -> {
            VBox b = new VBox();
            I18n.getSupportedLanguages().stream().forEach(l -> {
                Button btn = new Button(l.toString());
                b.getChildren().add(btn);
                btn.setOnAction(ee -> {
                    I18n.setLanguage(l.toString());
                    lang.setText(I18n.getLanguage().toString());
                    GeneralPM.LANGUAGE_BUTTON_OPTIONS.set(new VBox());
                });
            });

            GeneralPM.LANGUAGE_BUTTON_OPTIONS.set(b);
        });

        return bp;
    }

    @Override
    public void start(Stage stage) throws Exception {BorderPane bp = new BorderPane();
        I18n.setLanguage("en");
        GridPane buttonsGridPane = new GridPane();

        Button homeButton = createBottomButton("Home");
        Button bookingButton = createBottomButton("Booking");
        Button alertsButton = createBottomButton("Alerts");
        Button accountButton = createBottomButton("Account");

        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(homeButton);
        buttons.add(bookingButton);
        buttons.add(alertsButton);
        buttons.add(accountButton);

        setPage(homeButton, buttons);

        buttons.stream().forEach(b -> {
            b.setOnAction(e -> {setPage(b, buttons);});
        });

        buttonsGridPane.add(homeButton, 0, 0);
        buttonsGridPane.add(bookingButton, 1, 0);
        buttonsGridPane.add(alertsButton, 2, 0);
        buttonsGridPane.add(accountButton, 3, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        ColumnConstraints col4 = new ColumnConstraints();

        col1.setPercentWidth(25);
        col2.setPercentWidth(25);
        col3.setPercentWidth(25);
        col4.setPercentWidth(25);

        buttonsGridPane.getColumnConstraints().addAll(
                col1, col2, col3, col4
        );

        bp.setTop(createLanguagesOption());
        bp.setBottom(buttonsGridPane);
//        bp.centerProperty().bind(GeneralPM.SELECTED_PAGE);
        bp.setOnMouseClicked(e -> {
            GeneralPM.LANGUAGE_BUTTON_OPTIONS.set(new VBox());
        });

        stage.setScene(new Scene(bp));
        stage.setMinWidth(400);
        stage.setMinHeight(800);
        stage.show();
    }
}
