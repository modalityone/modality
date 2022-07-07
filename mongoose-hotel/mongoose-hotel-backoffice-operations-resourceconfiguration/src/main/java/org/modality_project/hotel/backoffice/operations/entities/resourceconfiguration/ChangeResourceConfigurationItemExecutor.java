package org.modality_project.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.framework.client.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.framework.client.ui.controls.dialog.DialogContent;
import dev.webfx.framework.client.ui.controls.dialog.DialogUtil;
import dev.webfx.framework.client.ui.controls.entity.selector.ButtonSelector;
import dev.webfx.framework.client.ui.controls.entity.selector.EntityButtonSelector;
import dev.webfx.framework.shared.orm.domainmodel.DataSourceModel;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.UpdateStore;
import dev.webfx.platform.shared.services.submit.SubmitArgument;
import dev.webfx.platform.shared.async.Future;
import dev.webfx.platform.shared.async.Promise;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.modality_project.base.shared.entities.Item;

final class ChangeResourceConfigurationItemExecutor {

    static Future<Void> executeRequest(ChangeResourceConfigurationItemRequest rq) {
        return execute(rq.getResourceConfiguration(), rq.getParentContainer(), rq.getItemFamilyCode(), rq.getSiteId());
    }

    private static Future<Void> execute(Entity resourceConfiguration, Pane parentContainer, String itemFamilyCode, EntityId siteId) {
        Promise<Void> promise = Promise.promise();
        DataSourceModel dataSourceModel = resourceConfiguration.getStore().getDataSourceModel();
        EntityButtonSelector<Item> itemSelector = new EntityButtonSelector<>("{class: `Item`, alias: `i`, where: `family.code='" + itemFamilyCode + "' and exists(select Option where item=i and site=" + siteId.getPrimaryKey() + ")`, orderBy: `ord,id`}", new ButtonFactoryMixin() {
        }, parentContainer, dataSourceModel);
        itemSelector.setShowMode(ButtonSelector.ShowMode.MODAL_DIALOG);
        itemSelector.showDialog();
        itemSelector.setCloseHandler(() -> {
            Item selectedItem = itemSelector.getSelectedItem();
            if (selectedItem != null) {
                DialogContent dialogContent = new DialogContent().setContent(new Text("Are you sure?"));
                DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer).addCloseHook(() -> {
                    if (!promise.future().isComplete())
                        promise.complete();
                });
                DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                    UpdateStore updateStore = UpdateStore.create(dataSourceModel);
                    updateStore.updateEntity(resourceConfiguration).setForeignField("item", selectedItem);
                    updateStore.submitChanges(SubmitArgument.builder()
                            .setStatement("select set_transaction_parameters(true)")
                            .setDataSourceId(dataSourceModel.getDataSourceId())
                            .build())
                            .onFailure(dialogCallback::showException)
                            .onSuccess(x -> {
                                dialogCallback.closeDialog();
                                promise.complete();
                            });
                });
            }
        });
        return promise.future();
    }
}
