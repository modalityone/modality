package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Item;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import static one.modality.event.backoffice.activities.program.ProgramCssSelectors.*;

/**
 * UI component for Step 1 of the program setup: Preliminary Bookable Items Configuration.
 *
 * This step allows the user to:
 * - Select the item type for bookable teaching sessions
 * - Generate preliminary scheduled items in the database
 *
 * @author David Hello
 */
final class ProgramStep1View {

    private static final double MAX_WIDTH = 1600;
    private final ProgramModel programModel;
    private final ButtonFactoryMixin buttonFactory;
    private ButtonSelector<Item> itemSelector;
    private final HBox containerBox;

    /**
     * Creates the Step 1 view.
     *
     * @param programModel The program model containing business logic
     * @param buttonFactory The button factory for creating entity selectors
     */
    ProgramStep1View(ProgramModel programModel, ButtonFactoryMixin buttonFactory) {
        this.programModel = programModel;
        this.buttonFactory = buttonFactory;
        this.containerBox = buildUi();
    }

    /**
     * Returns the root UI node for Step 1.
     */
    Node getView() {
        return containerBox;
    }

    /**
     * Returns the item selector for external binding.
     */
    ButtonSelector<Item> getItemSelector() {
        return itemSelector;
    }

    /**
     * Builds the complete UI for Step 1.
     */
    private HBox buildUi() {
        BooleanProperty dayTicketPreliminaryScheduledItemProperty = programModel.getDayTicketPreliminaryScheduledItemProperty();

        // ========== Step Indicator ==========
        // Step 1 Circle (active)
        Label step1Number = new Label("1");
        step1Number.getStyleClass().add(program_step_number_active);
        StackPane step1Circle = new StackPane(step1Number);
        step1Circle.getStyleClass().add(program_step_circle_active);
        step1Circle.setMinSize(40, 40);
        step1Circle.setMaxSize(40, 40);

        Label step1Label = I18nControls.newLabel(ProgramI18nKeys.GeneratePreliminaryBookableSI);
        step1Label.getStyleClass().add(program_step_label_active);
        step1Label.setWrapText(true);
        step1Label.setMaxWidth(150);
        step1Label.setAlignment(Pos.CENTER);
        step1Label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox step1Box = new VBox(12, step1Circle, step1Label);
        step1Box.setAlignment(Pos.CENTER);

        // Connector line
        Region connector1 = new Region();
        connector1.getStyleClass().add(program_step_connector_inactive);
        connector1.setPrefHeight(2);
        connector1.setMaxHeight(2);
        connector1.setMinWidth(80);
        connector1.setPrefWidth(120);
        HBox.setHgrow(connector1, Priority.ALWAYS);

        // Step 2 Circle (inactive)
        Label step2NumberInactive = new Label("2");
        step2NumberInactive.getStyleClass().add(program_step_number_inactive);
        StackPane step2CircleInactive = new StackPane(step2NumberInactive);
        step2CircleInactive.getStyleClass().add(program_step_circle_inactive);
        step2CircleInactive.setMinSize(40, 40);
        step2CircleInactive.setMaxSize(40, 40);

        Label step2LabelInactive = I18nControls.newLabel(ProgramI18nKeys.GenerateProgramSchedule);
        step2LabelInactive.getStyleClass().add(program_step_label_inactive);
        step2LabelInactive.setWrapText(true);
        step2LabelInactive.setMaxWidth(150);
        step2LabelInactive.setAlignment(Pos.CENTER);
        step2LabelInactive.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox step2BoxInactive = new VBox(12, step2CircleInactive, step2LabelInactive);
        step2BoxInactive.setAlignment(Pos.CENTER);

        // Connector line 1b
        Region connector1b = new Region();
        connector1b.getStyleClass().add(program_step_connector_inactive);
        connector1b.setPrefHeight(2);
        connector1b.setMaxHeight(2);
        connector1b.setMinWidth(80);
        connector1b.setPrefWidth(120);
        HBox.setHgrow(connector1b, Priority.ALWAYS);

        // Step 3 Circle (inactive)
        Label step3NumberInactive = new Label("3");
        step3NumberInactive.getStyleClass().add(program_step_number_inactive);
        StackPane step3CircleInactive = new StackPane(step3NumberInactive);
        step3CircleInactive.getStyleClass().add(program_step_circle_inactive);
        step3CircleInactive.setMinSize(40, 40);
        step3CircleInactive.setMaxSize(40, 40);

        Label step3LabelInactive = I18nControls.newLabel(ProgramI18nKeys.ValidateProgram);
        step3LabelInactive.getStyleClass().add(program_step_label_inactive);
        step3LabelInactive.setWrapText(true);
        step3LabelInactive.setMaxWidth(150);
        step3LabelInactive.setAlignment(Pos.CENTER);
        step3LabelInactive.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox step3BoxInactive = new VBox(12, step3CircleInactive, step3LabelInactive);
        step3BoxInactive.setAlignment(Pos.CENTER);

        // Steps container for Step 1 view
        HBox stepsRowStep1 = new HBox(20);
        stepsRowStep1.setAlignment(Pos.CENTER);
        stepsRowStep1.getChildren().addAll(step1Box, connector1, step2BoxInactive, connector1b, step3BoxInactive);
        stepsRowStep1.setMaxWidth(800);
        stepsRowStep1.setPadding(new Insets(30, 20, 30, 20));

        HBox stepsLineStep1 = new HBox(stepsRowStep1);
        stepsLineStep1.setAlignment(Pos.CENTER);

        // ========== Warning Alert Box ==========
        Label warningTitle = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.PreliminariesNotGenerated));
        warningTitle.getStyleClass().add(program_warning_title_amber);

        Label warningMessage = I18nControls.newLabel(ProgramI18nKeys.PreliminariesNotGeneratedMessage);
        warningMessage.setWrapText(true);
        warningMessage.getStyleClass().add(program_warning_message_amber);

        VBox warningContent = new VBox(4, warningTitle, warningMessage);
        HBox warningBox = new HBox(12, warningContent);
        warningBox.setAlignment(Pos.TOP_LEFT);
        warningBox.getStyleClass().add(program_warning_box_amber);
        warningBox.setPadding(new Insets(16, 20, 16, 20));
        warningBox.setMaxWidth(MAX_WIDTH);

        // ========== Step Content Box ==========
        Label stepTitle = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.Step1ConfigureBookableTeachingItems));
        stepTitle.getStyleClass().add(program_step_title);

        // Form Group
        Label formLabel = I18nControls.newLabel(ProgramI18nKeys.SelectItemTypeForBookableTeachingSessions);
        formLabel.getStyleClass().add(program_form_label);

        // Item selector
        itemSelector = new EntityButtonSelector<Item>( // language=JSON5
            "{class: 'Item', alias: 's', where: 'family.code=`teach`', orderBy :'name'}",
            buttonFactory, FXMainFrameDialogArea::getDialogArea, programModel.getEntityStore().getDataSourceModel())
            .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=$1", o));
        Button itemSelectorButton = itemSelector.getButton();
        itemSelectorButton.setMinWidth(250);
        itemSelectorButton.setPadding(new Insets(12, 16, 12, 16));
        itemSelectorButton.getStyleClass().add(program_selector_button);

        Label formHelp = I18nControls.newLabel(ProgramI18nKeys.ItemTypeWillBeUsedForBookableSchedule);
        formHelp.setWrapText(true);
        formHelp.getStyleClass().add(program_form_help);

        VBox formGroup = new VBox(8, formLabel, itemSelectorButton, formHelp);
        formGroup.setPadding(new Insets(0, 0, 20, 0));

        // Info Box
        Label infoTitle = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.WhatHappensNext));
        infoTitle.getStyleClass().add(program_info_title_blue);

        Label infoContent = I18nControls.newLabel(ProgramI18nKeys.WhatHappensNextMessage);
        infoContent.setWrapText(true);
        infoContent.getStyleClass().add(program_info_content_blue);

        VBox infoBox = new VBox(6, infoTitle, infoContent);
        infoBox.getStyleClass().add(program_info_box_blue);
        infoBox.setPadding(new Insets(16));

        // Generate Button
        UpdateStore updateStore = programModel.getUpdateStore();
        Button generatePreliminaryBookableSIButton = Bootstrap.largePrimaryButton(I18nControls.newButton(ProgramI18nKeys.GeneratePreliminaryBookableSI));
        generatePreliminaryBookableSIButton.getStyleClass().add(program_generate_button);
        generatePreliminaryBookableSIButton.setOnAction(e -> {
            if(itemSelector.getSelectedItem()==null) {
                DialogContent dialogContent = new DialogContent().setContentText(I18n.getI18nText(ProgramI18nKeys.PleaseSelectAnItem)).setOk();
                DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, FXMainFrameDialogArea.getDialogArea());
                DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
                return;
            }
            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                programModel.generatePreliminaryBookableSI()
                    .inUiThread()
                    .onFailure(error -> {
                        DialogContent dialogContent = new DialogContent().setContentText(error.getMessage()).setOk();
                        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, FXMainFrameDialogArea.getDialogArea());
                        DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
                        updateStore.cancelChanges();
                    })
                    .onSuccess(success-> programModel.getDayTicketPreliminaryScheduledItemProperty().setValue(true))
                , generatePreliminaryBookableSIButton);
        });

        HBox buttonBox = new HBox(generatePreliminaryBookableSIButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(24, 0, 0, 0));

        VBox stepContent = new VBox(16, stepTitle, formGroup, infoBox, buttonBox);
        stepContent.getStyleClass().add(program_step_content);
        stepContent.setPadding(new Insets(24));

        // Step Indicator Container
        VBox stepIndicator = new VBox(20, stepsLineStep1, warningBox, stepContent);
        stepIndicator.setPadding(new Insets(30));
        stepIndicator.setMaxWidth(MAX_WIDTH);

        HBox container = new HBox(stepIndicator);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0, 20, 24, 20));

        // Show this section only when preliminary items NOT yet generated
        container.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty.not());
        container.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty.not());

        return container;
    }
}
