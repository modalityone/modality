package one.modality.ecommerce.backoffice.activities.payments;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;

import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilder;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import one.modality.base.backoffice.operations.entities.generic.CopyAllRequest;
import one.modality.base.backoffice.operations.entities.generic.CopySelectionRequest;
import one.modality.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.ecommerce.backoffice.operations.entities.moneytransfer.DeletePaymentRequest;
import one.modality.ecommerce.backoffice.operations.entities.moneytransfer.EditPaymentRequest;

final class PaymentsActivity extends EventDependentViewDomainActivity
        implements ConventionalUiBuilderMixin, OperationActionFactoryMixin {

    /*==================================================================================================================
    ================================================= Graphical layer ==================================================
    ==================================================================================================================*/

    private final PaymentsPresentationModel pm = new PaymentsPresentationModel();

    @Override
    public PaymentsPresentationModel getPresentationModel() {
        return pm; // eventId and organizationId will then be updated from route
    }

    private ConventionalUiBuilder ui; // Keeping this reference for activity resume

    @Override
    public Node buildUi() {
        ui = createAndBindGroupMasterSlaveViewWithFilterSearchBar(pm, "payments", "MoneyTransfer");

        CheckBox flatPaymentsCheckBox = newCheckBox("Flat payments");
        flatPaymentsCheckBox.setSelected(pm.flatPaymentsProperty().get());
        pm.flatPaymentsProperty().bind(flatPaymentsCheckBox.selectedProperty());

        ui.setLeftTopNodes(flatPaymentsCheckBox);

        Pane container = ui.buildUi();
        setUpContextMenu(
                LayoutUtil.lookupChild(container, node -> node instanceof VisualGrid),
                () ->
                        newActionGroup(
                                newSeparatorActionGroup(
                                        newOperationAction(
                                                () ->
                                                        new EditPaymentRequest(
                                                                pm.getSelectedPayment(),
                                                                container)),
                                        newOperationAction(
                                                () ->
                                                        new DeletePaymentRequest(
                                                                pm.getSelectedPayment(),
                                                                container))),
                                newSeparatorActionGroup(
                                        newOperationAction(
                                                () ->
                                                        new CopySelectionRequest(
                                                                masterVisualMapper
                                                                        .getSelectedEntities(),
                                                                masterVisualMapper
                                                                        .getEntityColumns())),
                                        newOperationAction(
                                                () ->
                                                        new CopyAllRequest(
                                                                masterVisualMapper
                                                                        .getCurrentEntities(),
                                                                masterVisualMapper
                                                                        .getEntityColumns())))));
        return container;
    }

    @Override
    public void onResume() {
        super.onResume();
        ui.onResume();
    }

    /*==================================================================================================================
    =================================================== Logical layer ==================================================
    ==================================================================================================================*/

    private ReactiveVisualMapper<MoneyTransfer> groupVisualMapper,
            masterVisualMapper,
            slaveVisualMapper;

    @Override
    protected void startLogic() {
        // Setting up the group mapper that build the content displayed in the group view
        groupVisualMapper =
                ReactiveVisualMapper.<MoneyTransfer>createGroupReactiveChain(this, pm)
                        .always(
                                "{class: 'MoneyTransfer', alias: 'mt', where: '!receiptsTransfer', orderBy: 'date desc,parent nulls first,id'}")
                        // Applying the event condition
                        .ifNotNullOtherwiseEmpty(
                                pm.eventIdProperty(), eventId -> where("document.event=?", eventId))
                        .ifFalse(pm.flatPaymentsProperty(), where("parent=null"))
                        .start();

        // Setting up the master mapper that build the content displayed in the master view
        masterVisualMapper =
                ReactiveVisualMapper.<MoneyTransfer>createMasterPushReactiveChain(this, pm)
                        .always(
                                "{class: 'MoneyTransfer', alias: 'mt', where: '!receiptsTransfer', orderBy: 'date desc,parent nulls first,id'}")
                        .always(
                                "{columns: 'date,document,transactionRef,status,comment,amount,methodIcon,pending,successful'}")
                        // Applying the event condition
                        .ifNotNullOtherwiseEmpty(
                                pm.eventIdProperty(),
                                eventId ->
                                        where(
                                                "document..event=? or document is null and exists(select MoneyTransfer where parent=mt and document.event=?)",
                                                eventId,
                                                eventId))
                        // Applying the flat mode
                        .ifFalse(pm.flatPaymentsProperty(), where("parent=null"))
                        // Applying the user search
                        .ifTrimNotEmpty(
                                pm.searchTextProperty(),
                                s ->
                                        Character.isDigit(s.charAt(0))
                                                ? where("document.ref=?", Integer.parseInt(s))
                                                : s.contains("@")
                                                        ? where(
                                                                "{lower(document.person_email) like ?",
                                                                "%" + s.toLowerCase() + "%")
                                                        : DqlStatement.where(
                                                                "document.person_abcNames like ?",
                                                                AbcNames.evaluate(s, true)))
                        .applyDomainModelRowStyle() // Colorizing the rows
                        .autoSelectSingleRow() // When the result is a singe row, automatically
                        // select it
                        .start();

        slaveVisualMapper =
                ReactiveVisualMapper.<MoneyTransfer>createSlavePushReactiveChain(this, pm)
                        .always(
                                "{class: 'MoneyTransfer', alias: 'mt', orderBy: 'date desc,parent nulls first,id'}")
                        .always(
                                "{columns: 'date,document,transactionRef,status,comment,amount,methodIcon,pending,successful'}")
                        // Applying the selection condition
                        .ifNotNullOtherwiseEmpty(
                                pm.selectedPaymentProperty(), mt -> where("parent=?", mt))
                        // Applying the flat mode
                        .ifFalse(pm.flatPaymentsProperty(), where("parent=null"))
                        .applyDomainModelRowStyle() // Colorizing the rows
                        .start();
    }

    @Override
    protected void refreshDataOnActive() {
        groupVisualMapper.refreshWhenActive();
        masterVisualMapper.refreshWhenActive();
        slaveVisualMapper.refreshWhenActive();
    }
}
