package one.modality.event.backoffice.activities.createevent;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.textfield.MaterialTextField;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
import dev.webfx.extras.time.pickers.DateField;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.EventType;
import one.modality.base.shared.entities.Site;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.event.backoffice.eventcreator.EventCreator;

/**
 * @author Bruno Salmon
 */
final class CreateEventActivity extends ViewDomainActivityBase
    implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
    ModalityButtonFactoryMixin,
    OperationActionFactoryMixin {


    @Override
    public Node buildUi() {
        VBox mainContent = new VBox(80);

        Button createButton = Bootstrap.largeSuccessButton(I18nControls.newButton(BaseI18nKeys.Create), false);
        TextField eventNameTextField = new TextField();
        EntityButtonSelector<EventType> typeSelector = new EntityButtonSelector<EventType>( // language=JSON5
            "{class: 'EventType', where: '!deprecated', orderBy :'name'}",
            this, FXMainFrameDialogArea::getDialogArea, DataSourceModelService.getDefaultDataSourceModel()
        ).always(FXOrganizationId.organizationIdProperty(), orgId -> DqlStatement.where("organization=?", orgId));
        EntityButtonSelector<Site> siteSelector = new EntityButtonSelector<Site>( // language=JSON5
            "{class: 'Site', alias: 's', where: 'event=null', orderBy :'name'}",
            this, FXMainFrameDialogArea::getDialogArea, DataSourceModelService.getDefaultDataSourceModel()
        ).always(FXOrganizationId.organizationIdProperty(), orgId -> DqlStatement.where("organization=?", orgId));

        DateField startDateField = createDateField(BaseI18nKeys.StartDate);
        DateField endDateField = createDateField(BaseI18nKeys.EndDate);
        mainContent.getChildren().setAll(
            Bootstrap.h1Primary(I18nControls.newLabel(CreateEventI18nKeys.CreateEvent)),
            MaterialUtil.makeMaterial(eventNameTextField),
            typeSelector.toMaterialButton("Type"), // TODO: create i18n key
            siteSelector.toMaterialButton("Site"), // TODO: create i18n key
            new ColumnsPane(80, startDateField.getView(), endDateField.getView())
        );
        mainContent.setAlignment(Pos.CENTER);
        // Note: we use a different scale pane for the main content and the button bar, so that the button bar is not scaled down immediately
        VBox container = new VBox(
            new ScalePane(mainContent),
            new ScalePane(createButton)
        );
        container.spacingProperty().bind(container.heightProperty().multiply(0.1));
        MaterialTextField materialTextField = MaterialUtil.getMaterialTextField(eventNameTextField);
        materialTextField.labelTextProperty().bind(I18n.i18nTextProperty(CreateEventI18nKeys.EventName));
        materialTextField.setAnimateLabel(false);
        container.getStyleClass().add("event-creator-dialog"); // TODO
        container.setMaxWidth(700);

        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(eventNameTextField);
        validationSupport.addRequiredInput(startDateField.dateProperty(), startDateField.getTextField());
        validationSupport.addRequiredInput(endDateField.dateProperty(), endDateField.getTextField());
        createButton.setOnAction(e -> {
            if (validationSupport.isValid()) {
                UpdateStore updateStore = UpdateStore.create();
                EventType eventType = typeSelector.getSelectedEntity();
                Site venue = siteSelector.getSelectedEntity();
                AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    EventCreator.createEvent(eventNameTextField.getText(), eventType, venue, startDateField.getDate(), endDateField.getDate(), updateStore),
                    createButton
                );
            }
        });

        container.setBackground(Background.fill(Color.WHITE));
        container.setMaxWidth(800);
        container.setPadding(new Insets(50));

        return container;
    }

    private static DateField createDateField(Object i18nKey) {
        DateField dateField = new DateField(FXMainFrameDialogArea.getDialogArea());
        MaterialUtil.makeMaterial(dateField.getTextField());
        MaterialTextField dateMaterialTextField = MaterialUtil.getMaterialTextField(dateField.getTextField());
        I18n.bindI18nTextProperty(dateMaterialTextField.labelTextProperty(), i18nKey);
        dateMaterialTextField.setAnimateLabel(false);
        return dateField;
    }

}
