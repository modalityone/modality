package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.time.pickers.DatePicker;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import one.modality.base.client.icons.SvgIcons;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class DayTemplateDateView {

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");

    private final LocalDate date;
    private final DatePicker datePicker;

    private final BorderPane view;

    DayTemplateDateView(LocalDate date, DatePicker datePicker) {
        this.date = date;
        this.datePicker = datePicker;
        view = buildUi();
    }

    Node getView() {
        return view;
    }

    private BorderPane buildUi() {
        SVGPath trashDate = SvgIcons.createTrashSVGPath();
        trashDate.setTranslateY(2);
        Text currentDateValue = new Text(date.format(DATE_TIME_FORMATTER));
        trashDate.setOnMouseClicked(event -> datePicker.getSelectedDates().remove(date));
        ShapeTheme.createSecondaryShapeFacet(trashDate).style();
        BorderPane currentLineBorderPane = new BorderPane();
        BorderPane.setMargin(currentDateValue, new Insets(0, 20, 0, 10));
        currentLineBorderPane.setLeft(trashDate);
        currentLineBorderPane.setCenter(currentDateValue);
        currentLineBorderPane.setPadding(new Insets(0, 0, 3, 0));
        return currentLineBorderPane;
    }
}
