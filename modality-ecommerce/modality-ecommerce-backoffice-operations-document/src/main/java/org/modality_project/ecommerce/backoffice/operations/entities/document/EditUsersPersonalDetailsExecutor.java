package org.modality_project.ecommerce.backoffice.operations.entities.document;

import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogUtil;
import dev.webfx.platform.async.Future;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.modality_project.crm.client.controls.personaldetails.PersonalDetailsPanel;
import org.modality_project.base.shared.entities.Person;

final class EditUsersPersonalDetailsExecutor {

    static Future<Void> executeRequest(EditUsersPersonalDetailsRequest rq) {
        System.out.println("111111");
        return execute(rq.getPerson(), rq.getButtonFactoryMixin(), rq.getParentContainer());
    }

    private static Future<Void> execute(Person person, ButtonFactoryMixin buttonFactoryMixin, Pane parentContainer) {
        PersonalDetailsPanel details = new PersonalDetailsPanel(person.getEvent(), buttonFactoryMixin, parentContainer);
        details.setEditable(true);
        details.syncUiFromModel(person);
        BorderPane sectionPanel = details.getSectionPanel();
        ScrollPane scrollPane = new ScrollPane(sectionPanel);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sectionPanel.setPrefWidth(400);
        scrollPane.setPrefWidth(400);
        scrollPane.setPrefHeight(600);
        //scrollPane.setFitToWidth(true);
        DialogContent dialogContent = new DialogContent().setContent(scrollPane);
        DialogUtil.showModalNodeInGoldLayout(dialogContent, parentContainer, 0, 0.9);
        DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
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
