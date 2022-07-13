package org.modality_project.event.backoffice.activities.options;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.modality_project.base.client.actions.ModalityActions;
import org.modality_project.ecommerce.client.businessdata.feesgroup.FeesGroup;
import org.modality_project.ecommerce.client.businessdata.preselection.OptionsPreselection;
import org.modality_project.base.backoffice.controls.multilangeditor.MultiLanguageEditor;
import org.modality_project.event.client.controls.bookingcalendar.BookingCalendar;
import org.modality_project.event.frontoffice.activities.options.OptionsActivity;
import org.modality_project.base.shared.entities.Label;
import org.modality_project.base.shared.entities.Option;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.controls.dialog.DialogCallback;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogUtil;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.db.submit.SubmitService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static dev.webfx.stack.ui.util.layout.LayoutUtil.*;

/**
 * @author Bruno Salmon
 */
final class EditableOptionsActivity extends OptionsActivity {

    private ObservableValue<Boolean> editModeProperty;

    @Override
    protected void createViewNodes() {
        CheckBox editModeCheckBox = newCheckBox("EditMode");
        editModeProperty = editModeCheckBox.selectedProperty();
        FXProperties.runOnPropertiesChange(() -> ((EditableBookingCalendar) bookingCalendar).setEditMode(editModeProperty.getValue()), editModeProperty);
        Button addOptionButton = newButton(ModalityActions.newAddOptionAction(this::showAddOptionDialog));
        addOptionButton.visibleProperty().bind(editModeProperty);
        super.createViewNodes();
        HBox hbox = new HBox(20, addOptionButton, createHGrowable(), editModeCheckBox, createHGrowable(), priceText);
        hbox.setPadding(new Insets(5, 10, 0, 10));
        HBox.setMargin(editModeCheckBox, new Insets(5, 0, 5, 0));
        hbox.setAlignment(Pos.CENTER);
        pageContainer.setTop(hbox);
    }

    @Override
    protected void addPriceText() {
        // Already included in the hBox
    }

    @Override
    protected BookingCalendar createBookingCalendar() {
        return new EditableBookingCalendar(true, pageContainer);
    }

    @Override
    protected List<Node> createOptionPanelHeaderNodes(Option option, Property<String> i18nTitle) {
        List<Node> list = new ArrayList<>(super.createOptionPanelHeaderNodes(option, i18nTitle));
        Collections.addAll(list, createHGrowable(), setUnmanagedWhenInvisible(newRemoveButton(() -> showRemoveOptionDialog(option)), editModeProperty));
        return list;
    }

    @Override
    protected Node createLabelNode(Label label) {
        Node labelNode = super.createLabelNode(label);
        labelNode.setOnMouseClicked(e -> {
            if (editModeProperty.getValue())
                showLabelDialog(label);
        });
        return labelNode;
    }

    private void showRemoveOptionDialog(Option option) {
        DialogUtil.showDialog(
                DialogContent.createConfirmationDialog(
                        "Removing an option",
                        "Do you really want to remove this option?"),
                dialogCallback -> {
                    // Creating an update store
                    UpdateStore store = UpdateStore.create(getDataSourceModel());
                    // Deleting the option entity
                    store.deleteEntity(option);
                    // Submitting this change into the database
                    store.submitChanges()
                            .onFailure(dialogCallback::showException)
                            .onSuccess(resultBatch -> {
                                dialogCallback.closeDialog();
                                // Updating the UI
                                getEventActiveWorkingDocument().getWorkingDocumentLines().removeIf(line -> getTopParentOption(line.getOption()) == option);
                                getEventActiveOptionsPreselection().getOptionPreselections().removeIf(optionPreselection -> getTopParentOption(optionPreselection.getOption()) == option);
                                clearEventOptions();
                                startLogic();
                            });
                }, pageContainer);
    }

    private BorderPane addOptionDialogPane;
    private DialogCallback addOptionDialogCallback;
    private ReactiveVisualMapper<Option> addOptionDialogVisualMapper;

