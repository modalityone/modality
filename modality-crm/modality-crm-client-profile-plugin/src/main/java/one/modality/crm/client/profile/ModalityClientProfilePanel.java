package one.modality.crm.client.profile;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.authn.UserClaims;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequest;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.backoffice.activities.mainframe.fx.FXMainFrame;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.profile.fx.FXProfile;
import one.modality.base.shared.entities.Person;
import one.modality.crm.client.controls.personaldetails.PersonalDetailsPanel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class ModalityClientProfilePanel {

    public static Node createProfilePanel() {
        ModalityButtonFactoryMixin buttonFactoryMixin = new ModalityButtonFactoryMixin() {};
        Hyperlink identityLink = new Hyperlink();
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Person userPerson = FXUserPerson.getUserPerson();
            if (userPerson != null) {
                identityLink.setText(userPerson.getFullName());
                identityLink.setOnAction(e -> {
                    FXProfile.setProfilePanel(null);
                    UpdateStore updateStore = UpdateStore.createAbove(userPerson.getStore());
                    Person updatingPerson = updateStore.updateEntity(userPerson);
                    PersonalDetailsPanel details = new PersonalDetailsPanel(updateStore.getDataSourceModel(), buttonFactoryMixin, FXMainFrame.getDialogArea());
                    details.setEditable(true);
                    details.syncUiFromModel(updatingPerson);
                    BorderPane sectionPanel = details.getContainer();
                    ScrollPane scrollPane = new ScrollPane(sectionPanel);
                    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    sectionPanel.setPrefWidth(400);
                    scrollPane.setPrefWidth(400);
                    scrollPane.setPrefHeight(600);
                    DialogContent dialogContent = new DialogContent().setContent(scrollPane);
                    DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, FXMainFrame.getDialogArea(), 0, 0.9);
                    DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                        if (!details.isValid())
                            return;
                        details.syncModelFromUi(updatingPerson);
                        if (!updateStore.hasChanges())
                            dialogCallback.closeDialog();
                        else {
                            updateStore.submitChanges()
                                    .onFailure(dialogCallback::showException)
                                    .onSuccess(x -> {
                                        details.syncModelFromUi(userPerson);
                                        dialogCallback.closeDialog();
                                    });
                        }
                    });
                });
            } else {
                UserClaims userClaims = FXUserClaims.getUserClaims();
                identityLink.setText(userClaims == null ? null : userClaims.getEmail());
                identityLink.setOnAction(null);
            }
        }, FXUserPerson.userPersonProperty(), FXUserClaims.userClaimsProperty());
        OperationActionFactoryMixin actionFactory = new OperationActionFactoryMixin() {};

        VBox vBox = new VBox(10);

        Button langButton = new Button();
        List<ChangeLanguageRequest> langRequests = ChangeLanguageRequestEmitter.getProvidedEmitters().stream()
                .map(ChangeLanguageRequestEmitter::emitLanguageRequest).collect(Collectors.toList());
        FXProperties.runNowAndOnPropertiesChange(() -> Platform.runLater(() -> {
            ChangeLanguageRequest currentLanguageRequest = Collections.findFirst(langRequests, req -> req.getLanguage().equals(I18n.getLanguage()));
            ActionBinder.bindButtonToAction(langButton, actionFactory.newOperationAction(() -> currentLanguageRequest));
            langButton.setOnAction(e -> {
                ContextMenu contextMenu = buttonFactoryMixin.getOrCreateContextMenu(langButton, () -> actionFactory.newActionGroup(Collections.toArray(Collections.map(langRequests, req -> actionFactory.newOperationAction(() -> req)), Action[]::new)));
                contextMenu.setMinWidth(langButton.getWidth());
                contextMenu.setStyle("-fx-min-width: " + langButton.getWidth() + "px");
                Point2D langButtonPosition = langButton.localToScreen(0, 0);
                contextMenu.show(langButton, langButtonPosition.getX(), langButtonPosition.getY());
            });
        }), I18n.dictionaryProperty());

        Button logoutButton = ActionBinder.bindButtonToAction(new Button(), actionFactory.newOperationAction(LogoutRequest::new));

        vBox.getChildren().setAll(identityLink, langButton, logoutButton);
        vBox.setAlignment(Pos.CENTER);
        langButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        vBox.setPadding(new Insets(10));
        int radius = 10;
        vBox.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(radius), null)));
        vBox.setBorder(new Border(new BorderStroke(Color.gray(0.8), BorderStrokeStyle.SOLID, new CornerRadii(radius), BorderStroke.THIN)));
        vBox.setEffect(new DropShadow(10, Color.gray(0.8)));
        return vBox;
    }

}
