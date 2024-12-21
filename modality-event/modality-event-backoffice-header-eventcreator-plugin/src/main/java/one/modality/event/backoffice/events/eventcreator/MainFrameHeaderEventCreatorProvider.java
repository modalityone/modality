package one.modality.event.backoffice.events.eventcreator;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.textfield.MaterialTextField;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.controls.dialog.DialogBuilder;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import dev.webfx.stack.ui.controls.dialog.SimpleDialogBuilder;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.validation.ValidationSupport;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.backoffice.mainframe.headernode.MainFrameHeaderNodeProvider;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

/**
 * @author Bruno Salmon
 */
public class MainFrameHeaderEventCreatorProvider implements MainFrameHeaderNodeProvider {

    private final Button createHeaderButton = Bootstrap.successButton(I18nControls.newButton(EventCreatorI18nKeys.CreateEvent));

    @Override
    public String getName() {
        return "eventCreator";
    }

    @Override
    public Node getHeaderNode(ButtonFactoryMixin buttonFactory, Pane frameContainer, DataSourceModel dataSourceModel) {
        createHeaderButton.visibleProperty().bind(FXProperties.combine(FXEventSelector.eventSelectorVisibleProperty(), FXOrganizationId.organizationIdProperty(),
            (eventSelectorVisible, organizationId) ->
                !eventSelectorVisible // TODO: replace this with a property that is true when home page is displayed
                && Numbers.identicalObjectsOrNumberValues(Entities.getPrimaryKey(organizationId), 1)) // restricted to NKT only for now (organizationId = 1) as we don't manage events other than NKT Festivals for now
        );
        createHeaderButton.managedProperty().bind(createHeaderButton.visibleProperty());
        // TODO: Use an operation request instead of a direct call to allow authorization checks
        createHeaderButton.setOnAction(e -> openNKTFestivalCreatorDialog());
        return createHeaderButton;
    }

    private void openNKTFestivalCreatorDialog() {
        ToggleGroup toggleGroup = new ToggleGroup();
        ToggleButton springButton = createEventTypeButton(EventCreatorI18nKeys.Spring, toggleGroup);
        ToggleButton summerButton = createEventTypeButton(EventCreatorI18nKeys.Summer, toggleGroup);
        ToggleButton fallButton   = createEventTypeButton(EventCreatorI18nKeys.Fall,   toggleGroup);
        HBox eventTypeBar = new HBox(10, springButton, summerButton, fallButton);
        Button cancelButton = Bootstrap.secondaryButton(I18nControls.newButton(ModalityI18nKeys.Cancel));
        Button createButton = Bootstrap.successButton(I18nControls.newButton(ModalityI18nKeys.Create));
        HBox buttonBar = new HBox(10,
            cancelButton,
            createButton
        );
        buttonBar.setAlignment(Pos.CENTER);
        TextField eventNameTextField = new TextField();
        VBox content = new VBox(30,
            I18nControls.newLabel(EventCreatorI18nKeys.SelectEventType),
            eventTypeBar,
            I18nControls.newLabel(EventCreatorI18nKeys.NameEvent),
            MaterialUtil.makeMaterial(eventNameTextField),
            buttonBar
        );
        content.setPadding(new Insets(30));
        MaterialTextField materialTextField = MaterialUtil.getMaterialTextField(eventNameTextField);
        I18n.bindI18nTextProperty(materialTextField.labelTextProperty(), EventCreatorI18nKeys.NameEventTextField);
        materialTextField.setAnimateLabel(false);
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("event-creator-dialog");
        DialogBuilder dialogBuilder = new SimpleDialogBuilder(content);
        DialogCallback dialogCallback = DialogBuilderUtil.showModalNodeInGoldLayout(dialogBuilder, FXMainFrameDialogArea.getDialogArea());
        createHeaderButton.setDisable(true);
        dialogCallback.addCloseHook(() -> createHeaderButton.setDisable(false));
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(toggleGroup.selectedToggleProperty(), eventTypeBar);
        validationSupport.addRequiredInput(eventNameTextField);
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());
        createButton.setOnAction(e -> {
            if (validationSupport.isValid()) {
                // TODO
                dialogCallback.closeDialog();
            }
        });
    }

    private static ToggleButton createEventTypeButton(String i18nKey, ToggleGroup toggleGroup) {
        ToggleButton button = I18nControls.newToggleButton(i18nKey);
        button.setContentDisplay(ContentDisplay.TOP);
        button.setGraphicTextGap(20);
        button.setMinSize(277, 173);
        button.setToggleGroup(toggleGroup);
        return button;
    }
}
