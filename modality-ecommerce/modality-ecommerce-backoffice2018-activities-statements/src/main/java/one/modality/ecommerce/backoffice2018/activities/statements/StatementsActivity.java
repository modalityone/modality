package one.modality.ecommerce.backoffice2018.activities.statements;

import dev.webfx.stack.orm.dql.DqlStatement;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilder;
import one.modality.base.backoffice.controls.masterslave.ConventionalUiBuilderMixin;
import one.modality.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import one.modality.base.shared.entities.MoneyTransfer;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.entity.Entity;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

final class StatementsActivity extends EventDependentViewDomainActivity implements
        ConventionalUiBuilderMixin {

    /*==================================================================================================================
    ================================================= Graphical layer ==================================================
    ==================================================================================================================*/

    private final StatementsPresentationModel pm = new StatementsPresentationModel();

    @Override
    public StatementsPresentationModel getPresentationModel() {
        return pm; // eventId and organizationId will then be updated from route
    }

    private ConventionalUiBuilder ui; // Keeping this reference for activity resume

    @Override
    public Node buildUi() {
        ui = createAndBindGroupMasterSlaveViewWithFilterSearchBar(pm, "statements", "MoneyTransfer");

        EntityButtonSelector<Entity> moneyAccountSelector = new EntityButtonSelector<>(
                "{class: 'MoneyAccount', alias: 'ma', columns: [{expression: `[icon,name,currency.icon]`}], where: 'type.internal=true', orderBy: 'closed,type desc,exists(select MoneyFlow where toMoneyAccount=ma and positiveAmounts) ? 0 : 1,id desc'}",
                this, ui::buildUi, getDataSourceModel())
                .autoSelectFirstEntity()
                .setAutoOpenOnMouseEntered(true)
                .setShowMode(ButtonSelector.ShowMode.DROP_DOWN)
                //.combineIfNotNullOtherwiseForceEmptyResult(pm.organizationIdProperty(), organizationId -> where("organization=?", organizationId))
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("(event=? or event=null) and organization=(select organization from Event where id=?)", eventId, eventId));
        pm.selectedMoneyAccountProperty().bind(moneyAccountSelector.selectedItemProperty());

        CheckBox flatPaymentsCheckBox = newCheckBox("Flat payments");
        flatPaymentsCheckBox.setSelected(pm.flatPaymentsProperty().get());
        pm.flatPaymentsProperty().bind(flatPaymentsCheckBox.selectedProperty());

        CheckBox flatBatchesCheckBox = newCheckBox("Flat batches");
        flatBatchesCheckBox.setSelected(pm.flatBatchesProperty().get());
        pm.flatBatchesProperty().bind(flatBatchesCheckBox.selectedProperty());

        ui.setLeftTopNodes(flatPaymentsCheckBox, moneyAccountSelector.getButton());
        ui.setRightTopNodes(flatBatchesCheckBox);

        return ui.buildUi();
    }

    @Override
    public void onResume() {
        super.onResume();
        ui.onResume();
    }


    /*==================================================================================================================
    =================================================== Logical layer ==================================================
    ==================================================================================================================*/

    private ReactiveVisualMapper<MoneyTransfer> groupVisualMapper, masterVisualMapper, slaveVisualMapper;

    @Override
    protected void startLogic() {
        // Setting up the group mapper that build the content displayed in the group view
        groupVisualMapper = ReactiveVisualMapper.<MoneyTransfer>createGroupReactiveChain(this, pm)
                .always("{class: 'MoneyTransfer', alias: 'mt', orderBy: 'date desc,parent nulls first,id'}")
                // Applying the money account condition
                .ifNotNullOtherwiseEmpty(pm.selectedMoneyAccountProperty(), ma -> where("parent = null and (fromMoneyAccount=? or toMoneyAccount=?) or parent != null and (parent..fromMoneyAccount=? or parent..toMoneyAccount=?", ma, ma, ma, ma))
                .ifFalse(pm.flatPaymentsProperty(), where("parent=null"))
                .ifFalse(pm.flatBatchesProperty(), where("transfer=null and (parent=null || parent..transfer=null)"))
                .start();

        // Setting up the master mapper that build the content displayed in the master view
        masterVisualMapper = ReactiveVisualMapper.<MoneyTransfer>createMasterPushReactiveChain(this, pm)
                .always("{class: 'MoneyTransfer', alias: 'mt', orderBy: 'date desc,parent nulls first,id'}")
                .always("{columns: 'date,document.event,document,transactionRef,status,comment,amount,methodIcon,pending,successful'}")
                // Applying the money account condition
                .ifNotNullOtherwiseEmpty(pm.selectedMoneyAccountProperty(), ma -> where("parent = null and (fromMoneyAccount=? or toMoneyAccount=?) or parent != null and (parent..fromMoneyAccount=? or parent..toMoneyAccount=?)", ma.getPrimaryKey(), ma.getPrimaryKey(), ma.getPrimaryKey(), ma.getPrimaryKey()))
                // Applying the flat modes
                .ifFalse(pm.flatPaymentsProperty(), where("parent=null"))
                .ifFalse(pm.flatBatchesProperty(), where("transfer=null and (parent=null || parent..transfer=null)"))
                // Applying the user search
                .ifTrimNotEmpty(pm.searchTextProperty(), s ->
                        Character.isDigit(s.charAt(0)) ? where("document.ref=?", Integer.parseInt(s))
                                : s.contains("@") ? where("lower(document.person_email) like ?", "%" + s.toLowerCase() + "%")
                                : DqlStatement.where("document.person_abcNames like ?", AbcNames.evaluate(s, true)))
                .applyDomainModelRowStyle() // Colorizing the rows
                .autoSelectSingleRow() // When the result is a singe row, automatically select it
                .start();

        slaveVisualMapper = ReactiveVisualMapper.<MoneyTransfer>createSlavePushReactiveChain(this, pm)
                .always("{class: 'MoneyTransfer', alias: 'mt', orderBy: 'date desc,parent nulls first,id'}")
                .always("{columns: 'date,document.event,document,transactionRef,status,comment,amount,methodIcon,pending,successful'}")
                // Applying the selection condition
                .ifNotNullOtherwiseEmpty(pm.selectedPaymentProperty(), mt -> where("parent=? or transfer=? or parent..transfer=?", mt, mt, mt))
                // Applying the flat modes
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
