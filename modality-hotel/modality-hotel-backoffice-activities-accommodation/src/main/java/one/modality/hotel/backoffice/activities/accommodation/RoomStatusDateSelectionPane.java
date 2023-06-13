package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class RoomStatusDateSelectionPane extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-YY");

    private static final FontDef SELECT_PERIOD_FONT = FontDef.font(FontWeight.BOLD, 15);

    private final TextField fromTextField;
    private final TextField toTextField;

    public RoomStatusDateSelectionPane() {
        setAlignment(Pos.CENTER);
        setSpacing(16);
        Label selectPeriodLabel = new Label("Select your period");
        TextTheme.createPrimaryTextFacet(selectPeriodLabel)
                .requestedFont(SELECT_PERIOD_FONT)
                .style();

        String todayDateText = DATE_FORMATTER.format(LocalDate.now());

        GridPane fromToGridPane = new GridPane();
        Label fromLabel = new Label("From");
        Label toLabel = new Label("To");
        TextTheme.createDefaultTextFacet(fromLabel)
                .style();
        TextTheme.createDefaultTextFacet(toLabel)
                .style();
        fromTextField = new TextField(todayDateText);
        fromTextField.setPromptText("eg. 31-01-2000");
        toTextField = new TextField(todayDateText);
        toTextField.setPromptText("eg. 31-01-2000");
        fromToGridPane.add(fromLabel, 0, 0);
        fromToGridPane.add(fromTextField, 1, 0);
        fromToGridPane.add(toLabel, 0, 1);
        fromToGridPane.add(toTextField, 1, 1);

        getChildren().setAll(selectPeriodLabel, fromToGridPane);
    }

    public LocalDate getFrom() {
        String text = fromTextField.getText();
        try {
            return LocalDate.parse(text, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return LocalDate.parse("2022-01-01");
        }
    }

    public LocalDate getTo() {
        String text = toTextField.getText();
        try {
            return LocalDate.parse(text, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            //return LocalDate.parse("2050-01-01");
            return LocalDate.now().atStartOfDay().toLocalDate();
        }
    }
}
