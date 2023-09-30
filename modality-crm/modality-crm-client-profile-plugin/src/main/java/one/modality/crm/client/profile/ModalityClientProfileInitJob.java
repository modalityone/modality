package one.modality.crm.client.profile;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import one.modality.base.client.profile.fx.FXProfile;

/**
 * @author Bruno Salmon
 */
public final class ModalityClientProfileInitJob implements ApplicationJob, OperationActionFactoryMixin {

    @Override
    public void onStart() {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object userId = FXUserId.getUserId();
            if (userId == null)
                FXProfile.setProfileButton(null);
            else {
                Circle button = new Circle(16, Color.GRAY);
                FXProfile.setProfileButton(button);
                button.setOnMouseClicked(e -> {
                    if (FXProfile.getProfilePanel() != null)
                        FXProfile.setProfilePanel(null);
                    else {
                        Button logoutButton = ActionBinder.bindButtonToAction(new Button(), newOperationAction(LogoutRequest::new));
                        FXProfile.setProfilePanel(logoutButton);
                    }
                });
            }
        }, FXUserId.userIdProperty());
    }
}
