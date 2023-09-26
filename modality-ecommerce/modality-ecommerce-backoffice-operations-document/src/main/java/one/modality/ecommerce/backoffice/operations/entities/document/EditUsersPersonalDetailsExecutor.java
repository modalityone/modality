package one.modality.ecommerce.backoffice.operations.entities.document;

import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import one.modality.crm.client.controls.personaldetails.PersonalDetailsPanel;
import one.modality.base.shared.entities.Person;

final class EditUsersPersonalDetailsExecutor {

    static Future<Void> executeRequest(EditUsersPersonalDetailsRequest rq) {
        System.out.println("111111");
        return execute(rq.getPerson(), rq.getButtonFactoryMixin(), rq.getParentContainer());
    }

    private static Future<Void> execute(Person person, ButtonFactoryMixin buttonFactoryMixin, Pane parentContainer) {
        PersonalDetailsPanel details = new PersonalDetailsPanel(person.getEvent(), buttonFactoryMixin, parentContainer);
        details.setEditable(true);
        details.syncUiFromModel(person);
        BorderPane detailsContainer = details.getContainer();
        ScrollPane scrollPane = new ScrollPane(detailsContainer);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        detailsContainer.setPrefWidth(400);
        scrollPane.setPrefWidth(400);
        scrollPane.setPrefHeight(600);
        //scrollPane.setFitToWidth(true);
        DialogContent dialogContent = new DialogContent().setContent(scrollPane);
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parentContainer, 0, 0.9);
        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            details.isValid();
            //dialogCallback.closeDialog();
            /*
            syncModelFromUi();
            if (!updateStore.hasChanges())
                dialogCallback.closeDialog();
            else {
                updateStore.executeUpdate().setHandler(ar -> {
                    if (ar.failed())
                        dialogCallback.showException(ar.cause());
                    else
                        dialogCallback.closeDialog();
                });
            }
*/
        });
        return Future.succeededFuture();
    }
}
