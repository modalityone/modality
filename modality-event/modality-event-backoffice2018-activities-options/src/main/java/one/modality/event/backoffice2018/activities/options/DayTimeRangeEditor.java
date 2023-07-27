package one.modality.event.backoffice2018.activities.options;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import one.modality.hotel.shared2018.businessdata.time.DayTimeRange;
import one.modality.event.client2018.businessdata.calendar.CalendarTimeline;
import one.modality.event.client2018.controls.calendargraphic.impl.DayColumnBodyBlockViewModel;
import one.modality.event.client2018.controls.calendargraphic.impl.DayColumnHeaderViewModel;
import dev.webfx.stack.ui.controls.dialog.DialogCallback;
import dev.webfx.stack.ui.controls.dialog.DialogUtil;
import dev.webfx.stack.ui.controls.dialog.GridPaneBuilder;

import java.util.function.BiConsumer;

/**
 * @author Bruno Salmon
 */
final class DayTimeRangeEditor {

    static void showDayTimeRangeEditorDialog(DayTimeRange dayTimeRange, long epochDay, CalendarTimeline timeline, BiConsumer<DayTimeRange, DialogCallback> okConsumer, Node parentOwner) {
        showDayTimeRangeInternDialog(dayTimeRange, epochDay, timeline, okConsumer, parentOwner);
    }

    private static void showDayTimeRangeInternDialog(DayTimeRange dayTimeRange, long epochDay, CalendarTimeline timeline, BiConsumer<DayTimeRange, DialogCallback> okConsumer, Node parentOwner) {
        DayTimeRange.TimeRangeRule generalRule = dayTimeRange.getGeneralRule();
        DayTimeRange.TimeRangeRule ruleForDay = dayTimeRange.getRuleForDay(epochDay);

        String generalText = generalRule.getDayTimeSeries().toText();
        String exceptionText = ruleForDay.getDayTimeSeries().toText();

        TextField generalTextField = new TextField(generalText);
        CheckBox exceptionCheckBox = new CheckBox();
        TextField exceptionTextField = new TextField(exceptionText);
        Button okButton = new Button();
        Button cancelButton = new Button();

        boolean hasException = !exceptionText.equals(generalText);
        exceptionCheckBox.setSelected(hasException);

        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(new GridPaneBuilder()
                        .addNodeFillingRow(new DayColumnBodyBlockViewModel(null, epochDay, generalRule.getDayTimeInterval(), timeline, true).getNode(), 2)
                        .addLabelTextInputRow("Hours", generalTextField)
                        .addNodeFillingRow(20, new DayColumnHeaderViewModel(epochDay).getNode())
                        .addCheckBoxTextInputRow("Exception", exceptionCheckBox, exceptionTextField)
                        .addButtons("Ok", okButton, "Cancel", cancelButton)
                        .build(),
                (Pane) parentOwner);

        okButton.setOnAction(e -> {
            String newGeneralText = generalTextField.getText();
            String newExceptionText = exceptionTextField.getText();
            DayTimeRange newDayTimeRange = dayTimeRange.changeGeneralRule(newGeneralText);
            boolean newHasException = exceptionCheckBox.isSelected() && !newExceptionText.equals(newGeneralText);
            if (newHasException != hasException || newHasException && !newExceptionText.equals(exceptionText)) {
                if (newHasException)
                    newDayTimeRange = newDayTimeRange.addExceptionRuleForDay(epochDay, newExceptionText);
                else
                    newDayTimeRange = newDayTimeRange.removeExceptionRuleForDay(epochDay);
            }
            okConsumer.accept(newDayTimeRange, dialogCallback);
        });
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());
    }
}
