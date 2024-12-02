package one.modality.event.backoffice.activities.program;


import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.client.util.masterslave.ModalitySlaveEditor;
import one.modality.base.shared.entities.*;
import one.modality.event.client.event.fx.FXEvent;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class ProgramView extends ModalitySlaveEditor<Event> {

    private static final double MAX_WIDTH = 1600;

    private final ProgramModel programModel;

    //This parameter will allow us to manage the interaction and behaviour of the Panel that display the details of an event and the event selected
    private final MasterSlaveLinker<Event> masterSlaveEventLinker = new MasterSlaveLinker<>(this);
    final ObservableList<DayTemplateView> workingDayTemplateViews = FXCollections.observableArrayList();

    private final VBox mainVBox;

    ProgramView(ProgramModel programModel) {
        this.programModel = programModel;
        mainVBox = buildUi();
        mainVBox.setMaxWidth(MAX_WIDTH);
        ObservableLists.bindConverted(workingDayTemplateViews, programModel.getWorkingDayTemplates(), DayTemplateView::new);
    }

    public Node getView() {
        return mainVBox;
    }

    void startLogic() {
        masterSlaveEventLinker.masterProperty().bindBidirectional(FXEvent.eventProperty());
    }

    @Override
    public void setSlave(Event approvedEntity) {
        super.setSlave(approvedEntity);
        programModel.reloadProgramFromSelectedEvent(approvedEntity);
    }

    @Override
    public boolean hasChanges() {
        return programModel.getUpdateStore().hasChanges();
    }

    // Private implementation

    private VBox buildUi() {
        // Building the top line
        ObjectProperty<Event> loadedEventProperty = programModel.loadedEventProperty();
        Label subtitle = Bootstrap.h4(I18nControls.newLabel(
            new I18nSubKey("expression: '[" + ProgramI18nKeys.Programme + "] - ' + name + ' (' + dateIntervalFormat(startDate, endDate) +')'", loadedEventProperty), loadedEventProperty));
        subtitle.setWrapText(true);
        TextTheme.createSecondaryTextFacet(subtitle).style();

        Button addTemplateButton = Bootstrap.primaryButton(I18nControls.newButton(ProgramI18nKeys.AddDayTemplate));
        addTemplateButton.setGraphicTextGap(10);
        addTemplateButton.setOnAction(e -> programModel.addNewDayTemplate());

        HBox topLine = new HBox(subtitle, LayoutUtil.createHGrowable(), addTemplateButton);

        // Building the bottom line
        Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.newButton(ProgramI18nKeys.CancelProgram));
        cancelButton.setOnAction(e -> programModel.cancelChanges());

        Button saveButton = Bootstrap.largeSuccessButton(I18nControls.newButton(ProgramI18nKeys.SaveProgram));
        saveButton.setOnAction(e -> programModel.saveChanges(saveButton, cancelButton));

        UpdateStore updateStore = programModel.getUpdateStore();
        BooleanExpression hasChangesProperty = updateStore.hasChangesProperty();
        BooleanBinding hasNoChangesProperty = hasChangesProperty.not();
        saveButton.disableProperty().bind(hasNoChangesProperty);
        cancelButton.disableProperty().bind(hasNoChangesProperty);

        Button generateProgramButton = Bootstrap.largePrimaryButton(I18nControls.newButton(ProgramI18nKeys.GenerateProgram));
        generateProgramButton.setOnAction(e -> ModalityDialog.showConfirmationDialog(ProgramI18nKeys.ProgramGenerationConfirmation, () -> programModel.generateProgram(generateProgramButton)));

        Button deleteProgramButton = Bootstrap.largePrimaryButton(I18nControls.newButton(ProgramI18nKeys.DeleteProgram));
        deleteProgramButton.setOnAction(e -> ModalityDialog.showConfirmationDialog(ProgramI18nKeys.DeleteProgramConfirmation, () -> programModel.deleteProgram(deleteProgramButton)));

        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();
        generateProgramButton.visibleProperty().bind(programGeneratedProperty.not());
        deleteProgramButton.visibleProperty().bind(programGeneratedProperty);
        LayoutUtil.setAllUnmanagedWhenInvisible(generateProgramButton, deleteProgramButton);

        HBox bottomLine = new HBox(cancelButton, saveButton, generateProgramButton, deleteProgramButton);

        bottomLine.setAlignment(Pos.BASELINE_CENTER);
        bottomLine.setSpacing(100);

        // Building the template days
        ColumnsPane templateDayColumnsPane = new ColumnsPane();
        templateDayColumnsPane.setMinColumnWidth(500);
        templateDayColumnsPane.hgapProperty().bind(FXProperties.compute(templateDayColumnsPane.widthProperty(), w -> Math.min(50, 0.02 * w.doubleValue())));
        templateDayColumnsPane.vgapProperty().bind(templateDayColumnsPane.hgapProperty());
        templateDayColumnsPane.setPadding(new Insets(50, 0, 50, 0));
        templateDayColumnsPane.setAlignment(Pos.TOP_CENTER);
        ObservableLists.bindConverted(templateDayColumnsPane.getChildren(), workingDayTemplateViews, DayTemplateView::getPanel);

        // Building the event state line
        Label eventStateLabel = Bootstrap.h4(Bootstrap.textSecondary(new Label()));
        FXProperties.runNowAndOnPropertyChange(programGenerated ->
            I18nControls.bindI18nProperties(eventStateLabel, programGenerated ? ProgramI18nKeys.ScheduledItemsAlreadyGenerated : ProgramI18nKeys.ScheduledItemsNotYetGenerated), programGeneratedProperty
        );

        HBox eventStateLine = new HBox(eventStateLabel);
        eventStateLine.setAlignment(Pos.CENTER);
        eventStateLine.setPadding(new Insets(0, 0, 30, 0));

        return new VBox(
            topLine,
            templateDayColumnsPane,
            eventStateLine,
            bottomLine
        );
    }
}

