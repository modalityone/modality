package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.time.pickers.DatePicker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.BackOfficeTimeFormats;

import java.time.LocalDate;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class DayTemplateDateView {

    private final LocalDate date;
    private final DatePicker datePicker;

    private final HBox view;

    DayTemplateDateView(LocalDate date, DatePicker datePicker) {
        this.date = date;
        this.datePicker = datePicker;
        view = buildUi();
    }

    Node getView() {
        return view;
    }

    private HBox buildUi() {
        SVGPath trashDate = SvgIcons.armButton(SvgIcons.createTrashSVGPath(), () -> datePicker.getSelectedDates().remove(date));
        ShapeTheme.createSecondaryShapeFacet(trashDate).style(); // Make it gray

        Text dateText = new Text();
        dateText.textProperty().bind(LocalizedTime.formatMonthDayProperty(date, BackOfficeTimeFormats.PROGRAM_DAY_TEMPLATE_MONTH_DAY_FORMAT));

        HBox hBox = new HBox(10, trashDate, dateText);
        hBox.setAlignment(Pos.CENTER_LEFT);
        return hBox;
    }
}
