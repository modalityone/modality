package one.modality.crm.backoffice.activities.organizations;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Organization;

import static one.modality.crm.backoffice.activities.organizations.OrganizationsI18nKeys.*;

/**
 * @author Bruno Salmon
 */
final class OrganizationsDialogs {
    static void showEditDialog(Organization organization, OrganizationsActivity activity) {
        EditOrganizationDialog.show(organization, activity::refresh);
    }

    static void toggleClosedStatus(Organization organization, OrganizationsActivity activity) {
        Boolean currentClosed = organization.getBooleanFieldValue("closed");
        boolean newClosed = currentClosed == null ? true : !currentClosed;

        // Create update store and update the organization
        UpdateStore localUpdateStore = UpdateStore.createAbove(organization.getStore());
        Organization orgToUpdate = localUpdateStore.updateEntity(organization);
        orgToUpdate.setClosed(newClosed);

        localUpdateStore.submitChanges()
            .onSuccess(result -> activity.refresh())
            .onFailure(error -> showErrorDialog(error.getMessage()));
    }

    static void showDeleteDialog(Organization organization, OrganizationsActivity activity) {
        // Create dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        // Title
        Label titleLabel = I18nControls.newLabel(DeleteOrganization);
        titleLabel.getStyleClass().add("delete-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Message
        Label messageLabel = new Label(I18n.getI18nText(BaseI18nKeys.Delete) + " " + organization.getName() + I18n.getI18nText(BaseI18nKeys.QuestionMark));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.getStyleClass().add("delete-dialog-message");

        // Confirmation text
        Label confirmLabel = I18nControls.newLabel(DeleteOrganizationConfirm);
        confirmLabel.setWrapText(true);
        confirmLabel.setMaxWidth(Double.MAX_VALUE);
        confirmLabel.getStyleClass().add("delete-dialog-confirm");

        dialogContent.getChildren().addAll(titleLabel, messageLabel, confirmLabel);

        // Buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(BaseI18nKeys.Cancel));
        Button deleteButton = Bootstrap.dangerButton(I18nControls.newButton(BaseI18nKeys.Delete));

        footer.getChildren().addAll(cancelButton, deleteButton);
        dialogContent.getChildren().add(footer);

        // Show dialog
        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Button actions
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());
        deleteButton.setOnAction(e -> {
            // Soft delete - set closed to true instead of actual deletion
            UpdateStore localUpdateStore = UpdateStore.createAbove(organization.getStore());
            Organization orgToUpdate = localUpdateStore.updateEntity(organization);
            orgToUpdate.setClosed(true);

            localUpdateStore.submitChanges()
                .onSuccess(result -> {
                    dialogCallback.closeDialog();
                    activity.refresh();
                })
                .onFailure(error -> {
                    dialogCallback.closeDialog();
                    showErrorDialog(error.getMessage());
                });
        });
    }

    private static void showErrorDialog(String content) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        Label titleLabel = I18nControls.newLabel(BaseI18nKeys.Error);
        titleLabel.getStyleClass().add("error-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label headerLabel = I18nControls.newLabel(FailedToSaveOrganization);
        headerLabel.setWrapText(true);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.getStyleClass().add("error-dialog-header");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.getStyleClass().add("error-dialog-content");

        dialogContent.getChildren().addAll(titleLabel, headerLabel, contentLabel);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(BaseI18nKeys.OK));

        footer.getChildren().add(okButton);
        dialogContent.getChildren().add(footer);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }
}
