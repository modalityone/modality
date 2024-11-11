package one.modality.crm.client.profile;

import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.FXLuminanceMode;
import dev.webfx.extras.theme.palette.FXPaletteMode;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.authn.UserClaims;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequest;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.session.state.client.fx.FXUserClaims;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.profile.fx.FXProfile;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.crm.client.controls.personaldetails.PersonalDetailsPanel;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class ModalityClientProfilePanel {

    public static Node createProfilePanel() {
        VBox vBox = new VBox(10);

        ModalityButtonFactoryMixin buttonFactoryMixin = new ModalityButtonFactoryMixin() {};
        OperationActionFactoryMixin actionFactory = new OperationActionFactoryMixin() {};

        Hyperlink identityLink = new Hyperlink();
        VisualGrid roleGrid = new VisualGrid();
        roleGrid.setFullHeight(true);

        FXProperties.runNowAndOnPropertiesChange(() -> {
            Person userPerson = FXUserPerson.getUserPerson();
            if (userPerson != null) {
                identityLink.setText(userPerson.getFullName());
                identityLink.setOnAction(e -> {
                    FXProfile.hideProfilePanel();
                    PersonalDetailsPanel.editPersonalDetails(userPerson, true, new ButtonSelectorParameters().setButtonFactory(buttonFactoryMixin).setDialogParent(FXMainFrameDialogArea.getDialogArea()));
                });
                // If there is no organization selected by the user (which happens on very first login), then we set it to its affiliated organization by default
                if (FXOrganization.getOrganization() == null
                        // Also checking organizationId because if it's non-null (ex: retrieved from session), it means the Organization is loading
                        && Entities.getPrimaryKey(FXOrganizationId.getOrganizationId()) == null) {
                    FXOrganization.setOrganization(userPerson.getOrganization());
                }
                ReactiveVisualMapper.createReactiveChain(null)
                    .setDataSourceModel(userPerson.getStore().getDataSourceModel())
                    .always("{class: 'AuthorizationAssignment', columns: [{expression: 'role.name', label:'Role', textAlign: 'center'},{expression: 'management.manager.fullName', label:'Granted by', textAlign: 'center'}]}")
                    .always(DqlStatement.where("active and management.user=?", userPerson))
                    .visualizeResultInto(roleGrid)
                    .start();
            } else {
                UserClaims userClaims = FXUserClaims.getUserClaims();
                identityLink.setText(userClaims == null ? null : userClaims.getEmail());
                identityLink.setOnAction(null);
                roleGrid.visualResultProperty().unbind();
                roleGrid.setVisualResult(null);
            }
        }, FXUserPerson.userPersonProperty(), FXUserClaims.userClaimsProperty());


        Button langButton = new Button();
        List<ChangeLanguageRequest> langRequests = ChangeLanguageRequestEmitter.getProvidedEmitters().stream()
                .map(ChangeLanguageRequestEmitter::emitLanguageRequest).collect(Collectors.toList());
        FXProperties.runNowAndOnPropertyChange(() -> Platform.runLater(() -> {
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

        EntityButtonSelector<Organization> organizationSelector = new EntityButtonSelector<>(
                "{class: 'Organization', alias: 'o', where: 'exists(select Event where organization=o)'}",
                buttonFactoryMixin, vBox, DataSourceModelService.getDefaultDataSourceModel()
        );
        // Doing a bidirectional binding with FXOrganization
        organizationSelector.selectedItemProperty().bindBidirectional(FXOrganization.organizationProperty());

        // Compact mode
        Switch compactModeSwitch = new Switch();
        HBox compactModeHBox = new HBox(new Label("Compact mode"), LayoutUtil.createHGrowable(), compactModeSwitch);
        compactModeSwitch.setSelected(FXLayoutMode.isCompactMode());
        FXProperties.runOnPropertyChange(FXLayoutMode::setCompactMode, compactModeSwitch.selectedProperty());

        // Dark mode
        Switch darkModeSwitch = new Switch();
        HBox darkModeHBox = new HBox(new Label("Dark mode"), LayoutUtil.createHGrowable(), darkModeSwitch);
        darkModeSwitch.setSelected(FXLuminanceMode.isDarkMode());
        FXProperties.runOnPropertyChange(FXLuminanceMode::setDarkMode, darkModeSwitch.selectedProperty());

        // Colorful palette
        Switch paletteModeSwitch = new Switch();
        HBox paletteModeHBox = new HBox(new Label("Colorful palette"), LayoutUtil.createHGrowable(), paletteModeSwitch);
        paletteModeSwitch.setSelected(FXPaletteMode.isVariedPalette());
        FXProperties.runOnPropertyChange(FXPaletteMode::setVariedPalette, paletteModeSwitch.selectedProperty());

        // Logout button
        Button logoutButton = ActionBinder.bindButtonToAction(new Button(), actionFactory.newOperationAction(LogoutRequest::new));

        vBox.getChildren().setAll(
                identityLink,
                //rolesBox,
                organizationSelector.getButton(),
                roleGrid,
                langButton,
                compactModeHBox,
                darkModeHBox,
                paletteModeHBox,
                logoutButton);
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