    private void showAddOptionDialog() {
        if (addOptionDialogPane == null) {
            VisualGrid visualGrid = new VisualGrid();
            addOptionDialogPane = new BorderPane(setMaxPrefSizeToInfinite(visualGrid));
            addOptionDialogVisualMapper = ReactiveVisualMapper.<Option>createPushReactiveChain(this)
                    .always("{class: 'Option', alias: 'o', where: 'parent=null and template', orderBy: 'event.id desc,ord'}")
                    .always(eventIdProperty(), e -> where("event.organization=?", getEvent().getOrganization()))
                    .setEntityColumns("[" +
                            "{label: 'Option', expression: 'coalesce(itemFamily.icon,item.family.icon),coalesce(name, item.name)'}," +
                            "{label: 'Event', expression: 'event.(icon, name + ` ~ ` + dateIntervalFormat(startDate,endDate))'}," +
                            "{label: 'Event type', expression: 'event.type'}" +
                            "]")
                    .visualizeResultInto(visualGrid.visualResultProperty())
                    .setVisualSelectionProperty(visualGrid.visualSelectionProperty())
                    .start();
            HBox hBox = new HBox(20, createHGrowable(), newOkButton(this::onOkAddOptionDialog), newCancelButton(this::onCancelAddOptionDialog), createHGrowable());
            hBox.setPadding(new Insets(20, 0, 0, 0));
            addOptionDialogPane.setBottom(hBox);
            visualGrid.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) closeAddOptionDialog();
            });
        }
        addOptionDialogCallback = DialogUtil.showModalNodeInGoldLayout(addOptionDialogPane, pageContainer, 0.9, 0.8);
    }

    private void onOkAddOptionDialog() {
        Option selectedOption = addOptionDialogVisualMapper.getSelectedEntity();
        if (selectedOption != null) {
            SubmitService.executeSubmit(SubmitArgument.builder()
                    .setStatement("select copy_option(null,?::int,?::int,null)")
                    .setParameters(selectedOption.getPrimaryKey(), getEventId())
                    .setReturnGeneratedKeys(true)
                    .setDataSourceId(getDataSourceId())
                    .build())
                    .onFailure(addOptionDialogCallback::showException)
                    .onSuccess(batchResult -> {
                        closeAddOptionDialog();
                        OptionsPreselection selectedOptionsPreselection = getEventActiveOptionsPreselection();
                        clearEventOptions();
                        onEventFeesGroups().onComplete(ar2 -> {
                            if (ar2.succeeded()) {
                                for (FeesGroup feesGroup : ar2.result()) {
                                    for (OptionsPreselection optionsPreselection : feesGroup.getOptionsPreselections()) {
                                        if (optionsPreselection.getLabel() == selectedOptionsPreselection.getLabel()) {
                                            optionsPreselection.setEventActive();
                                            optionsPreselection.getWorkingDocument().setEventActive();
                                            startLogic();
                                            return;
                                        }
                                    }
                                }
                            }
                        });
                    });
        }
        closeAddOptionDialog();
    }

    private void onCancelAddOptionDialog() {
        closeAddOptionDialog();
    }

    private void closeAddOptionDialog() {
        addOptionDialogCallback.closeDialog();
        addOptionDialogVisualMapper.stop();
    }

    private Option getTopParentOption(Option option) {
        Option parent = option == null ? null : option.getParent();
        return parent == null ? option : getTopParentOption(parent);
    }

    private DialogCallback labelDialogCallback;

    private void showLabelDialog(Label label) {
        if (labelDialogCallback == null)
            labelDialogCallback = DialogUtil.showModalNodeInGoldLayout(
                    new MultiLanguageEditor(this, label, lang -> lang, null)
                            .showOkCancelButton(e -> closeLabelDialog(e, label))
                            .getUiNode(), pageContainer, 0.9, 0.8);
    }

    private void closeLabelDialog(Entity savedEntity, Label label) {
        labelDialogCallback.closeDialog();
        labelDialogCallback = null;
        if (savedEntity != null) {
            label.getStore().copyEntity(savedEntity);
            updateLabelText(label);
        }
    }
}
