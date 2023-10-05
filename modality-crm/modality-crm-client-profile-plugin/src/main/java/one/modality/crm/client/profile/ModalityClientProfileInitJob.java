package one.modality.crm.client.profile;

import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import one.modality.base.client.profile.fx.FXProfile;

/**
 * @author Bruno Salmon
 */
public final class ModalityClientProfileInitJob implements ApplicationJob, OperationActionFactoryMixin {

    @Override
    public void onStart() {
        FXProfile.setProfileButtonFactory(ModalityClientProfileButton::createProfileButton);
        FXProfile.setProfilePanelFactory(ModalityClientProfilePanel::createProfilePanel);
    }
}
