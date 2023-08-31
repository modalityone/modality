package one.modality.base.backoffice.application;

import one.modality.base.backoffice.controls.masterslave.MasterSlaveView;
import one.modality.base.client.application.ModalityClientStarterActivity;
import one.modality.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;

/**
 * @author Bruno Salmon
 */
final class ModalityBackOfficeStarterActivity extends ModalityClientStarterActivity {

    private static final String DEFAULT_START_PATH = "/home";

    ModalityBackOfficeStarterActivity() {
        super(DEFAULT_START_PATH, ModalityBackOfficeMainFrameContainerActivity::new);
        MasterSlaveView.registerSlaveViewBuilder(BookingDetailsPanel::createAndBindIfApplicable);
    }

}
