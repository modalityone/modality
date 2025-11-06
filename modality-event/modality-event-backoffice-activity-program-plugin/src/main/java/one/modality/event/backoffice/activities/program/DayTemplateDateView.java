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
 * View component representing a single selected date in the day template date list.
 * This view displays a single date that has been selected for a day template, along with
 * a delete button to remove the date from the selection. It's used within {@link DayTemplateView}
 * to show the list of dates assigned to a specific day template.
 *
 * <p>The component displays:
 * <ul>
 *   <li>A trash icon button for removing the date</li>
 *   <li>The date formatted in localized month/day format (e.g., "Jan 15" or "15 janv.")</li>
 * </ul>
 *
 * <p>Visual layout: [🗑️] Jan 15
 *
 * <p>The date format is automatically localized based on the user's language preference
 * using {@link LocalizedTime#formatMonthDayProperty}.
 *
 * @author David Hello
 * @author Bruno Salmon
 *
 * @see DayTemplateView
 * @see DatePicker
 */
final class DayTemplateDateView {

    /**
     * The date being displayed by this view component.
     */
    private final LocalDate date;

    /**
     * Reference to the parent DatePicker component.
     * Used to remove this date from the selection when the trash button is clicked.
     */
    private final DatePicker datePicker;

    /**
     * The root HBox container for this view component.
     * Contains the trash icon and the formatted date text.
     */
    private final HBox view;

    /**
     * Constructs a new DayTemplateDateView for displaying a selected date.
     *
     * @param date The date to display (must not be null)
     * @param datePicker The parent DatePicker component, used for removing the date from selection
     */
    DayTemplateDateView(LocalDate date, DatePicker datePicker) {
        this.date = date;
        this.datePicker = datePicker;
        view = buildUi();
    }

    /**
     * Returns the root JavaFX node for this view component.
     *
     * @return The HBox containing the trash icon and date text
     */
    Node getView() {
        return view;
    }

    /**
     * Builds the user interface for this date view component.
     * Creates an HBox containing:
     * <ul>
     *   <li>A gray trash icon that removes this date from the DatePicker's selection when clicked</li>
     *   <li>A text node displaying the date in localized month/day format</li>
     * </ul>
     *
     * The date text automatically updates if the user changes their language preference,
     * thanks to the reactive binding with {@link LocalizedTime#formatMonthDayProperty}.
     *
     * @return Configured HBox with trash icon and formatted date text
     */
    private HBox buildUi() {
        // Create trash icon button that removes this date from the selection
        SVGPath trashDate = SvgIcons.armButton(SvgIcons.createTrashSVGPath(), () -> datePicker.getSelectedDates().remove(date));
        ShapeTheme.createSecondaryShapeFacet(trashDate).style(); // Apply gray secondary theme color

        // Create date text with localized format (e.g., "Jan 15" in English, "15 janv." in French)
        Text dateText = new Text();
        dateText.textProperty().bind(LocalizedTime.formatMonthDayProperty(date, BackOfficeTimeFormats.PROGRAM_DAY_TEMPLATE_MONTH_DAY_FORMAT));

        // Combine trash icon and date text in horizontal layout
        HBox hBox = new HBox(10, trashDate, dateText);  // 10px spacing between elements
        hBox.setAlignment(Pos.CENTER_LEFT);
        return hBox;
    }
}
