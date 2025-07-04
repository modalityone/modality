package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.panes.HPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Booleans;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.validation.ValidationSupport;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Timeline;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.time.LocalTime;
import java.util.Objects;

import static one.modality.event.backoffice.activities.program.DatesToStringConversion.isLocalTimeTextValid;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class DayTemplateTimelineView implements ButtonFactoryMixin {

    private final DayTemplateTimelineModel dayTemplateTimelineModel;

    private final Region view;
    private ButtonSelector<Item> itemSelector;
    private final TextField fromTextField = new TextField();
    private final TextField untilTextField = new TextField();
    private final TextField nameTextField = new TextField();
    private final SVGPath audioAvailableIcon = SvgIcons.setSVGPathFill(SvgIcons.createSoundIconPath(), Color.GREEN);
    private final SVGPath audioUnavailableIcon = SvgIcons.setSVGPathFill(SvgIcons.createSoundIconInactivePath(), Color.RED);
    private final MonoPane audioToggleButton;
    private final SVGPath videoAvailableIcon = SvgIcons.setSVGPathFill(SvgIcons.createVideoIconPath(), Color.GREEN);
    private final SVGPath videoUnavailableIcon = SvgIcons.setSVGPathFill(SvgIcons.createVideoIconInactivePath(), Color.RED);
    private final MonoPane videoToggleButton;

    DayTemplateTimelineView(DayTemplateTimelineModel dayTemplateTimelineModel) {
        this.dayTemplateTimelineModel = dayTemplateTimelineModel;
        dayTemplateTimelineModel.setSyncUiFromModelRunnable(this::syncUiFromModel);
        Timeline timeline = dayTemplateTimelineModel.getTimeline();
        audioToggleButton = SvgIcons.createToggleButtonPane(audioAvailableIcon, audioUnavailableIcon, timeline::isAudioOffered, timeline::setAudioOffered);
        videoToggleButton = SvgIcons.createToggleButtonPane(videoAvailableIcon, videoUnavailableIcon, timeline::isVideoOffered, timeline::setVideoOffered);
        view = buildUi();
    }

    Node getView() {
        return view;
    }

    private Timeline getTimeline() {
        return dayTemplateTimelineModel.getTimeline();
    }


    private ValidationSupport getValidationSupport() {
        return dayTemplateTimelineModel.getValidationSupport();
    }

    private void syncUiFromModel() {
        syncItemUiFromModel();
        syncStartTimeUiFromModel();
        syncEndTimeUiFromModel();
        syncNameUiFromModel();
        syncAudioUiFromModel();
        syncVideoUiFromModel();
    }

    /* Not necessary
    private void syncModelFromUi() {
        syncItemModelFromUi();
        syncStartTimeModelFromUi();
        syncEndTimeModelFromUi();
        syncNameModelFromUi();
        // syncAudioModelFromUi();
    }*/

    private void syncItemUiFromModel() {
        itemSelector.setSelectedItem(getTimeline().getItem());
    }

    private void syncItemModelFromUi() {
        getTimeline().setItem(itemSelector.getSelectedItem());
    }

    private void syncStartTimeUiFromModel() {
        LocalTime startTime = getTimeline().getStartTime();
        if (startTime != null)
            fromTextField.setText(startTime.format(DatesToStringConversion.TIME_FORMATTER));
    }

    private void syncStartTimeModelFromUi() {
        String text = fromTextField.getText();
        if (isLocalTimeTextValid(text)) {
            getTimeline().setStartTime(LocalTime.parse(text));
        }
    }

    private void syncEndTimeUiFromModel() {
        LocalTime endTime = getTimeline().getEndTime();
        if (endTime != null)
            untilTextField.setText(endTime.format(DatesToStringConversion.TIME_FORMATTER));
    }

    private void syncEndTimeModelFromUi() {
        String text = untilTextField.getText();
        if (isLocalTimeTextValid(text)) {
            getTimeline().setEndTime(LocalTime.parse(text));
        }
    }

    private void syncNameUiFromModel() {
        String name = getTimeline().getName();
        if (name != null)
            nameTextField.setText(name);
    }

    private void syncNameModelFromUi() {
        String name = nameTextField.getText();
        if (name != null)
            getTimeline().setName(name);
    }

    private void syncAudioUiFromModel() {
        audioToggleButton.setContent(Booleans.isTrue(getTimeline().isAudioOffered()) ? audioAvailableIcon : audioUnavailableIcon);
    }

    private void syncVideoUiFromModel() {
        videoToggleButton.setContent(Booleans.isTrue(getTimeline().isVideoOffered()) ? videoAvailableIcon : videoUnavailableIcon);
    }

    private Region buildUi() {
        itemSelector = new EntityButtonSelector<Item>(
            "{class: 'Item', alias: 's', where: 'family.code=`teach`', orderBy :'name'}",
            this, FXMainFrameDialogArea.getDialogArea(), getTimeline().getStore().getDataSourceModel()
        )
            .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o));

        syncItemUiFromModel();
        FXProperties.runOnPropertyChange(this::syncItemModelFromUi, itemSelector.selectedItemProperty());

        Button itemButton = itemSelector.getButton();
        getValidationSupport().addValidationRule(itemSelector.selectedItemProperty().map(Objects::nonNull),
            itemButton,
            I18n.i18nTextProperty(ProgramI18nKeys.ItemSelectedShouldntBeNull));

        fromTextField.setPromptText("8:46");
        fromTextField.setAlignment(Pos.CENTER);
        getValidationSupport().addValidationRule(fromTextField.textProperty().map(DatesToStringConversion::isLocalTimeTextValid),
            fromTextField,
            I18n.i18nTextProperty("ValidationTimeFormatIncorrect")); // Declared in Recurring activity!

        syncStartTimeUiFromModel();
        FXProperties.runOnPropertyChange(this::syncStartTimeModelFromUi, fromTextField.textProperty());

        Label toLabel = I18nControls.newLabel(ProgramI18nKeys.To);
        //TextTheme.createSecondaryTextFacet(subtitle).style();

        untilTextField.setPromptText("13:00");
        untilTextField.setAlignment(Pos.CENTER);
        getValidationSupport().addValidationRule(untilTextField.textProperty().map(DatesToStringConversion::isLocalTimeTextValid),
            untilTextField,
            I18n.i18nTextProperty("ValidationTimeFormatIncorrect")); // Declared in Recurring activity!

        syncEndTimeUiFromModel();
        FXProperties.runOnPropertyChange(this::syncEndTimeModelFromUi, untilTextField.textProperty());

        I18nControls.bindI18nPromptProperty(nameTextField, ProgramI18nKeys.NameThisLine);
        syncNameUiFromModel();
        FXProperties.runOnPropertyChange(this::syncNameModelFromUi, nameTextField.textProperty());

        MonoPane trashButton = SvgIcons.createButtonPane(SvgIcons.createTrashSVGPath(), dayTemplateTimelineModel::removeTemplateTimeLine);
        ShapeTheme.createSecondaryShapeFacet(trashButton).style(); // Make it gray

        return new HPane(itemButton, fromTextField, toLabel, untilTextField, nameTextField, audioToggleButton, videoToggleButton, trashButton) {
            private static final double FROM_WIDTH = 60, TO_WIDTH = 20, UNTIL_WIDTH = 60, AUDIO_WIDTH = 21, VIDEO_WIDTH = 24, TRASH_WIDTH = 20;
            private static final double HGAP = 5, TOTAL_HGAP = HGAP * 9;
            @Override
            protected void layoutChildren(double width, double height) {
                double remainingWidth = Math.max(0, width - (FROM_WIDTH + TO_WIDTH + UNTIL_WIDTH + AUDIO_WIDTH + VIDEO_WIDTH + TRASH_WIDTH + TOTAL_HGAP));
                double itemWidth = Math.max(100, remainingWidth * 0.4);
                double nameWidth = remainingWidth - itemWidth;
                double x = 0;
                layoutInArea(itemButton   ,  x +=              0 * HGAP, 0, itemWidth,  height, Pos.CENTER_LEFT);
                layoutInArea(fromTextField,  x += itemWidth  + 2 * HGAP, 0, FROM_WIDTH,  height, Pos.CENTER);
                layoutInArea(toLabel,        x += FROM_WIDTH + 1 * HGAP   , 0, TO_WIDTH,    height, Pos.CENTER);
                layoutInArea(untilTextField, x += TO_WIDTH + 1 * HGAP   , 0, UNTIL_WIDTH, height, Pos.CENTER);
                layoutInArea(nameTextField,  x += UNTIL_WIDTH + 2 * HGAP, 0, nameWidth,  height, Pos.CENTER_LEFT);
                layoutInArea(audioToggleButton,  x += nameWidth + 1 * HGAP, 0, AUDIO_WIDTH, height, Pos.CENTER);
                layoutInArea(videoToggleButton,  x += AUDIO_WIDTH + 1 * HGAP, 0, VIDEO_WIDTH, height, Pos.CENTER);
                layoutInArea(trashButton, x += VIDEO_WIDTH + 1 * HGAP, 0, TRASH_WIDTH, height, Pos.CENTER);
            }
        };
    }
}
