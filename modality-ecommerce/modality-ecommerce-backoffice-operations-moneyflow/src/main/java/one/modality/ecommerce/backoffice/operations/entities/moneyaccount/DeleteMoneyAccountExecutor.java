package one.modality.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.MoneyAccount;
import one.modality.base.shared.entities.MoneyFlow;

import java.util.List;
import java.util.stream.Collectors;

final class DeleteMoneyAccountExecutor {

    static Future<Void> executeRequest(DeleteMoneyAccountRequest rq) {
        return execute(rq.getMoneyAccount(), rq.getMoneyFlows(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyAccount moneyAccount, List<MoneyFlow> moneyFlows, Pane parentContainer) {
        String msg = buildDeleteMoneyAccountMsg(moneyAccount, moneyFlows);
        DialogContent dialogContent = new DialogContent().setContentText(msg);
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer);
        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            deleteSelectedMoneyAccount(moneyAccount, moneyFlows);
            dialogCallback.closeDialog();
        });
        return Future.succeededFuture();
    }

    private static String buildDeleteMoneyAccountMsg(MoneyAccount moneyAccount, List<MoneyFlow> moneyFlows) {
        List<MoneyFlow> moneyFlowsLinkedToAccount = getMoneyFlowsLinkedToAccount(moneyAccount, moneyFlows);

        if (moneyFlowsLinkedToAccount.isEmpty()) {
            return "Are you sure you wish to delete " + moneyAccount.getName() + "?";
        } else {
            String joinedAccountNames = moneyFlowsLinkedToAccount.stream()
                    .map(moneyFlow -> moneyFlow.getToMoneyAccount().equals(moneyAccount) ?
                            moneyFlow.getFromMoneyAccount().getName() : moneyFlow.getToMoneyAccount().getName())
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.joining("\n"));

            return moneyAccount.getName() + " has money flows with the following accounts:\n\n" +
                    joinedAccountNames + "\n\nThese money flows will also be deleted. Continue?";
        }
    }

    private static List<MoneyFlow> getMoneyFlowsLinkedToAccount(MoneyAccount moneyAccount, List<MoneyFlow> moneyFlows) {
        return moneyFlows.stream()
                .filter(moneyFlow -> moneyFlow.getToMoneyAccount().equals(moneyAccount) || moneyFlow.getFromMoneyAccount().equals(moneyAccount))
                .collect(Collectors.toList());
    }

    private static void deleteSelectedMoneyAccount(MoneyAccount moneyAccount, List<MoneyFlow> moneyFlows) {
        List<MoneyFlow> moneyFlowsLinkedToAccount = getMoneyFlowsLinkedToAccount(moneyAccount, moneyFlows);

        for (MoneyFlow moneyFlow : moneyFlowsLinkedToAccount) {
            UpdateStore updateStore = UpdateStore.createAbove(moneyFlow.getStore());
            updateStore.deleteEntity(moneyFlow);
            updateStore.submitChanges(SubmitArgument.builder()
                    .setStatement("select set_transaction_parameters(false)")
                    .setDataSourceId(updateStore.getDataSourceId())
                    .build());
        }

        UpdateStore updateStore = UpdateStore.createAbove(moneyAccount.getStore());
        updateStore.deleteEntity(moneyAccount);
        updateStore.submitChanges(SubmitArgument.builder()
                .setStatement("select set_transaction_parameters(false)")
                .setDataSourceId(updateStore.getDataSourceId())
                .build());
    }
}