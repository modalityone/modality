package one.modality.crm.client.profile;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import one.modality.base.client.profile.fx.FXProfile;

/**
 * @author Bruno Salmon
 */
public final class ModalityClientProfileInitJob implements ApplicationJob, OperationActionFactoryMixin {

    @Override
    public void onStart() {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object userId = FXUserId.getUserId();
            FXProfile.setProfileButton(userId == null ? null : ModalityClientProfileButton.createProfileButton());
        }, FXUserId.userIdProperty());
    }
}
