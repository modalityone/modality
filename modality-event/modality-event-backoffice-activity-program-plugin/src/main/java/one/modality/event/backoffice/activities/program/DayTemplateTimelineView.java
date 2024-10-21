package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Booleans;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Timeline;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.time.LocalTime;

import static one.modality.event.backoffice.activities.program.DatesToStringConversion.*;

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
    private final SVGPath audioAvailableIcon = SvgIcons.createSoundIconPath();
    private final SVGPath audioUnavailableIcon = SvgIcons.createSoundIconInactivePath();
    private final MonoPane audioMonoPane = new MonoPane();
    private final SVGPath videoAvailableIcon = SvgIcons.createVideoIconPath();
    private final SVGPath videoUnavailableIcon = SvgIcons.createVideoIconInactivePath();
    private final MonoPane videoMonoPane = new MonoPane();

    DayTemplateTimelineView(DayTemplateTimelineModel dayTemplateTimelineModel) {
        this.dayTemplateTimelineModel = dayTemplateTimelineModel;
        dayTemplateTimelineModel.setSyncUiFromModelRunnable(this::syncUiFromModel);
        view = buildUi();
    }

    Node getView() {
        return view;
    }

    private Timeline getTimeline() {
        return dayTemplateTimelineModel.getTimeline();
    }


    private ModalityValidationSupport getValidationSupport() {
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
        audioMonoPane.setContent(Booleans.isTrue(getTimeline().isAudioOffered()) ? audioAvailableIcon : audioUnavailableIcon);
    }

    private void syncVideoUiFromModel() {
        videoMonoPane.setContent(Booleans.isTrue(getTimeline().isVideoOffered()) ? videoAvailableIcon : videoUnavailableIcon);
    }

    private Region buildUi() {
        itemSelector = new EntityButtonSelector<Item>(
            "{class: 'Item', alias: 's', where: 'family.code=`teach`', orderBy :'name'}",
            this, FXMainFrameDialogArea.getDialogArea(), getTimeline().getStore().getDataSourceModel()
        )
            .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o));

        syncItemUiFromModel();
        FXProperties.runOnPropertiesChange(this::syncItemModelFromUi, itemSelector.selectedItemProperty());

        Button itemButton = itemSelector.getButton();
        getValidationSupport().addValidationRule(FXProperties.compute(itemSelector.selectedItemProperty(),
            s1 -> itemSelector.getSelectedItem() != null),
            itemButton,
            I18n.getI18nText(ProgramI18nKeys.ItemSelectedShouldntBeNull));

        fromTextField.setPromptText("8:46");
        fromTextField.setAlignment(Pos.CENTER);
        getValidationSupport().addValidationRule(FXProperties.compute(fromTextField.textProperty(),
            s1 -> isLocalTimeTextValid(fromTextField.getText())),
            fromTextField,
            I18n.getI18nText("ValidationTimeFormatIncorrect")); // Declared in Recurring activity!

        syncStartTimeUiFromModel();
        FXProperties.runOnPropertiesChange(this::syncStartTimeModelFromUi, fromTextField.textProperty());

        Label toLabel = I18nControls.bindI18nProperties(new Label(), ProgramI18nKeys.To);
        //TextTheme.createSecondaryTextFacet(subtitle).style();

        untilTextField.setPromptText("13:00");
        untilTextField.setAlignment(Pos.CENTER);
        getValidationSupport().addValidationRule(FXProperties.compute(untilTextField.textProperty(),
            s1 -> isLocalTimeTextValid(untilTextField.getText())),
            untilTextField,
            I18n.getI18nText("ValidationTimeFormatIncorrect")); // Declared in Recurring activity!

        syncEndTimeUiFromModel();
        FXProperties.runOnPropertiesChange(this::syncEndTimeModelFromUi, untilTextField.textProperty());

        nameTextField.setPromptText(ProgramI18nKeys.NameThisLine);
        syncNameUiFromModel();
        FXProperties.runOnPropertiesChange(this::syncNameModelFromUi, nameTextField.textProperty());

        audioAvailableIcon.setFill(Color.GREEN);
        audioUnavailableIcon.setFill(Color.RED);
        audioMonoPane.setAlignment(Pos.CENTER);
        audioMonoPane.setCursor(Cursor.HAND);
        syncAudioUiFromModel();
        audioMonoPane.setOnMouseClicked(e -> dayTemplateTimelineModel.toggleAudioOffered());

        videoAvailableIcon.setFill(Color.GREEN);
        videoUnavailableIcon.setFill(Color.RED);
        videoMonoPane.setCursor(Cursor.HAND);
        syncVideoUiFromModel();
        videoMonoPane.setOnMouseClicked(e -> dayTemplateTimelineModel.toggleVideoOffered());

        SVGPath trashImage = SvgIcons.createTrashSVGPath();
        MonoPane trashContainer = new MonoPane(trashImage);
        trashContainer.setCursor(Cursor.HAND);
        trashContainer.setOnMouseClicked(event -> dayTemplateTimelineModel.removeTemplateTimeLine());
        ShapeTheme.createSecondaryShapeFacet(trashImage).style();

        return new Pane(itemButton, fromTextField, toLabel, untilTextField, nameTextField, audioMonoPane, videoMonoPane, trashContainer) {
            private static final double FROM_WIDTH = 60, TO_WIDTH = 20, UNTIL_WIDTH = 60, AUDIO_WIDTH = 21, VIDEO_WIDTH = 24, TRASH_WIDTH = 20;
            private static final double HGAP = 5, TOTAL_HGAP = HGAP * 9;
            @Override
            protected void layoutChildren() {
                double width = getWidth(), height = getHeight();
                double remainingWidth = Math.max(0, width - (FROM_WIDTH + TO_WIDTH + UNTIL_WIDTH + AUDIO_WIDTH + VIDEO_WIDTH + TRASH_WIDTH + TOTAL_HGAP));
                double itemWidth = Math.max(100, remainingWidth * 0.4);
                double nameWidth = remainingWidth - itemWidth;
                double x = 0;
                layoutInArea(itemButton   ,  x +=              0 * HGAP, 0, itemWidth,  height, 0, HPos.LEFT,   VPos.CENTER);
                layoutInArea(fromTextField,  x += itemWidth  + 2 * HGAP, 0, FROM_WIDTH,  height, 0, HPos.CENTER, VPos.CENTER);
                layoutInArea(toLabel,        x += FROM_WIDTH + 1 * HGAP   , 0, TO_WIDTH,    height, 0, HPos.CENTER, VPos.CENTER);
                layoutInArea(untilTextField, x += TO_WIDTH + 1 * HGAP   , 0, UNTIL_WIDTH, height, 0, HPos.CENTER, VPos.CENTER);
                layoutInArea(nameTextField,  x += UNTIL_WIDTH + 2 * HGAP, 0, nameWidth,  height, 0, HPos.LEFT,   VPos.CENTER);
                layoutInArea(audioMonoPane,  x += nameWidth  + 1 * HGAP, 0, AUDIO_WIDTH, height, 0, HPos.CENTER, VPos.CENTER);
                layoutInArea(videoMonoPane,  x += AUDIO_WIDTH + 1 * HGAP, 0, VIDEO_WIDTH, height, 0, HPos.CENTER, VPos.CENTER);
                layoutInArea(trashContainer, x += VIDEO_WIDTH + 1 * HGAP, 0, TRASH_WIDTH, height, 0, HPos.CENTER, VPos.CENTER);
            }
        };
    }
}
